package com.nmdw.ansimon.risk.infra;

import com.nmdw.ansimon.risk.domain.RiskSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskSnapshotRepository extends JpaRepository<RiskSnapshot, Long> {
}
