package org.jeecg.modules.common.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author Tong Ling
 * @since 2021/8/11 16:06
 */
@Data @Accessors(chain = true) @EqualsAndHashCode(callSuper = false) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "VantCascaderVO", description = "vant级联对象") public class VantCascaderVO implements Serializable {
    private String value;
    private String text;
    private String orgCode; // 用来排序
    private List<VantCascaderVO> children;
}
