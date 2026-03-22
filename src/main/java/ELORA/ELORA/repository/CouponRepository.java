package ELORA.ELORA.repository;

import ELORA.ELORA.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
}