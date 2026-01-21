package org.jeecg.modules.content.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "ZyFileDTO", description = "文件DTO") public class ZyFileDTO implements Serializable {
    private Integer category;
    private Integer parentId;
    private String keyword;
}
