package com.codereview.platform.controller;

import com.codereview.platform.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file")MultipartFile file,
            @RequestParam("path") String path
            ){
        String storedPath = fileStorageService.uploadFile(path, file);
        return ResponseEntity.ok("File uploaded to: "+storedPath);
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestParam("path")  String path
    ){
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+path+"\"")
                .body(new InputStreamResource(fileStorageService.downloadFile(path)));
    }
}
