package ksh.tryptocompensation.holding;

import ksh.tryptocompensation.entity.HoldingJpaEntity;
import ksh.tryptocompensation.entity.OrderJpaEntity;
import ksh.tryptocompensation.entity.OrderStatus;
import ksh.tryptocompensation.entity.Side;
import ksh.tryptocompensation.repository.HoldingJpaRepository;
import ksh.tryptocompensation.repository.OrderJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HoldingRecalculatorTest {

    private static final Long WALLET_ID = 1L;
    private static final Long COIN_ID = 10L;

    @Mock private OrderJpaRepository orderJpaRepository;
    @Mock private HoldingJpaRepository holdingJpaRepository;
    @InjectMocks private HoldingRecalculator recalculator;

    @Test
    @DisplayName("BUY 단건: avg=체결가, qty=수량, totalBuy=가*수량, averagingDownCount=0")
    void singleBuy() {
        OrderJpaEntity buy = fill(Side.BUY, bd("100"), bd("2"));
        givenFills(List.of(buy));
        givenNoExistingHolding();

        recalculator.recalculate(WALLET_ID, COIN_ID);

        HoldingJpaEntity saved = capture();
        assertThat(saved.getAvgBuyPrice()).isEqualByComparingTo("100");
        assertThat(saved.getTotalQuantity()).isEqualByComparingTo("2");
        assertThat(saved.getTotalBuyAmount()).isEqualByComparingTo("200");
        assertThat(saved.getAveragingDownCount()).isZero();
    }

    @Test
    @DisplayName("BUY 후 더 낮은 가격으로 BUY: 평단 하락, averagingDownCount 1 증가")
    void averagingDown() {
        OrderJpaEntity b1 = fill(Side.BUY, bd("100"), bd("2"));
        OrderJpaEntity b2 = fill(Side.BUY, bd("80"), bd("2"));
        givenFills(List.of(b1, b2));
        givenNoExistingHolding();

        recalculator.recalculate(WALLET_ID, COIN_ID);

        HoldingJpaEntity saved = capture();
        assertThat(saved.getAvgBuyPrice()).isEqualByComparingTo("90");
        assertThat(saved.getTotalQuantity()).isEqualByComparingTo("4");
        assertThat(saved.getTotalBuyAmount()).isEqualByComparingTo("360");
        assertThat(saved.getAveragingDownCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("BUY 후 더 높은 가격으로 BUY: 평단 상승, averagingDownCount 유지")
    void averagingUp() {
        OrderJpaEntity b1 = fill(Side.BUY, bd("100"), bd("2"));
        OrderJpaEntity b2 = fill(Side.BUY, bd("120"), bd("2"));
        givenFills(List.of(b1, b2));
        givenNoExistingHolding();

        recalculator.recalculate(WALLET_ID, COIN_ID);

        HoldingJpaEntity saved = capture();
        assertThat(saved.getAvgBuyPrice()).isEqualByComparingTo("110");
        assertThat(saved.getAveragingDownCount()).isZero();
    }

    @Test
    @DisplayName("BUY 후 SELL: qty 만 감소, avg 와 totalBuy 는 유지")
    void sellReducesQuantityOnly() {
        OrderJpaEntity buy = fill(Side.BUY, bd("100"), bd("5"));
        OrderJpaEntity sell = fill(Side.SELL, bd("150"), bd("2"));
        givenFills(List.of(buy, sell));
        givenNoExistingHolding();

        recalculator.recalculate(WALLET_ID, COIN_ID);

        HoldingJpaEntity saved = capture();
        assertThat(saved.getAvgBuyPrice()).isEqualByComparingTo("100");
        assertThat(saved.getTotalQuantity()).isEqualByComparingTo("3");
        assertThat(saved.getTotalBuyAmount()).isEqualByComparingTo("500");
        assertThat(saved.getAveragingDownCount()).isZero();
    }

    @Test
    @DisplayName("기존 holding 이 있으면 새로 생성하지 않고 해당 엔티티를 update 해서 저장한다")
    void updatesExistingHolding() {
        HoldingJpaEntity existing = new HoldingJpaEntity(WALLET_ID, COIN_ID);
        OrderJpaEntity buy = fill(Side.BUY, bd("100"), bd("1"));
        givenFills(List.of(buy));
        given(holdingJpaRepository.findByWalletIdAndCoinId(WALLET_ID, COIN_ID))
            .willReturn(Optional.of(existing));

        recalculator.recalculate(WALLET_ID, COIN_ID);

        HoldingJpaEntity saved = capture();
        assertThat(saved).isSameAs(existing);
        assertThat(existing.getAvgBuyPrice()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("기존 holding 이 없으면 walletId/coinId 를 가진 새 엔티티가 저장된다")
    void createsNewHoldingWhenAbsent() {
        OrderJpaEntity buy = fill(Side.BUY, bd("100"), bd("1"));
        givenFills(List.of(buy));
        givenNoExistingHolding();

        recalculator.recalculate(WALLET_ID, COIN_ID);

        HoldingJpaEntity saved = capture();
        assertThat(saved.getWalletId()).isEqualTo(WALLET_ID);
        assertThat(saved.getCoinId()).isEqualTo(COIN_ID);
    }

    private void givenFills(List<OrderJpaEntity> fills) {
        given(orderJpaRepository
            .findByWalletIdAndCoinIdAndStatusOrderByFilledAtAscIdAsc(WALLET_ID, COIN_ID, OrderStatus.FILLED))
            .willReturn(fills);
    }

    private void givenNoExistingHolding() {
        given(holdingJpaRepository.findByWalletIdAndCoinId(WALLET_ID, COIN_ID))
            .willReturn(Optional.empty());
    }

    private HoldingJpaEntity capture() {
        ArgumentCaptor<HoldingJpaEntity> captor = ArgumentCaptor.forClass(HoldingJpaEntity.class);
        verify(holdingJpaRepository).save(captor.capture());
        return captor.getValue();
    }

    private OrderJpaEntity fill(Side side, BigDecimal price, BigDecimal qty) {
        OrderJpaEntity m = mock(OrderJpaEntity.class);
        given(m.getSide()).willReturn(side);
        given(m.getFilledPrice()).willReturn(price);
        given(m.getQuantity()).willReturn(qty);
        return m;
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
