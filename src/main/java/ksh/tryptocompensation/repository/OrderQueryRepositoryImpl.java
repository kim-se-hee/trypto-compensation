package ksh.tryptocompensation.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ksh.tryptocompensation.entity.OrderStatus;
import ksh.tryptocompensation.entity.QExchangeCoinJpaEntity;
import ksh.tryptocompensation.entity.QExchangeMarketJpaEntity;
import ksh.tryptocompensation.entity.QOrderJpaEntity;
import ksh.tryptocompensation.entity.Side;
import ksh.tryptocompensation.model.PendingOrder;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class OrderQueryRepositoryImpl implements OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PendingOrder> findBoundaryPending(int boundarySeconds) {
        QOrderJpaEntity o = QOrderJpaEntity.orderJpaEntity;
        QExchangeCoinJpaEntity ec = QExchangeCoinJpaEntity.exchangeCoinJpaEntity;
        QExchangeMarketJpaEntity em = QExchangeMarketJpaEntity.exchangeMarketJpaEntity;

        NumberExpression<Long> lockedCoinId = new CaseBuilder()
            .when(o.side.eq(Side.BUY)).then(o.baseCoinId)
            .otherwise(o.coinId);

        return queryFactory
            .select(Projections.constructor(PendingOrder.class,
                o.id,
                o.userId,
                o.walletId,
                o.side,
                o.exchangeCoinId,
                em.name,
                o.coinId,
                o.baseCoinId,
                ec.displayName,
                o.price,
                o.quantity,
                o.orderAmount,
                lockedCoinId,
                o.createdAt))
            .from(o)
            .join(ec).on(ec.id.eq(o.exchangeCoinId))
            .join(em).on(em.id.eq(ec.exchangeId))
            .where(
                o.status.eq(OrderStatus.PENDING),
                o.createdAt.lt(LocalDateTime.now().minusSeconds(boundarySeconds)),
                o.price.isNotNull()
            )
            .fetch();
    }

    @Override
    public boolean fillIfPending(Long orderId, BigDecimal filledPrice, LocalDateTime filledAt) {
        QOrderJpaEntity o = QOrderJpaEntity.orderJpaEntity;
        long updated = queryFactory.update(o)
            .set(o.status, OrderStatus.FILLED)
            .set(o.filledPrice, filledPrice)
            .set(o.filledAt, filledAt)
            .where(o.id.eq(orderId).and(o.status.eq(OrderStatus.PENDING)))
            .execute();
        return updated > 0;
    }
}
