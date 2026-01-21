package org.jeecg.modules.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data @TableName("zy_file") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "zy_file", description = "文件信息") public class ZyFile extends JeecgEntity {
    @TableId(type = IdType.AUTO) @ApiModelProperty(value = "id") private Integer id;
    @ApiModelProperty(value = "删除标识（0：未删除；1：已删除）") private Integer delFlag;

    @ApiModelProperty(value = "文件所属业务种类ID") private Integer category;
    @ApiModelProperty(value = "关联ID") private Integer parentId;
    @ApiModelProperty(value = "关键字") private String keyword;

    @ApiModelProperty(value = "作者（内管平台）") private String author;
    @ApiModelProperty(value = "对象ID（内管平台）") private String objectId;
    @ApiModelProperty(value = "文档ID（内管平台）") private String docId;
    @ApiModelProperty(value = "文件ID（内管平台）") private String fileId;
    @ApiModelProperty(value = "文件名称（内管平台）") private String fileName;
    @ApiModelProperty(value = "文件路径（内管平台）") private String folderFilePath;
    @ApiModelProperty(value = "WPS ID（内管平台）") private String wpsId;
    @ApiModelProperty(value = "预览链接（内管平台）") private String previewUrl;
    @ApiModelProperty(value = "下载链接（内管平台）") private String downloadUrl;
}
