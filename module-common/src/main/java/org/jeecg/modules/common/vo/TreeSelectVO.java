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
 * @author Guangyi Hu
 */
@Data @Accessors(chain = true) @EqualsAndHashCode(callSuper = false) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "TreeSelectVO", description = "部门多选级联对象") public class TreeSelectVO implements Serializable {
    private String id;
    private String label;
    private String icon;
    private String url;
    private String state;
    private List<TreeSelectVO> children;
}
