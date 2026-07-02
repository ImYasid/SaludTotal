# 🏥 SaludTotal

Aplicación móvil Android para la reserva de citas médicas, diseñada con un enfoque de **accesibilidad para todas las edades**, incluyendo adultos mayores.

Proyecto desarrollado para la materia de **Aplicaciones Móviles** — **Grupo 7**.

---

## 👥 Integrantes del Grupo 7

| Nombres |
|---|
| _Bravo Leandro_ |
| _Enriquez Michael_ |
| _Hernandez Mark_ |
| _Jimenez Yasid_ | 

---

## 📱 Descripción del Proyecto

**SaludTotal** es una aplicación móvil que permite a los usuarios registrarse, iniciar sesión y agendar citas médicas seleccionando una especialidad, pensada para que **cualquier persona, sin importar su edad o experiencia con la tecnología, pueda usarla sin dificultad**.

El diseño prioriza:
- Textos y botones grandes, fáciles de leer y tocar.
- Mensajes de error claros que no desaparecen solos.
- Navegación simple, sin menús ocultos ni íconos ambiguos.
- Acceso directo a ayuda telefónica desde la app.

---

## ✨ Funcionalidades

- **Inicio de Sesión**: autenticación con número de cédula y contraseña, con validación de campos y mensajes de error persistentes.
- **Registro de Usuario**: creación de cuenta con validación de cédula (10 dígitos), correo electrónico, contraseña segura y selección de fecha de nacimiento mediante calendario (con cálculo automático de edad).
- **Modo Fácil**: se activa automáticamente para usuarios mayores de 60 años.
- **Bienvenida**: pantalla principal con acceso directo a agendar cita y a la línea de ayuda telefónica.
- **Especialidades**: selección de especialidad médica (Médico General, Cardiología, Traumatología, Oftalmología, Neurología, Vacunación) con buscador en tiempo real.
- **Navegación persistente**: barra inferior con Inicio, Agendar y Cerrar sesión, visible en todas las pantallas.

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **IDE:** Android Studio
- **UI:** XML + Material Components for Android
- **Almacenamiento local:** SharedPreferences
- **Arquitectura:** Activities (sin librerías externas de arquitectura)

---

## 📂 Estructura del Proyecto

```
app/src/main/java/com/example/myapplication/
├── LoginActivity.kt         # Inicio de sesión
├── RegisterActivity.kt      # Registro de nuevos usuarios
├── WelcomeActivity.kt       # Pantalla de bienvenida
└── SpecialtiesActivity.kt   # Selección de especialidad médica

app/src/main/res/layout/
├── activity_login.xml
├── activity_register.xml
├── activity_welcome.xml
└── activity_specialties.xml
```

---

## 🚀 Instalación y Ejecución

1. Clona este repositorio:
   ```bash
   git clone https://github.com/ImYasid/SaludTotal.git
   ```
2. Abre el proyecto en **Android Studio**.
3. Espera a que Gradle sincronice las dependencias.
4. Conecta un dispositivo físico o inicia un emulador (API 24 o superior recomendado).
5. Presiona **Run ▶** para compilar e instalar la app.

**Credenciales de prueba:**
| Cédula | Contraseña |
|---|---|
| `1754378097` | `Yasid123@` |

> ⚠️ Estas credenciales están únicamente para fines de prueba y demostración académica.

---

## 🎓 Información Académica

- **Materia:** Aplicaciones Móviles
- **Universidad:** _Escuela Politecnica Nacional_
- **Docente:** _Saa Pablo_
- **Periodo/Semestre:** _2026-A 7mo Semestre_

---

## 📄 Licencia

Este proyecto fue desarrollado con fines académicos para la materia de Aplicaciones Móviles.
