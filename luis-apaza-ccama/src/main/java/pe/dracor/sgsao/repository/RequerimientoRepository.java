package pe.dracor.sgsao.repository;

import pe.dracor.sgsao.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequerimientoRepository extends JpaRepository<Requerimiento, Long> {

    List<Requerimiento> findAllByOrderByFechaRecepcionDescIdDesc();
    List<Requerimiento> findByClienteIdOrderByFechaRecepcionDesc(Long clienteId);
}
