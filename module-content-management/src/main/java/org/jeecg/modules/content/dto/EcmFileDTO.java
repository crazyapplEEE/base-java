package org.jeecg.modules.content.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "内容管理平台文件DTO", description = "内容管理平台文件DTO") public class EcmFileDTO implements Serializable {
    private String author;
    private String objectId;
    private String docId;

    private String fileId;
    private String fileName;
    private String folderFilePath;
    private String wpsId;

    private String downloadUrl;
    private String previewUrl;
}
