package ksh.tryptocompensation.model;

import ksh.tryptocompensation.entity.Side;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PendingOrder(
    Long orderId,
    Long userId,
    Long walletId,
    Side side,
    Long exchangeCoinId,
    String exchangeName,
    Long coinId,
    Long baseCoinId,
    String displayName,
    BigDecimal price,
    BigDecimal quantity,
    BigDecimal lockedAmount,
    Long lockedCoinId,
    LocalDateTime createdAt
) {
}
