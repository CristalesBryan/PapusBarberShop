package com.papusbarbershop.service;

import com.papusbarbershop.dto.ResumenBarberoDTO;
import com.papusbarbershop.dto.ResumenDiarioDTO;
import com.papusbarbershop.dto.ResumenMensualDTO;
import com.papusbarbershop.entity.Barbero;
import com.papusbarbershop.repository.BarberoRepository;
import com.papusbarbershop.repository.ServicioRepository;
import com.papusbarbershop.repository.VentaProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para la generación de reportes y resúmenes.
 * 
 * Este servicio calcula totales, pagos a barberos y genera reportes
 * diarios y mensuales.
 */
@Service
public class ReporteService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private VentaProductoRepository ventaProductoRepository;

    @Autowired
    private BarberoRepository barberoRepository;

    /**
     * Genera un resumen diario para una fecha específica.
     * 
     * @param fecha Fecha del resumen
     * @return Resumen diario
     */
    public ResumenDiarioDTO generarResumenDiario(LocalDate fecha) {
        ResumenDiarioDTO resumen = new ResumenDiarioDTO();
        resumen.setFecha(fecha);

        // Calcular totales de servicios
        List<com.papusbarbershop.entity.Servicio> servicios = servicioRepository.findByFecha(fecha);
        BigDecimal totalServicios = servicios.stream()
                .map(com.papusbarbershop.entity.Servicio::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumen.setTotalServicios(totalServicios);
        resumen.setCantidadServicios(servicios.size());

        // Calcular totales de ventas
        List<com.papusbarbershop.entity.VentaProducto> ventas = ventaProductoRepository.findByFecha(fecha);
        BigDecimal totalVentas = ventas.stream()
                .map(com.papusbarbershop.entity.VentaProducto::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumen.setTotalVentas(totalVentas);
        resumen.setCantidadVentas(ventas.size());

        // Calcular total de comisiones
        // Comisión = comision del producto * cantidad vendida
        BigDecimal totalComisiones = ventas.stream()
                .map(venta -> {
                    Integer comision = venta.getProducto().getComision();
                    if (comision == null) {
                        comision = 0;
                    }
                    // Debug: verificar comisión del producto
                    System.out.println("DEBUG Diario - Venta ID: " + venta.getId() + 
                            ", Producto: " + venta.getProducto().getNombre() + 
                            ", Comisión: " + comision + 
                            ", Cantidad: " + venta.getCantidad());
                    // Comisión = comision del producto * cantidad vendida
                    return BigDecimal.valueOf(comision).multiply(BigDecimal.valueOf(venta.getCantidad()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumen.setTotalComisiones(totalComisiones);

        // Total general
        resumen.setTotalGeneral(totalServicios.add(totalVentas));

        // Resumen por barbero
        List<ResumenBarberoDTO> resumenBarberos = calcularResumenBarberos(fecha, fecha);
        resumen.setResumenBarberos(resumenBarberos);

        return resumen;
    }

    /**
     * Genera un resumen mensual para un mes específico.
     * 
     * @param yearMonth Año y mes del resumen
     * @return Resumen mensual
     */
    public ResumenMensualDTO generarResumenMensual(YearMonth yearMonth) {
        ResumenMensualDTO resumen = new ResumenMensualDTO();
        resumen.setMes(yearMonth);

        LocalDate fechaInicio = yearMonth.atDay(1);
        LocalDate fechaFin = yearMonth.atEndOfMonth();

        // Calcular totales de servicios
        List<com.papusbarbershop.entity.Servicio> servicios = servicioRepository.findByFechaBetween(fechaInicio, fechaFin);
        BigDecimal totalServicios = servicios.stream()
                .map(com.papusbarbershop.entity.Servicio::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumen.setTotalServicios(totalServicios);
        resumen.setCantidadServicios(servicios.size());

        // Calcular totales de ventas
        List<com.papusbarbershop.entity.VentaProducto> ventas = ventaProductoRepository.findByFechaBetween(fechaInicio, fechaFin);
        BigDecimal totalVentas = ventas.stream()
                .map(com.papusbarbershop.entity.VentaProducto::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumen.setTotalVentas(totalVentas);
        resumen.setCantidadVentas(ventas.size());

        // Calcular total de comisiones
        // Comisión = comision del producto * cantidad vendida
        BigDecimal totalComisiones = ventas.stream()
                .map(venta -> {
                    Integer comision = venta.getProducto().getComision();
                    if (comision == null) {
                        comision = 0;
                    }
                    // Debug: verificar comisión del producto
                    System.out.println("DEBUG Mensual - Venta ID: " + venta.getId() + 
                            ", Producto: " + venta.getProducto().getNombre() + 
                            ", Comisión: " + comision + 
                            ", Cantidad: " + venta.getCantidad());
                    // Comisión = comision del producto * cantidad vendida
                    return BigDecimal.valueOf(comision).multiply(BigDecimal.valueOf(venta.getCantidad()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumen.setTotalComisiones(totalComisiones);

        // Total general
        resumen.setTotalGeneral(totalServicios.add(totalVentas));

        // Resumen por barbero
        List<ResumenBarberoDTO> resumenBarberos = calcularResumenBarberos(fechaInicio, fechaFin);
        resumen.setResumenBarberos(resumenBarberos);

        return resumen;
    }

    /**
     * Genera un resumen para una fecha específica (puede ser diario o mensual según la fecha).
     * 
     * @param fecha Fecha del resumen
     * @return Resumen diario
     */
    public ResumenDiarioDTO generarResumenPorFecha(LocalDate fecha) {
        return generarResumenDiario(fecha);
    }

    /**
     * Calcula el resumen de todos los barberos en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de resúmenes por barbero
     */
    private List<ResumenBarberoDTO> calcularResumenBarberos(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Barbero> barberos = barberoRepository.findAll();
        List<ResumenBarberoDTO> resumenBarberos = new ArrayList<>();

        for (Barbero barbero : barberos) {
            ResumenBarberoDTO resumen = new ResumenBarberoDTO();
            resumen.setBarberoId(barbero.getId());
            resumen.setBarberoNombre(barbero.getNombre());
            resumen.setPorcentajeServicio(barbero.getPorcentajeServicio());

            // Calcular total de servicios
            BigDecimal totalServicios = servicioRepository.calcularTotalPorBarbero(
                    barbero.getId(), fechaInicio, fechaFin);
            resumen.setTotalServicios(totalServicios);

            // Contar servicios
            List<com.papusbarbershop.entity.Servicio> servicios = servicioRepository.findByBarberoId(barbero.getId());
            servicios = servicios.stream()
                    .filter(s -> !s.getFecha().isBefore(fechaInicio) && !s.getFecha().isAfter(fechaFin))
                    .toList();
            resumen.setCantidadServicios(servicios.size());

            // Calcular total de ventas
            BigDecimal totalVentas = ventaProductoRepository.calcularTotalPorBarbero(
                    barbero.getId(), fechaInicio, fechaFin);
            resumen.setTotalVentas(totalVentas);

            // Contar ventas
            List<com.papusbarbershop.entity.VentaProducto> ventas = ventaProductoRepository.findByBarberoId(barbero.getId());
            ventas = ventas.stream()
                    .filter(v -> !v.getFecha().isBefore(fechaInicio) && !v.getFecha().isAfter(fechaFin))
                    .toList();
            resumen.setCantidadVentas(ventas.size());

            // Calcular total de comisiones por barbero
            // Comisión = comision del producto * cantidad vendida
            BigDecimal totalComisiones = ventas.stream()
                    .map(venta -> {
                        Integer comision = venta.getProducto().getComision();
                        if (comision == null) {
                            comision = 0;
                        }
                        // Debug: verificar comisión del producto
                        System.out.println("DEBUG - Venta ID: " + venta.getId() + 
                                ", Producto: " + venta.getProducto().getNombre() + 
                                ", Comisión: " + comision + 
                                ", Cantidad: " + venta.getCantidad());
                        // Comisión = comision del producto * cantidad vendida
                        return BigDecimal.valueOf(comision).multiply(BigDecimal.valueOf(venta.getCantidad()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            resumen.setTotalComisiones(totalComisiones);

            // Total generado (servicios + ventas)
            BigDecimal totalGenerado = totalServicios.add(totalVentas);
            resumen.setTotalGenerado(totalGenerado);

            // Calcular pago del barbero (porcentaje sobre servicios + total de comisiones)
            BigDecimal porcentaje = barbero.getPorcentajeServicio().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal pagoPorServicios = totalServicios.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
            // Pago total = pago por servicios + comisiones
            BigDecimal pagoBarbero = pagoPorServicios.add(totalComisiones).setScale(2, RoundingMode.HALF_UP);
            resumen.setPagoBarbero(pagoBarbero);

            resumenBarberos.add(resumen);
        }

        return resumenBarberos;
    }
}

