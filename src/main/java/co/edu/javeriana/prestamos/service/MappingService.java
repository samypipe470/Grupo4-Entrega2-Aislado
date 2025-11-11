package co.edu.javeriana.prestamos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio simple para mapear ids del Catálogo (G3) a ids reales de BD (G1)
 * sin tocar la BD. Carga el mapeo desde la variable de entorno BOOK_ID_MAP con formato:
 *   429287771:96, 12345:98
 */
@Component
public class MappingService {

    private final Map<Integer, Integer> map = new HashMap<>();

    public MappingService(@Value("${BOOK_ID_MAP:}") String raw) {
        if (raw != null && !raw.isBlank()) {
            String[] pairs = raw.split(",");
            for (String p : pairs) {
                String s = p.trim();
                if (s.isEmpty()) continue;
                String[] kv = s.split(":");
                if (kv.length == 2) {
                    try {
                        Integer from = Integer.parseInt(kv[0].trim());
                        Integer to = Integer.parseInt(kv[1].trim());
                        map.put(from, to);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    public Integer mapToDbId(Integer catalogId) {
        if (catalogId == null) return null;
        return map.getOrDefault(catalogId, catalogId);
    }
}

