package com.shesha4572.cdnslave.repositories;

import com.shesha4572.cdnslave.entities.FileChunk;
import org.springframework.data.repository.CrudRepository;

public interface FileChunkRedisRepository extends CrudRepository<FileChunk , String> {
}
