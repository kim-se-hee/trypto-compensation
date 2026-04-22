package ksh.tryptocompensation.scheduler;

import ksh.tryptocompensation.detector.MissedFillDetector;
import ksh.tryptocompensation.model.PendingOrder;
import ksh.tryptocompensation.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanOrderCompensationScheduler {

    private final OrderJpaRepository orderJpaRepository;
    private final MissedFillDetector detector;

    @Value("${compensation.boundary-seconds}")
    private int boundarySeconds;

    @Scheduled(fixedDelayString = "${compensation.interval-ms}")
    public void scan() {
        List<PendingOrder> pending = orderJpaRepository.findBoundaryPending(boundarySeconds);
        if (pending.isEmpty()) return;
        log.info("compensation scan pending={}", pending.size());
        int filled = 0;
        for (PendingOrder o : pending) {
            if (detector.tryCompensate(o)) filled++;
        }
        if (filled > 0) log.info("compensation filled {} of {}", filled, pending.size());
    }
}
