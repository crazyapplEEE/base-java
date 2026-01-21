package org.jeecg.modules.regulation.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "制度VO", description = "制度VO") public class ZyRegulationBjmoaVO implements Serializable {
    private Integer id;
    private Integer active;
    private String name;
    private String code;
    private String categoryId;
    private String categoryName;
    private String levelId;
    private String levelName;
    private String contentDocId;
    private String contentFileId;
    private String version;
    private String createTime;
    private Date publishTime;
    private Date abolishTime;
    private String previewUrl;
    private String downloadUrl;
    private String managementCategoryId;
    private String managementCategoryName;
    private String subCategoryId;
    private String subCategoryName;
    private String contingencyPlanCategoryId;
    private String contingencyPlanCategoryName;
    private String lineId;
    private String lineName;
    private String requestId;
    private String identifier;
    private String watermarkPdfContentFileId;
    private String watermarkPdfContentDocId;
    private String pdfPreviewUrl;
    private String pdfDownloadUrl;
    @ApiModelProperty("全文搜索出来的内容") private String fragment;
    @ApiModelProperty("主责部门") private List<String> deptList;
    @ApiModelProperty("历史记录") private List<ZyRegulationBjmoaHistoryVO> historyList;
    @ApiModelProperty("关联制度") private List<ZyRelatedRegulationVO> relatedRegulationList;
}
