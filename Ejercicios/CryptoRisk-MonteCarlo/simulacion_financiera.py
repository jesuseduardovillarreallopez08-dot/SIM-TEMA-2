import numpy as np
import matplotlib.pyplot as plt

def simulacion_monte_carlo_financiera(precio_inicial=60000, dias=30, mu=0.001, sigma=0.03, num_simulaciones=1000):
    """
    Simulación de Monte Carlo usando Movimiento Browniano Geométrico (GBM)
    para predecir la volatilidad y distribución de precios futuros.
    
    Parametros:
    - precio_inicial: Precio actual del activo ($)
    - dias: Horizonte de tiempo a simular
    - mu: Rendimiento diario esperado (Drift)
    - sigma: Volatilidad diaria estimada
    - num_simulaciones: Cantidad de trayectorias estocásticas
    """
    dt = 1 # Paso de tiempo (1 día)
    
    # Matriz de precios: filas = días, columnas = simulaciones
    precios = np.zeros((dias + 1, num_simulaciones))
    precios[0] = precio_inicial

    print(f"Generando {num_simulaciones} proyecciones financieras a {dias} días...")

    # Generación estocástica de trayectorias mediante Monte Carlo
    for t in range(1, dias + 1):
        # Shock aleatorio estandarizado (Distribución Normal N(0,1))
        Z = np.random.standard_normal(num_simulaciones)
        
        # Ecuación estocástica GBM
        precios[t] = precios[t - 1] * np.exp((mu - 0.5 * sigma**2) * dt + sigma * np.sqrt(dt) * Z)

    return precios

def graficar_simulacion_riesgo(precios, precio_inicial, dias):
    """
    Renderiza el abanico de trayectorias estocásticas y la distribución final de precios.
    """
    precios_finales = precios[-1]
    
    # Métricas clave de riesgo
    precio_promedio = np.mean(precios_finales)
    var_95 = np.percentile(precios_finales, 5) # Value at Risk al 95%
    prob_ganancia = (np.sum(precios_finales > precio_inicial) / len(precios_finales)) * 100

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6), gridspec_kw={'width_ratios': [2, 1]})

    # 1. Gráfica de abanico de trayectorias
    eje_dias = np.arange(dias + 1)
    ax1.plot(eje_dias, precios, lw=0.5, alpha=0.3)
    ax1.axhline(precio_inicial, color='black', linestyle='--', label=f'Precio Inicial (${precio_inicial:,})')
    ax1.axhline(precio_promedio, color='blue', linestyle='-', lw=2, label=f'Promedio Esperado (${precio_promedio:,.0f})')
    ax1.axhline(var_95, color='red', linestyle=':', lw=2, label=f'Límite VaR 95% (${var_95:,.0f})')
    
    ax1.set_title('Monte Carlo: Trayectorias Estocásticas de Precio (GBM)', fontsize=11, fontweight='bold')
    ax1.set_xlabel('Días Proyectados')
    ax1.set_ylabel('Precio ($ USD)')
    ax1.grid(True, linestyle=':', alpha=0.6)
    ax1.legend(loc='upper left')

    # 2. Histograma de Distribución Final
    ax2.hist(precios_finales, bins=40, color='skyblue', edgecolor='black', orientation='horizontal', alpha=0.7)
    ax2.axhline(precio_inicial, color='black', linestyle='--')
    ax2.axhline(var_95, color='red', linestyle=':')
    
    ax2.set_title('Distribución Final de Precios', fontsize=11, fontweight='bold')
    ax2.set_xlabel('Frecuencia (Simulaciones)')
    ax2.grid(True, linestyle=':', alpha=0.6)

    plt.suptitle(f'Análisis de Riesgo Financiero | Prob. de Ganancia: {prob_ganancia:.1f}%', fontsize=13, fontweight='bold')
    plt.tight_layout()
    plt.show()

if __name__ == '__main__':
    # Simular Bitcoin: Precio inicial $60,000, proyectado a 30 días con 1,000 simulaciones
    matriz_precios = simulacion_monte_carlo_financiera(
        precio_inicial=60000, 
        dias=30, 
        mu=0.0015, 
        sigma=0.035, 
        num_simulaciones=1000
    )
    graficar_simulacion_riesgo(matriz_precios, precio_inicial=60000, dias=30)