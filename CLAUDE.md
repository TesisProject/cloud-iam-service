# CLAUDE.md — ParkVision Backend (spoticar-service)

Eres un arquitecto de software senior especializado en Java 21, Spring Boot 4, Axon Framework y Domain-Driven Design. Este archivo es tu referencia canónica para el desarrollo del backend de ParkVision. Toda decisión de diseño debe respetar la estructura de **monolito modular por Bounded Contexts**, las 4 capas DDD, el patrón Resource/Assembler en la capa `interfaces` y los estándares de seguridad aquí definidos.

---

## 1. Visión General del Sistema

**ParkVision** es un sistema de gestión inteligente de estacionamientos con:
- Monitoreo de ocupación en tiempo real mediante cámaras IP (OpenCV).
- Predicción de disponibilidad futura mediante modelos de Machine Learning (Python / Random Forest).
- Notificaciones push a usuarios vía Firebase Cloud Messaging.

Este repositorio (`spoticar-service`) implementa el backend como un **monolito modular**: un único proceso desplegable donde cada carpeta de primer nivel representa un **Bounded Context** independiente con sus propias capas. Los bounded contexts se comunican entre sí únicamente a través de eventos de dominio o interfaces públicas definidas — nunca mediante acceso directo a las capas internas de otro contexto.

---

## 2. Arquitectura del Sistema

### Estilo Arquitectónico

**Monolito Modular** organizado por **Bounded Contexts** (patrón DDD).
- Un solo proceso / artefacto desplegable.
- Cada bounded context es autónomo dentro del monolito: tiene su propio modelo de dominio, su propia lógica y su propia persistencia (tablas separadas, mismo datasource).
- La comunicación entre contextos ocurre vía eventos de Axon o llamadas a interfaces de aplicación públicas (`application/internal/`). **Nunca** cruzar directamente a las capas `domain` o `infrastructure` de otro contexto.

### Componentes de Infraestructura

| Componente | Rol |
|---|---|
| **Spring Security + JWT** | Seguridad stateless, filtro global en el request pipeline |
| **Apache Kafka** | Eventos de dominio cross-context (disponibilidad, alertas) |
| **PostgreSQL** | Base de datos relacional compartida — tablas separadas por contexto |
| **Axon Server** | Event Store + Message Bus para CQRS y Event Sourcing |

### Flujo de una Request

```
HTTP Request
    ↓
[Spring Security JWT Filter]
    ↓
interfaces/ → Controller
    ↓
application/ → CommandGateway / QueryGateway (Axon)
    ↓
domain/ → Aggregate (@CommandHandler → apply(Event))
    ↓
Event Store (Axon) → @EventSourcingHandler (reconstruye estado)
    ↓
@EventHandler (Projection) → PostgreSQL (read model)
    ↓
@QueryHandler → Resource (vía Assembler)
    ↓
HTTP Response (Resource con links HATEOAS)
```

---

## 3. Stack Tecnológico

| Capa | Tecnología | Versión / Notas |
|---|---|---|
| Lenguaje | Java | 21 (LTS) |
| Framework principal | Spring Boot | 4.0.5 |
| CQRS / Event Sourcing | Axon Framework | 4.10.x |
| Seguridad | Spring Security + JWT | Stateless, BCrypt para contraseñas |
| Persistencia | Spring Data JPA + Hibernate | PostgreSQL, tablas por bounded context |
| Mensajería cross-context | Apache Kafka | Eventos de dominio entre bounded contexts |
| Hypermedia / Presentación | Spring HATEOAS | Patrón Resource/Assembler en capa `interfaces` |
| Utilidades | Lombok | Reducción de boilerplate |
| Build | Maven | Wrapper incluido (`mvnw`) |
| Tests | JUnit 5 + Mockito + AxonFramework Test | Unit + Integration |

---

## 4. Estructura de Paquetes

El paquete raíz es `com.spoticar.spoticarservice`. Cada Bounded Context es una subcarpeta directa de la raíz.

