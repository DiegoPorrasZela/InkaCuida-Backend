# InkaCuida: contrato de API (frontend ↔ Spring Boot)

El frontend ya funciona con un backend simulado (`src/app/core/mock-backend.ts`).
Cuando el backend esté listo, cambia `USE_MOCK = false` en `src/app/core/config.ts`.
Si respeta este contrato, el frontend funciona sin tocar nada más.

- **Base URL:** `http://localhost:8080/api`
- **CORS:** permitir `http://localhost:4200` (métodos GET, POST, PUT, PATCH, DELETE y la cabecera `Authorization`)
- **Auth:** JWT en la cabecera `Authorization: Bearer <token>` en todas las rutas salvo `/auth/**`
- **Errores:** cuerpo `{ "mensaje": "texto legible" }` con código 400/401/404/409
- **Fechas:** `"yyyy-MM-dd"` (LocalDate). **Horas:** `"HH:mm"` (String o LocalTime con `@JsonFormat(pattern="HH:mm")`). Fecha y hora: ISO‑8601
- **Usuario actual:** sale siempre del token, nunca del body ni de la URL

## Auth

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| POST | `/auth/login` | `{ email, password }` | `200 { token, usuario }` · `401` si las credenciales son inválidas |
| POST | `/auth/registro` | `{ nombre, email, password, consentimientoAceptado }` | `201 { token, usuario }` · `400` si no hay consentimiento · `409` si el email ya existe |

`usuario` = `{ id, nombre, email, fechaNacimiento?, ciudad?, consentimientoAceptado }` (nunca devolver la contraseña; guardarla con BCrypt).

## Medicamentos (carnet digital, HU01)

```json
{
  "id": 1,
  "nombre": "Metformina",
  "dosis": "850 mg",
  "frecuencia": "UNA_VEZ_DIA",
  "horarios": ["13:00"],
  "fechaInicio": "2026-11-25",
  "fechaFin": null,
  "indicaciones": "Tomar con el almuerzo",
  "unidadesDisponibles": 30,
  "activo": true
}
```
`frecuencia` ∈ `UNA_VEZ_DIA | DOS_VECES_DIA | TRES_VECES_DIA | CADA_8_HORAS | CADA_12_HORAS`

| Método | Ruta | Notas |
|---|---|---|
| GET | `/medicamentos` | Solo los `activo = true` del usuario |
| GET | `/medicamentos/{id}` | 404 si no existe o es de otro usuario |
| POST | `/medicamentos` | Body sin `id` ni `activo`. Validar: nombre, dosis, frecuencia, ≥1 horario, fechaInicio, y que fechaFin ≥ fechaInicio. **Guardar los valores tal cual, sin modificar la dosis.** Devuelve 201 |
| PUT | `/medicamentos/{id}` | Mismo body y mismas validaciones |
| DELETE | `/medicamentos/{id}` | Borrado lógico (`activo=false`) y borra solo las tomas PENDIENTE desde hoy. El historial se conserva. 204 |

## Tomas (recordatorios y adherencia, HU02, HU05 y HU06)

```json
{
  "id": 101,
  "medicamentoId": 2,
  "medicamentoNombre": "Metformina",
  "dosis": "850 mg",
  "indicaciones": "Tomar con el almuerzo",
  "frecuencia": "UNA_VEZ_DIA",
  "fecha": "2026-11-25",
  "hora": "13:00",
  "estado": "PENDIENTE",
  "respondidaEn": null,
  "pospuestaHasta": null
}
```
`estado` ∈ `PENDIENTE | TOMADA | OMITIDA`. **Una toma sin respuesta se queda en PENDIENTE: nunca pasa sola a OMITIDA.**

| Método | Ruta | Notas |
|---|---|---|
| GET | `/tomas/hoy` | **Genera las tomas de hoy si aún no existen**: una por cada (medicamento activo vigente hoy × horario) y luego las devuelve |
| GET | `/tomas?desde=yyyy-MM-dd&hasta=yyyy-MM-dd` | Historial en ese rango, incluidos ambos extremos |
| GET | `/tomas/{id}` | |
| PATCH | `/tomas/{id}/confirmar` | estado=TOMADA, respondidaEn=now, pospuestaHasta=null, y `unidadesDisponibles--` del medicamento si es > 0. 409 si no estaba PENDIENTE |
| PATCH | `/tomas/{id}/omitir` | estado=OMITIDA, respondidaEn=now. 409 si no estaba PENDIENTE |
| PATCH | `/tomas/{id}/posponer` | pospuestaHasta = now + 30 min y estado sigue PENDIENTE |

Las tres PATCH devuelven la toma actualizada.

## Cuidadores (seguimiento familiar)

`{ id, nombre, email, parentesco, recibeAlertas }`

| Método | Ruta | Notas |
|---|---|---|
| GET | `/cuidadores` | Los del usuario |
| POST | `/cuidadores` | Body sin `id`. 201 |
| DELETE | `/cuidadores/{id}` | 204 |

## Reglas de negocio (como constantes con nombre)
- `UMBRAL_ALERTA_CUIDADOR_MIN = 30`: el front muestra la alerta cuando una toma PENDIENTE pasa 30 min sin respuesta. Opcional en el back: un `@Scheduled` que registre el aviso a los cuidadores con `recibeAlertas=true`.
- `UMBRAL_REPOSICION_DIAS = 5`: días restantes = `unidadesDisponibles / horarios.size()`. El front lo calcula y no hace ninguna compra automática.

## Modelo de tablas sugerido (JPA)
- `usuario(id, nombre, email UNIQUE, password_hash, fecha_nacimiento, ciudad, consentimiento_aceptado, creado_en)`
- `medicamento(id, usuario_id FK, nombre, dosis, frecuencia, fecha_inicio, fecha_fin, indicaciones, unidades_disponibles, activo)`
- `medicamento_horario(medicamento_id FK, hora)`, que es `@ElementCollection List<String> horarios`
- `toma(id, usuario_id FK, medicamento_id FK, fecha, hora, estado, respondida_en, pospuesta_hasta)` con UNIQUE(medicamento_id, fecha, hora)
- `cuidador(id, usuario_id FK, nombre, email, parentesco, recibe_alertas)`

Usuario de demostración que conviene sembrar: `maria@inkacuida.pe` / `123456`, María González, con Losartán 50 mg (08:00), Metformina 850 mg (13:00) y Atorvastatina 20 mg (21:00).
