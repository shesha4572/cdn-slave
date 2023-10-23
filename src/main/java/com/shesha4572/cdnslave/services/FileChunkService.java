package com.shesha4572.cdnslave.services;

import com.shesha4572.cdnslave.entities.FileChunk;
import com.shesha4572.cdnslave.repositories.FileChunkRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

@Service
@Slf4j
public class FileChunkService {
    private final FileChunkRedisRepository fileChunkRedis;
    private final Path root;
    private ArrayList<String> newFileChunks;

    private final String masterNodeUrl;

    @Autowired
    public FileChunkService(FileChunkRedisRepository fileChunkRedis , @Value("${FILE_PATH}") String dir , @Value("${MASTER_NODE_URL}") String masterNodeUrl){
        this.fileChunkRedis = fileChunkRedis;
        this.root = Paths.get(dir);
        this.newFileChunks = new ArrayList<>();
        this.masterNodeUrl = masterNodeUrl;
    }
    public void saveFileChunk(MultipartFile chunk , FileChunk fileChunkDetails) throws RuntimeException{
        try {
            if(chunk.getSize() > 64000000){
                throw new RuntimeException("File chunk is too large");
            }
            Files.copy(chunk.getInputStream(), this.root.resolve(fileChunkDetails.getFileChunkId()));
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }

            throw new RuntimeException(e.getMessage());
        }
        fileChunkDetails.setChunkAddedOn(LocalDateTime.now());
        fileChunkDetails.setChunkLength(chunk.getSize());
        fileChunkDetails.setIsChunkFull(chunk.getSize() == 64000000);
        fileChunkRedis.save(fileChunkDetails);
        newFileChunks.add(fileChunkDetails.getFileChunkId());
    }

    public Resource downloadFileChunk(String fileChunkId) throws RuntimeException {
        try {
            if(!fileChunkRedis.existsById(fileChunkId)){
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

    public Resource downloadPartialFileChunk(String fileChunkId , int startIndex , int endIndex) throws RuntimeException{
        try {
            if(!fileChunkRedis.existsById(fileChunkId)){
                throw new RuntimeException("Could not read the file!");
            }
            Path file = root.resolve(fileChunkId);
            Resource resource = new UrlResource(file.toUri());

            if (resource.isReadable()) {
                InputStream completeBytes = resource.getInputStream();
                byte[] requiredBytes = new byte[endIndex - startIndex + 1];
                completeBytes.read(requiredBytes , startIndex , requiredBytes.length);
                return new ByteArrayResource(requiredBytes);
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Scheduled(cron = "*/120 * * * * ?")
    public void sendHeartBeat(){
        if(newFileChunks.isEmpty()){
            return;
        }

        log.info("Syncing with Master Node..");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> map= new LinkedMultiValueMap<>();
        map.add("newChunks" , newFileChunks);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(masterNodeUrl + "/api/v1/heartbeat");
        System.out.println(request.getBody());
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.PUT,
                request,
                String.class);
        System.out.println(response);
        if(response.getStatusCode() == HttpStatusCode.valueOf(200)){
            newFileChunks.clear();
        }
        else{
            log.warn("Syncing with Master Node failed: " + response.getBody());
        }
    }


}
