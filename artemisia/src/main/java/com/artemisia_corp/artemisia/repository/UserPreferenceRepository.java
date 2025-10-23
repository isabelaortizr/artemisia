package com.artemisia_corp.artemisia.repository;

import com.artemisia_corp.artemisia.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserId(Long userId);
    Optional<UserPreference> findByUser(com.artemisia_corp.artemisia.entity.User user);

    @Query("SELECT up FROM UserPreference up WHERE up.userCluster = :cluster")
    List<UserPreference> findByUserCluster(@Param("cluster") Integer cluster);

    @Query("SELECT up FROM UserPreference up WHERE up.lastUpdated < :cutoffDate")
    List<UserPreference> findStalePreferences(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
