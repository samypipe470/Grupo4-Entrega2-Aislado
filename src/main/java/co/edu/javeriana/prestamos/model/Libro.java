package co.edu.javeriana.prestamos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "libro") // El nombre exacto de la tabla de G1
public class Libro {
    @Id
    private Integer id_libro;
    private String titulo;
    private String autor;
    private int cantidad_total;
    private int cantidad_disponible;

    public Libro() {
    }

    // Constructor simulado (para pruebas)
    public Libro(Integer id, String titulo, String autor, int disponible) {
        this.id_libro = id;
        this.titulo = titulo;
        this.autor = autor;
        this.cantidad_total = disponible;
        this.cantidad_disponible = disponible;
    }

    public Integer getId_libro() {
        return id_libro;
    }

    public void setId_libro(Integer id_libro) {
        this.id_libro = id_libro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public int getCantidad_total() {
        return cantidad_total;
    }

    public void setCantidad_total(int cantidad_total) {
        this.cantidad_total = cantidad_total;
    }

    public int getCantidad_disponible() {
        return cantidad_disponible;
    }

    public void setCantidad_disponible(int cantidad_disponible) {
        this.cantidad_disponible = cantidad_disponible;
    }
}
