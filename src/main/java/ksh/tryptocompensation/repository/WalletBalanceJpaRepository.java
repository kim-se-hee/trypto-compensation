package ksh.tryptocompensation.repository;

import ksh.tryptocompensation.entity.WalletBalanceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface WalletBalanceJpaRepository extends JpaRepository<WalletBalanceJpaEntity, Long> {

    @Modifying
    @Query("update WalletBalanceJpaEntity wb set wb.locked = wb.locked - :amount " +
        "where wb.walletId = :walletId and wb.coinId = :coinId")
    int decreaseLocked(@Param("walletId") Long walletId,
                       @Param("coinId") Long coinId,
                       @Param("amount") BigDecimal amount);
}
