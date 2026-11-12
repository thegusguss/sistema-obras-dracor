package pe.dracor.sgsao.repository;

import pe.dracor.sgsao.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findByObraIdOrderByFechaInicioAscIdAsc(Long obraId);
    long countByObraId(Long obraId);
    @Query("select a from Actividad a where a.fechaFin < :hoy and a.avance < 100 and a.justificada = false and a.obra.estado in (pe.dracor.sgsao.domain.EstadoObra.PLANIFICADA, pe.dracor.sgsao.domain.EstadoObra.EN_EJECUCION) order by a.fechaFin asc")
    List<Actividad> findVencidas(@Param("hoy") LocalDate hoy);
    @Query("select a from Actividad a where a.fechaFin >= :hoy and a.fechaFin <= :limite and a.avance < 100 and a.obra.estado in (pe.dracor.sgsao.domain.EstadoObra.PLANIFICADA, pe.dracor.sgsao.domain.EstadoObra.EN_EJECUCION) order by a.fechaFin asc")
    List<Actividad> findPorVencer(@Param("hoy") LocalDate hoy, @Param("limite") LocalDate limite);
}
