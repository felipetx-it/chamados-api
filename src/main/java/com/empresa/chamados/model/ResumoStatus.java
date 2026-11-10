package com.empresa.chamados.model;

/**
 * Resumo com as quantidades de chamados por status e o total geral.
 * Usado por GET /chamados/resumo.
 */
public record ResumoStatus(int abertos, int emAndamento, int resolvidos, int total) {
}
