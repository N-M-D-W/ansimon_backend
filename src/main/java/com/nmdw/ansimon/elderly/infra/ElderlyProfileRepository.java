package com.nmdw.ansimon.elderly.infra;

import com.nmdw.ansimon.elderly.domain.ElderlyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElderlyProfileRepository extends JpaRepository<ElderlyProfile, Long> {
}
