package com.shesha4572.cdnslave.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash("FileChunk")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FileChunk {
    @Id
    @Indexed
    private String fileChunkId;
    private int fileChunkIndex;
    private LocalDateTime chunkAddedOn;
    private Long chunkLength;
    private Boolean isChunkFull;
    private int replicationNo;
    @Indexed
    private Boolean isMasterAware;
    @Indexed
    private Boolean isDeleted;

}
