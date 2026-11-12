package pe.dracor.sgsao.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.dracor.sgsao.domain.*;
import pe.dracor.sgsao.repository.*;

/**
 * Carga los datos iniciales la primera vez que se ejecuta el sistema:
 * usuarios de cada perfil, catalogo de tipos documentales y (opcional) datos de ejemplo.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepository usuarios;
    private final TipoDocumentoRepository tipos;
    private final ClienteRepository clientes;
    private final RequerimientoRepository requerimientos;
    private final PropuestaRepository propuestas;
    private final ObraRepository obras;
    private final ActividadRepository actividades;
    private final AvanceActividadRepository avances;
    private final IncidenciaRepository incidencias;
    private final HistorialActualizacionRepository historial;
    private final PasswordEncoder encoder;
    private final boolean datosDemo;

    public DatosIniciales(UsuarioRepository usuarios, TipoDocumentoRepository tipos, ClienteRepository clientes,
                          RequerimientoRepository requerimientos, PropuestaRepository propuestas, ObraRepository obras,
                          ActividadRepository actividades, AvanceActividadRepository avances,
                          IncidenciaRepository incidencias, HistorialActualizacionRepository historial,
                          PasswordEncoder encoder, @Value("${dracor.datos-demo:true}") boolean datosDemo) {
        this.usuarios = usuarios;
        this.tipos = tipos;
        this.clientes = clientes;
        this.requerimientos = requerimientos;
        this.propuestas = propuestas;
        this.obras = obras;
        this.actividades = actividades;
        this.avances = avances;
        this.incidencias = incidencias;
        this.historial = historial;
        this.encoder = encoder;
        this.datosDemo = datosDemo;
    }

    @Override
    public void run(String... args) {
        boolean primeraVez = usuarios.count() == 0;
        if (primeraVez) {
            crearUsuarios();
        }
        if (tipos.count() == 0) {
            crearTiposDocumentales();
        }
        if (primeraVez && datosDemo && obras.count() == 0) {
            crearDatosDeEjemplo();
        }
        if (primeraVez) {
            LOG.info("Datos iniciales cargados. Usuario administrador: admin / Admin2026");
        }
    }

    // ------------------------------------------------------------------ usuarios
    private Usuario usuario(String nombres, String apellidos, String dni, String username, String clave, Set<Rol> roles) {
        Usuario u = new Usuario();
        u.setNombres(nombres);
        u.setApellidos(apellidos);
        u.setDni(dni);
        u.setUsername(username);
        u.setPassword(encoder.encode(clave));
        u.setRoles(roles);
        u.setActivo(true);
        return usuarios.save(u);
    }

    private void crearUsuarios() {
        usuario("Administrador", "del Sistema", "00000001", "admin", "Admin2026", EnumSet.of(Rol.ADMINISTRADOR));
        usuario("Carlos", "Mamani Quispe", "42851963", "jefe", "Jefe2026", EnumSet.of(Rol.JEFE_PROYECTO));
        usuario("Rosa", "Condori Flores", "45217890", "supervisor", "Super2026", EnumSet.of(Rol.SUPERVISOR));
        usuario("María", "Huanca Ramos", "47093218", "oficina", "Oficina2026", EnumSet.of(Rol.ADMINISTRATIVO));
    }

    // ------------------------------------------------------------------ catalogo documental (RF-08)
    private void tipo(String nombre, boolean obligatorio, boolean conformidad) {
        TipoDocumento t = new TipoDocumento();
        t.setNombre(nombre);
        t.setObligatorio(obligatorio);
        t.setConformidad(conformidad);
        t.setActivo(true);
        tipos.save(t);
    }

    private void crearTiposDocumentales() {
        tipo("Términos de Referencia (TDR)", false, false);
        tipo("Solicitud de cotización", false, false);
        tipo("Cotización", true, false);
        tipo("Declaración jurada", true, false);
        tipo("Ficha RUC", false, false);
        tipo("Registro Nacional de Proveedores (RNP)", false, false);
        tipo("DNI del representante", false, false);
        tipo("Carta de autorización CCI", false, false);
        tipo("Propuesta técnica", false, false);
        tipo("Orden de servicio", false, false);
        tipo("Contrato", false, false);
        tipo("Informe de avance", false, false);
        tipo("Plano o documento técnico", false, false);
        tipo("Constancia de conformidad", false, true);
    }

    // ------------------------------------------------------------------ datos de ejemplo
    private Cliente cliente(TipoCliente tipo, String razon, String ruc, String dir, String contacto, String tel, String correo) {
        Cliente c = new Cliente();
        c.setTipo(tipo);
        c.setRazonSocial(razon);
        c.setRuc(ruc);
        c.setDireccion(dir);
        c.setPersonaContacto(contacto);
        c.setTelefono(tel);
        c.setCorreo(correo);
        c.setActivo(true);
        return clientes.save(c);
    }

    private Requerimiento req(Cliente c, String desc, OrigenRequerimiento o, LocalDate recep, LocalDate limite, Usuario por) {
        Requerimiento r = new Requerimiento();
        r.setCliente(c);
        r.setDescripcion(desc);
        r.setOrigen(o);
        r.setFechaRecepcion(recep);
        r.setFechaLimite(limite);
        r.setRegistradoPor(por);
        return requerimientos.save(r);
    }

    private Propuesta propuesta(Requerimiento r, String monto, LocalDate envio, EstadoPropuesta e, Usuario por) {
        Propuesta p = new Propuesta();
        p.setRequerimiento(r);
        p.setMonto(new BigDecimal(monto));
        p.setFechaEnvio(envio);
        p.setEstado(e);
        p.setRegistradoPor(por);
        return propuestas.save(p);
    }

    private Actividad actividad(Obra o, String nombre, Usuario resp, LocalDate ini, LocalDate fin, int peso, int avance) {
        Actividad a = new Actividad();
        a.setObra(o);
        a.setNombre(nombre);
        a.setResponsable(resp);
        a.setFechaInicio(ini);
        a.setFechaFin(fin);
        a.setPeso(peso);
        a.setAvance(avance);
        a.setUltimaActualizacion(LocalDateTime.now().minusDays(2));
        a = actividades.save(a);
        if (avance > 0) {
            AvanceActividad av = new AvanceActividad();
            av.setActividad(a);
            av.setPorcentaje(avance);
            av.setFecha(LocalDate.now().minusDays(2));
            av.setObservacion("Avance registrado en la visita de supervisión.");
            av.setUsuario(resp);
            av.setFechaRegistro(LocalDateTime.now().minusDays(2));
            avances.save(av);
        }
        return a;
    }

    private void historia(String entidad, Long id, Long obraId, String accion, String detalle) {
        HistorialActualizacion h = new HistorialActualizacion();
        h.setEntidad(entidad);
        h.setEntidadId(id);
        h.setObraId(obraId);
        h.setAccion(accion);
        h.setDetalle(detalle);
        h.setUsuario("sistema");
        h.setFechaHora(LocalDateTime.now().minusDays(1));
        historial.save(h);
    }

    private void crearDatosDeEjemplo() {
        Usuario jefe = usuarios.findByUsername("jefe").orElse(null);
        Usuario sup = usuarios.findByUsername("supervisor").orElse(null);
        Usuario ofi = usuarios.findByUsername("oficina").orElse(null);
        LocalDate hoy = LocalDate.now();

        Cliente muni = cliente(TipoCliente.ENTIDAD_PUBLICA, "Municipalidad Distrital de Ejemplo", "20123456789",
                "Plaza de Armas s/n", "Ing. Luis Paredes", "952 000 111", "obras@muniejemplo.gob.pe");
        Cliente priv = cliente(TipoCliente.CLIENTE_PRIVADO, "Inmobiliaria Los Andes S.A.C.", "20456789012",
                "Av. Bolognesi 450", "Sra. Elena Gutiérrez", "952 000 222", "compras@losandes.pe");

        Requerimiento r1 = req(muni, "Mejoramiento de pistas y veredas del Jr. Los Pinos, cuadras 1 a 4.",
                OrigenRequerimiento.CONVOCATORIA, hoy.minusDays(75), hoy.minusDays(60), ofi);
        Requerimiento r2 = req(priv, "Construcción de cerco perimétrico y almacén metálico en terreno industrial.",
                OrigenRequerimiento.CORREO, hoy.minusDays(20), hoy.plusDays(5), ofi);
        Requerimiento r3 = req(muni, "Mantenimiento de losa deportiva y mobiliario del parque principal.",
                OrigenRequerimiento.CONTACTO_DIRECTO, hoy.minusDays(3), hoy.plusDays(12), ofi);

        Propuesta p1 = propuesta(r1, "148500.00", hoy.minusDays(65), EstadoPropuesta.ACEPTADA, ofi);
        propuesta(r2, "86200.00", hoy.minusDays(12), EstadoPropuesta.PENDIENTE_RESPUESTA, ofi);
        propuesta(r3, "23900.00", null, EstadoPropuesta.EN_PREPARACION, ofi);

        // Obra 1: en ejecucion, con una actividad vencida y otra por vencer
        Obra o1 = new Obra();
        o1.setCodigo("OB-" + hoy.getYear() + "-001");
        o1.setNombre("Mejoramiento de pistas y veredas del Jr. Los Pinos");
        o1.setCliente(muni);
        o1.setPropuesta(p1);
        o1.setOrigen(OrigenObra.PROPUESTA_ACEPTADA);
        o1.setUbicacion("Jr. Los Pinos, cuadras 1 a 4");
        o1.setFechaInicio(hoy.minusDays(40));
        o1.setFechaFin(hoy.plusDays(50));
        o1.setEstado(EstadoObra.EN_EJECUCION);
        o1.setJefe(jefe);
        o1 = obras.save(o1);
        actividad(o1, "Movilización y trazo", sup, hoy.minusDays(40), hoy.minusDays(32), 1, 100);
        actividad(o1, "Movimiento de tierras", sup, hoy.minusDays(31), hoy.minusDays(5), 3, 70);
        Actividad vence = actividad(o1, "Base y sub base granular", sup, hoy.minusDays(20), hoy.plusDays(2), 3, 40);
        actividad(o1, "Pavimento y veredas", jefe, hoy.minusDays(2), hoy.plusDays(35), 5, 10);
        actividad(o1, "Señalización y limpieza final", jefe, hoy.plusDays(36), hoy.plusDays(50), 1, 0);

        Incidencia i1 = new Incidencia();
        i1.setObra(o1);
        i1.setTipo(TipoIncidencia.CLIMATICA);
        i1.setDescripcion("Lluvias intensas paralizaron el vaciado de concreto por dos días.");
        i1.setFecha(hoy.minusDays(6));
        i1.setResponsable(sup);
        i1.setEstado(EstadoIncidencia.ABIERTA);
        i1.setRegistradoPor(sup);
        incidencias.save(i1);
        Incidencia i2 = new Incidencia();
        i2.setObra(o1);
        i2.setTipo(TipoIncidencia.MATERIALES);
        i2.setDescripcion("Retraso en la entrega de material granular por parte del proveedor.");
        i2.setFecha(hoy.minusDays(15));
        i2.setResponsable(jefe);
        i2.setEstado(EstadoIncidencia.RESUELTA);
        i2.setFechaResolucion(hoy.minusDays(12));
        i2.setSolucion("Se coordinó otro proveedor y se recibió el material.");
        i2.setRegistradoPor(jefe);
        incidencias.save(i2);

        // Obra 2: planificada, nacida de una orden de servicio
        Obra o2 = new Obra();
        o2.setCodigo("OB-" + hoy.getYear() + "-002");
        o2.setNombre("Cerco perimétrico y almacén metálico - Terreno industrial");
        o2.setCliente(priv);
        o2.setOrigen(OrigenObra.ORDEN_SERVICIO);
        o2.setNroOrdenContrato("OS-0684");
        o2.setUbicacion("Zona industrial");
        o2.setFechaInicio(hoy.plusDays(7));
        o2.setFechaFin(hoy.plusDays(70));
        o2.setEstado(EstadoObra.PLANIFICADA);
        o2.setJefe(jefe);
        o2 = obras.save(o2);
        actividad(o2, "Cimentación del cerco", sup, hoy.plusDays(7), hoy.plusDays(25), 2, 0);
        actividad(o2, "Montaje de estructura metálica", sup, hoy.plusDays(26), hoy.plusDays(60), 4, 0);

        historia("Obra", o1.getId(), o1.getId(), "CREACION", "Se registró la obra " + o1.getCodigo() + " (datos de ejemplo).");
        historia("Obra", o2.getId(), o2.getId(), "CREACION", "Se registró la obra " + o2.getCodigo() + " (datos de ejemplo).");
        historia("Actividad", vence.getId(), o1.getId(), "AVANCE", "Actividad «Base y sub base granular»: avance a 40 %.");
        LOG.info("Datos de ejemplo cargados (2 obras, 3 propuestas, 2 clientes).");
    }
}
