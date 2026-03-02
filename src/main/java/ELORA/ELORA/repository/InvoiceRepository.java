package ELORA.ELORA.repository;

import ELORA.ELORA.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Optional<Invoice> findByOrderId(Integer orderId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}