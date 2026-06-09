package com.biblioteca.backend.dto.response;

import java.util.List;

public record DashboardResumenResponse(
    long totalLibros,
    long totalLibrosActivos,
    long totalEjemplares,
    long totalUsuarios,
    long usuariosActivos,
    long totalAutores,
    long totalCategorias,
    long prestamosActivos,
    long prestamosAtrasados,
    long prestamosPorVencer,
    long prestamosDevueltosEsteMes,
    long librosAgotados,
    List<PrestamoResponse> prestamosRecientes,
    List<PrestamoResponse> prestamosVencidos,
    List<PrestamoResponse> prestamosProximosAVencer,
    List<DashboardRankingResponse> librosMasPrestados,
    List<DashboardRankingResponse> lectoresMasActivos,
    List<DashboardRankingResponse> categoriasConMasLibros,
    List<DashboardRankingResponse> autoresConMasLibros,
    List<DashboardLibroInventarioResponse> alertasInventario
) {}
