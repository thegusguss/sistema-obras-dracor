package pe.dracor.sgsao.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "obras")
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "propuesta_id")
    private Propuesta propuesta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrigenObra origen;

    @Column(length = 60)
    private String nroOrdenContrato;

    @Column(length = 200)
    private String ubicacion;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    private LocalDate fechaTermino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoObra estado = EstadoObra.PLANIFICADA;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "jefe_id")
    private Usuario jefe;

    private boolean requiereConformidad = true;

    @Column(length = 1000)
    private String observacionCierre;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Obra() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Propuesta getPropuesta() {
        return propuesta;
    }

    public void setPropuesta(Propuesta propuesta) {
        this.propuesta = propuesta;
    }

    public OrigenObra getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenObra origen) {
        this.origen = origen;
    }

    public String getNroOrdenContrato() {
        return nroOrdenContrato;
    }

    public void setNroOrdenContrato(String nroOrdenContrato) {
        this.nroOrdenContrato = nroOrdenContrato;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaTermino() {
        return fechaTermino;
    }

    public void setFechaTermino(LocalDate fechaTermino) {
        this.fechaTermino = fechaTermino;
    }

    public EstadoObra getEstado() {
        return estado;
    }

    public void setEstado(EstadoObra estado) {
        this.estado = estado;
    }

    public Usuario getJefe() {
        return jefe;
    }

    public void setJefe(Usuario jefe) {
        this.jefe = jefe;
    }

    public boolean isRequiereConformidad() {
        return requiereConformidad;
    }

    public void setRequiereConformidad(boolean requiereConformidad) {
        this.requiereConformidad = requiereConformidad;
    }

    public String getObservacionCierre() {
        return observacionCierre;
    }

    public void setObservacionCierre(String observacionCierre) {
        this.observacionCierre = observacionCierre;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

}
