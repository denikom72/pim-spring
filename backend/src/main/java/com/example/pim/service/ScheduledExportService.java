package com.example.pim.service;

import com.example.pim.domain.ExportTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class ScheduledExportService {

    private final ExportService exportService;
    private final ExportTemplateService exportTemplateService;
    private final NotificationService notificationService;

    @Autowired
    public ScheduledExportService(ExportService exportService, ExportTemplateService exportTemplateService, NotificationService notificationService) {
        this.exportService = exportService;
        this.exportTemplateService = exportTemplateService;
        this.notificationService = notificationService;
    }

    // Example: Schedule an export every day at 2 AM
    // TODO: Make scheduling configurable per template
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyProductExport() {
        System.out.println("Initiating daily product export...");
        List<ExportTemplate> templates = exportTemplateService.getAllExportTemplates();

        for (ExportTemplate template : templates) {
            try {
                // Assuming a default completeness score for scheduled exports, or it could be configured per template
                Path exportedFilePath = exportService.exportProductsByTemplateId(template.getId(), 80);
                notificationService.sendNotification("admin", "Scheduled Export Success",
                        "Successfully completed scheduled export for template '" + template.getName() + "'. File: " + exportedFilePath.getFileName());
            } catch (Exception e) {
                System.err.println("Scheduled export failed for template '" + template.getName() + "': " + e.getMessage());
                notificationService.sendNotification("admin", "Scheduled Export Failed",
                        "Failed to complete scheduled export for template '" + template.getName() + "'. Error: " + e.getMessage());
            }
        }
        System.out.println("Daily product export finished.");
    }
}
