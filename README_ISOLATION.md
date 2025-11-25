G4 - Pruebas en Aislamiento (JUnit + Postman)

Objetivo: validar el módulo de Préstamos y Reservas sin depender de G2 (Auth) ni G3 (Catálogo).

Contenido
- JUnit con mocks: sin llamadas HTTP ni BD real.
- Postman Collection: ejercita los endpoints usando un usuario simulado y un Mock Server para G3.

1) JUnit: ejecutar pruebas unitarias
- Requisitos: Java 17, Maven
- Comando: mvn -q -Dtest=*Service*Test test
- Qué se prueba:
  - LoanService: reglas de límite, vencidos, reservar/devolver en catálogo (mock), transiciones de estado, notificación de cola.
  - ReservationService: creación cuando no hay disponibilidad, duplicados, notificación al siguiente, expiraciones.

2) API en Postman (aislado)
Paso A — Mock Server de G3
- En Postman, crea un Mock Server nuevo (Design > Mock server > Create a new API), base path vacío.
- Agrega ejemplos mínimos:
  1. GET /api/books/123 -> 200 JSON
     {
       "id": "123",
       "titulo": "Libro simulado",
       "autor": "Autor",
       "cantidadTotal": 3,
       "cantidadDisponible": 0,
       "categoria": "General"
     }
  2. PUT /api/books/123 -> 200 (sin body) para aceptar actualizaciones de disponibilidad.

Nota: Si quieres simular disponibilidad, cambia cantidadDisponible a 1.

Paso B — Ejecutar G4 contra el Mock
- Arranca el backend de G4 apuntando al Mock:
  mvn spring-boot:run -Dspring-boot.run.arguments="--catalog.base-url=https://<postman-mock-id>.mock.pstmn.io"

Paso C — Enviar requests desde Postman
- Importa Prestamos_Grupo4_Isolated.postman_collection.json
- Define variable de colección G4_BASE = http://localhost:8081
- Importante: los requests incluyen headers X-User-Id y X-User-Roles. El backend crea un usuario simulado (sin JWT).
  - X-User-Roles: ESTUDIANTE | BIBLIOTECARIO | ADMIN

Endpoints incluidos
- POST {{G4_BASE}}/api/loans          { "libro_id": 123 }
- GET  {{G4_BASE}}/api/loans/my-loans
- PUT  {{G4_BASE}}/api/loans/1/return (usa ADMIN para privilegios)
- POST {{G4_BASE}}/api/reservations   { "libro_id": 123 }
- GET  {{G4_BASE}}/api/reservations/my-reservations

3) Cómo funciona el aislamiento
- MockUserFilter (habilitado por defecto) inyecta un principal de prueba si no hay JWT y permite simular roles vía headers.
- CatalogClient usa la propiedad catalog.base-url; apúntala al Mock Server de Postman.
- MappingService toma BOOK_ID_MAP del entorno para mapear ids de G3 a ids de BD (si aplica). Ejemplo:
  BOOK_ID_MAP=123:96,456:97

4) Producción / endurecer
- Desactiva el usuario simulado con: --security.mock-user.enabled=false
- Ajusta reglas de seguridad para requerir JWT real si ya integras G2.

