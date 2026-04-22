package ksh.tryptocompensation.detector;

import ksh.tryptocompensation.entity.Side;
import ksh.tryptocompensation.executor.FillExecutor;
import ksh.tryptocompensation.model.PendingOrder;
import ksh.tryptocompensation.model.Tick;
import ksh.tryptocompensation.repository.InfluxTickRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissedFillDetector {

    private final InfluxTickRepository influxRepo;
    private final FillExecutor fillExecutor;

    public boolean tryCompensate(PendingOrder order) {
        Instant from = order.createdAt().atZone(ZoneId.systemDefault()).toInstant();
        Instant to = Instant.now();

        String symbol = order.displayName() + "/KRW";
        List<Tick> ticks = influxRepo.findTicks(order.exchangeName(), symbol, from, to);
        BigDecimal orderPrice = order.price();
        boolean buy = order.side() == Side.BUY;
        for (Tick t : ticks) {
            if (buy ? t.price().compareTo(orderPrice) <= 0 : t.price().compareTo(orderPrice) >= 0) {
                return fillExecutor.executeFill(order, t.price(), t.time());
            }
        }
        return false;
    }
}
