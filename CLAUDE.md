# CLAUDE.md — ParkVision Backend (parkvision-service)

Eres un arquitecto de software senior especializado en Java 21, Spring Boot 4, Spring Data JPA y Domain-Driven Design. Este archivo es tu referencia canónica para el desarrollo del backend de ParkVision. Toda decisión de diseño debe respetar la estructura de **monolito modular por Bounded Contexts**, las 4 capas DDD, el patrón Resource/Assembler en la capa `interfaces` y los estándares de seguridad aquí definidos.

---

## 1. Visión General del Sistema

**ParkVision** es un sistema de gestión inteligente de estacionamientos con:
- Monitoreo de ocupación en tiempo real mediante cámaras IP (OpenCV) sobre nodos Fog.
- Predicción de disponibilidad futura mediante modelos de Machine Learning (Python / Random Forest) — *parcialmente implementado*.
- Notificaciones a usuarios y alertas operativas (emails transaccionales vía SendGrid + historial de notificaciones in-app persistido).

Este repositorio (`parkvision-service`) implementa el backend como un **monolito modular**: un único proceso desplegable donde cada carpeta de primer nivel representa un **Bounded Context** independiente con sus propias capas. Los bounded contexts se comunican entre sí únicamente a través de eventos de dominio o interfaces públicas definidas — nunca mediante acceso directo a las capas internas de otro contexto.

---

## 2. Arquitectura del Sistema

### Estilo Arquitectónico

**Monolito Modular** organizado por **Bounded Contexts** (patrón DDD).
- Un solo proceso / artefacto desplegable.
- Cada bounded context es autónomo dentro del monolito: tiene su propio modelo de dominio, su propia lógica y su propia persistencia (tablas separadas, mismo datasource).
- La comunicación entre contextos ocurre vía **eventos de dominio en proceso** (Spring `ApplicationEventPublisher` / `AbstractAggregateRoot.registerEvent`) o llamadas a interfaces de aplicación públicas (`application/internal/`). **Nunca** cruzar directamente a las capas `domain` o `infrastructure` de otro contexto.

### Componentes de Infraestructura

| Componente | Rol |
|---|---|
| **Spring Security + OAuth2 Resource Server** | Seguridad del pipeline de requests: JWT (RS256) stateless + autenticación por API-key para nodos Fog (contexto `iam`) |
| **Spring Data JPA + Hibernate** | Persistencia relacional; auditoría temporal automática |
| **PostgreSQL** | Base de datos relacional compartida — tablas separadas por contexto |
| **springdoc-openapi** | Documentación de la API (Swagger UI / OpenAPI 3.1) |
| **SendGrid** | Emails transaccionales (OTP de recuperación de contraseña, notificaciones) |
| **Bucket4j** | Rate limiting por IP en endpoints sensibles (`RateLimitInterceptor`) |
| **Apache Kafka** *(no implementado / futuro)* | Eventual integración con módulos externos Python (visión / predicción). Hoy la ingesta es REST. |

### Flujo de una Request

```
HTTP Request
    ↓
[Spring Security Filter]   (JWT RS256 + API-key Fog)
    ↓
interfaces/rest → Controller        (recibe Request record + @Valid)
    ↓
application/ → CommandService / QueryService   (orquesta el caso de uso, @Transactional)
    ↓
domain/model → Aggregate (@Entity)  (lógica de negocio)
    ↓
infrastructure/persistence → Repository (Spring Data JPA) → PostgreSQL
    ↓
interfaces/assembler → Assembler    (Aggregate → Resource DTO)
    ↓
HTTP Response (JSON)
```

---

## 3. Stack Tecnológico

