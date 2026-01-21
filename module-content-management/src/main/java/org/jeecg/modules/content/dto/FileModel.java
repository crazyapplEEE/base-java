package org.jeecg.modules.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor public class FileModel {
    /**
     * 文件名
     */
    private String title;

    /**
     * 文件内容
     */
    private String content;
    private String appId;
    private String docId;
    private String fileId;
    private String version;
    private Long timestamp;
}