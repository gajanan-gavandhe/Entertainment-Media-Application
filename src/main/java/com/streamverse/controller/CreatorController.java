package com.streamverse.controller;

import com.streamverse.dto.*;
import com.streamverse.entity.User;
import com.streamverse.repository.EarningRepository;
import com.streamverse.repository.ViewHistoryRepository;
import com.streamverse.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/creator")
@RequiredArgsConstructor
public class CreatorController {

    private final ContentService contentService;
    private final UserService userService;
    private final StatsService statsService;
    private final EarningRepository earningRepository;
    private final FileStorageService fileStorageService;
    private final ViewHistoryRepository viewHistoryRepository;

    private User currentUser(UserDetails ud) {
        return userService.getByEmail(ud.getUsername());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getCreatorStats(currentUser(ud))));
    }

    @GetMapping("/content")
    public ResponseEntity<?> myContent(
        @AuthenticationPrincipal UserDetails ud,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contentService.getCreatorContent(currentUser(ud), page, size)));
    }

    @PostMapping("/content")
    public ResponseEntity<?> create(@RequestBody ContentRequest req,
                                    @AuthenticationPrincipal UserDetails ud) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(contentService.createContent(req, currentUser(ud))));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/content/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody ContentRequest req,
                                    @AuthenticationPrincipal UserDetails ud) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(contentService.updateContent(id, req, currentUser(ud))));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails ud) {
        try {
            contentService.deleteContent(id, currentUser(ud));
            return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

   

    @GetMapping("/earnings")
    public ResponseEntity<?> earnings(@AuthenticationPrincipal UserDetails ud,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "50") int size) {
        User user = currentUser(ud);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "earnedAt"));
        var result = earningRepository.findByCreatorOrderByEarnedAtDesc(user, pageable);
        var list = result.getContent().stream().map(EarningDto::from).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
