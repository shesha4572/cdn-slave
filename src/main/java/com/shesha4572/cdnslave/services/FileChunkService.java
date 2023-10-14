package com.shesha4572.cdnslave.services;

import com.shesha4572.cdnslave.entities.FileChunk;
import com.shesha4572.cdnslave.repositories.FileChunkDAOImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

}
