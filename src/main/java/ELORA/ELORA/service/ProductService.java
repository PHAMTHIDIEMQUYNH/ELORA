package ELORA.ELORA.service;

import ELORA.ELORA.entity.Product;
import ELORA.ELORA.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // 1. Lấy tất cả sản phẩm đang hoạt động
    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    // 2. Lấy chi tiết sản phẩm theo ID (kèm cả biến thể)
    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + id));
    }

    // 3. Lọc sản phẩm đa điều kiện (Có thêm keyword)
    public List<Product> getFilteredProducts(
            String keyword, // Thêm tham số này
            Integer categoryId,
            Integer brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            String sortBy) {

        // 1. Xử lý logic Sắp xếp (Sort)
        Sort sort = Sort.by(Sort.Direction.DESC, "id"); // Mặc định là mới nhất
        if (sortBy != null) {
            switch (sortBy) {
                case "bestSelling":
                    sort = Sort.by(Sort.Direction.DESC, "soldCount");
                    break;
                case "priceAsc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "priceDesc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
            }
        }

        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;

        // 2. Gọi Repository để lọc
        return productRepository.filterProducts(searchKeyword, categoryId, brandId, minPrice, maxPrice, minRating, sort);
    }
}