| Capa | Tecnología | Versión / Notas |
|---|---|---|
| Lenguaje | Java | 21 (LTS) |
| Framework principal | Spring Boot | 4.0.5 |
| Persistencia | Spring Data JPA + Hibernate | PostgreSQL, tablas por bounded context; auditoría con `@CreatedDate`/`@LastModifiedDate` |
| Seguridad | Spring Security + OAuth2 Resource Server (JWT RS256) | Stateless; BCrypt (strength 12); auth por API-key (rol `FOG`); rate limiting con Bucket4j |
| Validación | Bean Validation (Jakarta) | `@Valid` sobre los Request records en la capa `interfaces` |
| Documentación API | springdoc-openapi | Swagger UI / OpenAPI 3.1 (3.0.3 para Spring Boot 4) |
| Email transaccional | SendGrid (`sendgrid-java`) | OTP de recuperación de contraseña y notificaciones |
| Rate limiting | Bucket4j (`bucket4j-core`) | Límite por IP en endpoints sensibles |
| Mensajería con módulos externos *(no implementado / futuro)* | Apache Kafka | Eventual integración visión / predicción (Python); hoy la ingesta es REST |
| Utilidades | Lombok | Reducción de boilerplate |
| Build | Maven | Wrapper incluido (`mvnw`) |
| Tests | JUnit 5 + Mockito + Spring Boot Test | Unit + Integration |

---

## 4. Estructura de Paquetes

El paquete raíz es `com.parkvision.parkvisionservice`. Cada Bounded Context es una subcarpeta directa de la raíz.

```
com.parkvision.parkvisionservice/
│
├── shared/                              # Kernel compartido (Value Objects, excepciones base, utils)
│
├── iam/                                 # Bounded Context: Identidad y Acceso
│   ├── interfaces/                      # Capa Interfaces — entrada HTTP y presentación
│   │   ├── rest/                        #   Controllers REST (@RestController)
│   │   ├── resource/                    #   Resources = DTOs de entrada/salida (records)
│   │   └── assembler/                   #   Assemblers (mapean Aggregate → Resource)
│   │
│   ├── application/                     # Capa Aplicación — orquestación de casos de uso
│   │   ├── commandservices/             #   Application Command Services (@Service, @Transactional)
│   │   ├── queryservices/              #   Application Query Services (@Service, readOnly)
│   │   └── internal/                   #   Servicios públicos consumibles por otros contextos
│   │
│   ├── domain/                          # Capa Dominio
│   │   ├── model/                       #   Aggregates (@Entity), Entidades, Value Objects
│   │   │   ├── aggregates/
│   │   │   ├── entities/
│   │   │   └── valueobjects/
│   │   ├── commands/                    #   Objetos Command (inmutables, records)
│   │   ├── events/                      #   Eventos de dominio en proceso (records)
│   │   ├── queries/                     #   Objetos Query (inmutables, records)
│   │   └── exceptions/                  #   Excepciones de dominio
│   │
│   └── infrastructure/                  # Capa Infraestructura — detalles técnicos
│       ├── persistence/                 #   Repositorios Spring Data JPA (@Repository), converters
│       ├── messaging/                   #   Kafka Producers / Consumers (módulos externos)
│       ├── external/                    #   Clientes REST hacia módulos externos (RestClient)
│       └── config/                      #   Configuraciones de Spring (@Configuration)
│
├── parking/                             # Bounded Context: Estacionamientos
│   ├── interfaces/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
│
├── prediction/                          # Bounded Context: Predicciones IA
│   ├── interfaces/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
│
├── occupancy/                           # Bounded Context: Ingesta de Ocupación (cámaras / nodos Fog)
│   ├── interfaces/
│   ├── application/                     #   incluye application/acl/ (puerto ACL hacia parking)
│   ├── domain/
│   └── infrastructure/                  #   incluye infrastructure/acl/ (impl. del puerto ACL)
│
└── notifications/                       # Bounded Context: Notificaciones
    ├── interfaces/
    ├── application/
    ├── domain/
    └── infrastructure/
```

### Regla de dependencias entre capas

```
interfaces → application → domain ← infrastructure
```

- `domain` contiene los aggregates (`@Entity`), value objects, commands/queries/events y excepciones. No importa de `interfaces` ni `application`.
- `infrastructure` provee los repositorios Spring Data JPA y converters sobre el modelo de `domain`.
- `interfaces` solo conoce Resources, Assemblers y llama a `application` vía los **Command/Query Services**.
- **Entre bounded contexts:** un contexto solo llama a los servicios en `application/internal/` de otro (o reacciona a sus eventos de dominio). Nunca accede a su `domain` o `infrastructure`.

