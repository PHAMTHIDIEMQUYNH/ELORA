package ELORA.ELORA.repository;

import ELORA.ELORA.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Integer productId);
}