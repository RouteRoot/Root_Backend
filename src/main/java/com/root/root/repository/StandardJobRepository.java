package com.root.root.repository;

import com.root.root.entity.standard.StandardJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StandardJobRepository extends JpaRepository<StandardJob, Long> {
    Optional<StandardJob> findByHope(String hope);
}
