package ksh.tryptocompensation.repository;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import ksh.tryptocompensation.model.Tick;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class InfluxTickRepository {

    @Value("${influxdb.url}") private String url;
    @Value("${influxdb.token}") private String token;
    @Value("${influxdb.org}") private String organization;
    @Value("${influxdb.bucket}") private String bucket;

    private InfluxDBClient client;

    @PostConstruct
    void init() {
        client = InfluxDBClientFactory.create(url, token.toCharArray(), organization, bucket);
    }

    /**
     * 주어진 거래쌍·거래소에 대해 [start, end) 구간의 tick 을 시간 오름차순으로 조회.
     * price 가 매수 주문이면 <= orderPrice, 매도면 >= orderPrice 인 첫 tick 을 호출자가 필터.
     */
    public List<Tick> findTicks(String exchange, String symbol, Instant start, Instant end) {
        String flux = String.format(
            "from(bucket:\"%s\") " +
                "|> range(start: %s, stop: %s) " +
                "|> filter(fn: (r) => r[\"_measurement\"] == \"ticker_raw\" " +
                "  and r[\"exchange\"] == \"%s\" and r[\"symbol\"] == \"%s\" " +
                "  and r[\"_field\"] == \"price\") " +
                "|> sort(columns: [\"_time\"])",
            bucket, start, end, exchange, symbol
        );
        List<Tick> out = new ArrayList<>();
        List<FluxTable> tables = client.getQueryApi().query(flux, organization);
        for (FluxTable t : tables) {
            for (FluxRecord r : t.getRecords()) {
                Object v = r.getValue();
                if (v == null) continue;
                BigDecimal price = new BigDecimal(v.toString());
                out.add(new Tick(r.getTime(), price));
            }
        }
        return out;
    }

    @PreDestroy
    void close() {
        if (client != null) client.close();
    }
}
