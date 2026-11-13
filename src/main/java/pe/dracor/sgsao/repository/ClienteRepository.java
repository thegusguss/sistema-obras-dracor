package pe.dracor.sgsao.repository;

import pe.dracor.sgsao.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByRuc(String ruc);
    boolean existsByRucAndIdNot(String ruc, Long id);
    List<Cliente> findAllByOrderByRazonSocialAsc();
    List<Cliente> findByActivoTrueOrderByRazonSocialAsc();
    List<Cliente> findByRazonSocialContainingIgnoreCaseOrRucContainingOrderByRazonSocialAsc(String razonSocial, String ruc);
}
