package org.jeecg.modules.regulation.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "关联制度VO", description = "关联制度VO")
public class ZyRelatedRegulationVO {
    @ApiModelProperty("关联制度类型") private String type;
    @ApiModelProperty("内部关联制度存储制度identifier，外部关联制度存储fileid") private String identifier;
    @ApiModelProperty("内部关联制度id") private Integer id;
    @ApiModelProperty("关联制度名称") private String name;
    @ApiModelProperty("外部关联制度文件下载链接") private String downloadUrl;
}
