package com.shesha4572.cdnslave.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileChunk implements Serializable {
    private String fileChunkId;
    private int fileChunkIndex;
    private LocalDateTime chunkAddedOn;
    private Long chunkLength;
    private Boolean isChunkFull;
    private int replicationNo;

}