---

## 5. Patrón Resource / Assembler (capa `interfaces`)

La capa `interfaces` expone los datos de dominio como **Resources** (DTOs JSON planos, sin HATEOAS). El **Resource** es la representación HTTP de salida; el **Request** es el DTO de entrada; el **Assembler** mapea el aggregate al Resource.

### Resource (DTO de salida)

Un **Resource** es un `record` inmutable. **Nunca** expone el aggregate `@Entity` directamente.

```java
// interfaces/resource/ParkingSpaceResource.java
public record ParkingSpaceResource(
        Long id,
        Long zoneId,
        boolean occupied,
        String classification
) {}
```

### Request (DTO de entrada)

```java
// interfaces/resource/OccupySpaceRequest.java
public record OccupySpaceRequest(
        @NotNull Long cameraId,
        @PositiveOrZero double confidence
) {}
```

### Assembler

Un **Assembler** (`@Component`) transforma el aggregate en su Resource. Es el único lugar donde ocurre el mapeo dominio → DTO.

```java
// interfaces/assembler/ParkingSpaceResourceAssembler.java
@Component
public class ParkingSpaceResourceAssembler {

    public ParkingSpaceResource toResource(ParkingSpace space) {
        return new ParkingSpaceResource(
                space.getId(),
                space.getZoneId(),
                space.isOccupied(),
                space.getClassification());
    }
}
```

### Controller (usa los Services + el Assembler)

```java
// interfaces/rest/ParkingSpaceController.java
@RestController
@RequestMapping(value = "/api/v1/parking/spaces", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ParkingSpaceController {

    private final ParkingSpaceCommandService commandService;
    private final ParkingSpaceQueryService queryService;
    private final ParkingSpaceResourceAssembler assembler;

    @GetMapping("/{spaceId}")
    public ResponseEntity<ParkingSpaceResource> getSpace(@PathVariable Long spaceId) {
        ParkingSpace space = queryService.handle(new GetSpaceStatusQuery(spaceId))
                .orElseThrow(() -> new SpaceNotFoundException(spaceId));
        return ResponseEntity.ok(assembler.toResource(space));
    }

    @PostMapping("/{spaceId}/occupy")
    public ResponseEntity<Void> occupy(@PathVariable Long spaceId,
                                       @Valid @RequestBody OccupySpaceRequest request) {
        commandService.handle(new OccupySpaceCommand(spaceId, request.cameraId(), request.confidence()));
        return ResponseEntity.accepted().build();
    }
}
```

### Reglas del patrón Resource/Assembler

- Un Resource **nunca** expone entidades JPA (`@Entity`) ni Aggregates directamente — siempre un `record`.
- El Assembler es el único lugar donde ocurre el mapeo Aggregate → Resource.
- Las requests entrantes se reciben como `record` con anotaciones `@Valid`/`@NotNull`, y se transforman en Commands en el Controller.
- Las colecciones se devuelven como `List<XResource>`.

---

## 6. Bounded Contexts Definidos

### 6.1 IAM — Identidad y Acceso (`iam`)

**Responsabilidad:** Registro, login y gestión de roles.

**Responsabilidad ampliada:** además de usuarios y roles, gestiona permisos, perfil y preferencias de usuario, favoritos y reseñas (ratings) de zonas, API-keys para nodos Fog y recuperación de contraseña por OTP.

**Endpoints clave:**
- `POST /api/v1/iam/users` — Registra un usuario (público; **solo POST** es público en esta ruta)
- `POST /api/v1/iam/auth/sign-in` — Valida credenciales, retorna JWT firmado
- `POST /api/v1/iam/auth/forgot-password` · `verify-otp` · `reset-password` — Recuperación de contraseña por OTP (email SendGrid)
- `GET /api/v1/iam/users/{userId}` — Consulta perfil (dueño o ADMIN)
- `GET /api/v1/iam/users` — Lista usuarios (ADMIN)
- `/api/v1/iam/api-keys/**` — Gestión de API-keys (ADMIN)

