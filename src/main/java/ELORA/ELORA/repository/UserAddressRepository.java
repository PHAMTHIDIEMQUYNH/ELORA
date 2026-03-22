package ELORA.ELORA.repository;

import ELORA.ELORA.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {
    List<UserAddress> findByUserId(Integer userId);
    List<UserAddress> findByUserIdAndIsDefaultTrue(Integer userId);
}