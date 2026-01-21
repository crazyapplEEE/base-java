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
@Data @TableName("zy_read") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "zy_read对象", description = "已读记录") public class ZyRead extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO) @ApiModelProperty(value = "id") private Integer id;
    private String identifier;
    private String version;
}
