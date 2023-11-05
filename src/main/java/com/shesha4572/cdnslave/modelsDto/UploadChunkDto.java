package com.shesha4572.cdnslave.modelsDto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestHeader;

@Data
public class UploadChunkDto {
    String fileChunkId;
    int fileChunkIndex;
    int replicationNum;
}
