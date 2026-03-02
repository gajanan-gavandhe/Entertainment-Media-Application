package com.streamverse.dto;
import com.streamverse.entity.Content;
import lombok.Data;
@Data
public class ContentRequest {
    private String title;
    private String description;
    private Content.ContentType type;
    private String genre;
    private String mediaUrl;
    private String ageRating;
    private String duration;
    private String castMembers;
    private Integer releaseYear;
    private Content.ContentStatus status;
    private Content.MonetizationType monetizationType;
    private Double price;
}
