package com.shesha4572.cdnslave.modelsDto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class DownloadChunkPartialDto {
    String fileChunkId;
    int startIndex;
    int endIndex;
}
