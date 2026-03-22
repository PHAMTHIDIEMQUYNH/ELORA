package ELORA.ELORA.repository;

import ELORA.ELORA.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    List<Brand> findByIsActiveTrue();
}