```
com.spoticar.spoticarservice/
│
├── shared/                              # Kernel compartido (Value Objects, excepciones base, utils)
│
├── iam/                                 # Bounded Context: Identidad y Acceso
│   ├── interfaces/                      # Capa Interfaces — entrada HTTP y presentación
│   │   ├── rest/                        #   Controllers REST (@RestController)
│   │   ├── resource/                    #   Resources HATEOAS (representación HTTP)
│   │   └── assembler/                   #   Assemblers (mapean Domain/View → Resource)
│   │
│   ├── application/                     # Capa Aplicación — orquestación de casos de uso
│   │   ├── commandservices/             #   Command Handlers / Application Command Services
│   │   ├── queryservices/              #   Query Handlers / Application Query Services
│   │   └── internal/                   #   Servicios públicos consumibles por otros contextos
│   │
│   ├── domain/                          # Capa Dominio — lógica pura, sin dependencias de framework
│   │   ├── model/                       #   Aggregates (@Aggregate), Entidades, Value Objects
│   │   │   ├── aggregates/
│   │   │   ├── entities/
│   │   │   └── valueobjects/
│   │   ├── commands/                    #   Objetos Command (inmutables, records)
│   │   ├── events/                      #   Objetos Event (inmutables, records)
│   │   ├── queries/                     #   Objetos Query (inmutables, records)
│   │   └── exceptions/                  #   Excepciones de dominio
│   │
│   └── infrastructure/                  # Capa Infraestructura — detalles técnicos
│       ├── persistence/                 #   Repositorios JPA (@Repository), entidades @Entity (read model)
│       ├── messaging/                   #   Kafka Producers / Consumers
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
├── vision/                              # Bounded Context: Visión Computacional
│   ├── interfaces/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
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

- `domain` no importa nada de `interfaces`, `application` ni `infrastructure`.
- `infrastructure` implementa interfaces/puertos definidos en `domain`.
- `interfaces` solo conoce Resources, Assemblers y llama a `application` vía `CommandGateway` / `QueryGateway`.
- **Entre bounded contexts:** un contexto solo llama a los servicios en `application/internal/` de otro. Nunca accede a su `domain` o `infrastructure`.

---

## 5. Patrón Resource / Assembler (capa `interfaces`)

La capa `interfaces` expone los datos de dominio como **Resources HTTP** usando el patrón Resource/Assembler, apoyado en Spring HATEOAS.

### Resource

Un **Resource** es la representación HTTP de una entidad o vista de dominio. Extiende `RepresentationModel` para soportar links HATEOAS.

```java
// interfaces/resource/ParkingSpaceResource.java
public class ParkingSpaceResource extends RepresentationModel<ParkingSpaceResource> {
    private String spaceId;
    private String zoneId;
    private boolean occupied;
    private String classification;
    // Lombok @Getter / @Setter o constructor manual
}
```

### Assembler

Un **Assembler** transforma una vista de dominio (o entidad de lectura) en un Resource, añadiendo los links HATEOAS correspondientes.

```java
// interfaces/assembler/ParkingSpaceResourceAssembler.java
@Component
public class ParkingSpaceResourceAssembler
        implements RepresentationModelAssembler<ParkingSpaceView, ParkingSpaceResource> {

    @Override
    public ParkingSpaceResource toModel(ParkingSpaceView view) {
        ParkingSpaceResource resource = new ParkingSpaceResource();
        resource.setSpaceId(view.getSpaceId());
        resource.setZoneId(view.getZoneId());
        resource.setOccupied(view.isOccupied());
        resource.setClassification(view.getClassification());
        resource.add(linkTo(methodOn(ParkingSpaceController.class)
                .getSpace(view.getSpaceId())).withSelfRel());
        resource.add(linkTo(methodOn(ZoneController.class)
                .getZone(view.getZoneId())).withRel("zone"));
        return resource;
    }
}
```

### Controller (usa el Assembler)

```java
// interfaces/rest/ParkingSpaceController.java
@RestController
@RequestMapping("/api/v1/parking/spaces")
@RequiredArgsConstructor
public class ParkingSpaceController {

    private final QueryGateway queryGateway;
    private final CommandGateway commandGateway;
    private final ParkingSpaceResourceAssembler assembler;

