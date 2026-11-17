package pe.dracor.sgsao.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "respuestas_propuesta")
public class RespuestaPropuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "propuesta_id")
    private Propuesta propuesta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRespuesta tipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 1500)
    private String detalle;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public RespuestaPropuesta() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Propuesta getPropuesta() {
        return propuesta;
    }

    public void setPropuesta(Propuesta propuesta) {
        this.propuesta = propuesta;
    }

    public TipoRespuesta getTipo() {
        return tipo;
    }

    public void setTipo(TipoRespuesta tipo) {
        this.tipo = tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

}
