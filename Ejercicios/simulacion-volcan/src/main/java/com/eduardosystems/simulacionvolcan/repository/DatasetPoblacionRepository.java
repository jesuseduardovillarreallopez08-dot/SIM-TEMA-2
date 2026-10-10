package com.eduardosystems.simulacionvolcan.repository;

import com.eduardosystems.simulacionvolcan.entity.DatasetPoblacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetPoblacionRepository extends JpaRepository<DatasetPoblacion, Long> {
}