    @GetMapping("/{spaceId}")
    public ResponseEntity<ParkingSpaceResource> getSpace(@PathVariable String spaceId) {
        ParkingSpaceView view = queryGateway.query(
            new GetSpaceStatusQuery(spaceId), ParkingSpaceView.class).join();
        return ResponseEntity.ok(assembler.toModel(view));
    }

    @PostMapping("/{spaceId}/occupy")
    public ResponseEntity<Void> occupy(@PathVariable String spaceId,
                                       @RequestBody OccupySpaceRequest request) {
        commandGateway.sendAndWait(new OccupySpaceCommand(spaceId, request.cameraId(), request.confidence()));
        return ResponseEntity.accepted().build();
    }
}
```

### Reglas del patrón Resource/Assembler

- Un Resource **nunca** expone entidades JPA (`@Entity`) ni Aggregates directamente.
- El Assembler es el único lugar donde ocurre el mapeo Domain/View → HTTP.
- Las requests entrantes se reciben como `record` o clase inmutable (no el Resource), y se transforman en Commands directamente en el Controller.
- Usar `CollectionModel<ParkingSpaceResource>` para respuestas de colección.

---

## 6. Bounded Contexts Definidos

### 6.1 IAM — Identidad y Acceso (`iam`)

**Responsabilidad:** Registro, login y gestión de roles.

**Endpoints clave:**
- `POST /api/v1/iam/users` — Registra usuario, emite `UserRegisteredEvent`
- `POST /api/v1/iam/auth/sign-in` — Valida credenciales, retorna JWT firmado
- `GET /api/v1/iam/users/{userId}` — Consulta perfil de usuario

**Seguridad:**
- Contraseñas almacenadas con `BCryptPasswordEncoder` (strength = 12).
- JWT firmado con RS256 (par de claves asimétrico).
- Tokens stateless — sin sesión en servidor.

**Aggregate principal:** `UserAggregate`
**Eventos:** `UserRegisteredEvent`, `UserRoleChangedEvent`, `UserDeactivatedEvent`
**Resource:** `UserResource`, `AuthenticationResource`
**Assembler:** `UserResourceAssembler`

---

### 6.2 Parking — Estacionamientos (`parking`)

**Responsabilidad:** Gestión de estacionamientos, zonas, espacios y cálculo de disponibilidad.

**Lógica de negocio clave:**
- Cálculo de porcentaje de ocupación por zona.
- Clasificación automática de zonas: `LIBRE` (<30%), `MODERADO` (30-70%), `OCUPADO` (>70%).
- Exposición de disponibilidad actual vía Query side.

**Aggregates:** `ParkingLotAggregate`, `ZoneAggregate`, `ParkingSpaceAggregate`
**Eventos:** `ParkingLotCreatedEvent`, `SpaceOccupiedEvent`, `SpaceFreedEvent`, `ZoneStatusChangedEvent`
**Kafka:** publica al topic `availability.updated` cuando cambia la clasificación de una zona.
**Resources:** `ParkingLotResource`, `ZoneResource`, `ParkingSpaceResource`

---

### 6.3 Prediction — Predicciones IA (`prediction`)

**Responsabilidad:** Obtener predicciones de ocupación futura integrando el módulo Python.

**Integración:**
- Llama al módulo Python (Random Forest) vía `RestClient` — cliente: `PredictionEngineClient` en `infrastructure/external/`.
- Almacena historial de predicciones y metadatos del modelo (versión, accuracy).

**Aggregate principal:** `PredictionAggregate`
**Eventos:** `PredictionRequestedEvent`, `PredictionGeneratedEvent`
**Resource:** `PredictionResource`

---

### 6.4 Vision — Visión Computacional (`vision`)

**Responsabilidad:** Coordinación con el módulo OpenCV/Python.

**Integración:**
- Recibe frames procesados vía HTTP o Kafka topic `cv.occupancy.detected`.
- Transforma los resultados en `OccupySpaceCommand` / `FreeSpaceCommand` hacia el contexto `parking`.

**Aggregate principal:** `CameraAggregate`
**Eventos:** `FrameProcessedEvent`, `OccupancyDetectedEvent`
**Resource:** `CameraResource`

---

### 6.5 Notifications — Notificaciones (`notifications`)

**Responsabilidad:** Envío de notificaciones push vía Firebase Cloud Messaging.

**Integración:**
- Consume Kafka topics: `availability.critical`, `prediction.alert`.
- Llama al Firebase Admin SDK.
- Registra historial de notificaciones.

**Aggregate principal:** `NotificationAggregate`
**Eventos:** `NotificationSentEvent`, `NotificationFailedEvent`
**Resource:** `NotificationResource`

---

## 7. Modelo de Datos (6 Módulos — 12 Tablas)

Las tablas pertenecen a un solo datasource PostgreSQL. Los bounded contexts no hacen JOINs entre tablas de otros contextos — los datos necesarios se replican mediante eventos.

### Módulo 1: Identidad y Acceso (iam)

| Tabla | Columnas clave |
|---|---|
| `users` | `id` (UUID), `email`, `password_hash`, `role` (ADMIN/OPERATOR/USER), `active`, `created_at` |
| `user_tokens` | `id`, `user_id` (FK), `token_hash`, `expires_at`, `revoked` |

### Módulo 2: Estacionamientos (parking)

| Tabla | Columnas clave |
|---|---|
| `parking_lots` | `id` (UUID), `name`, `address`, `total_capacity`, `status` |
| `zones` | `id` (UUID), `parking_lot_id` (FK), `name`, `capacity`, `occupied_count`, `classification` |
| `parking_spaces` | `id` (UUID), `zone_id` (FK), `space_number`, `is_occupied`, `last_updated` |

### Módulo 3: Ocupación (vision)

| Tabla | Columnas clave |
|---|---|
| `cameras` | `id` (UUID), `zone_id` (FK), `ip_address`, `status` (ACTIVE/INACTIVE/ERROR) |
| `occupancy_records` | `id` (UUID), `space_id` (FK), `camera_id` (FK), `occupied`, `detected_at`, `confidence` |

### Módulo 4: Predicciones (prediction)

| Tabla | Columnas clave |
|---|---|
| `prediction_models` | `id` (UUID), `version`, `algorithm` (RANDOM_FOREST), `accuracy`, `deployed_at` |
| `predictions` | `id` (UUID), `zone_id`, `model_id` (FK), `predicted_occupancy`, `target_datetime`, `confidence_score`, `created_at` |

### Módulo 5: Notificaciones (notifications)

| Tabla | Columnas clave |
|---|---|
| `notification_templates` | `id` (UUID), `type` (AVAILABILITY/ALERT), `title_template`, `body_template` |
| `notification_history` | `id` (UUID), `user_id`, `template_id` (FK), `payload`, `status` (SENT/FAILED), `sent_at` |

### Módulo 6: Administración (shared)

| Tabla | Columnas clave |
|---|---|
| `audit_log` | `id` (UUID), `entity_type`, `entity_id`, `action`, `performed_by`, `timestamp`, `details` (JSONB) |

---

## 8. Patrones Axon Framework

### Command → Aggregate → Event → Projection

```java
// domain/commands/ — inmutable, record
public record OccupySpaceCommand(
    @TargetAggregateIdentifier String spaceId,
    String cameraId,
    double confidence
) {}

