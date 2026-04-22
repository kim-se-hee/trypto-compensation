package ksh.tryptocompensation.repository;

import ksh.tryptocompensation.entity.OrderJpaEntity;
import ksh.tryptocompensation.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository
    extends JpaRepository<OrderJpaEntity, Long>, OrderQueryRepository {

    List<OrderJpaEntity> findByWalletIdAndCoinIdAndStatusOrderByFilledAtAscIdAsc(
        Long walletId, Long coinId, OrderStatus status);
}
