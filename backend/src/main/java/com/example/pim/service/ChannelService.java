package com.example.pim.service;

import com.example.pim.domain.BulkOperation;
import com.example.pim.domain.Channel;
import com.example.pim.domain.ExportTemplate;
import com.example.pim.repository.AuditLogRepository;
import com.example.pim.repository.BulkOperationRepository;
import com.example.pim.repository.ChannelRepository;
import com.example.pim.repository.ExportTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final AuditLogService auditLogService;
    private final ExportTemplateRepository exportTemplateRepository;
    private final BulkOperationRepository bulkOperationRepository;

    @Autowired
    public ChannelService(ChannelRepository channelRepository, AuditLogService auditLogService, ExportTemplateRepository exportTemplateRepository, BulkOperationRepository bulkOperationRepository) {
        this.channelRepository = channelRepository;
        this.auditLogService = auditLogService;
        this.exportTemplateRepository = exportTemplateRepository;
        this.bulkOperationRepository = bulkOperationRepository;
    }

    public Channel createChannel(Channel channel) {
        if (channelRepository.findByCode(channel.getCode()).isPresent()) {
            throw new IllegalArgumentException("Channel with code '" + channel.getCode() + "' already exists.");
        }
        Channel createdChannel = channelRepository.save(channel);
        auditLogService.log("CREATE", "Channel", createdChannel.getId(), "system");
        return createdChannel;
    }

    public Optional<Channel> getChannelById(Long id) {
        return channelRepository.findById(id);
    }

    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    @Transactional
    public Channel updateChannelActivation(Long channelId, boolean isActive) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
        channel.setActive(isActive);
        Channel updatedChannel = channelRepository.save(channel);
        auditLogService.log(isActive ? "ACTIVATE_CHANNEL" : "DEACTIVATE_CHANNEL", "Channel", updatedChannel.getId(), "system");
        return updatedChannel;
    }

    @Transactional
    public void deleteChannel(Long channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        // Check for associated export templates
        List<ExportTemplate> associatedTemplates = exportTemplateRepository.findByChannel(channel);
        if (!associatedTemplates.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete channel '" + channel.getName() + "' as it is associated with " + associatedTemplates.size() + " export template(s).");
        }

        // Check for active exports using this channel (via templates)
        // This is a simplified check; a more robust solution would involve linking BulkOperation directly to Channel or more complex query
        List<BulkOperation> activeExports = bulkOperationRepository.findByOperationTypeAndStatus("PRODUCT_EXPORT", "IN_PROGRESS");
        boolean hasActiveExports = activeExports.stream().anyMatch(op -> {
            // This part is a placeholder, as BulkOperation doesn't directly store channelId yet.
            // A more robust solution would involve parsing errorDetails or having a direct link.
            return op.getErrorDetails() != null && op.getErrorDetails().contains("channel_id: " + channelId);
        });

        if (hasActiveExports) {
            throw new IllegalArgumentException("Cannot delete channel '" + channel.getName() + "' as there are active exports using it.");
        }

        channelRepository.delete(channel);
        auditLogService.log("DELETE", "Channel", channelId, "system");
    }
}
