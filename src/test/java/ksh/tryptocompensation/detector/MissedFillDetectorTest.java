package ksh.tryptocompensation.detector;

import ksh.tryptocompensation.entity.Side;
import ksh.tryptocompensation.executor.FillExecutor;
import ksh.tryptocompensation.model.PendingOrder;
import ksh.tryptocompensation.model.Tick;
import ksh.tryptocompensation.repository.InfluxTickRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MissedFillDetectorTest {

    @Mock private InfluxTickRepository influxRepo;
    @Mock private FillExecutor fillExecutor;
    @InjectMocks private MissedFillDetector detector;

    @Test
    @DisplayName("BUY 주문: tick 가격 <= 주문가 인 첫 tick 에서 체결된다")
    void buyFillsOnFirstTickAtOrBelowOrderPrice() {
        // given
        PendingOrder order = buyOrder(bd("100"));
        Instant t1 = Instant.parse("2026-04-21T00:00:01Z");
        Instant t2 = Instant.parse("2026-04-21T00:00:02Z");
        Instant t3 = Instant.parse("2026-04-21T00:00:03Z");
        given(influxRepo.findTicks(eq("upbit"), eq("BTC/KRW"), any(), any()))
            .willReturn(List.of(
                new Tick(t1, bd("105")),
                new Tick(t2, bd("99")),
                new Tick(t3, bd("98"))
            ));
        given(fillExecutor.executeFill(order, bd("99"), t2)).willReturn(true);

        // when
        boolean result = detector.tryCompensate(order);

        // then
        assertThat(result).isTrue();
        verify(fillExecutor, times(1)).executeFill(order, bd("99"), t2);
    }

    @Test
    @DisplayName("BUY 주문: 교차하는 tick 이 없으면 false 반환, executeFill 호출 안 함")
    void buyReturnsFalseWhenNoTickCrosses() {
        // given
        PendingOrder order = buyOrder(bd("100"));
        given(influxRepo.findTicks(any(), any(), any(), any()))
            .willReturn(List.of(
                new Tick(Instant.parse("2026-04-21T00:00:01Z"), bd("105")),
                new Tick(Instant.parse("2026-04-21T00:00:02Z"), bd("110"))
            ));

        // when
        boolean result = detector.tryCompensate(order);

        // then
        assertThat(result).isFalse();
        verify(fillExecutor, never()).executeFill(any(), any(), any());
    }

    @Test
    @DisplayName("SELL 주문: tick 가격 >= 주문가 인 첫 tick 에서 체결된다")
    void sellFillsOnFirstTickAtOrAboveOrderPrice() {
        // given
        PendingOrder order = sellOrder(bd("100"));
        Instant t1 = Instant.parse("2026-04-21T00:00:01Z");
        Instant t2 = Instant.parse("2026-04-21T00:00:02Z");
        given(influxRepo.findTicks(any(), any(), any(), any()))
            .willReturn(List.of(
                new Tick(t1, bd("95")),
                new Tick(t2, bd("101"))
            ));
        given(fillExecutor.executeFill(order, bd("101"), t2)).willReturn(true);

        // when
        boolean result = detector.tryCompensate(order);

        // then
        assertThat(result).isTrue();
        verify(fillExecutor).executeFill(order, bd("101"), t2);
    }

    @Test
    @DisplayName("첫 매칭 tick 이후 루프를 멈춘다: executeFill 은 한 번만 호출된다")
    void stopsAtFirstMatchingTick() {
        // given
        PendingOrder order = buyOrder(bd("100"));
        Instant t1 = Instant.parse("2026-04-21T00:00:01Z");
        Instant t2 = Instant.parse("2026-04-21T00:00:02Z");
        given(influxRepo.findTicks(any(), any(), any(), any()))
            .willReturn(List.of(
                new Tick(t1, bd("95")),
                new Tick(t2, bd("90"))
            ));
        given(fillExecutor.executeFill(order, bd("95"), t1)).willReturn(true);

        // when
        detector.tryCompensate(order);

        // then
        verify(fillExecutor, times(1)).executeFill(any(), any(), any());
        verify(fillExecutor).executeFill(order, bd("95"), t1);
    }

    private PendingOrder buyOrder(BigDecimal price) {
        return new PendingOrder(
            1L, 1L, 1L, Side.BUY, 1L, "upbit", 1L, 2L, "BTC",
            price, bd("1"), bd("100"), 2L,
            LocalDateTime.of(2026, 4, 21, 0, 0, 0));
    }

    private PendingOrder sellOrder(BigDecimal price) {
        return new PendingOrder(
            1L, 1L, 1L, Side.SELL, 1L, "upbit", 1L, 2L, "BTC",
            price, bd("1"), bd("1"), 1L,
            LocalDateTime.of(2026, 4, 21, 0, 0, 0));
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