// domain/model/aggregates/ — sin @Autowired, sin dependencias de infra
@Aggregate
public class ParkingSpaceAggregate {

    @AggregateIdentifier
    private String spaceId;
    private boolean occupied;

    @CommandHandler
    public ParkingSpaceAggregate(CreateSpaceCommand cmd) {
        apply(new SpaceCreatedEvent(cmd.spaceId(), cmd.zoneId()));
    }

    @CommandHandler
    public void handle(OccupySpaceCommand cmd) {
        if (!this.occupied) {
            apply(new SpaceOccupiedEvent(cmd.spaceId(), cmd.cameraId(), cmd.confidence()));
        }
    }

    @EventSourcingHandler
    public void on(SpaceCreatedEvent e) { this.spaceId = e.spaceId(); }

    @EventSourcingHandler
    public void on(SpaceOccupiedEvent e) { this.occupied = true; }
}

// domain/events/ — inmutable, record
public record SpaceOccupiedEvent(String spaceId, String cameraId, double confidence) {}

// application/queryservices/ — actualiza read model y responde queries
@Component
@ProcessingGroup("parking-projection")
public class ParkingSpaceProjection {

    private final ParkingSpaceViewRepository repository;

    @EventHandler
    public void on(SpaceOccupiedEvent e) {
        repository.findById(e.spaceId()).ifPresent(view -> {
            view.setOccupied(true);
            repository.save(view);
        });
    }

