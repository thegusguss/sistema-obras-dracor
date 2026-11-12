package pe.dracor.sgsao.repository;

import pe.dracor.sgsao.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ObraRepository extends JpaRepository<Obra, Long> {

    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    List<Obra> findAllByOrderByIdDesc();
    List<Obra> findByEstadoOrderByIdDesc(EstadoObra estado);
    long countByEstado(EstadoObra estado);
    boolean existsByPropuestaId(Long propuestaId);
}
