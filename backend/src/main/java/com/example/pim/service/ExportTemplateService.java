package com.example.pim.service;

import com.example.pim.domain.ExportTemplate;
import com.example.pim.repository.ExportTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExportTemplateService {

    private final ExportTemplateRepository exportTemplateRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public ExportTemplateService(ExportTemplateRepository exportTemplateRepository, AuditLogService auditLogService) {
        this.exportTemplateRepository = exportTemplateRepository;
        this.auditLogService = auditLogService;
    }

    public ExportTemplate createExportTemplate(ExportTemplate exportTemplate) {
        // TODO: Add validation for template syntax
        ExportTemplate createdTemplate = exportTemplateRepository.save(exportTemplate);
        auditLogService.log("CREATE", "ExportTemplate", createdTemplate.getId(), "system");
        return createdTemplate;
    }

    public Optional<ExportTemplate> getExportTemplateById(Long id) {
        return exportTemplateRepository.findById(id);
    }

    public List<ExportTemplate> getAllExportTemplates() {
        return exportTemplateRepository.findAll();
    }

    public void deleteExportTemplate(Long id) {
        exportTemplateRepository.deleteById(id);
        auditLogService.log("DELETE", "ExportTemplate", id, "system");
    }
}
