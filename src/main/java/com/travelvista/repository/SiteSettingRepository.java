package com.travelvista.repository;

import com.travelvista.model.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, Long> {
    Optional<SiteSetting> findByKey(String key);
}
