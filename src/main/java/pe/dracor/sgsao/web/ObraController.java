package pe.dracor.sgsao.web;

import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.dracor.sgsao.domain.EstadoObra;
import pe.dracor.sgsao.domain.Obra;
import pe.dracor.sgsao.domain.OrigenObra;
import pe.dracor.sgsao.domain.Rol;
import pe.dracor.sgsao.service.ActividadService;
import pe.dracor.sgsao.service.ClienteService;
import pe.dracor.sgsao.service.DocumentoService;
import pe.dracor.sgsao.service.EvidenciaService;
import pe.dracor.sgsao.service.HistorialService;
import pe.dracor.sgsao.service.IncidenciaService;
import pe.dracor.sgsao.service.ObraService;
import pe.dracor.sgsao.service.PropuestaService;
import pe.dracor.sgsao.service.ReglaNegocioException;
import pe.dracor.sgsao.service.UsuarioService;

/** RF-09, RF-12 y RF-17: obras, detalle de avance y cierre. */
@Controller
@RequestMapping("/obras")
public class ObraController {

    private final ObraService service;
    private final ClienteService clientes;
    private final PropuestaService propuestas;
    private final UsuarioService usuarios;
    private final ActividadService actividades;
    private final IncidenciaService incidencias;
    private final EvidenciaService evidencias;
    private final DocumentoService documentos;
    private final HistorialService historial;

    public ObraController(ObraService service, ClienteService clientes, PropuestaService propuestas,
                          UsuarioService usuarios, ActividadService actividades, IncidenciaService incidencias,
                          EvidenciaService evidencias, DocumentoService documentos, HistorialService historial) {
        this.service = service;
        this.clientes = clientes;
        this.propuestas = propuestas;
        this.usuarios = usuarios;
        this.actividades = actividades;
        this.incidencias = incidencias;
        this.evidencias = evidencias;
        this.documentos = documentos;
        this.historial = historial;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("obras", service.resumenes());
        return "obras/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        Obra o = new Obra();
        o.setCodigo(service.sugerirCodigo());
        o.setOrigen(OrigenObra.PROPUESTA_ACEPTADA);
        return formulario(model, o);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        return formulario(model, service.obtener(id));
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("obra") Obra form, Model model, RedirectAttributes ra) {
        try {
            Obra o = service.guardar(form);
            ra.addFlashAttribute("ok", "Obra " + o.getCodigo() + " guardada correctamente.");
            return "redirect:/obras/" + o.getId();
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(model, form);
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        var resumen = service.resumen(id);
        Obra o = resumen.getObra();
        model.addAttribute("r", resumen);
        model.addAttribute("obra", o);
        model.addAttribute("actividades", actividades.deObra(id));
        model.addAttribute("incidencias", incidencias.deObra(id));
        model.addAttribute("evidencias", evidencias.deObra(id));
        model.addAttribute("documentos", documentos.deObra(id));
        model.addAttribute("historial", historial.deObra(id));
        model.addAttribute("tipos", documentos.tiposActivos());
        model.addAttribute("siguientes", service.siguientesEstados(o));
        model.addAttribute("responsables", usuarios.activos());
        model.addAttribute("hoy", LocalDate.now());
        return "obras/detalle";
    }

    @PostMapping("/{id}/estado")
    public String estado(@PathVariable Long id, @RequestParam EstadoObra nuevo, RedirectAttributes ra) {
        try {
            service.cambiarEstado(id, nuevo);
            ra.addFlashAttribute("ok", "La obra pasó a «" + nuevo.getEtiqueta() + "».");
        } catch (ReglaNegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/obras/" + id;
    }

    private String formulario(Model model, Obra o) {
        model.addAttribute("obra", o);
        model.addAttribute("clientes", clientes.activos());
        model.addAttribute("propuestas", propuestas.aceptadas());
        model.addAttribute("jefes", usuarios.activos().stream()
                .filter(u -> u.tieneRol(Rol.JEFE_PROYECTO) || u.tieneRol(Rol.ADMINISTRADOR)).toList());
        model.addAttribute("origenes", OrigenObra.values());
        return "obras/form";
    }
}