**Seguridad:**
- Contraseñas almacenadas con `BCryptPasswordEncoder` (strength = 12).
- JWT firmado con RS256 (par de claves asimétrico cargado vía env var; ver §8).
- API-key para nodos Fog: header `X-API-Key`, formato `keyId.secret`, otorga rol `FOG` (`ApiKeyAuthenticationFilter`).
- Tokens stateless — sin sesión en servidor.

**Aggregates (`@Entity`):** `User`, `Role`, `ApiKey`
**Entidades:** `Permission`, `RolePermission`, `UserProfile`, `UserPreferences`, `Favorite`, `Rating`
**Exposición cross-context:** `iam/application/internal/IamEngagementFacade` entrega las reseñas de una zona como `ZoneRatingView` (DTO plano, display name resuelto y email enmascarado) al contexto `parking` — sin filtrar entidades de `iam`.
**Nota:** Actualmente **no** se emiten eventos de dominio (no hay clases en `domain/events/`).
**Resources:** `UserResource`, `AuthenticationResource`, `RoleResource`, `ApiKeyResource`, `UserProfileResource`, `UserPreferencesResource`, `FavoriteResource`, `RatingResource`
**Assembler:** `UserResourceAssembler` (y los demás por entidad)

---

### 6.2 Parking — Estacionamientos (`parking`)

**Responsabilidad:** Gestión de estacionamientos, zonas, espacios y cálculo de disponibilidad.

**Lógica de negocio clave:**
- Cálculo de porcentaje de ocupación por zona.
- Clasificación automática de zonas: `LIBRE` (<30%), `MODERADO` (30-70%), `OCUPADO` (>70%).
- Exposición de disponibilidad actual vía Query side.

**Aggregates (`@Entity`):** `Zone`, `ParkingSpace` (no existe `ParkingLot`).
**Exposición cross-context:** `parking/application/internal/ParkingContextFacade` (open-host service consumido por `occupancy` vía su ACL): aplica cambios de estado de espacios y expone snapshots de zona/espacios. Para las reseñas públicas de una zona consume `iam` vía `IamEngagementFacade`.
**Nota:** No se emiten eventos de dominio actualmente.
**Resources:** `ZoneResource`, `ParkingSpaceResource`, `ZoneRatingResource`

---

### 6.3 Prediction — Predicciones IA (`prediction`)

**Responsabilidad:** Almacenar y exponer predicciones de ocupación futura por zona y ventana de tiempo.

**Estado actual (parcial):** Solo capa de dominio + persistencia. Implementado: aggregate `OccupancyForecast`, value object `TimeWindow`, command `UpsertForecastCommand`, query `GetCurrentForecastQuery` y `OccupancyForecastRepository`. **Pendiente:** Command/Query Services, Controller/Resources y el cliente `PredictionEngineClient` (`infrastructure/external/`) hacia el módulo Python (Random Forest). **No hay endpoints todavía.**

**Aggregate principal (`@Entity`):** `OccupancyForecast`

---

### 6.4 Occupancy — Ingesta de Ocupación (`occupancy`)

**Responsabilidad:** Gestión de cámaras y nodos Fog, e ingesta de los cambios de ocupación detectados por el módulo de visión (OpenCV/Python) que corre en el Fog.

**Integración:**
- Ingesta vía **REST** (no Kafka): `POST /api/v1/occupancy/events` y `POST /api/v1/occupancy/cameras/{cameraId}/events`, autenticados con **API-key (rol `FOG`)**.
- Aplica los cambios hacia `parking` a través de un **puerto ACL** (`occupancy/application/acl/ParkingContextPort`, implementado en `occupancy/infrastructure/acl/`), que consume `parking/application/internal/ParkingContextFacade`. `occupancy` **nunca** toca el `domain`/`infrastructure` de `parking`.

**Aggregates (`@Entity`):** `Camera`, `Node`. Logs de ocupación: `OccupancyLog`, `ZoneOccupancyLog`. Value object: `SpotStatus`.
**Resources:** `CameraResource`, `NodeResource`
**Assembler:** `CameraResourceAssembler`

---

### 6.5 Notifications — Notificaciones (`notifications`)

**Responsabilidad:** Persistir el historial de notificaciones a usuarios y gestionar las alertas de cámara (fallos / incidencias).

