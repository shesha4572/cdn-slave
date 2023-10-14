package com.shesha4572.cdnslave.controllers;

import com.shesha4572.cdnslave.entities.FileChunk;
import com.shesha4572.cdnslave.services.FileChunkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/slave/chunk")
public class FileChunkController {
    private final FileChunkService fileChunkService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadChunk(@RequestParam("file") MultipartFile file ,
                                         @RequestHeader(value = "fileChunkId") String fileChunkId ,
                                         @RequestHeader(value = "fileChunkIndex") String fileChunkIndex) throws Exception{

        FileChunk fileChunk = FileChunk.builder()
                .fileChunkId(fileChunkId)
                .fileChunkIndex(Integer.parseInt(fileChunkIndex))
                .build();

        String message = "";
        try {
            fileChunkService.saveFileChunk(file , fileChunk);

            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.ok().body(message);
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.internalServerError().body(message);
        }

    }
}
