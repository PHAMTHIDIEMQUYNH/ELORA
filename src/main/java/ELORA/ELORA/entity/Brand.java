package ELORA.ELORA.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "brands")
@Data
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    @Column(name = "logo_url")
    private String logoUrl;
    @Column(name = "is_active")
    private Boolean isActive = true;
}