package com.streamverse.controller;

import com.streamverse.dto.*;
import com.streamverse.entity.User;
import com.streamverse.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> browse(
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String genre,
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "trending") String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contentService.browse(type, genre, q, sort, page, size)));
    }

    @GetMapping("/trending")
    public ResponseEntity<?> trending(@RequestParam(defaultValue = "12") int size) {
        List<ContentDto> list = contentService.getTrending(size);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(contentService.getById(id)));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<?> recordView(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails ud) {
        User user = userService.getByEmail(ud.getUsername());
        contentService.recordView(id, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("View recorded", null));
    }

    @PostMapping("/{id}/play")
    public ResponseEntity<?> recordPlay(@PathVariable Long id) {
        contentService.recordPlay(id);
        return ResponseEntity.ok(ApiResponse.ok("Play recorded", null));
    }
}
