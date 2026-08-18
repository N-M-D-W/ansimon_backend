package com.nmdw.ansimon.risk.infra;

import com.nmdw.ansimon.risk.domain.RiskLevel;
import com.nmdw.ansimon.risk.domain.RiskSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RiskSnapshotRepositoryTest {

    @Autowired
    private RiskSnapshotRepository riskSnapshotRepository;

    @Test
    void findsTheMostRecentSnapshotForARegion() {
        Instant now = Instant.now();
        riskSnapshotRepository.saveAndFlush(snapshot("11110", now.minus(2, ChronoUnit.HOURS)));
        RiskSnapshot latest = riskSnapshotRepository.saveAndFlush(snapshot("11110", now));
        riskSnapshotRepository.saveAndFlush(snapshot("11440", now));

        RiskSnapshot found = riskSnapshotRepository.findTopByRegionCodeOrderByGeneratedAtDesc("11110").orElseThrow();

        assertThat(found.getId()).isEqualTo(latest.getId());
    }

    @Test
    void returnsEmptyWhenNoSnapshotExistsForTheRegion() {
        assertThat(riskSnapshotRepository.findTopByRegionCodeOrderByGeneratedAtDesc("99999")).isEmpty();
    }

    private RiskSnapshot snapshot(String regionCode, Instant generatedAt) {
        return RiskSnapshot.builder()
                .regionCode(regionCode)
                .riskScore(new BigDecimal("0.8000"))
                .riskLevel(RiskLevel.HIGH)
                .targetStartAt(generatedAt)
                .targetEndAt(generatedAt.plus(3, ChronoUnit.HOURS))
                .modelVersion("test-model-v1")
                .generatedAt(generatedAt)
                .build();
    }
}
