package com.nmdw.ansimon.contact.infra;

import com.nmdw.ansimon.contact.domain.CallObservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallObservationRepository extends JpaRepository<CallObservation, Long> {
}
