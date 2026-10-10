package com.eduardosystems.simulaciondamas.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class IaService {

    public String calcularMejorMovimiento(String tablero) {
        char[][] grid = deserializar(tablero);
        List<int[]> movimientos = obtenerMovimientosValidos(grid, 'b', 'B');

        if (movimientos.isEmpty()) return tablero;

        int mejorValor = Integer.MIN_VALUE;
        int[] mejorMov = movimientos.get(0);

        for (int[] mov : movimientos) {
            char[][] copia = clonarGrid(grid);
            aplicarMovimiento(copia, mov);
            int valor = minimax(copia, 3, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (valor > mejorValor) {
                mejorValor = valor;
                mejorMov = mov;
            }
        }

        aplicarMovimiento(grid, mejorMov);
        return serializar(grid);
    }

    private int minimax(char[][] grid, int profundidad, boolean esMax, int alpha, int beta) {
        if (profundidad == 0) return evaluarTablero(grid);

        if (esMax) {
            int maxEval = Integer.MIN_VALUE;
            List<int[]> movs = obtenerMovimientosValidos(grid, 'b', 'B');
            if (movs.isEmpty()) return -1000;
            for (int[] mov : movs) {
                char[][] copia = clonarGrid(grid);
                aplicarMovimiento(copia, mov);
                int eval = minimax(copia, profundidad - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            List<int[]> movs = obtenerMovimientosValidos(grid, 'r', 'R');
            if (movs.isEmpty()) return 1000;
            for (int[] mov : movs) {
                char[][] copia = clonarGrid(grid);
                aplicarMovimiento(copia, mov);
                int eval = minimax(copia, profundidad - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluarTablero(char[][] grid) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] == 'b') score += 10 + r;
                if (grid[r][c] == 'B') score += 20;
                if (grid[r][c] == 'r') score -= (10 + (7 - r));
                if (grid[r][c] == 'R') score -= 20;
            }
        }
        return score;
    }

    public List<int[]> obtenerMovimientosValidos(char[][] grid, char normal, char reina) {
        List<int[]> lista = new ArrayList<>();
        int dir = (normal == 'b') ? 1 : -1;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] == normal || grid[r][c] == reina) {
                    boolean esReina = (grid[r][c] == reina);

                    comprobarDireccion(grid, r, c, dir, -1, normal, esReina, lista);
                    comprobarDireccion(grid, r, c, dir, 1, normal, esReina, lista);

                    if (esReina) {
                        comprobarDireccion(grid, r, c, -dir, -1, normal, true, lista);
                        comprobarDireccion(grid, r, c, -dir, 1, normal, true, lista);
                    }
                }
            }
        }
        return lista;
    }

    private void comprobarDireccion(char[][] g, int r, int c, int dr, int dc, char normal, boolean esReina, List<int[]> list) {
        int nr = r + dr, nc = c + dc;
        if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
            if (g[nr][nc] == '.') {
                list.add(new int[]{r, c, nr, nc, 0});
            } else if (esEnemigo(g[nr][nc], normal)) {
                int nnr = nr + dr, nnc = nc + dc;
                if (nnr >= 0 && nnr < 8 && nnc >= 0 && nnc < 8 && g[nnr][nnc] == '.') {
                    list.add(new int[]{r, c, nnr, nnc, 1});
                }
            }
        }
    }

    private boolean esEnemigo(char ficha, char alineacionNormal) {
        if (ficha == '.') return false;
        if (alineacionNormal == 'b') return ficha == 'r' || ficha == 'R';
        return ficha == 'b' || ficha == 'B';
    }

    public void aplicarMovimiento(char[][] g, int[] m) {
        char p = g[m[0]][m[1]];
        g[m[0]][m[1]] = '.';

        if (p == 'b' && m[2] == 7) p = 'B';
        if (p == 'r' && m[2] == 0) p = 'R';
        g[m[2]][m[3]] = p;

        if (m[4] == 1) {
            int mr = (m[0] + m[2]) / 2;
            int mc = (m[1] + m[3]) / 2;
            g[mr][mc] = '.';
        }
    }

    public char[][] deserializar(String s) {
        char[][] g = new char[8][8];
        for (int i = 0; i < 64; i++) g[i / 8][i % 8] = s.charAt(i);
        return g;
    }

    public String serializar(char[][] g) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) sb.append(g[r][c]);
        return sb.toString();
    }

    private char[][] clonarGrid(char[][] g) {
        char[][] c = new char[8][8];
        for (int i = 0; i < 8; i++) c[i] = g[i].clone();
        return c;
    }
}