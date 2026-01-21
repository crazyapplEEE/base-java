package org.jeecg.modules.qiqiao.constants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j @Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) public class RecordVO {
    private String applicationId;
    private String formModelId;
    private String processId;
    private String processInstanceId;
    private String fileId;
    private String id;
    private Integer version;
    private Map data;
    private String loginUserId;
    private List<File> files;
    private String formFieldType;

    /**
     * 筛选有关字段
     */
    private Long startTime;
    private Long endTime;
    private String orderField;
    private Boolean isAsc;

    /**
     * 分页参数
     */
    private Integer page;
    private Integer pageSize;

    /**
     * 过滤条件
     */
    private List<FieldFilter> filter;

    @JsonIgnore public String getRequestParam() {
        StringBuilder stringBuilder = new StringBuilder();
        if (startTime != null) {
            stringBuilder.append("&startTime=").append(startTime);
        }
        if (endTime != null) {
            stringBuilder.append("&endTime=").append(endTime);
        }
        if (StringUtils.isNotBlank(orderField)) {
            stringBuilder.append("&orderField=").append(orderField);
        }
        if (isAsc != null) {
            stringBuilder.append("&isAsc=").append(isAsc);
        }
        if (page != null) {
            stringBuilder.append("&page=").append(page);
        }
        if (pageSize != null) {
            stringBuilder.append("&pageSize=").append(pageSize);
        }
        if (stringBuilder.length() > 0) {
            return "?" + stringBuilder.substring(1);
        } else {
            return "";
        }
    }

    @JsonIgnore public void setDefaultRecordVO() {
        if (page == null || page <= 0) {
            page = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
    }
}
