# Distribución del trabajo por integrante – SGSAO DRACOR

Rutas relativas a `src/main/java/pe/dracor/sgsao/` (Java) y `src/main/resources/templates/` (vistas).

## Resumen

| Integrante | Requerimientos |
|---|---|
| Oscar | RF-01, RF-10, RF-12, RF-18 |
| Gustavo | RF-02, RF-09, RF-11, RF-16 |
| Josh | RF-03, RF-08, RF-15, RF-19 |
| Luis | RF-04, RF-06, RF-13, RF-17 |
| Alessandro | RF-05, RF-07, RF-14, RF-20 |

---

## 0. Base común del proyecto (la sube un integrante primero; se sugiere Gustavo)
Sin esto nada compila. Es la "estructura" acordada por el grupo.

- `pom.xml`, `.gitignore`, `README.md`, `database/`, `launcher/`, `Iniciar_DRACOR.bat`
- `src/main/resources/application.properties`
- `SgsaoDracorApplication.java`
- `service/ReglaNegocioException.java`, `util/Redireccion.java`, `web/GlobalModel.java`
- `util/ExportadorPdf.java`, `util/ExportadorExcel.java`, `util/ReporteTabla.java` (los usan RF-19 y RF-20)
- `config/DatosIniciales.java`
- `templates/fragments/layout.html`, `templates/fragments/ui.html`, `templates/error.html`
- `static/css/app.css`, `static/img/`

> Nota: `Iniciar_DRACOR.exe` es un binario; conviene no subirlo (se genera desde `launcher/`).

---

## Oscar — RF-01, RF-10, RF-12, RF-18
**RF-01 Iniciar y cerrar sesión**
- `config/SecurityConfig.java`, `config/DracorUserDetailsService.java`
- `service/IntentosLoginService.java`
- `web/LoginController.java`
- `templates/login.html`, `templates/acceso-denegado.html`

**RF-10 Planificar actividades y responsables**
- `domain/Actividad.java`
- `repository/ActividadRepository.java`
- `service/ActividadService.java` (incluye reasignar y justificar)
- `web/ActividadController.java`
- `templates/actividades/form.html`

**RF-12 Calcular el avance de la obra**
- `service/ObraResumen.java`
- En `service/ObraService.java`: métodos `promedio`, `calcularAvance`, `resumen` y `recalcularEstado`

**RF-18 Panel de obras**
- `service/DashboardService.java`, `service/PanelResumen.java`
- `web/PanelController.java`
- `templates/panel.html`

## Gustavo — RF-02, RF-09, RF-11, RF-16
**RF-02 Usuarios y perfiles**
- `domain/Usuario.java`, `domain/Rol.java`
- `repository/UsuarioRepository.java`
- `service/UsuarioService.java`, `service/UsuarioActual.java`
- `web/UsuarioController.java`
- `templates/usuarios/lista.html`, `templates/usuarios/form.html`

**RF-09 Registrar obras o servicios**
- `domain/Obra.java`, `domain/EstadoObra.java`, `domain/OrigenObra.java`
- `repository/ObraRepository.java`
- `service/ObraService.java` (métodos `guardar`, `cambiarEstado`, `sugerirCodigo`, consultas)
- `web/ObraController.java`
- `templates/obras/lista.html`, `templates/obras/form.html`, `templates/obras/detalle.html`

**RF-11 Registrar avance de actividades**
- `domain/AvanceActividad.java`
- `repository/AvanceActividadRepository.java`
- `service/AvanceService.java`
- `web/AvanceController.java`
- `templates/avances/form.html`

**RF-16 Alertas de actividades vencidas o por vencer**
- `service/AlertaService.java`
- `web/AlertaController.java`
- `templates/alertas.html`

## Josh — RF-03, RF-08, RF-15, RF-19
**RF-03 Clientes o entidades**
- `domain/Cliente.java`, `domain/TipoCliente.java`
- `repository/ClienteRepository.java`
- `service/ClienteService.java`
- `web/ClienteController.java`
- `templates/clientes/lista.html`, `templates/clientes/form.html`

**RF-08 Tipos documentales**
- `domain/TipoDocumento.java`
- `repository/TipoDocumentoRepository.java`
- En `service/DocumentoService.java`: métodos de tipos (`todosLosTipos`, `tiposActivos`, `obtenerTipo`, `guardarTipo`, `cambiarEstadoTipo`)
- En `web/DocumentoController.java`: rutas `/tipos-documento/**`
- `templates/documentos/tipos.html`, `templates/documentos/tipo-form.html`

**RF-15 Historial de actualizaciones**
- `domain/HistorialActualizacion.java`
- `repository/HistorialActualizacionRepository.java`
- `service/HistorialService.java`
- `web/AuditoriaController.java`
- `templates/auditoria.html`

