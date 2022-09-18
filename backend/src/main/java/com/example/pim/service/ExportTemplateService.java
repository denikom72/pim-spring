package com.example.pim.service;

import com.example.pim.domain.ExportTemplate;
import com.example.pim.repository.ExportTemplateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExportTemplateService {

    private final ExportTemplateRepository exportTemplateRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ExportTemplateService(ExportTemplateRepository exportTemplateRepository, AuditLogService auditLogService) {
        this.exportTemplateRepository = exportTemplateRepository;
        this.auditLogService = auditLogService;
    }

    public ExportTemplate createExportTemplate(ExportTemplate exportTemplate) {
        validateTemplateSyntax(exportTemplate);
        ExportTemplate createdTemplate = exportTemplateRepository.save(exportTemplate);
        auditLogService.log("CREATE", "ExportTemplate", createdTemplate.getId(), "system");
        return createdTemplate;
    }

    private void validateTemplateSyntax(ExportTemplate template) {
        if (template.getTemplateSyntax() == null || template.getTemplateSyntax().isEmpty()) {
            throw new IllegalArgumentException("Template syntax cannot be empty.");
        }

        switch (template.getOutputFormat().toUpperCase()) {
            case "JSON":
                try {
                    objectMapper.readTree(template.getTemplateSyntax());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid JSON template syntax: " + e.getMessage());
                }
                break;
            case "CSV":
                // Basic validation: check if it contains at least one comma or a newline, implying a CSV structure
                if (!template.getTemplateSyntax().contains(",") && !template.getTemplateSyntax().contains("\n")) {
                    throw new IllegalArgumentException("Invalid CSV template syntax: Must contain comma-separated values or newlines.");
                }
                break;
            default:
                // For other formats, we might not have a specific validator yet
                System.out.println("No specific syntax validation for format: " + template.getOutputFormat());
                break;
        }
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