**Integración:**
- Emails transaccionales vía **SendGrid** (no Firebase, no Kafka).
- Persistencia del historial de notificaciones y de alertas de cámara.

**Aggregates (`@Entity`):** `Notification`, `CameraAlert`. Entidad: `CameraAlertNotification`.
**Resources:** `NotificationResource`, `CameraAlertResource`


## 7. Patrones de Persistencia (Spring Data JPA en capas)

El backend usa **JPA clásico en capas**, no Event Sourcing ni Axon. El aggregate root es una `@Entity` que hereda identidad `bigint` y auditoría temporal de una clase base compartida. Los Command/Query Services orquestan los casos de uso sobre el repositorio Spring Data.

### Aggregate (`@Entity`) → Command/Query Service → Repository

```java
// domain/commands/ — inmutable, record (sin id en la creación: lo genera la BD)
public record OccupySpaceCommand(Long spaceId, Long cameraId, double confidence) {}

// domain/model/aggregates/ — aggregate root JPA con lógica de negocio
@Entity
@Table(name = "parking_space")
@Getter
public class ParkingSpace extends AuditableAbstractAggregateRoot<ParkingSpace> {

    @Column(nullable = false)
    private Long zoneId;          // referencia replicada (sin FK cross-context)

    private boolean occupied;

    protected ParkingSpace() { }  // requerido por JPA

    public ParkingSpace(CreateSpaceCommand cmd) {
        this.zoneId = cmd.zoneId();
        this.occupied = false;
    }

    public void occupy() {
        if (!this.occupied) {
            this.occupied = true;
            // opcional: registerEvent(new SpaceOccupiedEvent(getId())) para reaccionar en otro contexto
        }
    }
}

// infrastructure/persistence/ — repositorio Spring Data JPA
@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    List<ParkingSpace> findByZoneId(Long zoneId);
}

// application/commandservices/ — orquesta la escritura
@Service
@RequiredArgsConstructor
public class ParkingSpaceCommandService {

    private final ParkingSpaceRepository repository;

    @Transactional
    public void handle(OccupySpaceCommand cmd) {
        ParkingSpace space = repository.findById(cmd.spaceId())
                .orElseThrow(() -> new SpaceNotFoundException(cmd.spaceId()));
        space.occupy();
        repository.save(space);
    }
}

// application/queryservices/ — orquesta la lectura
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkingSpaceQueryService {

    private final ParkingSpaceRepository repository;

    public Optional<ParkingSpace> handle(GetSpaceStatusQuery query) {
        return repository.findById(query.spaceId());
    }
}
```

### Reglas de persistencia

- Commands y Queries son **inmutables** — usar Java `record`. Los commands de creación **no** llevan id (lo genera la BD con `GENERATED ALWAYS AS IDENTITY`).
- El aggregate root extiende `AuditableAbstractAggregateRoot` (id `bigint` + `created_at`/`updated_at` automáticos). Las entidades hijas extienden `AuditableModel`.
- La lógica de negocio vive en métodos del aggregate (`occupy()`, `activate()`, …), no en los services. Los services solo orquestan y transaccionan.
- Los `@Service` de escritura usan `@Transactional`; los de lectura, `@Transactional(readOnly = true)`.
- Eventos de dominio **cross-context** (opcional): se publican en proceso con `registerEvent(...)` (Spring Data los emite en `save()`) y se consumen con `@EventListener` en `application/internal/` del otro contexto. Sin Axon.
- El Controller **nunca** instancia el repositorio directamente — siempre pasa por los Command/Query Services.

---

## 8. Seguridad

### JWT (RS256)

- Par de claves RSA cargado desde configuración: `iam.jwt.public-key` / `iam.jwt.private-key` (PEM, vía env vars `IAM_JWT_PUBLIC_KEY` / `IAM_JWT_PRIVATE_KEY`), en `IamConfiguration`.
- **Fallback de dev:** si las claves no están configuradas se genera un par **efímero** y se loguea un `WARN` — los tokens no sobreviven a reinicios ni a despliegues multi-instancia. **En producción las claves son obligatorias.**
- Verificación vía OAuth2 Resource Server (`NimbusJwtDecoder`); el rol del claim `role` se mapea a `ROLE_<role>` (`JwtAuthenticationConverter` en `SecurityConfig`).
- Claims: `sub` (userId), `role`, `iat`, `exp`. Tokens stateless — sin `HttpSession`.

