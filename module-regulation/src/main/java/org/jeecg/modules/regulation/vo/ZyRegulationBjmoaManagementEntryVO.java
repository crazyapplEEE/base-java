package org.jeecg.modules.regulation.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author zhouwei
 * @date 2023/10/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "管理工具快捷入口VO", description = "管理工具快捷入口VO")
public class ZyRegulationBjmoaManagementEntryVO implements Comparable<ZyRegulationBjmoaManagementEntryVO>{

    private Integer sort;
    private String button;
    private String url;
    private String iconUrl;

    @Override
    public int compareTo(ZyRegulationBjmoaManagementEntryVO o) {
        int sort = this.getSort() - o.getSort();
        return sort;
    }
}
