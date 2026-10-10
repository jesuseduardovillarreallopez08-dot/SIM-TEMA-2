import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sqlalchemy import create_engine

# --- CONFIGURACIÓN DE CONEXIÓN A PGADMIN / POSTGRESQL ---
DB_USER = 'postgres'
DB_PASS = 'tu_contrasena'  # <-- Reemplaza por tu contraseña de pgAdmin
DB_HOST = 'localhost'
DB_PORT = '5432'
DB_NAME = 'cryptorisk_db'

# Cadena de conexión usando explícitamente psycopg2
engine = create_engine(f'postgresql+psycopg2://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}')

def cargar_datos_pgadmin():
    """ Lee el dataset histórico de precios desde PostgreSQL """
    query = "SELECT fecha, precio_cierre FROM dataset_crypto ORDER BY fecha ASC;"
    df = pd.read_sql(query, engine)
    
    # Asegurar tipo numérico
    df['precio_cierre'] = df['precio_cierre'].astype(float)
    
    # Calcular rendimientos logarítmicos diarios
    df['rendimiento'] = np.log(df['precio_cierre'] / df['precio_cierre'].shift(1))
    
    mu = df['rendimiento'].mean()
    sigma = df['rendimiento'].std()
    ultimo_precio = df['precio_cierre'].iloc[-1]
    
    print(f"-> Datos cargados desde pgAdmin ({len(df)} registros).")
    print(f"-> Último precio registrado: ${ultimo_precio:,.2f}")
    print(f"-> Parámetros: Drift (mu) = {mu:.6f}, Volatilidad (sigma) = {sigma:.6f}")
    
    return df, ultimo_precio, mu, sigma

def guardar_metricas_pgadmin(precio_inicial, precio_promedio, var_95, prob_ganancia):
    """ Almacena las métricas calculadas en la tabla resultados_riesgo de pgAdmin """
    df_metricas = pd.DataFrame([{
        'precio_inicial': precio_inicial,
        'precio_promedio_proyectado': precio_promedio,
        'var_95': var_95,
        'probabilidad_ganancia': prob_ganancia
    }])
    
    df_metricas.to_sql('resultados_riesgo', engine, if_exists='append', index=False)
    print("-> Análisis de riesgo registrado exitosamente en PostgreSQL (pgAdmin).")

def simulacion_monte_carlo_gbm(precio_inicial, mu, sigma, dias=30, num_simulaciones=1000):
    """ Simulación por Movimiento Browniano Geométrico """
    dt = 1
    precios = np.zeros((dias + 1, num_simulaciones))
    precios[0] = precio_inicial

    print(f"Ejecutando {num_simulaciones} proyecciones estocásticas a {dias} días...")

    for t in range(1, dias + 1):
        Z = np.random.standard_normal(num_simulaciones)
        precios[t] = precios[t - 1] * np.exp((mu - 0.5 * sigma**2) * dt + sigma * np.sqrt(dt) * Z)

    return precios

def graficar_simulacion(df_historico, precios_simulados, precio_inicial, dias):
    precios_finales = precios_simulados[-1]
    precio_promedio = np.mean(precios_finales)
    var_95 = np.percentile(precios_finales, 5)
    prob_ganancia = (np.sum(precios_finales > precio_inicial) / len(precios_finales)) * 100

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

    # 1. Historial pgAdmin + Proyección
    dias_historicos = np.arange(len(df_historico))
    ax1.plot(dias_historicos, df_historico['precio_cierre'], color='black', lw=2, label='Histórico (pgAdmin)')

    eje_proyeccion = np.arange(len(df_historico) - 1, len(df_historico) + dias)
    ax1.plot(eje_proyeccion, precios_simulados, lw=0.5, alpha=0.25)
    
    ax1.axhline(precio_inicial, color='gray', linestyle='--', label=f'Base (${precio_inicial:,.0f})')
    ax1.axhline(precio_promedio, color='blue', linestyle='-', lw=2, label=f'Promedio (${precio_promedio:,.0f})')
    ax1.axhline(var_95, color='red', linestyle=':', lw=2, label=f'VaR 95% (${var_95:,.0f})')

    ax1.set_title('Histórico pgAdmin + Monte Carlo (GBM)', fontsize=11, fontweight='bold')
    ax1.set_xlabel('Días')
    ax1.set_ylabel('Precio ($ USD)')
    ax1.grid(True, linestyle=':', alpha=0.5)
    ax1.legend(loc='upper left')

    # 2. Histograma
    ax2.hist(precios_finales, bins=40, color='lightgreen', edgecolor='black', alpha=0.7)
    ax2.axvline(precio_inicial, color='black', linestyle='--')
    ax2.axvline(var_95, color='red', linestyle=':')

    ax2.set_title(f'Distribución Final | Prob. Ganancia: {prob_ganancia:.1f}%', fontsize=11, fontweight='bold')
    ax2.set_xlabel('Precio Proyectado ($ USD)')
    ax2.set_ylabel('Frecuencia')
    ax2.grid(True, linestyle=':', alpha=0.5)

    plt.tight_layout()
    plt.show()

    return precio_promedio, var_95, prob_ganancia

if __name__ == '__main__':
    # 1. Cargar datos desde pgAdmin
    df_historico, ultimo_precio, mu, sigma = cargar_datos_pgadmin()
    
    # 2. Ejecutar Monte Carlo
    precios_simulados = simulacion_monte_carlo_gbm(ultimo_precio, mu, sigma, dias=30, num_simulaciones=1000)
    
    # 3. Mostrar gráficas y obtener métricas
    prom_p, var_95, prob_g = graficar_simulacion(df_historico, precios_simulados, ultimo_precio, dias=30)
    
    # 4. Guardar métricas resultantes en pgAdmin
    guardar_metricas_pgadmin(ultimo_precio, prom_p, var_95, prob_g)