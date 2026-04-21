package ksh.tryptocompensation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "exchange_coin_id", nullable = false)
    private Long exchangeCoinId;

    @Column(name = "coin_id", nullable = false)
    private Long coinId;

    @Column(name = "base_coin_id", nullable = false)
    private Long baseCoinId;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 4)
    private Side side;

    @Column(name = "order_amount", nullable = false, precision = 30, scale = 8)
    private BigDecimal orderAmount;

    @Column(name = "quantity", nullable = false, precision = 30, scale = 8)
    private BigDecimal quantity;

    @Column(name = "price", precision = 30, scale = 8)
    private BigDecimal price;

    @Column(name = "filled_price", precision = 30, scale = 8)
    private BigDecimal filledPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "filled_at")
    private LocalDateTime filledAt;
}