### API-key (nodos Fog)

- `ApiKeyAuthenticationFilter` lee el header `X-API-Key` (formato `keyId.secret`) y otorga `ROLE_FOG`. Se registra antes del `BearerTokenAuthenticationFilter`.
- Protege los endpoints de ingesta de `occupancy` (`POST /api/v1/occupancy/events`, `/cameras/*/events`).

### Cadenas de filtros (`SecurityConfig`)

- **Cadena pública** (`publicFilterChain`): un `securityMatcher` sensible al método selecciona solo rutas públicas — `POST /api/v1/iam/users`, `/api/v1/iam/auth/**`, `GET .../zones/*/ratings`, Swagger y `/error`. **Importante:** el matcher debe distinguir el método HTTP; de lo contrario rutas compartidas (p.ej. el `GET` admin de `/iam/users`) caerían en la cadena sin JWT y nunca podrían autorizar.
- **Cadena protegida** (`protectedFilterChain`): JWT + API-key; `anyRequest().authenticated()` y autorización por método con `@PreAuthorize` / `@EnableMethodSecurity`.

### Rate limiting

- `RateLimitInterceptor` (Bucket4j) aplica un límite por IP en endpoints sensibles.

### BCrypt

```java
// Strength mínimo 12 — nunca bajar este valor
PasswordEncoder encoder = new BCryptPasswordEncoder(12);
```

### CORS

- Configurado globalmente en `shared/infrastructure/config/SecurityConfig.java` (aplicado en ambas cadenas).
- Los endpoints internos entre bounded contexts no necesitan CORS (mismo proceso).

---

## 9. Integración con Módulos Externos

### 9.1 Módulo IA (Python / Random Forest) — *pendiente*

- **Cliente previsto:** `PredictionEngineClient` en `prediction/infrastructure/external/` (aún no implementado).
- **Contrato previsto:**
  ```json
  // POST /predict  →  Request
  { "zone_id": 1, "features": { "hour": 14, "day_of_week": 2 } }
  // Response
  { "predicted_occupancy": 0.73, "confidence_score": 0.91, "model_version": "1.3.0" }
  ```

### 9.2 Módulo CV (OpenCV / Python) — ingesta REST

- La detección corre en el **nodo Fog**, que reporta los cambios al backend por **REST** (no Kafka):
  - `POST /api/v1/occupancy/cameras/{cameraId}/events` (lote por cámara) y `POST /api/v1/occupancy/events`.
  - Autenticación por **API-key (rol `FOG`)**.
- El Command Service de `occupancy` transforma el payload y aplica el cambio hacia `parking` vía el puerto ACL (`ParkingContextPort`).
- *Kafka (`cv.occupancy.detected`) queda como evolución futura, no está implementado.*

### 9.3 Email transaccional (SendGrid)

- **SDK:** `sendgrid-java`. El envío vive en `iam/infrastructure/email/EmailService` (OTP de recuperación de contraseña) y se reutiliza para notificaciones.
- Configuración vía `SENDGRID_API_KEY` y `MAIL_FROM`. Nunca commitear la API key.
- *Firebase Cloud Messaging no está implementado.*

---

## 10. Convenciones de Código

### Nomenclatura

| Artefacto | Patrón | Ejemplo |
|---|---|---|
| Command | VerbSustantivo + `Command` | `OccupySpaceCommand` |
| Event (dominio, opcional) | SustantivoParticipio + `Event` | `SpaceOccupiedEvent` |
| Query | `Get` + Sustantivo + `Query` | `GetZoneStatusQuery` |
| Aggregate (`@Entity`) | Entidad | `ParkingSpace` |
| Command Service | Entidad + `CommandService` | `ParkingSpaceCommandService` |
| Query Service | Entidad + `QueryService` | `ParkingSpaceQueryService` |
| Resource (DTO salida) | Entidad + `Resource` | `ParkingSpaceResource` |
| Request (DTO entrada) | Verbo + Entidad + `Request` | `OccupySpaceRequest` |
| Assembler | Entidad + `ResourceAssembler` | `ParkingSpaceResourceAssembler` |
| Controller | Entidad + `Controller` | `ParkingSpaceController` |
| Repository | Entidad + `Repository` | `ParkingSpaceRepository` |

