import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sqlalchemy import create_engine

# --- CONFIGURACIÓN DE CONEXIÓN A PGADMIN / POSTGRESQL ---
DB_USER = 'postgres'
DB_PASS = 'tu_contrasena'  # <-- Reemplaza por tu contraseña de pgAdmin
DB_HOST = 'localhost'
DB_PORT = '5432'
DB_NAME = 'biosim_db'

# Cadena de conexión SQLAlchemy
engine = create_engine(f'postgresql+psycopg2://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}')

def cargar_poblacion_pgadmin():
    """ Lee el dataset inicial desde la base de datos PostgreSQL """
    query = "SELECT colonia, pos_x, pos_y, estado_inicial FROM dataset_poblacion;"
    df = pd.read_sql(query, engine)
    print(f"-> Datos cargados exitosamente desde pgAdmin ({len(df)} registros).")
    return df

def guardar_resultados_pgadmin(prom_S, prom_I, prom_R, dias):
    """ Guarda el historial de resultados de Monte Carlo en PostgreSQL """
    df_resultados = pd.DataFrame({
        'dia': np.arange(dias),
        'promedio_susceptibles': prom_S,
        'promedio_infectados': prom_I,
        'promedio_recuperados': prom_R
    })
    
    # Insertar en la tabla resultados_simulacion
    df_resultados.to_sql('resultados_simulacion', engine, if_exists='append', index=False)
    print("-> Resultados de Monte Carlo guardados correctamente en la base de datos.")

def simulacion_monte_carlo(df_poblacion, dias=60, num_simulaciones=50):
    num_individuos = len(df_poblacion)
    prob_infeccion = 0.40
    duracion_infeccion = 12

    historial_S = np.zeros((num_simulaciones, dias))
    historial_I = np.zeros((num_simulaciones, dias))
    historial_R = np.zeros((num_simulaciones, dias))

    print(f"Ejecutando {num_simulaciones} simulaciones estocásticas...")

    for sim in range(num_simulaciones):
        pos_x = df_poblacion['pos_x'].values.astype(float)
        pos_y = df_poblacion['pos_y'].values.astype(float)
        estado = df_poblacion['estado_inicial'].values.astype(int)
        
        dias_enfermo = np.zeros(num_individuos, dtype=int)
        dias_enfermo[estado == 1] = 1

        for d in range(dias):
            historial_S[sim, d] = np.sum(estado == 0)
            historial_I[sim, d] = np.sum(estado == 1)
            historial_R[sim, d] = np.sum(estado == 2)

            pos_x += np.random.uniform(-1.5, 1.5, num_individuos)
            pos_y += np.random.uniform(-1.5, 1.5, num_individuos)
            pos_x = np.clip(pos_x, 0, 100)
            pos_y = np.clip(pos_y, 0, 100)

            infectados_idx = np.where(estado == 1)[0]
            susceptibles_idx = np.where(estado == 0)[0]

            for i in infectados_idx:
                for s in susceptibles_idx:
                    distancia = np.hypot(pos_x[i] - pos_x[s], pos_y[i] - pos_y[s])
                    if distancia < 4.0:
                        if np.random.rand() < prob_infeccion:
                            estado[s] = 1

            for i in infectados_idx:
                dias_enfermo[i] += 1
                if dias_enfermo[i] >= duracion_infeccion:
                    estado[i] = 2

    prom_S = np.mean(historial_S, axis=0)
    prom_I = np.mean(historial_I, axis=0)
    prom_R = np.mean(historial_R, axis=0)

    return prom_S, prom_I, prom_R, dias

def graficar_resultados(S, I, R, dias, df_poblacion):
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

    colonias = df_poblacion['colonia'].unique()
    for col in colonias:
        sub = df_poblacion[df_poblacion['colonia'] == col]
        ax1.scatter(sub['pos_x'], sub['pos_y'], label=col, s=70, alpha=0.8)
    
    p_cero = df_poblacion[df_poblacion['estado_inicial'] == 1]
    ax1.scatter(p_cero['pos_x'], p_cero['pos_y'], color='red', marker='X', s=150, label='Infectado Inicial')

    ax1.set_title('Ubicación Inicial (Desde pgAdmin)', fontsize=11, fontweight='bold')
    ax1.set_xlabel('Coordenada X')
    ax1.set_ylabel('Coordenada Y')
    ax1.grid(True, linestyle=':', alpha=0.5)
    ax1.legend()

    eje_dias = np.arange(dias)
    ax2.plot(eje_dias, S, color='blue', lw=2, label='Susceptibles (S)')
    ax2.plot(eje_dias, I, color='red', lw=2, label='Infectados (I)')
    ax2.plot(eje_dias, R, color='green', lw=2, label='Recuperados (R)')

    ax2.set_title('Monte Carlo: Proyección Guardada en PostgreSQL', fontsize=11, fontweight='bold')
    ax2.set_xlabel('Días')
    ax2.set_ylabel('N° de Personas')
    ax2.grid(True, linestyle=':', alpha=0.5)
    ax2.legend()

    plt.tight_layout()
    plt.show()

if __name__ == '__main__':
    # 1. Cargar desde pgAdmin
    df_poblacion = cargar_poblacion_pgadmin()
    
    # 2. Correr Monte Carlo
    S, I, R, dias = simulacion_monte_carlo(df_poblacion, dias=60, num_simulaciones=50)
    
    # 3. Guardar resultados finales en pgAdmin
    guardar_resultados_pgadmin(S, I, R, dias)
    
    # 4. Mostrar gráfica
    graficar_resultados(S, I, R, dias, df_poblacion)