package com.shesha4572.cdnslave.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@RedisHash("FileChunk")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FileChunk {
    @Id
    private String fileChunkId;
    private int fileChunkIndex;
    private LocalDateTime chunkAddedOn;
    private Long chunkLength;
    private Boolean isChunkFull;
    private int replicationNo;

}