### Identificadores

- Los IDs de todas las entidades son `Long` en Java / `bigint GENERATED ALWAYS AS IDENTITY` en PostgreSQL.
- El ID lo genera la base de datos al persistir; el comando de creación **no** lo incluye. El service obtiene el ID con `repository.save(entity).getId()`.
- La identidad y la auditoría (`created_at`/`updated_at`) se heredan de `AuditableAbstractAggregateRoot` (aggregate roots) o `AuditableModel` (entidades hijas), en `shared/domain/model/`.

### Requests entrantes

- Los cuerpos de request son **records** inmutables con anotaciones `@NotNull`/`@Valid`/`@Size`, en `interfaces/resource/`.
- Se reciben en el Controller y se transforman en Commands. El record de Request es el DTO de entrada (distinto del Resource de salida).

### Manejo de errores

- Excepciones de dominio en `<context>/domain/exceptions/`, extendiendo `ResourceNotFoundException` (en `shared/domain/exceptions/`) cuando aplique.
- `@RestControllerAdvice` en `shared/interfaces/rest/GlobalExceptionHandler` mapea excepciones a respuestas HTTP usando el record `ApiError`.
- Formato de error estándar:
  ```json
  {
    "timestamp": "2025-06-03T14:00:00Z",
    "status": 404,
    "error": "CAMERA_NOT_FOUND",
    "message": "Camera 123 not found",
    "path": "/api/v1/occupancy/cameras/123"
  }
  ```

---

## 11. Cómo Implementar un Nuevo Endpoint

Seguir siempre este orden:

1. **Definir el Command o Query** (records) en `<context>/domain/commands/` o `domain/queries/`.
2. **Implementar/actualizar el Aggregate** (`@Entity`) en `domain/model/aggregates/` — extiende `AuditableAbstractAggregateRoot` y contiene la lógica de negocio en métodos.
3. **Crear/actualizar el `@Repository`** (Spring Data JPA) en `infrastructure/persistence/`.
4. **Implementar el Command/Query Service** en `application/commandservices/` y `application/queryservices/` — orquesta la operación sobre el repositorio (`@Transactional`).
5. **Crear el Request y el Resource** (records) en `interfaces/resource/`.
6. **Crear o actualizar el Assembler** (`@Component`) en `interfaces/assembler/` — mapea el aggregate al Resource.
7. **Exponer el endpoint** en `interfaces/rest/` — inyecta los Command/Query Services y el Assembler; anota con `@Tag`/`@Operation` (springdoc).
8. **Escribir tests**: unit test del aggregate (lógica de negocio), test del service con repositorio mockeado, integration test del controller con `@SpringBootTest` / `@WebMvcTest`.

---

## 12. Verificación

```bash
# Compilar
./mvnw clean compile

# Tests unitarios
./mvnw test

# Levantar localmente
./mvnw spring-boot:run
```

### Checklist antes de marcar un endpoint como completo

- [ ] Command/Query definidos como `record` inmutables (sin id en la creación)
- [ ] Aggregate (`@Entity`) extiende `AuditableAbstractAggregateRoot`; la lógica vive en sus métodos
- [ ] Command/Query Service con `@Transactional` (readOnly en lectura)
- [ ] Resource (salida) y Request (entrada) son `record`; el Resource no expone el `@Entity`
- [ ] Assembler mapea Aggregate → Resource
- [ ] Tests: aggregate (lógica), service (repo mockeado), controller (`@WebMvcTest`/`@SpringBootTest`)
- [ ] Endpoint documentado con `@Operation` (springdoc)
- [ ] Sin credenciales hardcodeadas
- [ ] La comunicación cross-context pasa por `application/internal/` o eventos de dominio — nunca por `domain` de otro contexto
