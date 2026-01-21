package org.jeecg.modules.regulation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author Tong Ling
 * @date 2023-11-17
 */
@Data @TableName("zy_bii_regulation_admin") @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "zy_bii_regulation_admin对象", description = "本部制度管理员") public class ZyBiiRegulationAdmin {
    private static final long serialVersionUID = 1L;
    private String loginid;
}
