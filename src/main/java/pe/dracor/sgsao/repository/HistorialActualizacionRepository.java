package pe.dracor.sgsao.repository;

import pe.dracor.sgsao.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialActualizacionRepository extends JpaRepository<HistorialActualizacion, Long> {

    List<HistorialActualizacion> findByObraIdOrderByFechaHoraDescIdDesc(Long obraId);
    List<HistorialActualizacion> findTop200ByOrderByFechaHoraDescIdDesc();
    List<HistorialActualizacion> findTop200ByEntidadOrderByFechaHoraDescIdDesc(String entidad);
}
