package pe.dracor.sgsao.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "avances_actividad")
public class AvanceActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "actividad_id")
    private Actividad actividad;

    private int porcentaje;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 800)
    private String observacion;

    @Column(length = 500)
    private String motivoRetroceso;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public AvanceActividad() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(int porcentaje) {
        this.porcentaje = porcentaje;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getMotivoRetroceso() {
        return motivoRetroceso;
    }

    public void setMotivoRetroceso(String motivoRetroceso) {
        this.motivoRetroceso = motivoRetroceso;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

}
