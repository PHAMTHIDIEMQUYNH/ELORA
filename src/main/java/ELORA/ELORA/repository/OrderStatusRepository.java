package ELORA.ELORA.repository;

import ELORA.ELORA.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Integer> {
    Optional<OrderStatus> findByName(String name);
}