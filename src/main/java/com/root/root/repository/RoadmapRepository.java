package com.root.root.repository;

import com.root.root.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    @Query("SELECT DISTINCT r FROM Roadmap r " + "JOIN FETCH r.phases " + "WHERE r.user.id = :userId")
    Optional<Roadmap> findByUserId(@Param("userId") Long userId);
}
