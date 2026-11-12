package pe.dracor.sgsao.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.dracor.sgsao.domain.Actividad;
import pe.dracor.sgsao.repository.ActividadRepository;

/** RF-16: actividades vencidas y por vencer (RN-12 y RN-13). */
@Service
public class AlertaService {

    /** Actividad con los dias de atraso (vencidas) o los dias que faltan (por vencer). */
    public static class AlertaActividad {
        private final Actividad actividad;
        private final long dias;

        public AlertaActividad(Actividad actividad, long dias) {
            this.actividad = actividad;
            this.dias = dias;
        }

        public Actividad getActividad() {
            return actividad;
        }

        public long getDias() {
            return dias;
        }
    }

    private final ActividadRepository actividades;
    private final int diasPrevios;

    public AlertaService(ActividadRepository actividades, @Value("${dracor.alertas.dias-previos:3}") int diasPrevios) {
        this.actividades = actividades;
        this.diasPrevios = diasPrevios;
    }

    public int getDiasPrevios() {
        return diasPrevios;
    }

    @Transactional(readOnly = true)
    public List<AlertaActividad> vencidas() {
        LocalDate hoy = LocalDate.now();
        List<AlertaActividad> lista = new ArrayList<>();
        for (Actividad a : actividades.findVencidas(hoy)) {
            lista.add(new AlertaActividad(a, ChronoUnit.DAYS.between(a.getFechaFin(), hoy)));
        }
        return lista;
    }

    @Transactional(readOnly = true)
    public List<AlertaActividad> porVencer() {
        LocalDate hoy = LocalDate.now();
        List<AlertaActividad> lista = new ArrayList<>();
        for (Actividad a : actividades.findPorVencer(hoy, hoy.plusDays(diasPrevios))) {
            lista.add(new AlertaActividad(a, ChronoUnit.DAYS.between(hoy, a.getFechaFin())));
        }
        return lista;
    }
}
