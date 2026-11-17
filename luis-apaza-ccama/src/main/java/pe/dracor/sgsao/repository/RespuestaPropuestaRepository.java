package pe.dracor.sgsao.repository;

import pe.dracor.sgsao.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RespuestaPropuestaRepository extends JpaRepository<RespuestaPropuesta, Long> {

    List<RespuestaPropuesta> findByPropuestaIdOrderByFechaDescIdDesc(Long propuestaId);
}
