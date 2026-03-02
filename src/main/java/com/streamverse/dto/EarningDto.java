package com.streamverse.dto;
import com.streamverse.entity.Earning;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EarningDto {
    private Long id;
    private Long contentId;
    private String contentTitle;
    private Double amount;
    private Earning.EarningType type;
    private LocalDateTime earnedAt;
    public static EarningDto from(Earning e) {
        return EarningDto.builder()
            .id(e.getId())
            .contentId(e.getContent() != null ? e.getContent().getId() : null)
            .contentTitle(e.getContent() != null ? e.getContent().getTitle() : null)
            .amount(e.getAmount()).type(e.getType()).earnedAt(e.getEarnedAt()).build();
    }
}
