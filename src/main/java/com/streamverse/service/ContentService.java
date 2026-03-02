package com.streamverse.service;

import com.streamverse.dto.*;
import com.streamverse.entity.*;
import com.streamverse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final EarningRepository earningRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public PageResponse<ContentDto> browse(String type, String genre, String q, String sort, int page, int size) {
        Sort sorting = switch (sort != null ? sort : "trending") {
            case "newest"  -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "rating"  -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "name"    -> Sort.by(Sort.Direction.ASC, "title");
            default        -> Sort.by(Sort.Direction.DESC, "viewCount");
        };
        Pageable pageable = PageRequest.of(page, size, sorting);
        Content.ContentType typeEnum = type != null && !type.isBlank()
            ? Content.ContentType.valueOf(type) : null;
        Page<Content> result = contentRepository.findPublishedWithFilters(typeEnum, genre, q, pageable);
        Page<ContentDto> mapped = result.map(ContentDto::from);
        return PageResponse.from(mapped);
    }

    public ContentDto getById(Long id) {
        Content c = contentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Content not found"));
        return ContentDto.from(c);
    }

    public List<ContentDto> getTrending(int size) {
        return contentRepository.findTop12ByStatusOrderByViewCountDesc(Content.ContentStatus.PUBLISHED)
            .stream().limit(size).map(ContentDto::from).collect(Collectors.toList());
    }

    @Transactional
    public void recordView(Long contentId, Long userId) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new RuntimeException("Content not found"));
        content.setViewCount(content.getViewCount() + 1);
        contentRepository.save(content);

        // Record view history
        userRepository.findById(userId).ifPresent(user -> {
            ViewHistory vh = ViewHistory.builder().user(user).content(content).build();
            viewHistoryRepository.save(vh);
        });

        // Record earning for creator
        if (content.getCreator() != null && content.getMonetizationType() != Content.MonetizationType.FREE) {
            double earn = 0.01;
            Earning earning = Earning.builder()
                .creator(content.getCreator())
                .content(content)
                .amount(earn)
                .type(Earning.EarningType.VIEW)
                .build();
            earningRepository.save(earning);
            content.setTotalEarnings(content.getTotalEarnings() + earn);
            contentRepository.save(content);
        }

        // Broadcast update
        broadcastContentStats(content);
    }

    @Transactional
    public void recordPlay(Long contentId) {
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new RuntimeException("Content not found"));
        content.setPlayCount(content.getPlayCount() + 1);
        contentRepository.save(content);
        broadcastContentStats(content);
    }

    private void broadcastContentStats(Content c) {
        try {
            ContentStatsWsDto dto = ContentStatsWsDto.builder()
                .contentId(c.getId()).viewCount(c.getViewCount())
                .playCount(c.getPlayCount()).averageRating(c.getAverageRating()).build();
            messagingTemplate.convertAndSend("/topic/content/" + c.getId(), dto);
        } catch (Exception ignored) {}
    }

    // CREATOR methods
    public PageResponse<ContentDto> getCreatorContent(User creator, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Content> result = contentRepository.findByCreator(creator, pageable);
        return PageResponse.from(result.map(ContentDto::from));
    }

    @Transactional
    public ContentDto createContent(ContentRequest req, User creator) {
        Content c = Content.builder()
            .title(req.getTitle())
            .description(req.getDescription())
            .type(req.getType() != null ? req.getType() : Content.ContentType.MOVIE)
            .genre(req.getGenre())
            .mediaUrl(req.getMediaUrl())
            .ageRating(req.getAgeRating())
            .duration(req.getDuration())
            .castMembers(req.getCastMembers())
            .releaseYear(req.getReleaseYear())
            .status(req.getStatus() != null ? req.getStatus() : Content.ContentStatus.PENDING_REVIEW)
            .monetizationType(req.getMonetizationType() != null ? req.getMonetizationType() : Content.MonetizationType.FREE)
            .price(req.getPrice())
            .creator(creator)
            .viewCount(0L).playCount(0L).averageRating(0.0).totalEarnings(0.0)
            .build();
        return ContentDto.from(contentRepository.save(c));
    }

    @Transactional
    public ContentDto updateContent(Long id, ContentRequest req, User requester) {
        Content c = contentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Content not found"));
        // Only creator or admin can update
        if (!c.getCreator().getId().equals(requester.getId()) && requester.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        if (req.getTitle() != null) c.setTitle(req.getTitle());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getType() != null) c.setType(req.getType());
        if (req.getGenre() != null) c.setGenre(req.getGenre());
        if (req.getMediaUrl() != null) c.setMediaUrl(req.getMediaUrl());
        if (req.getMonetizationType() != null) c.setMonetizationType(req.getMonetizationType());
        if (req.getPrice() != null) c.setPrice(req.getPrice());
        return ContentDto.from(contentRepository.save(c));
    }

    @Transactional
    public void deleteContent(Long id, User requester) {
        Content c = contentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Content not found"));
        if (!c.getCreator().getId().equals(requester.getId()) && requester.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        contentRepository.delete(c);
    }

    // ADMIN methods
    public PageResponse<ContentDto> adminGetAll(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Content.ContentStatus statusEnum = status != null && !status.isBlank()
            ? Content.ContentStatus.valueOf(status) : null;
        Page<Content> result = contentRepository.findAllWithStatusFilter(statusEnum, pageable);
        return PageResponse.from(result.map(ContentDto::from));
    }

    @Transactional
    public ContentDto adminSetStatus(Long id, Content.ContentStatus status) {
        Content c = contentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Content not found"));
        c.setStatus(status);
        return ContentDto.from(contentRepository.save(c));
    }

    @Transactional
    public void adminDeleteById(Long id) {
        contentRepository.deleteById(id);
    }
}
