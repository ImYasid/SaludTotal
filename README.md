# 🏥 SaludTotal

Aplicación móvil Android para la reserva de citas médicas, diseñada con un enfoque de **accesibilidad para todas las edades**, incluyendo adultos mayores.

Proyecto desarrollado para la materia de **Aplicaciones Móviles** — **Grupo 7**.

---

## 👥 Integrantes del Grupo

Bravo Leandro, Enriquez Michael, Hernandez Mark y Jimenez Yasid 

---

## 📱 Descripción del Proyecto

**SaludTotal** es una aplicación móvil que permite a los usuarios registrarse, iniciar sesión y gestionar citas médicas en su totalidad. Está pensada para que **cualquier persona, sin importar su edad o experiencia con la tecnología, pueda usarla sin dificultad**.

El diseño prioriza:
- Textos y botones grandes, fáciles de leer y tocar.
- Mensajes de error claros que no desaparecen solos.
- Navegación simple, sin menús ocultos ni íconos ambiguos.
- Acceso directo a ayuda telefónica desde la app.

---

## ✨ Funcionalidades

- **Autenticación Segura**: Inicio de sesión y registro con validación estricta de cédula ecuatoriana (10 dígitos), contraseñas coincidentes y cálculo automático de edad mediante calendario.
- **Flujo Completo de Agendamiento**:
  - **Especialidades**: Búsqueda en tiempo real del catálogo médico.
  - **Doctores**: Selección de médicos filtrados por especialidad, mostrando distancia e **integración nativa con Google Maps** para ver cómo llegar.
  - **Horarios Dinámicos**: Generación automática de próximos días hábiles (omitiendo domingos) y horarios de mañana/tarde.
  - **Confirmación**: Resumen detallado antes de interactuar con la base de datos.
- **Integración Externa**: Tras confirmar una cita, permite **agregarla al Calendario de Google** o compartir los detalles vía **WhatsApp**.
- **Gestión de Citas (Mis Citas)**: Historial visual donde el usuario puede revisar sus citas agendadas y **cancelarlas** de ser necesario.

---

## 🛠️ Tecnologías y Arquitectura

El proyecto escaló de un almacenamiento básico a una arquitectura robusta orientada a buenas prácticas:

- **Lenguaje:** Kotlin
- **IDE:** Android Studio
- **UI:** XML + Material Components for Android + RecyclerViews
- **Base de Datos Local:** **Room Database** (SQLite abstraction) con soporte para Relaciones (Foreign Keys) e Índices Únicos.
- **Asincronismo:** **Corrutinas** (`lifecycleScope`, `Dispatchers.IO`) para evitar el bloqueo del hilo principal (Main Thread).
- **Arquitectura:** **Patrón Repositorio (Repository Pattern)** para abstraer las fuentes de datos y DAOs (Data Access Objects), aislando la lógica de base de datos de las Activities.

---

## 📂 Estructura del Proyecto

```text
app/src/main/java/com/example/myapplication/
├── data/
│   ├── local/
│   │   ├── dao/                 # Interfaces de acceso a datos (Queries SQL)
│   │   │   ├── AppointmentDao.kt
│   │   │   ├── DoctorDao.kt
│   │   │   ├── SpecialtyDao.kt
│   │   │   └── UserDao.kt
│   │   ├── entity/              # Modelos de tablas de la base de datos
│   │   │   ├── AppointmentEntity.kt
│   │   │   ├── DoctorEntity.kt
│   │   │   ├── SpecialtyEntity.kt
│   │   │   └── UserEntity.kt
│   │   └── SaludTotalDatabase.kt # Configuración principal de Room y SeedCallback
│   └── repository/
│       └── SaludTotalRepository.kt # Punto único de acceso a datos para la UI
│
├── LoginActivity.kt             # Inicio de sesión
├── RegisterActivity.kt          # Creación de cuentas
├── WelcomeActivity.kt           # Dashboard principal
├── SpecialtiesActivity.kt       # Paso 1: Catálogo y búsqueda
├── DoctoresDisponibles.kt       # Paso 2: Selección de médico y Maps
├── Horario.kt                   # Paso 3: Selección dinámica de fecha/hora
├── RevisarCita.kt               # Paso 4: Confirmación y guardado en DB
├── CitaExitosa.kt               # Paso 5: Integración con Calendario/WhatsApp
├── MisCitasActivity.kt          # Visualización y cancelación de citas
└── CitasAdapter.kt              # Adaptador para el RecyclerView de citas
