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

import java.util.Date;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Data @TableName("zy_regulation_bii_history") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "zy_regulation_bii_history对象", description = "京投本部制度历史版本")
public class ZyRegulationBiiHistory extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) @ApiModelProperty(value = "id") private Integer id;
    @ApiModelProperty("版本") private String version;
    @ApiModelProperty("制度名称") private String name;
    @ApiModelProperty("制度编号") private String code;
    @ApiModelProperty("制度唯一标识") private String identifier;
    @ApiModelProperty("内管文件ID") private String contentFileId;
    @ApiModelProperty("内管文档ID") private String contentDocId;
    @ApiModelProperty("水印") private String mark;

    @ApiModelProperty("七巧制度ID") private String qiqiaoRegulationId;
    @ApiModelProperty("七巧上级制度ID") private String qiqiaoParentRegulationId;
    @ApiModelProperty("七巧创建人ID") private String qiqiaoCreatorId;
    @ApiModelProperty("七巧创建人姓名") private String qiqiaoCreatorName;
    @ApiModelProperty("制度级别") private String levelId;
    @ApiModelProperty("制度级别") private String levelName;
    @ApiModelProperty("制度分类") private String categoryId;
    @ApiModelProperty("制度分类") private String categoryName;
    @ApiModelProperty("制度子分类") private String subCategoryId;
    @ApiModelProperty("制度子分类") private String subCategoryName;
    @ApiModelProperty("发布文号") private String publishNo;
    @ApiModelProperty("专项审核完成时间") private Date specialAuditTime;
    @ApiModelProperty("发布时间") private Date publishTime;
    @ApiModelProperty("废止时间") private Date abolishTime;
}
