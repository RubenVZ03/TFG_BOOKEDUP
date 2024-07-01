# BookedUp - Aplicación de Reserva de Citas

## 1. Resumen Ejecutivo

### 1.1 Descripción del Proyecto
Para llevar a cabo el desarrollo de este proyecto hemos tenido que combinar nuestras habilidades y conocimientos adquiridos durante el curso para cumplir las expectativas del mismo.

**BookedUp** es una aplicación móvil desarrollada en Android Studio que permite a los usuarios reservar y gestionar citas de una manera cómoda y sencilla para diversos servicios como barberías, tatuajes, piercings, etc.

La aplicación cuenta con sistema de registro e inicio de sesión para que el usuario disponga de una cuenta en la que queden almacenados sus datos, como por ejemplo: nombre de usuario, foto del perfil, correo electrónico y citas reservadas.

Los proveedores pueden gestionar los distintos tipos de servicios que ofrecen y la disponibilidad de ellos a través de la aplicación.

### 1.2 Características Principales de la Aplicación
**BookedUp** cuenta con diversas funcionalidades, entre ellas están:
- **Búsqueda de proveedores:** Los usuarios pueden buscar proveedores de servicios específicos en su área, filtrando por el tipo de servicio a realizar y su disponibilidad.
- **Búsqueda de servicios:** La aplicación cuenta con diversos apartados donde muestra diversos servicios disponibles ayudando a encontrar lo que buscas.
- **Servicios personalizados:** Cada proveedor puede personalizar el perfil de su servicio con información relevante, como los servicios ofrecidos, precios y horarios de atención.
- **Reserva de citas:** Los usuarios pueden seleccionar fechas y horas disponibles para reservar sus citas directamente desde la aplicación.
- **Perfil personalizado:** Cada usuario puede personalizar su perfil con información relevante, como foto de perfil, correo electrónico, teléfono, etc.

## 2. Planificación

### 2.1 Metodología Implementada
Para el desarrollo de la aplicación implementamos la metodología ágil SCRUM, muy útil para asegurar que todas las etapas del desarrollo se ejecuten de la manera más organizada y eficiente posible.

**¿Por qué esta metodología?**
Scrum se centra en iteraciones cortas y frecuentes, denominadas sprints, que permiten una adaptación continua y rápida respuesta a los cambios.

El proyecto se dividió en varios sprints, cada uno con una duración variable estimando cuántas semanas nos llevaría implementar cada aspecto del mismo.

### 2.2 Cronograma
En el cronograma del proyecto se detallan las actividades y los tiempos estimados para cada fase, proporcionando la hoja de ruta que se ha seguido para el desarrollo de BookedUp.

| Actividades                                      | Tiempo Estimado |
|--------------------------------------------------|------------------|
| Análisis de requisitos y viabilidad del proyecto | 1 semana         |
| Diseño de bocetos e interfaz en Figma            | 4 semanas        |
| Desarrollo completo de la aplicación             | 8 semanas        |
| Pruebas unitarias y corrección de bugs           | 1 semana         |
| Documentación de manual y presentación           | 2 semanas        |

## 3. Justificación

### 3.1 Necesidad del Proyecto
La gestión de citas en servicios personales, como barberías, estudios de tatuajes, centros de piercings, etc., enfrenta varios desafíos que afectan tanto a los usuarios como a los proveedores:

**Ineficiencia en la reserva de citas**
- **Cómo afecta a los Usuarios:** Los clientes a menudo tienen dificultades para encontrar disponibilidad y realizar reservas de manera rápida. Llamadas telefónicas y visitas presenciales pueden resultar en tiempos de espera largos.
- **Cómo afecta a los Proveedores:** Los proveedores deben dedicar tiempo y recursos significativos a la gestión manual de citas, lo cual puede resultar en citas perdidas y una mala organización del tiempo.

**Falta de recordatorios y comunicación**
- **Cómo afecta a ambos:** Sin recordatorios, los clientes pueden olvidar sus citas, resultando en pérdidas de tiempo y recursos para los proveedores.

### 3.2 Beneficios Esperados
Gracias a la solución digital moderna y eficiente que BookedUp supone, se esperan los siguientes beneficios.

**Beneficios para los Usuarios**
- **Facilidad para encontrar y reservar citas:** La aplicación permite a los usuarios buscar proveedores de servicios específicos en su área, consultar la disponibilidad en tiempo real y reservar citas en pocos pasos.

**Beneficios para los Proveedores**
- **Optimización del tiempo y recursos:** Al reducir el tiempo dedicado a la gestión manual de citas, los proveedores pueden enfocarse más en sus servicios y mejorar la calidad de atención al cliente.

**Beneficios para Ambos**
- **Reducción de citas perdidas:** Al tener la aplicación en algo cotidiano como es el dispositivo móvil es mucho más complicado que las citas pasen por alto.

## 4. Diseño del Sistema

### 4.1 Arquitectura del Sistema
Durante el desarrollo de la aplicación se han seleccionado tecnologías y patrones de diseño que aseguran un sistema robusto y escalable.

El sistema utiliza una arquitectura **MVVM (Model-View-ViewModel)**, ideal para aplicaciones móviles.
- **Modelo:** Contiene la lógica y los datos de la aplicación. Es independiente de la interfaz de usuario y maneja la persistencia de datos.
- **Vista:** Representa la interfaz de usuario. En Android Studio, diseñada utilizando XML. La vista se actualiza automáticamente reflejando los cambios detectados en la “VistaModelo”.
- **VistaModelo:** Actúa como un puente entre el “Modelo” y la “Vista”. Gestiona la lógica de interfaz y proporciona datos a la vista en un formato adecuado.

### 4.2 Diseño de la Base de Datos
Se utilizó Firebase para el almacenamiento de datos en tiempo real y herramientas robustas para la autenticación de la aplicación.

