package com.shesha4572.cdnslave.repositories;

import com.shesha4572.cdnslave.entities.FileChunk;
import com.shesha4572.cdnslave.interfaces.IFileChunkDAO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class FileChunkDAOImpl implements IFileChunkDAO {

    private final String hashReference = "FileChunk";

    @Resource(name = "redisTemplate")
    private HashOperations<String , String , FileChunk> hashOperations;
    @Override
    public void saveFileChunk(FileChunk fileChunk) {
        hashOperations.putIfAbsent(hashReference , fileChunk.getFileChunkId() , fileChunk);
    }

    @Override
    public FileChunk getFileChunk(String fileChunkId) {
        return hashOperations.get(hashReference , fileChunkId);
    }

    @Override
    public Map<String, FileChunk> getAllFileChunks() {
        return hashOperations.entries(hashReference);
    }

    @Override
    public void deleteFileChunk(String fileChunkId) {
        hashOperations.delete(hashReference , fileChunkId);
    }

    @Override
    public Boolean existsFileChunk(String fileChunkId) {
        return hashOperations.hasKey(hashReference , fileChunkId);
    }
}
