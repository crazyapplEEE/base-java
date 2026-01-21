package org.jeecg.modules.regulation.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "制度统计VO", description = "制度统计VO") public class ZyRegulationBjmoaStatisticsVO implements Serializable {
    @ApiModelProperty("制度大类") private String categoryName;
    @ApiModelProperty("数量") private Integer num;
}