Como gestor se utilizó **Firestore Database** que proporciona una base de datos en tiempo real conectada a las funciones principales de Firebase, lo que facilita la sincronización de datos entre los usuarios y los dispositivos.

**Estructura de la Base de Datos**
Firestore funciona mediante el uso de “colecciones” (tablas) y documentos (columnas).

**Colecciones (tablas):**
- **Actividades:** Almacena la información de acciones que ofrece cada servicio.
  - **Ejemplo:** Servicio de Tattoo → Ofrece: Eliminar un tatuaje.
- **Citas:** Almacena toda la información necesaria para llevar a cabo un registro de una cita.
  - **Ejemplo:** Cita reservada → Almacena: estado, día, hora, mes, profesional, etc.
- **Servicios:** Almacena toda la información necesaria para llevar a cabo un registro de un servicio. Esta colección utiliza una referencia a la colección “Actividades” para cargar las acciones que se realizarán en cada servicio.
  - **Ejemplo:** Registro de un servicio → Almacena: nombre, descripción, [actividades], ciudad, horario, valoración, etc.
- **Usuarios:** Almacena toda la información necesaria para llevar a cabo un registro de un usuario. Esta colección guarda los datos del usuario si ha sido registrado con Google o si ha sido registrado introduciendo los datos de forma manual en la aplicación.
  - **Ejemplo:** Usuario registrado → Almacena: usuario, email, contraseña, teléfono.

Se utilizó el servicio de **Storage** para almacenar archivos como imágenes que posteriormente serán referenciadas en la aplicación (imágenes de portada para servicios y fotos de perfil).

## 5. Tecnologías Utilizadas

### 5.1 Lenguajes
- **Java:** Utilizado para el desarrollo de aplicaciones Android, robusto, orientado a objetos y cuenta con una extensa comunidad que facilita el desarrollo gracias a la documentación y al soporte.
- **XML:** Fundamental para construir la interfaz de usuario en las aplicaciones de Android.

### 5.2 Entorno de Desarrollo
- **Android Studio (IDE):** Entorno de desarrollo integrado (IDE) para el desarrollo de aplicaciones Android que proporciona herramientas completas para el diseño, desarrollo, depuración y pruebas de aplicaciones. Incluye un editor de código inteligente, emuladores y soporte para gradle que gestiona dependencias.

### 5.3 Frameworks y Librerías
- **Firebase Authentication:** Servicio fácil de usar que proporciona métodos de autenticación seguros y rápidos, incluyendo email/password, Google Sign-In, etc.
- **Firestore Realtime Database:** Una base de datos que permite sincronizar datos entre todos los clientes en tiempo real.
- **Firebase Storage:** Servicio que permite el almacenamiento de archivos pudiendo ser referenciados a una base de datos.

### 5.4 Control de Versiones
- **GitHub:** Plataforma de alojamiento mediante repositorios para proyectos de Git que proporciona herramientas de colaboración, revisión de código y gestión de proyectos.

### 5.5 Diseño y Prototipado
- **Figma:** Herramienta de diseño de interfaz de usuario y prototipado colaborativo que permite crear y compartir diseños interactivos.

### 5.6 Gestión del Proyecto
- **Trello:** Aplicación de gestión de proyectos basada en tableros que permite organizar tareas y colaborar en tiempo real.

## 6. Conclusiones

### 6.1 Importancia de la Planificación
La planificación y el uso de la metodología ágil Scrum han sido fundamentales para llevar a cabo el proyecto. La división del trabajo en sprints y la realización de reuniones regulares han permitido una gestión eficiente del tiempo.

### 6.2 Capacidad Resolutiva
Uno de los aspectos más importantes cuando se trata de desarrollar una aplicación es disponer de la capacidad para resolver errores y adaptarse a la implementación de nuevas soluciones.

### 6.3 Escalabilidad y Posibles Mejoras
La aplicación está preparada para continuar con su desarrollo e implementar nuevas funcionalidades adicionales como pagos en línea, integración con redes sociales y análisis de datos que pueden aumentar el valor de la aplicación.

## 7. Bibliografía

### 7.1 Artículos y Sitios Web
- **[Android Studio developer](https://developer.android.com/studio)**: Guía oficial sobre implementaciones para aplicaciones Android.
  - **Descripción:** Esta guía oficial proporciona información detallada sobre cómo configurar y utilizar Android Studio para desarrollar aplicaciones Android. Incluye tutoriales, documentación sobre el entorno de desarrollo integrado (IDE), y mejores prácticas para el desarrollo de aplicaciones móviles.

- **[Documentación Firebase](https://firebase.google.com/docs)**: La documentación oficial de Firebase para la base de datos en tiempo real (Firestore).
  - **Descripción:** Firebase proporciona una base de datos en tiempo real que permite sincronizar datos entre todos los clientes en tiempo real. La documentación oficial detalla cómo configurar y utilizar Firebase, abarcando aspectos como la autenticación, la base de datos en tiempo real, Firestore, y otros servicios de Firebase que se pueden integrar en aplicaciones Android.

- **[Documentación Figma](https://www.figma.com/resources/learn-design/)**: Herramienta de diseño y prototipado de la aplicación.
  - **Descripción:** Figma es una herramienta de diseño colaborativo que permite a los diseñadores crear y compartir prototipos interactivos. La documentación y recursos en línea ofrecen guías sobre cómo utilizar Figma para diseñar interfaces de usuario y crear prototipos de alta fidelidad.

- **[Documentación Trello](https://trello.com/guide)**: Aplicación de gestión de proyectos.
  - **Descripción:** Trello es una herramienta de gestión de proyectos basada en tableros que permite organizar tareas y colaborar en tiempo real.
