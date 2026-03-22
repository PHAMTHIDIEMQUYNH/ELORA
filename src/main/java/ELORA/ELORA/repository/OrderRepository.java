package ELORA.ELORA.repository;

import ELORA.ELORA.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(Integer userId);
}