package pe.dracor.sgsao.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.Cliente;
import pe.dracor.sgsao.repository.ClienteRepository;

/** RF-03: clientes y entidades (RN-01 y RN-03). */
@Service
public class ClienteService {

    private final ClienteRepository repo;
    private final HistorialService historial;

    public ClienteService(ClienteRepository repo, HistorialService historial) {
        this.repo = repo;
        this.historial = historial;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(String texto) {
        if (texto == null || texto.isBlank()) {
            return repo.findAllByOrderByRazonSocialAsc();
        }
        String t = texto.trim();
        return repo.findByRazonSocialContainingIgnoreCaseOrRucContainingOrderByRazonSocialAsc(t, t);
    }

    @Transactional(readOnly = true)
    public List<Cliente> activos() {
        return repo.findByActivoTrueOrderByRazonSocialAsc();
    }

    @Transactional(readOnly = true)
    public Cliente obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new ReglaNegocioException("El cliente no existe."));
    }

    @Transactional
    public Cliente guardar(Cliente form) {
        boolean nuevo = form.getId() == null;
        String ruc = form.getRuc() == null ? "" : form.getRuc().trim();
        if (form.getTipo() == null) {
            throw new ReglaNegocioException("Seleccione el tipo de cliente.");
        }
        if (form.getRazonSocial() == null || form.getRazonSocial().isBlank()) {
            throw new ReglaNegocioException("Ingrese la razón social o el nombre del cliente.");
        }
        if (!ruc.matches("\\d{11}")) {
            throw new ReglaNegocioException("El RUC debe tener exactamente 11 dígitos.");
        }
        boolean duplicado = nuevo ? repo.existsByRuc(ruc) : repo.existsByRucAndIdNot(ruc, form.getId());
        if (duplicado) {
            throw new ReglaNegocioException("Ya existe un cliente registrado con el RUC " + ruc + ".");
        }
        Cliente c = nuevo ? new Cliente() : obtener(form.getId());
        c.setTipo(form.getTipo());
        c.setRazonSocial(form.getRazonSocial().trim());
        c.setRuc(ruc);
        c.setDireccion(limpiar(form.getDireccion()));
        c.setPersonaContacto(limpiar(form.getPersonaContacto()));
        c.setTelefono(limpiar(form.getTelefono()));
        c.setCorreo(limpiar(form.getCorreo()));
        if (nuevo) {
            c.setActivo(true);
        }
        c = repo.save(c);
        historial.registrar("Cliente", c.getId(), null, nuevo ? "CREACION" : "MODIFICACION",
                (nuevo ? "Se registró el cliente " : "Se modificó el cliente ") + c.getRazonSocial() + " (RUC " + c.getRuc() + ")");
        return c;
    }

    @Transactional
    public void cambiarEstado(Long id) {
        Cliente c = obtener(id);
        c.setActivo(!c.isActivo());
        repo.save(c);
        historial.registrar("Cliente", c.getId(), null, "MODIFICACION",
                "El cliente " + c.getRazonSocial() + (c.isActivo() ? " fue activado." : " fue desactivado."));
    }

    private String limpiar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
