package ksh.tryptocompensation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "holding",
    uniqueConstraints = @UniqueConstraint(columnNames = {"wallet_id", "coin_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoldingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "coin_id", nullable = false)
    private Long coinId;

    @Column(name = "avg_buy_price", nullable = false, precision = 30, scale = 8)
    private BigDecimal avgBuyPrice;

    @Column(name = "total_quantity", nullable = false, precision = 30, scale = 8)
    private BigDecimal totalQuantity;

    @Column(name = "total_buy_amount", nullable = false, precision = 30, scale = 8)
    private BigDecimal totalBuyAmount;

    @Column(name = "averaging_down_count", nullable = false)
    private int averagingDownCount;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public HoldingJpaEntity(Long walletId, Long coinId) {
        this.walletId = walletId;
        this.coinId = coinId;
        this.avgBuyPrice = BigDecimal.ZERO;
        this.totalQuantity = BigDecimal.ZERO;
        this.totalBuyAmount = BigDecimal.ZERO;
        this.averagingDownCount = 0;
    }

    public void update(BigDecimal avgBuyPrice, BigDecimal totalQuantity,
                       BigDecimal totalBuyAmount, int averagingDownCount) {
        this.avgBuyPrice = avgBuyPrice;
        this.totalQuantity = totalQuantity;
        this.totalBuyAmount = totalBuyAmount;
        this.averagingDownCount = averagingDownCount;
    }
}
