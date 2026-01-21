package org.jeecg.modules.regulation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Data @EqualsAndHashCode(callSuper = false) @Accessors(chain = true) @NoArgsConstructor @AllArgsConstructor
public class RegulationOld {
    // 主键
    @Id private String id;
    // 1-通知 2-单项制度
    private String type;
    // 上传人id
    private String userId;
    private String title;
    private String version;
    private List<String> keywords;
    private List<String> departments;
    private List<String> departmentIds;
    private String mainTo;
    private String subTo;
    private Date passTime;
    private Date lawTime;
    private Date recordTime;
    private String accordingText;
    private List<String> accordingFiles;
    private String isModified;
    private List<String> categories;
    private String level;
    private String status;
    private String statusReason;
    private String publishNo;
    private Date publishTime;
    private Date abolishTime;

    private List<RegulationFileOld> fileList;
    private List<String> fileDownloadList;
    private List<RegulationFileOld> attachmentList;
    private List<String> attachmentDownloadList;
    private List<String> recordList;
    // 通知关联的制度，仅对通知有效
    private List<String> linkList;
    // 制度关联的通知，仅对制度有效
    private List<RelationOld> noticeList;
    private String text;
    // 0-历史 1-当前生效
    private String isActive = "1";
    private List<RegulationFileOld> diffList;
    private String diffText;
    // 0-草稿(预览） 1-已发布 3-临时文件，为了预览而存储
    private String flag;
    // 可见范围 0-京投本部 1-分公司 2-全资子公司 3-控股公司 4-参股公司
    private List<String> companyTypes;
    private String isDeleted = "0";

    private String abolishText;
    private List<RegulationFileOld> abolishFileList;

    // deprecated
    private List<String> regulationFiles;
    private List<String> attachmentFiles;
    private List<String> diffFiles;
    private List<String> accordingFileNames;
    private List<String> regulationFileNames;
    private List<String> attachmentFileNames;
    private List<String> diffFileNames;
    private Date createdTime = new Date();
    private Date modifiedTime = new Date();
}
