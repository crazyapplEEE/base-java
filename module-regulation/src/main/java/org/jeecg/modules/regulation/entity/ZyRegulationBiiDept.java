package org.jeecg.modules.regulation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Data @TableName("zy_regulation_bii_dept") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "zy_regulation_bii_dept对象", description = "京投本部制度主责部门")
public class ZyRegulationBiiDept extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) @ApiModelProperty(value = "id") private Integer id;
    @ApiModelProperty("版本") private String version;
    @ApiModelProperty("制度名称") private String name;
    @ApiModelProperty("制度编号") private String code;
    @ApiModelProperty("制度唯一标识") private String identifier;
    @ApiModelProperty("七巧制度ID") private String qiqiaoRegulationId;
    @ApiModelProperty("七巧部门ID") private String qiqiaoDeptId;
    @ApiModelProperty("七巧部门名称") private String qiqiaoDeptName;
}
