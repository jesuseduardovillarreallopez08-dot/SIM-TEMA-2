import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from sqlalchemy import create_engine

# --- CONFIGURACIÓN DE CONEXIÓN A PGADMIN / POSTGRESQL ---
DB_USER = 'postgres'
DB_PASS = 'tu_contrasena'  # <-- Reemplaza por tu contraseña de pgAdmin
DB_HOST = 'localhost'
DB_PORT = '5432'
DB_NAME = 'galacticshield_db'

# Cadena de conexión usando explícitamente psycopg2
engine = create_engine(f'postgresql+psycopg2://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}')

def cargar_parametros_pgadmin():
    """ Lee los parámetros del asteroide desde PostgreSQL """
    query = "SELECT * FROM parametros_asteroide ORDER BY id DESC LIMIT 1;"
    df = pd.read_sql(query, engine)
    
    if df.empty:
        raise ValueError("¡Error! No hay parámetros registrados en la tabla 'parametros_asteroide' de pgAdmin.")
    
    params = df.iloc[0]
    print(f"-> Parámetros de '{params['nombre_asteroide']}' cargados desde pgAdmin.")
    print(f"-> Posición inicial: ({params['pos_x_inicial']}, {params['pos_y_inicial']}) km")
    print(f"-> Vector velocidad inicial: ({params['vx_inicial']}, {params['vy_inicial']})")
    
    return params

def guardar_resultados_pgadmin(nombre, num_sim, impactos, prob_impacto):
    """ Almacena los resultados de la simulación en PostgreSQL """
    df_resultado = pd.DataFrame([{
        'nombre_asteroide': nombre,
        'simulaciones_totales': num_sim,
        'impactos_detectados': impactos,
        'probabilidad_impacto_pct': prob_impacto
    }])
    
    df_resultado.to_sql('resultados_defensa', engine, if_exists='append', index=False)
    print("-> Evaluación de amenaza planetaria registrada exitosamente en pgAdmin.")

def simulacion_monte_carlo_asteroide(params, num_simulaciones=800, pasos_tiempo=100):
    """
    Simula trayectorias estocásticas usando los parámetros cargados de pgAdmin.
    """
    radio_tierra = params['radio_tierra_km']
    radio_atmosfera = params['radio_atmosfera_km']
    x_inicial = params['pos_x_inicial']
    y_inicial = params['pos_y_inicial']
    vx_inicial = params['vx_inicial']
    vy_inicial = params['vy_inicial']

    trayectorias = []
    impactos = 0
    posiciones_impacto = []

    print(f"Ejecutando {num_simulaciones} trayectorias orbitales de Monte Carlo...")

    for sim in range(num_simulaciones):
        x, y = x_inicial, y_inicial
        vx = vx_inicial + np.random.normal(0, 45)
        vy = vy_inicial + np.random.normal(0, 45)
        
        path_x, path_y = [x], [y]
        impacto_detectado = False

        for t in range(pasos_tiempo):
            distancia = np.hypot(x, y)

            if distancia <= radio_atmosfera:
                impactos += 1
                impacto_detectado = True
                posiciones_impacto.append((x, y))
                break

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
    return trayectorias, posiciones_impacto, impactos, prob_impacto, radio_tierra, radio_atmosfera

def graficar_defensa_planetaria(params, trayectorias, posiciones_impacto, prob_impacto, r_tierra, r_atmo):
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

    plt.title(f"Monte Carlo ({params['nombre_asteroide']}) | Probabilidad de Impacto: {prob_impacto:.2f}%", fontsize=12, fontweight='bold')
    plt.xlabel('Coordenada X (km)')
    plt.ylabel('Coordenada Y (km)')
    plt.grid(True, linestyle=':', alpha=0.5)
    plt.legend(loc='upper right')
    plt.axis('equal')

    plt.show()

if __name__ == '__main__':
    # 1. Cargar parámetros desde pgAdmin
    params = cargar_parametros_pgadmin()
    
    # 2. Correr la simulación estocástica
    trayectorias, pos_impactos, num_impactos, prob_imp, r_tierra, r_atmo = simulacion_monte_carlo_asteroide(
        params, num_simulaciones=800, pasos_tiempo=120
    )
    
    # 3. Registrar resultados en la BD
    guardar_resultados_pgadmin(params['nombre_asteroide'], 800, num_impactos, prob_imp)
    
    # 4. Renderizar visualización de la órbita
    graficar_defensa_planetaria(params, trayectorias, pos_impactos, prob_imp, r_tierra, r_atmo)