package com.papusbarbershop.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * DTO para resúmenes mensuales.
 */
public class ResumenMensualDTO {

    private YearMonth mes;
    private BigDecimal totalServicios;
    private BigDecimal totalVentas;
    private BigDecimal totalComisiones;
    private BigDecimal totalGeneral;
    private Integer cantidadServicios;
    private Integer cantidadVentas;
    private List<ResumenBarberoDTO> resumenBarberos;

    // ==================== CONSTRUCTORES ====================

    public ResumenMensualDTO() {
    }

    // ==================== GETTERS Y SETTERS ====================

    public YearMonth getMes() {
        return mes;
    }

    public void setMes(YearMonth mes) {
        this.mes = mes;
    }

    public BigDecimal getTotalServicios() {
        return totalServicios;
    }

    public void setTotalServicios(BigDecimal totalServicios) {
        this.totalServicios = totalServicios;
    }

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }

    public BigDecimal getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(BigDecimal totalGeneral) {
        this.totalGeneral = totalGeneral;
    }

    public Integer getCantidadServicios() {
        return cantidadServicios;
    }

    public void setCantidadServicios(Integer cantidadServicios) {
        this.cantidadServicios = cantidadServicios;
    }

    public Integer getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(Integer cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public BigDecimal getTotalComisiones() {
        return totalComisiones;
    }

    public void setTotalComisiones(BigDecimal totalComisiones) {
        this.totalComisiones = totalComisiones;
    }

    public List<ResumenBarberoDTO> getResumenBarberos() {
        return resumenBarberos;
    }

    public void setResumenBarberos(List<ResumenBarberoDTO> resumenBarberos) {
        this.resumenBarberos = resumenBarberos;
    }
}

