package org.jeecg.modules.regulation.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "临时技术变更VO", description = "临时技术变更VO") public class ZyRegulationTempBjmoaVO implements Serializable {
    private String name;
    private String code;
    private String status;
    private String previewUrl;
    @ApiModelProperty("qiqiao_id") private String id;
    @ApiModelProperty("发起人") private String creator;
    @ApiModelProperty("发起部门") private String createDepartment;
    @ApiModelProperty("联合发起部门") private String jointDepartment;
    @ApiModelProperty("收文部门") private String receiveDepartment;
    @ApiModelProperty("生效时间") private Date effectiveTime;
    @ApiModelProperty("到期时间") private Date dueTime;
    @ApiModelProperty("关联制度") private List<ZyRegulationBjmoa> relatedRegulationList;
}
