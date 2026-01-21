package org.jeecg.modules.regulation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class RegulationFileOld {
    // 主键
    @Id private String id;
    // 所属制度id
    private String regulationId;
    // 上传人id
    private String userId;
    // 文件类型
    private String type;
    // 文件名称
    private String name;
    // 文件类型
    private String contentType;
    private long size;
    private Date uploadDate;
    private String md5;
    // 文件内容
    private Binary content;
    // 是否可下载 0-不可下载 1-可下载
    private boolean download = false;
    private Date createdTime = new Date();
    private Date modifiedTime = new Date();

    public static class Type {
        public static String ACCORDING_FILE = "1";
        public static String REGULATION_MAIN = "2";
        public static String REGULATION_ATTACHMENT = "3";
        public static String DIFF_FILE = "4";
    }
}
