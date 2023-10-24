package com.shesha4572.cdnslave.repositories;

import com.shesha4572.cdnslave.entities.FileChunk;
import org.springframework.data.repository.CrudRepository;
import java.util.ArrayList;


public interface FileChunkRedisRepository extends CrudRepository<FileChunk , String> {
    ArrayList<FileChunk> getFileChunksByIsMasterAware(Boolean isMasterAware);
}
