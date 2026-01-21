package org.jeecg.modules.common.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Tong Ling
 * @date 2021/10/25
 */
@NoArgsConstructor @AllArgsConstructor @Data @Accessors(chain = true) @EqualsAndHashCode(callSuper = false)
@ApiModel(value = "BarVO对象", description = "BarVO") public class BarVO {
    private String name;
    private String company;
    private Integer value;
}
