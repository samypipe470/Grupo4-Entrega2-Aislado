package co.edu.javeriana.prestamos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "prestamo")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_prestamo;
    private Integer id_usuario;
    private Integer id_libro;
    private LocalDateTime fecha_prestamo;
    private LocalDateTime fecha_devolucion_esperada;
    private LocalDateTime fecha_devolucion_real;
    private Integer id_estado_prestamo;
    private Double valor_multa;

    public Prestamo() {
    }

    // Constructor para un préstamo nuevo
    public Prestamo(Integer id_prestamo, Integer id_usuario, Integer id_libro,
                    Integer id_estado_prestamo, LocalDateTime fecha_devolucion_esperada) {
        this.id_prestamo = id_prestamo;
        this.id_usuario = id_usuario;
        this.id_libro = id_libro;
        this.id_estado_prestamo = id_estado_prestamo;
        this.fecha_prestamo = LocalDateTime.now();
        this.fecha_devolucion_esperada = fecha_devolucion_esperada;
        this.fecha_devolucion_real = null;
        this.valor_multa = 0d;
    }

    public Integer getId_prestamo() {
        return id_prestamo;
    }

    public void setId_prestamo(Integer id_prestamo) {
        this.id_prestamo = id_prestamo;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public Integer getId_libro() {
        return id_libro;
    }

    public void setId_libro(Integer id_libro) {
        this.id_libro = id_libro;
    }

    public LocalDateTime getFecha_prestamo() {
        return fecha_prestamo;
    }

    public void setFecha_prestamo(LocalDateTime fecha_prestamo) {
        this.fecha_prestamo = fecha_prestamo;
    }

    public LocalDateTime getFecha_devolucion_esperada() {
        return fecha_devolucion_esperada;
    }

    public void setFecha_devolucion_esperada(LocalDateTime fecha_devolucion_esperada) {
        this.fecha_devolucion_esperada = fecha_devolucion_esperada;
    }

    public LocalDateTime getFecha_devolucion_real() {
        return fecha_devolucion_real;
    }

    public void setFecha_devolucion_real(LocalDateTime fecha_devolucion_real) {
        this.fecha_devolucion_real = fecha_devolucion_real;
    }

    public Integer getId_estado_prestamo() {
        return id_estado_prestamo;
    }

    public void setId_estado_prestamo(Integer id_estado_prestamo) {
        this.id_estado_prestamo = id_estado_prestamo;
    }

    public Double getValor_multa() {
        return valor_multa;
    }

    public void setValor_multa(Double valor_multa) {
        this.valor_multa = valor_multa;
    }
}
