package co.edu.javeriana.prestamos.service;

import org.springframework.stereotype.Component;

/**
 * Cliente simulado para el perfil aislado. No realiza llamadas HTTP, solo
 * devuelve valores deterministas para permitir pruebas sin depender de G3.
 */
@Component
public class CatalogClient {

    public BookDto getBook(String id) {
        BookDto dto = new BookDto();
        dto.id = id;
        dto.titulo = "Libro " + id;
        dto.autor = "Autor " + id;
        dto.cantidadTotal = 5;
        dto.cantidadDisponible = simulateDisponibilidad(id) ? 5 : 0;
        return dto;
    }

    public boolean reservarUno(String id) {
        BookDto book = getBook(id);
        return book.cantidadDisponible != null && book.cantidadDisponible > 0;
    }

    public boolean devolverUno(String id) { return true; }

    public static class BookDto {
        public String id;
        public String titulo;
        public String autor;
        public Integer cantidadTotal;
        public Integer cantidadDisponible;
        public String categoria;
    }

    private boolean simulateDisponibilidad(String id) {
        try {
            int num = Integer.parseInt(id);
            return num % 2 == 0; // pares disponibles, impares no
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
