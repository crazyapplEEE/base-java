package org.jeecg.modules.regulation.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "临时技术变更查询DTO", description = "临时技术变更查询DTO") public class RegulationTempQueryDTO {
    @ApiModelProperty("标题") private String name;
    @ApiModelProperty("编号") private String code;
    @ApiModelProperty("全文检索内容") private String searchMessage;
}
