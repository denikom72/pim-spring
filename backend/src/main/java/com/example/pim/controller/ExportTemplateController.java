package com.example.pim.controller;

import com.example.pim.domain.ExportTemplate;
import com.example.pim.service.ExportTemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/export-templates")
public class ExportTemplateController {

    private final ExportTemplateService exportTemplateService;

    @Autowired
    public ExportTemplateController(ExportTemplateService exportTemplateService) {
        this.exportTemplateService = exportTemplateService;
    }

    @PostMapping
    public ResponseEntity<ExportTemplate> createExportTemplate(@Valid @RequestBody ExportTemplate exportTemplate) {
        try {
            ExportTemplate createdTemplate = exportTemplateService.createExportTemplate(exportTemplate);
            return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExportTemplate> getExportTemplateById(@PathVariable Long id) {
        return exportTemplateService.getExportTemplateById(id)
                .map(template -> new ResponseEntity<>(template, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Export template not found"));
    }

    @GetMapping
    public ResponseEntity<List<ExportTemplate>> getAllExportTemplates() {
        List<ExportTemplate> templates = exportTemplateService.getAllExportTemplates();
        return new ResponseEntity<>(templates, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExportTemplate(@PathVariable Long id) {
        exportTemplateService.deleteExportTemplate(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
