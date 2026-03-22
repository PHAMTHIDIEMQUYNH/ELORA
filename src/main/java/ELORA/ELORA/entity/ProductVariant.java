package ELORA.ELORA.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference // Ngắt vòng lặp
    private Product product;

    @Column(name = "variant_name")
    private String variantName;

    @Column(name = "variant_value")
    private String variantValue;

    private BigDecimal price;
    private Integer stock;

    @Column(name = "is_active")
    private Boolean isActive = true;
}