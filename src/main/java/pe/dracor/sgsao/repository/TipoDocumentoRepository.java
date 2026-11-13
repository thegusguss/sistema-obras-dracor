package pe.dracor.sgsao.repository;

import pe.dracor.sgsao.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    List<TipoDocumento> findAllByOrderByNombreAsc();
    List<TipoDocumento> findByActivoTrueOrderByNombreAsc();
    List<TipoDocumento> findByObligatorioTrueAndActivoTrue();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
    Optional<TipoDocumento> findFirstByConformidadTrueAndActivoTrue();
}
