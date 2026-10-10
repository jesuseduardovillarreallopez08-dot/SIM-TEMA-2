import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

def cargar_dataset(filepath='dataset_poblacion.csv'):
    """
    Carga el dataset CSV con la ubicación inicial georreferenciada de la población.
    """
    df = pd.read_csv(filepath)
    return df

def simulacion_monte_carlo_con_dataset(df_poblacion, dias=60, num_simulaciones=50):
    """
    Simulación de Monte Carlo del modelo SIR utilizando las posiciones iniciales del dataset.
    """
    num_individuos = len(df_poblacion)
    prob_infeccion = 0.40   # Probabilidad de contagio por contacto directo
    duracion_infeccion = 12 # Días hasta recuperación

    # Matrices para acumular resultados de Monte Carlo
    historial_S = np.zeros((num_simulaciones, dias))
    historial_I = np.zeros((num_simulaciones, dias))
    historial_R = np.zeros((num_simulaciones, dias))

    print(f"Cargados {num_individuos} registros desde el dataset.")
    print(f"Ejecutando {num_simulaciones} simulaciones estocásticas de Monte Carlo...")

    for sim in range(num_simulaciones):
        # Cargar posiciones desde el dataset para cada simulación
        pos_x = df_poblacion['pos_x'].values.astype(float)
        pos_y = df_poblacion['pos_y'].values.astype(float)
        estado = df_poblacion['estado_inicial'].values.astype(int) # 0=S, 1=I, 2=R
        
        dias_enfermo = np.zeros(num_individuos, dtype=int)
        dias_enfermo[estado == 1] = 1

        for d in range(dias):
            historial_S[sim, d] = np.sum(estado == 0)
            historial_I[sim, d] = np.sum(estado == 1)
            historial_R[sim, d] = np.sum(estado == 2)

            # 1. Movimiento estocástico (Paso aleatorio)
            pos_x += np.random.uniform(-1.5, 1.5, num_individuos)
            pos_y += np.random.uniform(-1.5, 1.5, num_individuos)
            pos_x = np.clip(pos_x, 0, 100)
            pos_y = np.clip(pos_y, 0, 100)

            # 2. Evaluación de contagios por Monte Carlo
            infectados_idx = np.where(estado == 1)[0]
            susceptibles_idx = np.where(estado == 0)[0]

            for i in infectados_idx:
                for s in susceptibles_idx:
                    distancia = np.hypot(pos_x[i] - pos_x[s], pos_y[i] - pos_y[s])
                    if distancia < 4.0: # Distancia crítica de contagio
                        if np.random.rand() < prob_infeccion:
                            estado[s] = 1

            # 3. Recuperación
            for i in infectados_idx:
                dias_enfermo[i] += 1
                if dias_enfermo[i] >= duracion_infeccion:
                    estado[i] = 2

    # Promedios estocásticos finales
    prom_S = np.mean(historial_S, axis=0)
    prom_I = np.mean(historial_I, axis=0)
    prom_R = np.mean(historial_R, axis=0)

    return prom_S, prom_I, prom_R, dias

def graficar_resultados_dataset(S, I, R, dias, df_poblacion):
    """
    Muestra la curva epidemiológica y la distribución espacial inicial según el dataset.
    """
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))

    # 1. Distribución inicial del Dataset
    colonias = df_poblacion['colonia'].unique()
    for col in colonias:
        sub = df_poblacion[df_poblacion['colonia'] == col]
        ax1.scatter(sub['pos_x'], sub['pos_y'], label=col, s=70, alpha=0.8)
    
    # Destacar paciente cero
    p_cero = df_poblacion[df_poblacion['estado_inicial'] == 1]
    ax1.scatter(p_cero['pos_x'], p_cero['pos_y'], color='red', marker='X', s=150, label='Infectado Inicial')

    ax1.set_title('Ubicación Inicial por Colonias (Dataset CSV)', fontsize=11, fontweight='bold')
    ax1.set_xlabel('Coordenada X')
    ax1.set_ylabel('Coordenada Y')
    ax1.grid(True, linestyle=':', alpha=0.5)
    ax1.legend()

    # 2. Curvas epidemiológicas
    eje_dias = np.arange(dias)
    ax2.plot(eje_dias, S, color='blue', lw=2, label='Susceptibles (S)')
    ax2.plot(eje_dias, I, color='red', lw=2, label='Infectados (I)')
    ax2.plot(eje_dias, R, color='green', lw=2, label='Recuperados (R)')

    ax2.set_title('Monte Carlo: Proyección Epidémica Promedio', fontsize=11, fontweight='bold')
    ax2.set_xlabel('Días')
    ax2.set_ylabel('N° de Personas')
    ax2.grid(True, linestyle=':', alpha=0.5)
    ax2.legend()

    plt.tight_layout()
    plt.show()

if __name__ == '__main__':
    # 1. Cargar datos del CSV
    dataset = cargar_dataset('dataset_poblacion.csv')
    
    # 2. Ejecutar Monte Carlo
    S, I, R, dias = simulacion_monte_carlo_con_dataset(dataset, dias=60, num_simulaciones=100)
    
    # 3. Mostrar gráficas
    graficar_resultados_dataset(S, I, R, dias, dataset)