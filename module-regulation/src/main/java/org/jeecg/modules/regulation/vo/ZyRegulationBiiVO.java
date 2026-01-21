package org.jeecg.modules.regulation.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jeecg.modules.regulation.entity.ZyRegulationBii;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "制度VO", description = "制度VO") public class ZyRegulationBiiVO implements Serializable {
    private Integer id;
    private Integer readId;
    private Integer active;
    private String name;
    private String code;
    private String identifier;
    private String categoryId;
    private String categoryName;
    private String subCategoryId;
    private String subCategoryName;
    private String levelId;
    private String levelName;
    private String contentDocId;
    private String contentFileId;
    private String version;
    private String createTime;
    private String publishNo;
    private Date specialAuditTime;
    private Date publishTime;
    private Date abolishTime;
    private String previewUrl;
    private String downloadUrl;
    @ApiModelProperty("全文搜索出来的内容") private String fragment;
    @ApiModelProperty("主责部门") private List<String> deptList;
    @ApiModelProperty("历史记录") private List<ZyRegulationBiiHistoryVO> historyList;
    @ApiModelProperty("关联制度") private List<ZyRegulationBii> relatedRegulationList;
}
