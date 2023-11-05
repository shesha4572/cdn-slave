package com.shesha4572.cdnslave.services;

import com.shesha4572.cdnslave.entities.FileChunk;
import com.shesha4572.cdnslave.repositories.FileChunkRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FileChunkService {
    private final FileChunkRedisRepository fileChunkRedis;
    private final Path root;

    private final RedisAtomicInteger chunkRedisCounter;
    private final String masterNodeUrl = System.getenv("MASTER_NODE_URL");
    private final String podName = System.getenv("POD_NAME");

    @Autowired
    public FileChunkService(FileChunkRedisRepository fileChunkRedis, @Value("${FILE_PATH}") String dir , RedisAtomicInteger redisAtomicInteger) {
        this.fileChunkRedis = fileChunkRedis;
        this.root = Paths.get(dir);
        this.chunkRedisCounter = redisAtomicInteger;
    }

    public void saveFileChunk(MultipartFile chunk, FileChunk fileChunkDetails) throws RuntimeException {
        try {
            if (chunk.getSize() > 64000000) {
                throw new RuntimeException("File chunk is too large");
            } else if (chunkRedisCounter.get() == 30) {
                throw new RuntimeException("No More Chunks can be stored");
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
        chunkRedisCounter.incrementAndGet();
        fileChunkRedis.save(fileChunkDetails);
        log.info(fileChunkDetails + " added successfully");
    }

    public Resource downloadFileChunk(String fileChunkId) throws RuntimeException {
        try {
            if (!fileChunkRedis.existsById(fileChunkId)) {
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

    public Resource downloadPartialFileChunk(String fileChunkId, int startIndex, int endIndex) throws RuntimeException {
        try {
            if (!fileChunkRedis.existsById(fileChunkId)) {
                throw new RuntimeException("Could not read the file!");
            }
            Path file = root.resolve(fileChunkId);
            Resource resource = new UrlResource(file.toUri());

            if (resource.isReadable()) {
                InputStream completeBytes = resource.getInputStream();
                byte[] requiredBytes = new byte[endIndex - startIndex + 1];
                completeBytes.skipNBytes(startIndex);
                completeBytes.read(requiredBytes, 0, requiredBytes.length);
                return new ByteArrayResource(requiredBytes);
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Scheduled(cron = "*/60 * * * * ?")
    public void sendHeartBeat() {
        log.info("Syncing with Master Node..");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> map = new HashMap<>();
        ArrayList<FileChunk> newFileChunks = fileChunkRedis.getFileChunksByIsMasterAware(Boolean.FALSE);
        if(newFileChunks.isEmpty()){
            log.info("No new file Chunks found. Sync Complete");
            return;
        }
        ArrayList<String> newFileChunkStrings = new ArrayList<>();
        newFileChunks.forEach(fileChunk -> newFileChunkStrings.add(fileChunk.getFileChunkId()));
        log.info("Found " + newFileChunkStrings.size() + " new file chunk(s)");
        map.put("newChunks", newFileChunkStrings);
        BigDecimal chunkLoad = BigDecimal.valueOf(chunkRedisCounter.doubleValue() / 250);
        map.put("chunkLoad", chunkLoad.round(new MathContext(4)));
        log.info("Current load on node : " + chunkLoad.round(new MathContext(4)));
        map.put("podName" , podName);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(masterNodeUrl + "/api/v1/heartbeat/");
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.PUT,
                    request,
                    String.class);
            if (response.getStatusCode() == HttpStatusCode.valueOf(200)) {
                newFileChunks.forEach(fileChunk -> {fileChunk.setIsMasterAware(Boolean.TRUE); fileChunkRedis.save(fileChunk);});
            } else {
                log.warn("Syncing with Master Node failed: " + response.getBody());
            }
        }
        catch (Exception e){
            log.error(e.getMessage());
        }


    }


}
