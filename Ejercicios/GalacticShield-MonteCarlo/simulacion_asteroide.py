import numpy as np
import matplotlib.pyplot as plt

def simulacion_monte_carlo_asteroide(num_simulaciones=800, pasos_tiempo=100):
    """
    Simula trayectorias estocásticas de un asteroide acercándose a la Tierra
    bajo perturbaciones gravitacionales aleatorias (Monte Carlo).
    """
    # Coordenadas de la Tierra (Centro)
    tierra_x, tierra_y = 0, 0
    radio_tierra = 6371  # Radio en km (escalado)
    radio_atmosfera = 12000 # Zona de riesgo de impacto

    # Posición inicial del asteroide (a 150,000 km de distancia)
    x_inicial, y_inicial = -150000, 40000
    vx_inicial, vy_inicial = 1400, -350 # Vector de velocidad base

    trayectorias = []
    impactos = 0
    posiciones_impacto = []

    print(f"Calculando {num_simulaciones} trayectorias de órbita estocástica...")

    for sim in range(num_simulaciones):
        x, y = x_inicial, y_inicial
        vx = vx_inicial + np.random.normal(0, 45) # Incertidumbre en velocidad X
        vy = vy_inicial + np.random.normal(0, 45) # Incertidumbre en velocidad Y
        
        path_x, path_y = [x], [y]
        impacto_detectado = False

        for t in range(pasos_tiempo):
            distancia = np.hypot(x, y)

            # Evento de Impacto con la Tierra
            if distancia <= radio_atmosfera:
                impactos += 1
                impacto_detectado = True
                posiciones_impacto.append((x, y))
                break

            # Fuerza de gravedad estocástica con perturbaciones aleatorias
            fuerza_g = 800000000 / (distancia**2 + 1e-5)
            ax = -fuerza_g * (x / distancia) + np.random.normal(0, 5)
            ay = -fuerza_g * (y / distancia) + np.random.normal(0, 5)

            vx += ax
            vy += ay
            x += vx
            y += vy

            path_x.append(x)
            path_y.append(y)

        trayectorias.append((path_x, path_y, impacto_detectado))

    prob_impacto = (impactos / num_simulaciones) * 100
    return trayectorias, posiciones_impacto, prob_impacto, radio_tierra, radio_atmosfera

def graficar_defensa_planetaria(trayectorias, posiciones_impacto, prob_impacto, r_tierra, r_atmo):
    """
    Renderiza el mapa orbital y el cálculo estocástico de impacto.
    """
    plt.figure(figsize=(10, 8))

    # 1. Dibujar la Tierra y la Atmósfera
    circulo_atmo = plt.Circle((0, 0), r_atmo, color='red', alpha=0.2, label='Zona de Riesgo/Atmósfera')
    circulo_tierra = plt.Circle((0, 0), r_tierra, color='royalblue', label='Tierra')
    plt.gca().add_patch(circulo_atmo)
    plt.gca().add_patch(circulo_tierra)

    # 2. Dibujar las trayectorias
    for path_x, path_y, impacto in trayectorias:
        color = 'crimson' if impacto else 'darkgray'
        alpha = 0.6 if impacto else 0.15
        lw = 1.2 if impacto else 0.5
        plt.plot(path_x, path_y, color=color, alpha=alpha, linewidth=lw)

    # 3. Marcar los puntos de impacto
    if posiciones_impacto:
        ix, iy = zip(*posiciones_impacto)
        plt.scatter(ix, iy, color='yellow', marker='*', s=80, zorder=5, label='Puntos de Impacto')

    plt.title(f'Monte Carlo: Defensa Planetaria | Probabilidad de Impacto: {prob_impacto:.2f}%', fontsize=12, fontweight='bold')
    plt.xlabel('Coordenada X (km)')
    plt.ylabel('Coordenada Y (km)')
    plt.grid(True, linestyle=':', alpha=0.5)
    plt.legend(loc='upper right')
    plt.axis('equal')

    plt.show()

if __name__ == '__main__':
    trayectorias, impactos, prob, r_t, r_a = simulacion_monte_carlo_asteroide(num_simulaciones=600, pasos_tiempo=120)
    graficar_defensa_planetaria(trayectorias, impactos, prob, r_t, r_a)