package com.shesha4572.cdnslave.interfaces;

import com.shesha4572.cdnslave.entities.FileChunk;

import java.util.Map;
import java.util.Optional;

public interface IFileChunkDAO {

    void saveFileChunk(FileChunk fileChunk);
    FileChunk getFileChunk(String fileChunkId);
    Map<String , FileChunk> getAllFileChunks();
    void deleteFileChunk(String fileChunkId);
    Boolean existsFileChunk(String fileChunkId);
}