    @QueryHandler
    public ParkingSpaceView handle(GetSpaceStatusQuery query) {
        return repository.findById(query.spaceId())
            .orElseThrow(() -> new SpaceNotFoundException(query.spaceId()));
    }
}
```

### Reglas Axon estrictas

- Commands, Events y Queries son **inmutables** — usar Java `record`.
- Los Aggregates **nunca** tienen `@Autowired` ni dependencias de infraestructura.
- El estado del Aggregate se reconstruye **exclusivamente** mediante `@EventSourcingHandler`.
- Las proyecciones viven en `application/queryservices/` y persisten en PostgreSQL (read model).
- Usar `@ProcessingGroup` para asignar Event Handlers a un tracking processor nombrado.
- El Controller **nunca** instancia Aggregates directamente — siempre usa `CommandGateway` o `QueryGateway`.

---

## 9. Seguridad

### JWT (RS256)

- Clave privada para firmar (fuera del repo, cargada vía env var).
- Clave pública para verificar en el filtro de Spring Security.
- Claims mínimos: `sub` (userId), `role`, `iat`, `exp`.
- Expiración: 24h access token, 7d refresh token.
- Tokens stateless — sin `HttpSession`.

### BCrypt

```java
// Strength mínimo 12 — nunca bajar este valor
PasswordEncoder encoder = new BCryptPasswordEncoder(12);
```

### CORS

- Configurar CORS globalmente en `shared/infrastructure/config/SecurityConfig.java`.
- Los endpoints internos entre bounded contexts no necesitan CORS (mismo proceso).

---

## 10. Integración con Módulos Externos

### 10.1 Módulo IA (Python / Random Forest)

- **Cliente:** `PredictionEngineClient` en `prediction/infrastructure/external/`
- **Contrato:**
  ```json
  // POST /predict  →  Request
  { "zone_id": "uuid", "features": { "hour": 14, "day_of_week": 2 } }
  // Response
  { "predicted_occupancy": 0.73, "confidence_score": 0.91, "model_version": "1.3.0" }
  ```

### 10.2 Módulo CV (OpenCV / Python)

- **Kafka topic:** `cv.occupancy.detected`
- **Contrato:**
  ```json
  {
    "camera_id": "uuid",
    "space_detections": [{ "space_id": "uuid", "occupied": true, "confidence": 0.95 }],
    "frame_timestamp": "2025-06-03T14:00:00Z"
  }
  ```
- El consumer de `vision/infrastructure/messaging/` transforma el payload en `OccupySpaceCommand`.

### 10.3 Firebase Cloud Messaging

- **SDK:** `firebase-admin` (dependencia Maven) en `notifications/infrastructure/external/`
- Las credenciales se cargan vía `FIREBASE_CREDENTIALS_PATH`. Nunca commitear el JSON al repo.

---

## 11. Convenciones de Código

### Nomenclatura

| Artefacto | Patrón | Ejemplo |
|---|---|---|
| Command | VerbSustantivo + `Command` | `OccupySpaceCommand` |
| Event | SustantivoParticipio + `Event` | `SpaceOccupiedEvent` |
| Query | `Get/Find` + Sustantivo + `Query` | `GetZoneStatusQuery` |
| Aggregate | Entidad + `Aggregate` | `ParkingSpaceAggregate` |
| Projection | Entidad + `Projection` | `ParkingSpaceProjection` |
| Resource | Entidad + `Resource` | `ParkingSpaceResource` |
| Assembler | Entidad + `ResourceAssembler` | `ParkingSpaceResourceAssembler` |
| Controller | Entidad + `Controller` | `ParkingSpaceController` |
| Repository (read) | Entidad + `ViewRepository` | `ParkingSpaceViewRepository` |
| Repository (write) | gestionado por Axon Event Store | — |

### Identificadores

- Todas las entidades de dominio usan **UUID** como ID.
- Generar en el lado del cliente/command con `UUID.randomUUID().toString()`.

### Requests entrantes

- Los cuerpos de request son **records** inmutables o clases con `@NotNull`/`@Valid`.
- Se reciben en el Controller y se transforman directamente en Commands. No hay un "DTO de request" separado — el record de request ES el input del Controller.

### Manejo de errores

- Excepciones de dominio en `<context>/domain/exceptions/`.
- `@RestControllerAdvice` en `shared/interfaces/GlobalExceptionHandler` mapea excepciones a respuestas HTTP.
- Formato de error estándar:
  ```json
  {
    "timestamp": "2025-06-03T14:00:00Z",
    "status": 404,
    "error": "SPACE_NOT_FOUND",
    "message": "Parking space abc-123 not found",
    "path": "/api/v1/parking/spaces/abc-123"
  }
  ```

---

## 12. Variables de Entorno Requeridas

```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/parkvision_db
SPRING_DATASOURCE_USERNAME=parkvision
SPRING_DATASOURCE_PASSWORD=<secret>

