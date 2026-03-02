package com.streamverse.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/creator")
@CrossOrigin("*")
public class UploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ─────────────────────────────
    // UPLOAD MEDIA
    // ─────────────────────────────
    @PostMapping("/upload/media")
    public ResponseEntity<?> uploadMedia(@RequestParam("file") MultipartFile file) {

        try {

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "File is empty"));
            }

            File folder = new File(uploadDir);
            if (!folder.exists()) folder.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension = "";

            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID() + extension;

            File destination = new File(folder, fileName);
            file.transferTo(destination);

            String mediaUrl = "/apimedia/" + fileName;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "url", mediaUrl
            ));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Upload failed"));
        }
    }

    // ─────────────────────────────
    // DELETE MEDIA
    // ─────────────────────────────
    @DeleteMapping("/upload/media")
    public ResponseEntity<?> deleteMedia(@RequestParam("fileName") String fileName) {

        try {

            if (fileName == null || fileName.contains("..")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid file name"));
            }

            File file = new File(uploadDir, fileName);

            if (!file.exists()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "File not found"));
            }

            boolean deleted = file.delete();

            if (!deleted) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("message", "Could not delete file"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "File deleted successfully"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Delete failed"));
        }
    }
}