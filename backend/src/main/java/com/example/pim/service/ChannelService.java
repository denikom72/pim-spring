package com.example.pim.service;

import com.example.pim.domain.Channel;
import com.example.pim.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public ChannelService(ChannelRepository channelRepository, AuditLogService auditLogService) {
        this.channelRepository = channelRepository;
        this.auditLogService = auditLogService;
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
}
