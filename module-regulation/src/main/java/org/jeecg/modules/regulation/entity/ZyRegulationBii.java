package org.jeecg.modules.regulation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@Data @TableName("zy_regulation_bii") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "zy_regulation_bii对象", description = "京投本部制度") public class ZyRegulationBii
    extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) @ApiModelProperty(value = "id") private Integer id;
    @ApiModelProperty("是否有效") private Integer active;
    @ApiModelProperty("制度名称") private String name;
    @ApiModelProperty("制度编号") private String code;
    @ApiModelProperty("制度唯一标识") private String identifier;
    @ApiModelProperty("内管文件ID") private String contentFileId;
    @ApiModelProperty("内管文档ID") private String contentDocId;

    @ApiModelProperty("七巧制度ID") private String qiqiaoRegulationId;
    @ApiModelProperty("七巧创建人ID") private String qiqiaoCreatorId;
    @ApiModelProperty("七巧创建人姓名") private String qiqiaoCreatorName;
    @ApiModelProperty("制度级别") private String levelId;
    @ApiModelProperty("制度级别") private String levelName;
    @ApiModelProperty("制度分类") private String categoryId;
    @ApiModelProperty("制度分类") private String categoryName;
    @ApiModelProperty("制度子分类") private String subCategoryId;
    @ApiModelProperty("制度子分类") private String subCategoryName;

    @ApiModelProperty("预览链接") @TableField(exist = false) private String previewUrl;
    @ApiModelProperty("下载链接") @TableField(exist = false) private String downloadUrl;
}
