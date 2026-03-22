package ELORA.ELORA.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_addresses")
@Data
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    private String province;
    private String district;
    private String ward;

    @Column(name = "detailed_address")
    private String detailedAddress;

    @Column(name = "is_default")
    private Boolean isDefault = false;
}