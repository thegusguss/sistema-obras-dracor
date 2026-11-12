# Sistema de Gestión y Seguimiento del Avance de Obras – DRACOR

Sistema web (Spring Boot 3 · Java 17 · Thymeleaf · Spring Security · JPA/Hibernate · MySQL) que cubre los
**20 requerimientos funcionales** del SRS, con las reglas de negocio RN-01 a RN-19.

## 1. Requisitos
| Componente | Versión / dato |
|---|---|
| JDK | 17 o superior (configurado en NetBeans) |
| Apache NetBeans | 31 (trae Maven integrado) |
| XAMPP | MySQL / MariaDB en `localhost:3306`, usuario `root`, **sin contraseña** |
| HeidiSQL | Solo para consultar/administrar la base `dracor_db` |
| Internet | Solo la primera vez (Maven descarga las librerías) |

## 2. Puesta en marcha (opción A: ejecutable)
1. En XAMPP pulse **Start** en MySQL (o deje que el ejecutable lo inicie).
2. Doble clic en **`Iniciar_DRACOR.exe`**. Hace todo solo: inicia MySQL si hace falta, compila la primera vez,
   arranca el sistema y abre el navegador en <http://localhost:8080>.
   (Alternativa equivalente: `Iniciar_DRACOR.bat`.)
3. Para apagar el sistema cierre la ventana negra o pulse ENTER en ella.

> Si Windows muestra «SmartScreen», pulse *Más información → Ejecutar de todas formas* (el programa no está firmado digitalmente).

## 3. Puesta en marcha (opción B: NetBeans 31)
1. *File → Open Project…* y seleccione la carpeta `SistemaObrasDracor` (se reconoce como proyecto Maven).
2. Clic derecho en el proyecto → **Clean and Build** (descarga las dependencias).
3. Abra `src/main/java/pe/dracor/sgsao/SgsaoDracorApplication.java` → clic derecho → **Run File** (o *Run Project*).
4. Entre a <http://localhost:8080>.

## 4. Base de datos (HeidiSQL)
- Conexión: tipo *MariaDB/MySQL (TCP/IP)*, host `127.0.0.1`, usuario `root`, contraseña vacía, puerto `3306`.
- La base `dracor_db` y todas sus tablas se **crean solas** al iniciar el sistema (también hay un script opcional en `database/01_crear_base_de_datos.sql`).
- Para reiniciar de cero: en HeidiSQL ejecute `DROP DATABASE dracor_db;` y vuelva a iniciar el sistema.
- Si su MySQL tiene contraseña, cámbiela en `src/main/resources/application.properties` (`spring.datasource.password`).

## 5. Usuarios de prueba
| Usuario | Contraseña | Perfil |
|---|---|---|
| `admin` | `Admin2026` | Administrador |
| `jefe` | `Jefe2026` | Jefe de proyecto |
| `supervisor` | `Super2026` | Supervisor |
| `oficina` | `Oficina2026` | Administrativo |

Cambie estas contraseñas en *Usuarios y perfiles* antes de usarlo en serio. Para no cargar los datos de ejemplo
(clientes, propuestas y obras demo) ponga `dracor.datos-demo=false` en `application.properties` antes del primer arranque.

## 6. Mapa de requerimientos funcionales
| RF | Funcionalidad | Menú |
|---|---|---|
| RF-01 | Ingreso con bloqueo tras 5 intentos fallidos | Login |
| RF-02 | Usuarios y perfiles (roles múltiples) | Usuarios y perfiles |
| RF-03 | Clientes y entidades | Clientes |
| RF-04 | Requerimientos de clientes | Requerimientos |
| RF-05 | Propuestas y cotizaciones | Propuestas |
| RF-06 | Respuestas y observaciones del cliente | Propuestas → detalle |
| RF-07 | Documentos (carga, descarga, anulación) | Documentos / detalle de cada módulo |
| RF-08 | Tipos documentales | Tipos documentales |
| RF-09 | Obras (desde propuesta aceptada u orden/contrato) | Obras |
| RF-10 | Actividades y responsables | Obra → Actividades |
| RF-11 | Avance de actividades | Obra → botón de avance |
| RF-12 | Avance de obra (promedio ponderado) | Obra → detalle |
| RF-13 | Incidencias | Obra → Incidencias |
| RF-14 | Evidencias (anulables, no se borran) | Obra → Evidencias |
| RF-15 | Historial de actualizaciones | Historial |
| RF-16 | Alertas de vencidas y por vencer | Alertas |
| RF-17 | Cierre formal con conformidad | Obra → Cierre |
| RF-18 | Panel de obras | Panel |
| RF-19 | Reportes de obras (PDF / Excel) | Reportes |
| RF-20 | Reportes de propuestas y documentos (PDF / Excel) | Reportes |

## 7. Perfiles y permisos
- **Administrador**: todo. **Jefe de proyecto**: comercial, obras, actividades, cierre, reportes, historial.
- **Supervisor**: registra avances, incidencias y evidencias; consulta obras y documentos.
- **Administrativo**: clientes, requerimientos, propuestas, documentos y reportes comerciales.

## 8. Estructura
```
SistemaObrasDracor/
├─ pom.xml
├─ Iniciar_DRACOR.exe / .bat       lanzador
├─ launcher/                       código fuente del lanzador (Go)
├─ database/                       script opcional para HeidiSQL
├─ uploads/                        archivos cargados (se crea sola)
└─ src/main
   ├─ java/pe/dracor/sgsao
   │  ├─ config/    seguridad y datos iniciales
   │  ├─ domain/    entidades y enumeraciones
   │  ├─ repository/ acceso a datos
   │  ├─ service/   reglas de negocio
   │  ├─ util/      exportación PDF / Excel
   │  └─ web/       controladores
   └─ resources/    application.properties, plantillas Thymeleaf, CSS, logos
```
