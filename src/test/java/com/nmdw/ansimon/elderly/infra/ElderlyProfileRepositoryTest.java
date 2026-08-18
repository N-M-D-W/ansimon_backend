package com.nmdw.ansimon.elderly.infra;

import com.nmdw.ansimon.elderly.domain.ConsentStatus;
import com.nmdw.ansimon.elderly.domain.ElderlyProfile;
import com.nmdw.ansimon.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class ElderlyProfileRepositoryTest {

    @Autowired
    private ElderlyProfileRepository elderlyProfileRepository;

    @Test
    void savesAndReloadsElderlyProfileWithAuditedTimestamps() {
        ElderlyProfile profile = ElderlyProfile.builder()
                .displayName("김안심")
                .phone("hashed-phone-value")
                .address("서울시 종로구 1-1")
                .latitude(new BigDecimal("37.5730000"))
                .longitude(new BigDecimal("126.9794000"))
                .regionCode("11110")
                .consentStatus(ConsentStatus.CONSENTED)
                .build();

        ElderlyProfile saved = elderlyProfileRepository.saveAndFlush(profile);

        ElderlyProfile found = elderlyProfileRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getDisplayName()).isEqualTo("김안심");
        assertThat(found.getConsentStatus()).isEqualTo(ConsentStatus.CONSENTED);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
