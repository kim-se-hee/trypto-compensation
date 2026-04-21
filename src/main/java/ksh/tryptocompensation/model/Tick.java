package ksh.tryptocompensation.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Tick(Instant time, BigDecimal price) {
}
