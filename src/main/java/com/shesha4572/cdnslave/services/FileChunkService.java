package com.shesha4572.cdnslave.services;

import com.shesha4572.cdnslave.entities.FileChunk;
import com.shesha4572.cdnslave.repositories.FileChunkDAOImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
public class FileChunkService {
    private final FileChunkDAOImpl fileChunkRedis;
    private final Path root;

    @Autowired
    public FileChunkService(FileChunkDAOImpl fileChunkRedis , @Value("${FILE_PATH}") String dir){
        this.fileChunkRedis = fileChunkRedis;
        this.root = Paths.get(dir);
    }
    public void saveFileChunk(MultipartFile chunk , FileChunk fileChunkDetails) throws RuntimeException{
        try {
            Files.copy(chunk.getInputStream(), this.root.resolve(fileChunkDetails.getFileChunkId()));
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }

            throw new RuntimeException(e.getMessage());
        }
        fileChunkDetails.setChunkAddedOn(LocalDateTime.now());
        fileChunkDetails.setChunkLength(chunk.getSize());
        fileChunkDetails.setIsChunkFull(chunk.getSize() == 32 * 1024 * 1024);
        fileChunkRedis.saveFileChunk(fileChunkDetails);

    }

    public Resource downloadFileChunk(String fileChunkId) throws RuntimeException {
        try {
            if(!fileChunkRedis.existsFileChunk(fileChunkId)){
                throw new RuntimeException("Could not read the file!");
            }
            Path file = root.resolve(fileChunkId);
            Resource resource = new UrlResource(file.toUri());

            if (resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

}
