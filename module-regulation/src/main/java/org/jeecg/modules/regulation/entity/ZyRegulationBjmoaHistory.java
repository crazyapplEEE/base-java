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
@Data @TableName("zy_regulation_bjmoa_history") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "zy_regulation_bjmoa_history对象", description = "轨道运营制度历史版本")
public class ZyRegulationBjmoaHistory extends JeecgEntity {
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
    @ApiModelProperty("七巧创建人ID") private String qiqiaoCreatorId;
    @ApiModelProperty("七巧创建人姓名") private String qiqiaoCreatorName;
    @ApiModelProperty("制度级别") private String levelId;
    @ApiModelProperty("制度级别") private String levelName;
    @ApiModelProperty("大类") private String categoryId;
    @ApiModelProperty("大类") private String categoryName;
    @ApiModelProperty("管理类别") private String managementCategoryId;
    @ApiModelProperty("管理类别") private String managementCategoryName;
    @ApiModelProperty("业务子类") private String subCategoryId;
    @ApiModelProperty("业务子类") private String subCategoryName;
    @ApiModelProperty("预案分类") private String contingencyPlanCategoryId;
    @ApiModelProperty("预案分类") private String contingencyPlanCategoryName;
    @ApiModelProperty("线路") private String lineId;
    @ApiModelProperty("线路") private String lineName;
    @ApiModelProperty("发布时间") private Date publishTime;
    @ApiModelProperty("实施时间") private Date executeTime;
    @ApiModelProperty("废止时间") private Date abolishTime;
    @ApiModelProperty("OA流程请求ID") private String requestId;
    @ApiModelProperty("文件名称（带后缀）") private String fileName;

}
