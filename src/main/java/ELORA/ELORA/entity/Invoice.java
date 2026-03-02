package ELORA.ELORA.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "pdf_path")
    private String pdfPath;
    @Column(name = "sent_to_email")
    private Boolean sentToEmail = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}