**RF-19 Reportes de obras (PDF / Excel)**
- `util/ExportadorPdf.java`, `util/ExportadorExcel.java`, `util/ReporteTabla.java`
- `service/ReporteService.java` (método `obras` y `FILTROS_OBRAS`)
- `web/ReporteController.java` (rutas `/reportes` y `/reportes/obras/**`)
- `templates/reportes/indice.html`, `templates/reportes/obras.html`, `templates/reportes/_tabla.html`

## Luis — RF-04, RF-06, RF-13, RF-17
**RF-04 Registrar requerimientos**
- `domain/Requerimiento.java`, `domain/OrigenRequerimiento.java`
- `repository/RequerimientoRepository.java`
- `service/RequerimientoService.java`
- `web/RequerimientoController.java`
- `templates/requerimientos/lista.html`, `form.html`, `detalle.html`

**RF-06 Respuestas y observaciones**
- `domain/RespuestaPropuesta.java`, `domain/TipoRespuesta.java`
- `repository/RespuestaPropuestaRepository.java`
- En `service/PropuestaService.java`: métodos `registrarRespuesta` y `respuestasDe`
- En `web/PropuestaController.java`: ruta `/{id}/respuesta`

**RF-13 Incidencias**
- `domain/Incidencia.java`, `domain/TipoIncidencia.java`, `domain/EstadoIncidencia.java`
- `repository/IncidenciaRepository.java`
- `service/IncidenciaService.java`
- `web/IncidenciaController.java`
- Sección de incidencias en `templates/obras/detalle.html`

**RF-17 Cierre y conformidad**
- En `service/ObraService.java`: métodos `pendientesDeCierre`, `pendientesDeActividades`, `cerrar`
- En `service/DocumentoService.java`: método `tieneConformidad`
- En `web/ObraController.java`: ruta `/obras/{id}/cerrar`

## Alessandro — RF-05, RF-07, RF-14, RF-20
**RF-05 Propuestas y cotizaciones**
- `domain/Propuesta.java`, `domain/EstadoPropuesta.java`
- `repository/PropuestaRepository.java`
- `service/PropuestaService.java` (`guardar`, `cambiarEstado`, transiciones, consultas)
- `web/PropuestaController.java`
- `templates/propuestas/lista.html`, `form.html`, `detalle.html`

**RF-07 Documentos**
- `domain/Documento.java`
- `repository/DocumentoRepository.java`
- `service/DocumentoService.java` (`subir`, `anular`, `recurso`, `obligatoriosFaltantes`, consultas)
- `service/AlmacenArchivos.java`
- `web/DocumentoController.java` (rutas `/documentos/**`)
- `templates/documentos/lista.html`

**RF-14 Evidencias**
- `domain/Evidencia.java`
- `repository/EvidenciaRepository.java`
- `service/EvidenciaService.java`
- `web/EvidenciaController.java`
- Sección de evidencias en `templates/obras/detalle.html`

**RF-20 Reportes de propuestas y documentos**
- `service/ReporteService.java` (métodos `propuestas` y `documentos`)
- En `web/ReporteController.java`: rutas `/reportes/propuestas/**` y `/reportes/documentos/**`
- `templates/reportes/propuestas.html`, `templates/reportes/documentos.html`

---

## Archivos compartidos (varios integrantes tocan el mismo archivo)
| Archivo | Integrantes |
|---|---|
| `ObraService.java` | Gustavo (RF-09), Oscar (RF-12), Luis (RF-17) |
| `PropuestaService.java` / `PropuestaController.java` | Alessandro (RF-05), Luis (RF-06) |
| `DocumentoService.java` / `DocumentoController.java` | Alessandro (RF-07), Josh (RF-08), Luis (RF-17) |
| `ReporteService.java` / `ReporteController.java` | Josh (RF-19), Alessandro (RF-20) |
| `obras/detalle.html` | Gustavo, Oscar, Luis, Alessandro |
| `SecurityConfig.java` | Oscar (cada uno agrega la regla de acceso de su módulo) |

---

## Flujo sugerido en GitHub (cada uno desde su propia cuenta)
1. Un integrante crea el repositorio, sube la **base común** a `main` e invita a los demás como colaboradores.
2. Cada integrante clona el repo y configura **su propio** nombre y correo:
   `git config user.name "Su Nombre"` y `git config user.email "su-correo-de-github"`.
3. Cada uno crea su rama: `feature/rf-01-login`, `feature/rf-09-obras`, etc.
4. Copia en su rama los archivos de su parte, compila, y hace commits pequeños por requerimiento
   (ej.: `RF-03: registro y edición de clientes con RUC único`).
5. Abre un **Pull Request** hacia `main`; otro integrante lo revisa y lo aprueba.
6. Orden de subida (obligatorio, porque los archivos compartidos se completan por etapas):
   **1 Gustavo → 2 Oscar → 3 Alessandro → 4 Josh → 5 Luis**.
   Las carpetas de cada uno están en `ENTREGAS_POR_INTEGRANTE/`, cada una con su `LEEME_<NOMBRE>.md`.
   El sistema compila completo cuando están unidas las 5 partes.
