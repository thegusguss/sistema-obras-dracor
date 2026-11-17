package pe.dracor.sgsao.repository;

import pe.dracor.sgsao.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByObraIdOrderByIdDesc(Long obraId);
    long countByObraIdAndEstado(Long obraId, EstadoIncidencia estado);
    long countByEstado(EstadoIncidencia estado);
}
