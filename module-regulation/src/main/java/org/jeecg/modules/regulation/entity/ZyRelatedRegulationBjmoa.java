package org.jeecg.modules.regulation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Data @TableName("zy_related_regulation_bjmoa") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "zy_related_regulation_bjmoa对象", description = "关联制度") public class ZyRelatedRegulationBjmoa {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) @ApiModelProperty(value = "id") private Integer id;
    private String regulationIdentifierA;
    private String codeA;
    private String versionA;
    private String regulationIdentifierB;
    private String codeB;
    private String versionB;
    @ApiModelProperty(value = "制度类型(1: 内部文件, 2: 外部文件,3:关联文件-附件)") private String regulationType;
    @ApiModelProperty(value = "关联制度名称") private String regulationName;
}
