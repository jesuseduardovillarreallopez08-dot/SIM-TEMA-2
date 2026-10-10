import numpy as np
import matplotlib.pyplot as plt

def simulacion_monte_carlo_epidemia(num_individuos=200, dias=100, num_simulaciones=100):
    """
    Simulación de Monte Carlo del modelo SIR (Susceptibles, Infectados, Recuperados)
    con movimiento estocástico de personas en una cuadrícula.
    """
    prob_infeccion = 0.35   # Probabilidad de contagio al estar cerca de un infectado
    duracion_infeccion = 14 # Días que dura enfermo un individuo

    # Matrices para guardar los promedios de todas las simulaciones de Monte Carlo
    historial_S = np.zeros((num_simulaciones, dias))
    historial_I = np.zeros((num_simulaciones, dias))
    historial_R = np.zeros((num_simulaciones, dias))

    print(f"Iniciando {num_simulaciones} simulaciones de Monte Carlo...")

    for sim in range(num_simulaciones):
        # Posiciones iniciales (X, Y) aleatorias en un área de 100x100
        pos_x = np.random.uniform(0, 100, num_individuos)
        pos_y = np.random.uniform(0, 100, num_individuos)
        
        # Estado: 0 = Susceptible, 1 = Infectado, 2 = Recuperado
        estado = np.zeros(num_individuos, dtype=int)
        
        # Paciente cero
        estado[0] = 1
        dias_enfermo = np.zeros(num_individuos, dtype=int)
        dias_enfermo[0] = 1

        for d in range(dias):
            # Contar estados del día
            historial_S[sim, d] = np.sum(estado == 0)
            historial_I[sim, d] = np.sum(estado == 1)
            historial_R[sim, d] = np.sum(estado == 2)

            # 1. Movimiento estocástico de las personas
            pos_x += np.random.uniform(-2, 2, num_individuos)
            pos_y += np.random.uniform(-2, 2, num_individuos)
            pos_x = np.clip(pos_x, 0, 100)
            pos_y = np.clip(pos_y, 0, 100)

            # 2. Proceso de contagio por proximidad
            infectados_idx = np.where(estado == 1)[0]
            susceptibles_idx = np.where(estado == 0)[0]

            for i in infectados_idx:
                for s in susceptibles_idx:
                    distancia = np.hypot(pos_x[i] - pos_x[s], pos_y[i] - pos_y[s])
                    if distancia < 3.0: # Radio de contacto
                        # Evento de Monte Carlo para probabilidad de contagio
                        if np.random.rand() < prob_infeccion:
                            estado[s] = 1

            # 3. Actualización de días de enfermedad y recuperación
            for i in infectados_idx:
                dias_enfermo[i] += 1
                if dias_enfermo[i] >= duracion_infeccion:
                    estado[i] = 2 # Recuperado / Inmune

    # Calcular los promedios esperados
    promedio_S = np.mean(historial_S, axis=0)
    promedio_I = np.mean(historial_I, axis=0)
    promedio_R = np.mean(historial_R, axis=0)

    return promedio_S, promedio_I, promedio_R, dias

def graficar_curva_epidemiologica(S, I, R, dias):
    """
    Renderiza las curvas estocásticas de propagación.
    """
    eje_dias = np.arange(dias)
    plt.figure(figsize=(10, 6))

    plt.plot(eje_dias, S, color='blue', linewidth=2.5, label='Susceptibles (S)')
    plt.plot(eje_dias, I, color='red', linewidth=2.5, label='Infectados (I)')
    plt.plot(eje_dias, R, color='green', linewidth=2.5, label='Recuperados (R)')

    plt.title('Simulación de Monte Carlo: Propagación Epidémica (Modelo SIR)', fontsize=12, fontweight='bold')
    plt.xlabel('Tiempo (Días)')
    plt.ylabel('Cantidad de Personas (Promedio)')
    plt.grid(True, linestyle=':', alpha=0.6)
    plt.legend()

    plt.show()

if __name__ == '__main__':
    S, I, R, dias = simulacion_monte_carlo_epidemia(num_individuos=250, dias=80, num_simulaciones=50)
    graficar_curva_epidemiologica(S, I, R, dias)