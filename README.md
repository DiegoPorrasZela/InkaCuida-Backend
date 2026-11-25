# InkaCuida API (Spring Boot 4 + PostgreSQL/Neon)

API REST del MVP de InkaCuida. El contrato de endpoints está en [API.md](API.md). Frontend: https://github.com/DiegoPorrasZela/InkaCuida-Frontend

## 1. Crear la base en Neon (5 min)
1. Entra a https://neon.tech, crea un proyecto (región **AWS São Paulo** es la más cercana a Lima).
2. En **Connect** copia los datos de conexión. Neon te da algo así:
   `postgresql://neondb_owner:AbC123@ep-cool-name-123456-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require`
3. En esta carpeta copia `.env.example` como `.env` y llénalo **separando** usuario y contraseña de la URL:
   ```
   DB_URL=jdbc:postgresql://ep-cool-name-123456-pooler.sa-east-1.aws.neon.tech/neondb?sslmode=require
   DB_USER=neondb_owner
   DB_PASSWORD=AbC123
   JWT_SECRET=una-clave-larga-de-al-menos-32-caracteres-xxxxxxxx
   ```
   (La URL lleva el prefijo `jdbc:` y **no** incluye `usuario:password@`.) `.env` está en `.gitignore`: no lo subas a GitHub.

No hace falta crear tablas: al arrancar, Hibernate las crea en Neon y siembra la paciente demo.
Si quieres verlas o crearlas a mano, `db/schema.sql` tiene el DDL (pégalo en el SQL Editor de Neon).

## 2. Ejecutar
```bash
# Windows (PowerShell o CMD)
mvnw.cmd spring-boot:run

# Sin Neon (base H2 en memoria, para pruebas rápidas)
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```
- API: http://localhost:8080/api
- Prueba: http://localhost:8080/api/salud → `{"estado":"ok", ...}`
- Usuario demo: `maria@inkacuida.pe` / `123456`

## 3. Estructura
```
pe.inkacuida.api
├── dominio/       Entidades JPA (Usuario, Medicamento, Toma, Cuidador) y ReglasNegocio (umbrales 30 min / 5 días)
├── repositorio/   Spring Data JPA
├── dto/           Records de entrada/salida (con validaciones)
├── servicio/      Lógica: Auth (BCrypt + JWT), Medicamentos, Tomas, Cuidadores
├── web/           Controladores REST + manejador de errores { "mensaje": ... }
└── config/        Seguridad (JWT HS256, CORS) y datos demo
```

## Reglas del informe implementadas
- La dosis, la frecuencia y el horario se guardan tal cual se ingresaron; no existe ninguna función de recomendación clínica.
- Una toma sin respuesta sigue **PENDIENTE**; solo pasa a OMITIDA si el paciente la reporta.
- El registro exige consentimiento informado (Ley N.° 29733) y las contraseñas se guardan con BCrypt.
- Cada usuario solo accede a sus propios datos: el id sale del JWT y todas las consultas filtran por `usuario_id`.
- Al eliminar un medicamento se hace un borrado lógico y el historial se conserva.

## Variables de entorno
| Variable | Por defecto | Uso |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | localhost | Conexión PostgreSQL/Neon |
| `JWT_SECRET` | clave de desarrollo | Firma de tokens (≥ 32 caracteres) |
| `CORS_ORIGINS` | `http://localhost:4200` | Orígenes del front separados por comas (agrega tu dominio si lo despliegas) |
| `SEED_DEMO` | `true` | Crea la paciente demo si no existe |
| `PORT` | `8080` | Puerto HTTP |
