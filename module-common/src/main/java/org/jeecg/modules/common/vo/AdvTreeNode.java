package org.jeecg.modules.common.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "AntDesignVue的TreeSelect组件数据DTO") public class AdvTreeNode implements Serializable {
    private String key;
    private String title;
    private String value;
    private String orgCode;
    private List<AdvTreeNode> children;
}
