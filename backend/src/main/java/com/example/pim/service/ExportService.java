package com.example.pim.service;

import com.example.pim.domain.ExportTemplate;
import com.example.pim.domain.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ExportService {

    private final AuditLogService auditLogService;
    private final ProductService productService;
    private final ExportTemplateService exportTemplateService;
    private final ObjectMapper objectMapper; // For JSON export

    @Autowired
    public ExportService(AuditLogService auditLogService, ProductService productService, ExportTemplateService exportTemplateService) {
        this.auditLogService = auditLogService;
        this.productService = productService;
        this.exportTemplateService = exportTemplateService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Path exportProducts(ExportTemplate template, List<Product> products) throws IOException {
        Path exportFilePath = Paths.get("exports", template.getName() + "_" + System.currentTimeMillis() + "." + template.getOutputFormat().toLowerCase());
        Files.createDirectories(exportFilePath.getParent());

        try (FileWriter writer = new FileWriter(exportFilePath.toFile())) {
            switch (template.getOutputFormat().toUpperCase()) {
                case "JSON":
                    objectMapper.writeValue(writer, products);
                    break;
                case "CSV":
                    // TODO: Implement CSV export logic (requires more complex mapping)
                    writer.write("id,sku,name,description,status,completenessScore\n");
                    for (Product product : products) {
                        writer.write(String.format("%d,%s,%s,%s,%s,%d\n",
                                product.getId(),
                                product.getSku(),
                                product.getName(),
                                product.getDescription() != null ? product.getDescription().replace(",", ";") : "",
                                product.getStatus(),
                                product.getCompletenessScore()));
                    }
                    break;
                // TODO: Add more formats (XML, etc.)
                default:
                    throw new IllegalArgumentException("Unsupported output format: " + template.getOutputFormat());
            }
            auditLogService.log("EXPORT_COMPLETED", "ExportTemplate", template.getId(), "system");
            return exportFilePath;
        } catch (IOException e) {
            auditLogService.log("EXPORT_FAILED", "ExportTemplate", template.getId(), "system");
            throw new IOException("Failed to write export file: " + e.getMessage(), e);
        } catch (Exception e) {
            auditLogService.log("EXPORT_FAILED", "ExportTemplate", template.getId(), "system");
            throw new RuntimeException("Error during export: " + e.getMessage(), e);
        }
    }

    // This method will be called by a scheduled job or directly by a controller
    public Path exportProductsByTemplateId(Long templateId, int minCompletenessScore) throws IOException {
        ExportTemplate template = exportTemplateService.getExportTemplateById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Export template not found"));

        List<Product> productsToExport = productService.getExportableProducts(minCompletenessScore);

        return exportProducts(template, productsToExport);
    }
}

