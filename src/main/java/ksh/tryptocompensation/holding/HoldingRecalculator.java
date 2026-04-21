package ksh.tryptocompensation.holding;

import ksh.tryptocompensation.entity.HoldingJpaEntity;
import ksh.tryptocompensation.entity.OrderJpaEntity;
import ksh.tryptocompensation.entity.OrderStatus;
import ksh.tryptocompensation.entity.Side;
import ksh.tryptocompensation.repository.HoldingJpaRepository;
import ksh.tryptocompensation.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * orders 의 FILLED row 들을 체결시각 오름차순 재생하여 holding 재계산.
 * 엔진의 HoldingRecalculator 와 동일 알고리즘.
 */
@Component
@RequiredArgsConstructor
public class HoldingRecalculator {

    private final OrderJpaRepository orderJpaRepository;
    private final HoldingJpaRepository holdingJpaRepository;

    public void recalculate(Long walletId, Long coinId) {
        List<OrderJpaEntity> fills = orderJpaRepository
            .findByWalletIdAndCoinIdAndStatusOrderByFilledAtAscIdAsc(walletId, coinId, OrderStatus.FILLED);

        BigDecimal qty = BigDecimal.ZERO;
        BigDecimal totalBuy = BigDecimal.ZERO;
        BigDecimal avg = BigDecimal.ZERO;
        int averagingDownCount = 0;
        for (OrderJpaEntity f : fills) {
            BigDecimal p = f.getFilledPrice();
            BigDecimal q = f.getQuantity();
            if (f.getSide() == Side.BUY) {
                BigDecimal newQty = qty.add(q);
                BigDecimal newAvg = qty.signum() == 0
                    ? p
                    : avg.multiply(qty).add(p.multiply(q)).divide(newQty, 8, RoundingMode.HALF_UP);
                if (qty.signum() > 0 && newAvg.compareTo(avg) < 0) {
                    averagingDownCount++;
                }
                totalBuy = totalBuy.add(p.multiply(q));
                qty = newQty;
                avg = newAvg;
            } else {
                qty = qty.subtract(q);
            }
        }

        HoldingJpaEntity holding = holdingJpaRepository.findByWalletIdAndCoinId(walletId, coinId)
            .orElseGet(() -> new HoldingJpaEntity(walletId, coinId));
        holding.update(avg, qty, totalBuy, averagingDownCount);
        holdingJpaRepository.save(holding);
    }
}
