package com.eduardosystems.simulacionvolcan.repository;

import com.eduardosystems.simulacionvolcan.entity.RegistroVolcan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolcanRepository extends JpaRepository<RegistroVolcan, Long> {
}