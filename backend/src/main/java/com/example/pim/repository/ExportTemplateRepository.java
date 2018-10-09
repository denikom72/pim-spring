package com.example.pim.repository;

import com.example.pim.domain.ExportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExportTemplateRepository extends JpaRepository<ExportTemplate, Long> {
}
