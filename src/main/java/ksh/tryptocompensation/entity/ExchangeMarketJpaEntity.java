package ksh.tryptocompensation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exchange_market")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeMarketJpaEntity {

    @Id
    @Column(name = "exchange_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;
}
