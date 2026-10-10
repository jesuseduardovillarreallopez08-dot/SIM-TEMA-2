package com.eduardosystems.simulacionvolcan.service;

import com.eduardosystems.simulacionvolcan.entity.DatasetPoblacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class ErupcionService {

    public static final int FILAS = 130;
    public static final int COLUMNAS = 200;
    public static final int FILA_CRATER = 22;
    public static final double CENTRO_X = 100.0;

    @Autowired
    private DatasetService datasetService;

    private int personasEvacuadas = 0;
    private int animalesEvacuados = 0;

    public char[][] crearMapaInicial(int numPersonasExtra, int numGanadoExtra, int numFaunaExtra) {
        personasEvacuadas = 0;
        animalesEvacuados = 0;

        char[][] mapa = new char[FILAS][COLUMNAS];
        for (int r = 0; r < FILAS; r++) {
            for (int c = 0; c < COLUMNAS; c++) {
                mapa[r][c] = '.';
            }
        }

        trazarEstructurasVolcan(mapa);

        Random rand = new Random(2026);
        generarCiudadMediaLunaBaja(mapa, rand);

        // Cargar datos reales creados automáticamente dentro de PostgreSQL
        List<DatasetPoblacion> dataset = datasetService.obtenerDataset();
        if (dataset != null) {
            for (DatasetPoblacion reg : dataset) {
                int r = reg.getFila();
                int c = reg.getColumna();
                if (r >= 0 && r < FILAS && c >= 0 && c < COLUMNAS) {
                    mapa[r][c] = reg.getTipo();
                }
            }
        }

        generarEntidadesPersonalizadas(mapa, numPersonasExtra, 'P', rand);
        generarEntidadesPersonalizadas(mapa, numGanadoExtra, 'A', rand);
        generarEntidadesPersonalizadas(mapa, numFaunaExtra, 'F', rand);

        generarVegetacionValle(mapa, 180, 'Y', rand);
        generarVegetacionValle(mapa, 150, 'u', rand);

        return mapa;
    }

    private void trazarEstructurasVolcan(char[][] mapa) {
        for (int r = FILA_CRATER - 1; r <= FILA_CRATER + 1; r++) {
            for (int c = 97; c <= 103; c++) {
                mapa[r][c] = 'V';
            }
        }
        trazarCauce(mapa, 25, 100, 100, -0.7);
        trazarCauce(mapa, 25, 100, 100, -0.1);
        trazarCauce(mapa, 25, 100, 100, 0.6);
    }

    private void trazarCauce(char[][] mapa, int inicioR, int inicioC, int len, double bias) {
        double col = inicioC;
        for (int i = 0; i < len; i++) {
            int r = inicioR + i;
            col += bias + (Math.sin(i * 0.15) * 0.3);
            int c = (int) Math.round(col);
            if (r >= 0 && r < FILAS && c >= 0 && c < COLUMNAS) {
                mapa[r][c] = 'B';
            }
        }
    }

    private void generarCiudadMediaLunaBaja(char[][] mapa, Random rand) {
        double[] radiosCalles = {82.0, 92.0, 102.0, 112.0, 122.0};

        for (double radio : radiosCalles) {
            for (double angulo = 0.40; angulo <= 2.74; angulo += 0.02) {
                int r = (int) Math.round(FILA_CRATER + (radio * Math.sin(angulo) * 0.78));
                int c = (int) Math.round(CENTRO_X + (radio * Math.cos(angulo)));

                if (r >= 0 && r < FILAS && c >= 0 && c < COLUMNAS) {
                    if (mapa[r][c] == '.') mapa[r][c] = '=';
                }
            }
        }

        for (double angulo = 0.48; angulo <= 2.66; angulo += 0.22) {
            for (double rDist = 80.0; rDist <= 125.0; rDist += 1.0) {
                int r = (int) Math.round(FILA_CRATER + (rDist * Math.sin(angulo) * 0.78));
                int c = (int) Math.round(CENTRO_X + (rDist * Math.cos(angulo)));

                if (r >= 0 && r < FILAS && c >= 0 && c < COLUMNAS) {
                    if (mapa[r][c] == '.') mapa[r][c] = '=';
                }
            }
        }

        for (double radio : new double[]{87.0, 97.0, 107.0, 117.0}) {
            for (double angulo = 0.44; angulo <= 2.70; angulo += 0.03) {
                int r = (int) Math.round(FILA_CRATER + (radio * Math.sin(angulo) * 0.78));
                int c = (int) Math.round(CENTRO_X + (radio * Math.cos(angulo)));

                if (r >= 0 && r < FILAS && c >= 0 && c < COLUMNAS && mapa[r][c] == '.') {
                    double prob = rand.nextDouble();
                    if (prob < 0.65) {
                        mapa[r][c] = 'C';
                    } else if (prob < 0.85) {
                        mapa[r][c] = 'T';
                    } else {
                        mapa[r][c] = 'E';
                    }
                }
            }
        }
    }

    private void generarEntidadesPersonalizadas(char[][] mapa, int cantidad, char tipo, Random rand) {
        int colocadas = 0;
        int intentos = 0;
        while (colocadas < cantidad && intentos < 10000) {
            intentos++;
            int r = 50 + rand.nextInt(75);
            int col = rand.nextInt(COLUMNAS);

            if (r < FILAS && col < COLUMNAS && !esConoVolcan(r, col)) {
                if (mapa[r][col] == '.' || mapa[r][col] == '=') {
                    mapa[r][col] = tipo;
                    colocadas++;
                }
            }
        }
    }

    private void generarVegetacionValle(char[][] mapa, int cantidad, char tipo, Random rand) {
        int c = 0;
        while (c < cantidad) {
            int r = 50 + rand.nextInt(75);
            int col = rand.nextInt(COLUMNAS);
            if (r < FILAS && col < COLUMNAS && mapa[r][col] == '.' && !esConoVolcan(r, col)) {
                mapa[r][col] = tipo;
                c++;
            }
        }
    }

    private boolean esConoVolcan(int r, int c) {
        if (r < FILA_CRATER || r > 85) return false;
        double altoRelative = (r - FILA_CRATER) / 63.0;
        double anchoPermitido = 10.0 + (altoRelative * 110.0);
        return Math.abs(c - CENTRO_X) <= anchoPermitido;
    }

    public void propagarLava(char[][] mapa, double velocidadKmH) {
        Random rand = new Random();
        char[][] sig = clonarMapa(mapa);

        moverEvacuacionEntidades(mapa, sig, rand);

        double probMultiplicador = velocidadKmH / 25.0;

        for (int r = 0; r < FILAS; r++) {
            for (int c = 0; c < COLUMNAS; c++) {
                if (mapa[r][c] == 'V' || mapa[r][c] == 'L') {
                    int[][] dirs = {{1, 0}, {1, -1}, {1, 1}, {2, 0}};

                    for (int[] d : dirs) {
                        int nr = r + d[0];
                        int nc = c + d[1];

                        if (nr >= FILA_CRATER && nr < FILAS && nc >= 0 && nc < COLUMNAS) {
                            double prob = 0.02 * probMultiplicador;
                            if (d[0] >= 1) prob += 0.28 * probMultiplicador;
                            if (mapa[nr][nc] == 'B') prob += 0.65 * probMultiplicador;

                            if (rand.nextDouble() < prob) {
                                char dest = mapa[nr][nc];
                                if (dest == '.' || dest == '=' || dest == 'B' || dest == 'P' || dest == 'A' || dest == 'F' || dest == 'Y' || dest == 'u') {
                                    sig[nr][nc] = 'L';
                                } else if (dest == 'C' || dest == 'T' || dest == 'E') {
                                    sig[nr][nc] = 'D';
                                } else if (dest == 'D') {
                                    sig[nr][nc] = 'X';
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int r = 0; r < FILAS; r++) {
            System.arraycopy(sig[r], 0, mapa[r], 0, COLUMNAS);
        }
    }

    private void moverEvacuacionEntidades(char[][] mapa, char[][] sig, Random rand) {
        for (int r = FILAS - 1; r >= 0; r--) {
            for (int c = 0; c < COLUMNAS; c++) {
                char entidad = mapa[r][c];
                if (entidad == 'P' || entidad == 'A' || entidad == 'F') {
                    int dr = 1;
                    int dc = (c < CENTRO_X) ? -1 : 1;
                    if (rand.nextDouble() < 0.20) dc = 0;

                    int nr = r + dr;
                    int nc = c + dc;

                    if (nr >= FILAS || nc < 0 || nc >= COLUMNAS) {
                        if (entidad == 'P') personasEvacuadas++;
                        else animalesEvacuados++;

                        sig[r][c] = (mapa[r][c] == '=') ? '=' : '.';
                    } else if (sig[nr][nc] == '.' || sig[nr][nc] == '=') {
                        sig[nr][nc] = entidad;
                        if (sig[r][c] == entidad) {
                            sig[r][c] = (mapa[r][c] == '=') ? '=' : '.';
                        }
                    }
                }
            }
        }
    }

    public int contarPersonasTotalesASalvo(char[][] mapa) {
        return contarEntidad(mapa, 'P') + personasEvacuadas;
    }

    public int contarAnimalesTotalesASalvo(char[][] mapa) {
        return contarEntidad(mapa, 'A') + contarEntidad(mapa, 'F') + animalesEvacuados;
    }

    public int contarEntidad(char[][] mapa, char tipo) {
        int contador = 0;
        for (int r = 0; r < FILAS; r++) {
            for (int c = 0; c < COLUMNAS; c++) {
                if (mapa[r][c] == tipo) contador++;
            }
        }
        return contador;
    }

    public int contarEstructurasIntactas(char[][] mapa) {
        int contador = 0;
        for (int r = 0; r < FILAS; r++) {
            for (int c = 0; c < COLUMNAS; c++) {
                char cel = mapa[r][c];
                if (cel == 'C' || cel == 'T' || cel == 'E') contador++;
            }
        }
        return contador;
    }

    public double calcularPorcentajeDestruccion(char[][] mapa) {
        int total = 0;
        int afect = 0;

        for (int r = 0; r < FILAS; r++) {
            for (int c = 0; c < COLUMNAS; c++) {
                char cel = mapa[r][c];
                if (cel == 'C' || cel == 'T' || cel == 'E') total++;
                if (cel == 'D') { total++; afect += 1; }
                if (cel == 'X') { total++; afect += 2; }
            }
        }
        return total == 0 ? 0.0 : ((double) afect / (total * 2.0)) * 100.0;
    }

    private char[][] clonarMapa(char[][] m) {
        char[][] c = new char[FILAS][COLUMNAS];
        for (int i = 0; i < FILAS; i++) c[i] = m[i].clone();
        return c;
    }
}