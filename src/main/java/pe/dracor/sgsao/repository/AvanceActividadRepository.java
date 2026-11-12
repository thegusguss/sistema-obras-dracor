package pe.dracor.sgsao.repository;

import pe.dracor.sgsao.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvanceActividadRepository extends JpaRepository<AvanceActividad, Long> {

    List<AvanceActividad> findByActividadIdOrderByFechaRegistroDescIdDesc(Long actividadId);
}