# JWT
JWT_PRIVATE_KEY_PATH=/secrets/private.pem
JWT_PUBLIC_KEY_PATH=/secrets/public.pem
JWT_EXPIRATION_MS=86400000

# Axon Server
AXON_AXONSERVER_SERVERS=localhost:8124

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Firebase
FIREBASE_CREDENTIALS_PATH=/secrets/firebase-service-account.json

# Módulos externos
PREDICTION_ENGINE_URL=http://prediction-engine:8000
CV_ENGINE_URL=http://cv-engine:8001
```

---

## 13. Cómo Implementar un Nuevo Endpoint

Seguir siempre este orden:

1. **Definir el Command o Query** en `<context>/domain/commands/` o `domain/queries/`.
2. **Definir el Event** en `<context>/domain/events/` (solo si el Command muta estado).
3. **Implementar/actualizar el Aggregate** en `domain/model/aggregates/` — añadir `@CommandHandler` y `@EventSourcingHandler`.
4. **Implementar la Projection** en `application/queryservices/` — `@EventHandler` actualiza el read model en PostgreSQL, `@QueryHandler` lo consulta y retorna la vista.
5. **Crear/actualizar la entidad JPA** (read model) y el `@Repository` en `infrastructure/persistence/`.
6. **Crear el Resource** en `interfaces/resource/`.
7. **Crear o actualizar el Assembler** en `interfaces/assembler/` — mapea la vista al Resource y añade links.
8. **Exponer el endpoint** en `interfaces/rest/` — inyecta `CommandGateway`, `QueryGateway` y el Assembler.
9. **Escribir tests**: unit test del Aggregate con `AggregateTestFixture`, test del Assembler, integration test con `@SpringBootTest`.

---

## 14. Verificación

```bash
# Compilar
./mvnw clean compile

# Tests unitarios
./mvnw test

# Levantar localmente
./mvnw spring-boot:run
```

### Checklist antes de marcar un endpoint como completo

- [ ] Command/Query/Event definidos como `record` inmutables
- [ ] Aggregate sin dependencias de infraestructura ni `@Autowired`
- [ ] Read model actualizado en `@EventHandler`
- [ ] Resource no expone `@Entity` ni Aggregate directamente
- [ ] Assembler mapea vista → Resource y añade links HATEOAS
- [ ] Test del Aggregate con `AggregateTestFixture`
- [ ] Endpoint documentado con `@Operation` (OpenAPI/Springdoc)
- [ ] Sin credenciales hardcodeadas
- [ ] La comunicación cross-context pasa por `application/internal/` — nunca por `domain` de otro contexto
