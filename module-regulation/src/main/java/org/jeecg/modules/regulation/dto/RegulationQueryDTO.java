package org.jeecg.modules.regulation.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
@ApiModel(value = "制度查询DTO", description = "制度查询DTO") public class RegulationQueryDTO {
    @ApiModelProperty("是否有效") private Integer active = 1;
    @ApiModelProperty("部门") private List<String> deptIdList;
    @ApiModelProperty("标题") private String name;
    @ApiModelProperty("全文检索内容") private String searchMessage;
    @ApiModelProperty("文档ID列表") private List<String> docIdList;
    @ApiModelProperty("制度编号列表") private List<String> codeList;
    @ApiModelProperty("制度唯一标识列表") private List<String> identifierList;
    @ApiModelProperty("制度等级列表") private List<String> levelNameList;
    @ApiModelProperty("制度子分类列表") private List<String> subCategoryNameList;
    @ApiModelProperty("制度分类列表") private List<String> categoryNameList;
    @ApiModelProperty("制度管理分类列表") private List<String> managementCategoryNameList;

    @ApiModelProperty("全文检索文档ID到版本映射") private Map<String, String> docId2Version;
    @ApiModelProperty("全文检索文档ID到高亮内容映射") private Map<String, String> docId2Content;
}
