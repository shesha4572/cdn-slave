package com.shesha4572.cdnslave.controllers;

import com.shesha4572.cdnslave.entities.FileChunk;
import com.shesha4572.cdnslave.services.FileChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/slave/chunk")
public class FileChunkController {
    private final FileChunkService fileChunkService;

    @PostMapping(value = "/upload" , consumes = "multipart/form-data")
    public ResponseEntity<?> uploadChunk(@RequestParam("file") MultipartFile file ,
                                         @RequestHeader(value = "fileChunkId") String fileChunkId ,
                                         @RequestHeader(value = "fileChunkIndex") String fileChunkIndex,
                                         @RequestHeader(value = "fileChunkReplicationNum") int replicationNum){

        FileChunk fileChunk = FileChunk.builder()
                .fileChunkId(fileChunkId)
                .fileChunkIndex(Integer.parseInt(fileChunkIndex))
                .replicationNo(replicationNum)
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

    @GetMapping(value = "/get" , produces = "application/vnd.fileChunk")
    public ResponseEntity<?> getChunk(@RequestParam("fileChunkId") String fileChunkId){
        Resource chunk;
        try {
            chunk = fileChunkService.downloadFileChunk(fileChunkId);
        }
        catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION , "attachment; fileChunkId=\"" + chunk.getFilename() + "\"")
                .body(chunk);
    }
}
