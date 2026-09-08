# Ahevia — Sistema de Tests para Autoescuela

![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)
![UI](https://img.shields.io/badge/UI-Java%20Swing-4E9BB0)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-47A248?logo=mongodb&logoColor=white)
![Plataforma](https://img.shields.io/badge/Plataforma-Windows-0078D6?logo=windows&logoColor=white)
![Versión](https://img.shields.io/badge/versión-1.0-brightgreen)

**Ahevia** es un ecosistema completo de estudio para autoescuelas: una aplicación de escritorio en Java Swing donde los alumnos practican el examen teórico de conducir, conectada a MongoDB Atlas para guardar su progreso; un **Launcher** que la mantiene siempre actualizada sin intervención del usuario; y una **landing page** desde la que se descarga e instala todo el sistema. El banco de preguntas reproduce el **temario oficial de la DGT en formato Lectura Fácil para el permiso B**, organizado en **12 Temas**.

Este documento describe el sistema completo: la aplicación principal, el banco de preguntas, el Launcher/actualizador, la web de despliegue y el empaquetado.

## Tabla de contenidos

1. [Características principales](#características-principales)
2. [Arquitectura general](#arquitectura-general)
3. [Aplicación de escritorio](#aplicación-de-escritorio)
   - [3.1 Flujo de uso](#31-flujo-de-uso)
   - [3.2 Modos de test](#32-modos-de-test)
   - [3.3 Paquetes y responsabilidades](#33-paquetes-y-responsabilidades)
   - [3.4 Modelo de datos en MongoDB](#34-modelo-de-datos-en-mongodb)
   - [3.5 Motor del examen](#35-motor-del-examen)
   - [3.6 Panel de administración](#36-panel-de-administración)
   - [3.7 Interfaz gráfica y escalado](#37-interfaz-gráfica-y-escalado)
   - [3.8 Configuración de conexión](#38-configuración-de-conexión)
4. [Banco de preguntas — Temario oficial DGT](#banco-de-preguntas--temario-oficial-dgt)
   - [4.1 Los 12 Temas](#41-los-12-temas)
   - [4.2 Convención de identificadores](#42-convención-de-identificadores)
   - [4.3 Formato de pregunta (Lectura Fácil)](#43-formato-de-pregunta-lectura-fácil)
   - [4.4 Tests aleatorios](#44-tests-aleatorios)
   - [4.5 Test de errores](#45-test-de-errores)
5. [Launcher y actualizador automático](#launcher-y-actualizador-automático)
6. [Landing page](#landing-page)
7. [Empaquetado y distribución](#empaquetado-y-distribución)
8. [Tecnologías utilizadas](#tecnologías-utilizadas)
9. [Puesta en marcha (desarrollo)](#puesta-en-marcha-desarrollo)
10. [Estructura del repositorio](#estructura-del-repositorio)
11. [Licencia](#licencia)
12. [Autor](#autor)

---

## Características principales

- 🔐 **Login por DNI** con validación de formato español (8 dígitos + letra) y comprobación de licencia activa.
- 📚 **12 Temas oficiales de la DGT** (formato Lectura Fácil, permiso B) repartidos en 118 Tests.
- 📝 **3 modos de examen**: Estudio (corrección inmediata), Examen (cronometrado, 30 min, sin corrección visible) y Repaso (solo lectura del último intento).
- 🎲 **Tests aleatorios**: más de 200 combinaciones pregeneradas mezclando preguntas de todos los Temas.
- ❌ **Test de errores**: reagrupa dinámicamente todas las preguntas falladas por el alumno en cualquier Test, sin importar el Tema al que pertenezcan.
- 📊 **Seguimiento de progreso** por alumno y por Test, con coloreado automático del grid (sin hacer / bien / regular / mal).
- 🖼️ **Imágenes asociadas a preguntas**, cargadas bajo demanda desde GitHub.
- 👤 **Panel de administración** para crear alumnos y renovar licencias (+30 días).
- 🖥️ **Interfaz adaptativa**: reescala toda la UI según la resolución real de pantalla.
- 🔄 **Actualización automática silenciosa** vía el módulo Launcher, sin que el alumno tenga que reinstalar nada.
- 🌐 **Landing page** de descarga con contacto directo por WhatsApp, Instagram, TikTok y correo.

## Arquitectura general

El sistema se compone de tres módulos independientes que comparten la misma identidad visual pero no comparten código entre sí (cada uno es un JAR separado):

```
                     ┌───────────────────────────┐
                     │        Landing page        │
                     │   (index.html — descarga)   │
                     └──────────────┬──────────────┘
                                    │ descarga el instalador
                                    ▼
                     ┌───────────────────────────┐
                     │          Launcher           │
                     │  (paquete launcher, Main)   │
                     │  comprueba versión, JRE y   │
                     │  descarga la app si procede │
                     └──────────────┬──────────────┘
                                    │ arranca como proceso
                                    ▼
                     ┌───────────────────────────┐
                     │      Aplicación Ahevia       │
                     │   (paquete app, MainGUI)    │
                     │   Swing  ⇄  MongoDB Atlas    │
                     └───────────────────────────┘
```

La aplicación principal sigue una **separación estricta de responsabilidades**: la interfaz nunca toca Mongo directamente, la lógica de negocio no sabe nada de Swing, y el estilo visual no contiene ninguna regla de negocio. Ver [3.3](#33-paquetes-y-responsabilidades).

---

## Aplicación de escritorio

### 3.1 Flujo de uso

```
Login (DNI)
  ├── Es administrador ────────► Panel de administración (altas / renovar licencia)
  └── Es alumno ────────────────► Menú principal
                                     ├── Tests por temas ──► Selección de Tema ──► Grid de Tests
                                     ├── Tests aleatorios ─────────────────────► Grid de Tests
                                     └── Test de errores ──────────────────────► Motor de examen
                                                                                       │
                    Grid de Tests ──► Diálogo Estudio / Examen / Repaso ──► Motor de examen
                                                                                       │
                                                              ┌────────────────────────┴───┐
                                                              ▼                            ▼
                                                    Resultados (Estudio /            Fin de Examen
                                                     Test de errores)             (modo Examen) ──► Repaso
```

- El **login** valida el DNI (`^[0-9]{8}[A-Z]$`), comprueba conexión con la base de datos, existencia del usuario y vigencia de su licencia, mostrando el mensaje de error correspondiente en cada caso.
- El **Menú principal** aparece justo después del login del alumno y ofrece tres rutas: Temas, Aleatorios y Test de errores (este último se deshabilita automáticamente si el alumno no tiene ninguna pregunta pendiente de repaso).
- Al pulsar un Test en el grid se abre un diálogo modal para elegir el modo (Repaso solo está disponible si existe un intento previo en modo Examen para ese Test).
- La tecla **Escape** actúa como atajo global de "volver" en cualquier pantalla, detectando el panel visible en cada momento mediante un `KeyEventDispatcher`.

### 3.2 Modos de test

| Modo | Cronómetro | Corrección | ¿Se puede cambiar la respuesta? |
|---|---|---|---|
| **Estudio** | No | Inmediata, tras cada respuesta | Solo antes de responder |
| **Examen** | Sí, 30 minutos | Solo al finalizar el test | Sí, libremente hasta el final |
| **Repaso** | No | Del último intento guardado en modo Examen | No (solo lectura) |

Al terminar un test en **modo Examen** se muestra primero una pantalla intermedia de "Fin de Examen" (nota + veredicto APTO/NO APTO) con un botón **"Ver corrección"** que lleva al modo Repaso de ese mismo intento, sin volver a consultar MongoDB. En **modo Estudio** se va directo a la pantalla de Resultados con un botón para volver al grid de Tests.

El veredicto de aprobado replica la regla real del examen teórico de la DGT: se permite un **máximo de 3 fallos** sobre el total de preguntas del test.

### 3.3 Paquetes y responsabilidades

| Paquete | Clases | Responsabilidad |
|---|---|---|
| `app` | `MainGUI` | Cablea la interfaz, gestiona los listeners de eventos y la navegación entre pantallas. Contiene el punto de entrada (`main`) |
| `app.constantes` | `Constantes` | Todos los valores estáticos: dimensiones, colores, textos, duraciones, claves de configuración. Cero lógica |
| `app.estilo` | `Estilo` | Todo el aspecto visual: fuentes, layouts, pintado personalizado con `Graphics2D`. Cero lógica de negocio |
| `app.datos` | `ConexionBD`, `RepositorioBD` | Única capa que toca MongoDB: conexión, consultas, escrituras y caché en memoria |
| `app.logica` | `LogicaTest`, `EstadoTest`, `ModoTest` | Reglas de negocio: validación de DNI, corrección de tests, veredictos, clasificación de progreso |
| `app.modelo` | `Usuario`, `Tema`, `Test`, `Pregunta`, `ProgresoTest` | Objetos de dominio, mayoritariamente inmutables |

Puntos clave de diseño:

- **Carga perezosa (*lazy loading*)**: `Tema` solo guarda los IDs de sus Tests y `Test` solo los IDs de sus Preguntas; el contenido real se pide a `RepositorioBD` la primera vez que hace falta y se cachea en memoria (`cacheTestsPorTema`, `cachePreguntasPorTest`) para no repetir consultas.
- **Consultas en lote**: en vez de una consulta por documento, `RepositorioBD` indexa por `_id` usando un único filtro `$in` sobre el conjunto completo de IDs necesarios.
- **Actualizaciones granulares**: el progreso se guarda con rutas de campo específicas (`$set` sobre `Progreso.{idTest}.Fallos`, etc.), nunca reemplazando el subdocumento completo, para no perder datos guardados por otro flujo (p. ej. no borrar `RespuestasUltimoIntento` al corregir en modo Estudio).
- Toda clase de solo-estáticos (`Constantes`, `Estilo`, `LogicaTest`, `ConexionBD`, `RepositorioBD`, `MainGUI`) tiene constructor privado.

### 3.4 Modelo de datos en MongoDB

Base de datos `Ahevia`, con cinco colecciones:

**`Usuarios`** — un documento por alumno o administrador (`_id` = DNI)

| Campo | Tipo | Descripción |
|---|---|---|
| `_id` | String | DNI del usuario |
| `Nombre` | String | Nombre de la autoescuela asociada |
| `Administrador` | Boolean | Si el usuario tiene rol de administrador |
| `LicenciaActiva` | Boolean | Si la licencia está vigente (se revoca automáticamente al caducar) |
| `FechaLicencia` | Date | Fecha de alta o última renovación |
| `FechaCaducidad` | Date | Fecha límite de la licencia actual |
| `LicenciasVendidas` | Number | Contador de altas/renovaciones gestionadas (solo en documentos de administrador) |
| `Progreso` | Object | Mapa `idTest → { Fallos, PreguntasFalladas[], RespuestasUltimoIntento{} }` |

**`Temas`** — uno por cada uno de los 12 temas

| Campo | Tipo | Descripción |
|---|---|---|
| `_id` | String | Identificador del tema (p. ej. `PB-TM1`) |
| `Titulo` | String | Título descriptivo |
| `Tests` | String[] | IDs de los Tests que pertenecen al tema |

**`Tests`** — uno por cada Test (fijo o aleatorio)

| Campo | Tipo | Descripción |
|---|---|---|
| `_id` | String | Identificador del test (p. ej. `PB-TM1-T1`) |
| `Preguntas` | String[] | IDs de las preguntas que forman el test |

**`Preguntas`** — el banco completo de preguntas

| Campo | Tipo | Descripción |
|---|---|---|
| `_id` | String | Identificador de la pregunta (p. ej. `PB-TM1-P1`) |
| `Enunciado` | String | Texto de la pregunta |
| `Opciones` | String[] | Opciones de respuesta (ya incluyen su letra: `"A) ..."`) |
| `IndiceRespuestaCorrecta` | Number | Índice (0-based) de la opción correcta |
| `Explicacion` | String | Texto mostrado tras responder |
| `Imagen` | Boolean | Si tiene imagen asociada (opcional, `false` por defecto) |

**`Opciones`** — entradas del menú principal que reutilizan el modelo `Tema`

| Campo | Tipo | Descripción |
|---|---|---|
| `_id` | String | `OPCION1` → "Tests aleatorios". `OPCION2` reservado para "Test de errores" (actualmente esa función se genera en vivo a partir del progreso del alumno, sin leer este documento) |
| `Titulo` | String | Título mostrado en el grid |
| `Tests` | String[] | IDs de los tests que agrupa esa opción |

### 3.5 Motor del examen

- **Cronómetro**: en modo Examen arranca en 30:00 y descuenta cada segundo con un `javax.swing.Timer`; al llegar a 0 corrige y muestra resultados automáticamente.
- **Botonera numérica de navegación**: si el test tiene 30 preguntas o menos se listan todas de una vez; si tiene más, se pagina automáticamente reservando huecos para flechas "anterior"/"siguiente" cuando hacen falta.
- **Coloreado de respuestas**: verde/rojo según acierto (Estudio y Repaso), un azul distinto para "respondida sin corregir" (Examen).
- **Imagen de la pregunta**: si `Pregunta.tieneImagen` es `true`, el propio enunciado se resalta en azul, cambia el cursor a mano y al hacer clic abre un diálogo con la imagen descargada de:
  ```
  https://raw.githubusercontent.com/AngelHernandeezz/public-app-tests-dgt/main/Imagenes/{ID_PREGUNTA}.jpg
  ```
- **Test de errores al corregir**: cada pregunta acertada en el repaso de errores se elimina del conjunto `PreguntasFalladas` de su Test de origen (guardado aparte en memoria durante la sesión), sin tocar el contador histórico de fallos de ese Test.

### 3.6 Panel de administración

Accesible automáticamente si el DNI introducido en el login pertenece a un usuario con `Administrador = true`. Permite, a partir del DNI de un alumno:

- **Crear una licencia nueva** (si el DNI no existe): 30 días de validez desde el momento del alta, heredando el nombre de la autoescuela del propio administrador.
- **Renovar una licencia existente**: suma `DIAS_VALIDEZ_LICENCIA` (30) días a la fecha de caducidad actual si sigue activa, o al momento presente si ya había caducado.
- Cada operación incrementa en 1 el contador `LicenciasVendidas` del administrador que la realiza.

### 3.7 Interfaz gráfica y escalado

- 100% Java Swing sin librerías de terceros para el UI: los botones, flechas de navegación, bordes y degradados están **pintados a mano con `Graphics2D`** (incluyendo overrides de `BasicButtonUI` para evitar el efecto en relieve que Swing aplica por defecto a botones deshabilitados).
- `JTextPane` en lugar de `JLabel` para el enunciado y la explicación, ya que necesitan ajuste de línea fiable y fondo pintable.
- **Escalado automático**: al arrancar (y en cada redimensionado de ventana) se calcula un `factorEscala` comparando la resolución real de pantalla contra una resolución de referencia de 1920×1080, acotado entre 0.85 y 1.0, que se aplica a fuentes, márgenes y tamaños de componente.
- Los paneles se construyen **una sola vez** al arrancar la aplicación; en redimensionados solo se reaplica el estilo con el nuevo factor de escala, sin reconstruir la estructura ni duplicar *listeners*.
- Iconos de ventana en 4 resoluciones (16/32/48/64 px) para verse nítidos en cualquier contexto (barra de tareas, Alt+Tab, etc.).

### 3.8 Configuración de conexión

La URI de MongoDB **nunca está escrita en el código**. Se resuelve en este orden:

1. Variable de entorno `MONGODB_URI`.
2. Archivo `config.properties` (mismo paquete que `ConexionBD`, leído vía `getResourceAsStream`), con la clave `mongodb.uri`, tolerante a BOM y a mayúsculas/minúsculas en el nombre de la clave.

```properties
# config.properties
mongodb.uri=mongodb+srv://usuario:contraseña@cluster.mongodb.net/Ahevia?retryWrites=true&w=majority
```

```bash
# alternativa por variable de entorno (Windows)
setx MONGODB_URI "mongodb+srv://usuario:contraseña@cluster.mongodb.net/Ahevia?retryWrites=true&w=majority"
```

Si no se encuentra ninguna de las dos, la aplicación arranca igualmente pero todas las operaciones contra la base de datos fallan de forma controlada, mostrando el mensaje "Error de conexión" en la interfaz.

---

## Banco de preguntas — Temario oficial DGT

Todas las preguntas siguen el **temario oficial de la DGT en su versión "Lectura Fácil" para el permiso de conducir B**, adaptado a un formato más accesible: enunciados directos y **solo 3 opciones de respuesta (A/B/C)** en lugar de las 4-5 habituales, cada una con una explicación breve tras responder.

### 4.1 Los 12 Temas

| # | ID | Título | Nº de Tests |
|---|---|---|---|
| 1 | `PB-TM1` | Definiciones generales y documentación | 10 |
| 2 | `PB-TM2` | Obligaciones de los usuarios y la vía | 10 |
| 3 | `PB-TM3` | Jerarquía de señales y sus significados | 17 |
| 4 | `PB-TM4` | Sistema de luces de los vehículos | 6 |
| 5 | `PB-TM5` | Normas de preferencia para circular | 6 |
| 6 | `PB-TM6` | Tipos de maniobra y cómo efectuarlas | 9 |
| 7 | `PB-TM7` | Velocidades y distancias de separación | 8 |
| 8 | `PB-TM8` | Transporte de personas y mercancías | 6 |
| 9 | `PB-TM9` | Dispositivos del vehículo y cómo usarlos | 13 |
| 10 | `PB-TM10` | Mecánica y mantenimiento del vehículo | 5 |
| 11 | `PB-TM11` | Estados del conductor y accidentes de tráfico | 18 |
| 12 | `PB-TM12` | Conducción segura, preventiva y eficiente | 10 |
| | | **Total** | **118 Tests** |

Cada Test agrupa habitualmente 30 preguntas (el tamaño exacto puede variar en algún Test concreto). El Tema 11, por ejemplo, incluye contenidos sobre alcoholemia y drogas al volante, primeros auxilios, estadísticas de accidentalidad y sistemas de seguridad como el eCall.

### 4.2 Convención de identificadores

| Elemento | Patrón | Ejemplo |
|---|---|---|
| Tema | `PB-TM{n}` | `PB-TM11` |
| Test dentro de un tema | `PB-TM{n}-T{m}` | `PB-TM11-T18` |
| Pregunta dentro de un tema | `PB-TM{n}-P{k}` | `PB-TM11-P521` |
| Test aleatorio | `PB-TMA-T{m}` | `PB-TMA-T227` |

La numeración de preguntas es **continua a lo largo de todo el tema**, no reinicia en cada Test: el Test 1 agrupa normalmente las preguntas `P1`–`P30`, el Test 2 las `P31`–`P60`, y así sucesivamente.

### 4.3 Formato de pregunta (Lectura Fácil)

Ejemplo real extraído del banco (Tema 11, Test 18):

```json
{
  "_id": "PB-TM11-P510",
  "Enunciado": "¿Qué sistema tecnológico envía la ubicación exacta del vehículo al 112 de forma autónoma tras una colisión?",
  "Opciones": [
    "A) El sistema eCall.",
    "B) El sensor de proximidad trasera.",
    "C) El sistema de navegación GPS con ruta activa."
  ],
  "IndiceRespuestaCorrecta": 0,
  "Explicacion": "El eCall es un sistema de emergencia que se activa por impacto y comunica automáticamente el accidente a los servicios de socorro."
}
```

### 4.4 Tests aleatorios

La opción "Tests aleatorios" del menú principal carga el documento `OPCION1` de la colección `Opciones` y reutiliza exactamente el mismo grid de Tests que un Tema normal. Detrás hay un **pool de más de 200 tests pregenerados** (`PB-TMA-T1` … `PB-TMA-T227`), cada uno con 30 preguntas mezcladas aleatoriamente entre los 12 Temas, de forma que ningún test aleatorio se limita a un único tema.

### 4.5 Test de errores

A diferencia de "Tests aleatorios", el "Test de errores" **no lee ningún documento fijo de Mongo**: en el momento en que el alumno lo abre, la aplicación recorre todo su `Progreso` guardado, recopila el conjunto de IDs de preguntas falladas en cualquier Test (guardando de qué Test procede cada una) y arranca con ellas el motor de examen en modo Estudio. Al acertar una pregunta en este modo, se elimina automáticamente del conjunto de falladas de su Test de origen.

---

## Launcher y actualizador automático

Módulo independiente (paquete `launcher`, JAR separado que **no puede importar** `Estilo` ni `Constantes` de la app principal, por lo que replica sus propios colores) cuya única función es mantener la aplicación actualizada sin intervención del alumno.

**Flujo en cada arranque:**

1. Resuelve `%LOCALAPPDATA%\Ahevia\AheviaApp\` (o `user.home/AppData/Local` como *fallback*) y crea la carpeta si no existe.
2. Descarga `Version.txt` desde GitHub y lo compara contra el `Version.txt` guardado localmente.
3. Si es la primera instalación, la versión remota es distinta, o falta el ejecutable local → descarga el nuevo `Ahevia.exe`.
4. Si falta la carpeta `jre/` → descarga y extrae un JRE portable empaquetado en un `.zip` alojado en GitHub Releases, para que el ejecutable generado con Launch4j pueda correr **sin que el usuario tenga Java instalado**.
5. Lanza `Ahevia.exe` como proceso independiente vía `ProcessBuilder`.

**Detalles de implementación:**

- La **ventana de carga** (degradado azul + tarjeta blanca redondeada, igual que la app principal) con un coche 🚗 animado recorriendo una barra de progreso **solo se muestra en la instalación inicial**; las actualizaciones posteriores se descargan en segundo plano sin interrumpir al alumno. La animación del coche es decorativa (un `Timer` de Swing que avanza su posición cada 20 ms), no está ligada al porcentaje real de descarga.
- La extracción del ZIP del JRE valida cada ruta contra la carpeta destino para evitar *path traversal* (protección tipo *Zip Slip*).
- Si algo falla durante la comprobación o descarga, el Launcher intenta igualmente **arrancar el ejecutable ya existente** como último recurso antes de mostrar un diálogo de error.

**Endpoints remotos utilizados por el Launcher:**

| Recurso | URL |
|---|---|
| Versión actual | `raw.githubusercontent.com/AngelHernandeezz/app-tests-dgt/main/Version.txt` |
| Ejecutable | `raw.githubusercontent.com/AngelHernandeezz/app-tests-dgt/main/Ahevia.exe` |
| JRE portable | GitHub Release `JavaSupport/jre.zip` del mismo repositorio |

---

## Landing page

Página estática (`index.html`, sin build ni JavaScript de terceros) que sirve como escaparate público de Autoescuela AHE y punto de descarga del instalador.

- **Sección "hero"**: logo circular, titular *"Deja de memorizar. Empieza a entender."*, texto de presentación y botón de descarga con degradado naranja que enlaza directamente al asset de la última release (`.../releases/download/Launcher/Instalador.exe`).
- **Sección "contacto"**: cuatro tarjetas con código QR — **WhatsApp**, **Instagram**, **TikTok** y **Email** — cada una con su icono y enlace directo (`wa.me`, perfil de Instagram/TikTok, `mailto:`).
- **Tipografía**: `Fraunces` (serif, titulares) + `Inter` (sans-serif, cuerpo), vía Google Fonts.
- **Paleta de color**:

  | Variable | Color |
  |---|---|
  | `--azul-profundo` | `#163E63` |
  | `--azul-medio` | `#2C76A8` |
  | `--azul-claro` | `#8FCBE8` |
  | `--naranja-sol` | `#E8722C` |
  | `--naranja-claro` | `#F4A33D` |
  | `--crema` | `#FAF8F4` |
  | `--gris-texto` | `#3A4750` |

- **Responsive**: en pantallas ≤760px el layout pasa a columna única y se reactiva el scroll (en escritorio se fuerza `100vh` sin scroll). Como el instalador es un `.exe` de Windows, en móvil el **botón de descarga se deshabilita** y se sustituye por el aviso *"Solo disponible para ordenadores Windows"*.
- **Pie de página**: *"Temario homologado por la DGT - Autoescuela AHE"*.

---

## Empaquetado y distribución

- **Launch4j** envuelve tanto la aplicación principal como el Launcher en ejecutables nativos de Windows (`.exe`) a partir de sus respectivos JAR.
- Cada `.exe` corre sobre un **JRE portable** (el mismo Launcher se encarga de descargarlo y colocarlo junto al ejecutable si falta, ver [sección anterior](#launcher-y-actualizador-automático)), de forma que el alumno **no necesita tener Java instalado**.
- El Launcher se distribuye a su vez mediante un **instalador autoextraíble de Windows** (`Instalador.exe`, enlazado desde la landing page), que lo copia a `%LOCALAPPDATA%\Ahevia\AheviaLauncher\`; en tiempo de ejecución, el propio Launcher gestiona la carpeta hermana `%LOCALAPPDATA%\Ahevia\AheviaApp\` donde viven la aplicación principal y el JRE.
- **Compatibilidad**: Windows moderno (no se soporta Windows XP, ya que tanto el JRE requerido como el TLS 1.2 obligatorio para conectar con MongoDB Atlas son incompatibles con su stack de red).

---

## Tecnologías utilizadas

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17+ |
| Interfaz gráfica | Java Swing (sin librerías externas de UI; pintado personalizado con `Graphics2D`) |
| Base de datos | MongoDB Atlas |
| Driver de MongoDB | `mongo-java-driver` 3.12.14 |
| Empaquetado a `.exe` | Launch4j |
| Runtime distribuido | JRE portable |
| Instalador | Autoextraíble de Windows (WinRAR SFX) |
| Landing page | HTML5 + CSS3 (sin frameworks), Google Fonts |
| Hosting de assets | GitHub Releases + `raw.githubusercontent.com` |

---

## Puesta en marcha (desarrollo)

1. Clona el repositorio.
2. Configura el acceso a MongoDB por **una** de estas dos vías (ver [3.8](#38-configuración-de-conexión)):
   - Variable de entorno `MONGODB_URI`, o
   - Archivo `config.properties` en el mismo paquete que `ConexionBD.java`.
3. Añade el driver de Mongo al *classpath*. Si usas Maven:

   ```xml
   <dependency>
       <groupId>org.mongodb</groupId>
       <artifactId>mongo-java-driver</artifactId>
       <version>3.12.14</version>
   </dependency>
   ```

4. Compila y ejecuta `app.MainGUI` (contiene el `main`). La conexión a MongoDB se inicializa sola, en un bloque estático de `ConexionBD`, en cuanto se usa por primera vez.
5. Para el Launcher, el punto de entrada es `launcher.Main`; necesita que existan (o se puedan descargar) `Version.txt` y `Ahevia.exe` en el repositorio remoto configurado en sus constantes de URL.

---

## Estructura del repositorio

```
app/                        Aplicación principal (paquete Java "app")
├── MainGUI.java             Punto de entrada + cableado de la interfaz
├── constantes/
│   └── Constantes.java
├── estilo/
│   └── Estilo.java
├── datos/
│   ├── ConexionBD.java
│   └── RepositorioBD.java
├── logica/
│   ├── LogicaTest.java
│   ├── EstadoTest.java
│   └── ModoTest.java
└── modelo/
    ├── Usuario.java
    ├── Tema.java
    ├── Test.java
    ├── Pregunta.java
    └── ProgresoTest.java

launcher/                   Lanzador y actualizador automático
└── Main.java

web/                         Landing page de descarga
└── index.html

datos/                       Banco de preguntas (ejemplos / semillas para Mongo)
├── Temas.json
├── TestsOrdenados.json
├── TestsAleatorios.json
└── PreguntasT18.json
```

---

## Licencia

Software propietario desarrollado por Ángel Hernández Espinosa para uso comercial de autoescuelas. Todos los derechos reservados. Si vas a hacer público este repositorio, añade aquí los términos que prefieras (o un archivo `LICENSE` en la raíz).

## Autor

**Ángel Hernández Espinosa** — Autoescuela AHE
📱 WhatsApp: [+34 601 22 27 92](https://wa.me/34601222792) · 📷 [Instagram](https://www.instagram.com/autoescuela.ahe/) · 🎵 [TikTok](https://www.tiktok.com/@autoescuela.ahe) · ✉️ [autoescuela.ahe@gmail.com](mailto:autoescuela.ahe@gmail.com)
