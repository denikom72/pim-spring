package com.example.pim.repository;

import com.example.pim.domain.Channel;
import com.example.pim.domain.ExportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportTemplateRepository extends JpaRepository<ExportTemplate, Long> {
    List<ExportTemplate> findByChannel(Channel channel);
}

