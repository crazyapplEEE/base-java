package org.jeecg.modules.regulation.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiDept;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiHistory;

import java.util.List;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "制度历史VO", description = "制度历史VO")
public class ZyRegulationBiiHistoryVO extends ZyRegulationBiiHistory {
    private String previewUrl;
    private String pdfDownloadUrl;
    private String docxDownloadUrl;
    private List<ZyRegulationBiiDept> deptList;
}
