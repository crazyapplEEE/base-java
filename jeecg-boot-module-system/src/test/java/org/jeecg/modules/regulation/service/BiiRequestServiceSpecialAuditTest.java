package org.jeecg.modules.regulation.service;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServiceImpl;
import org.jeecg.modules.oa.webservices.soap.customDoc.CustomDocServicePortType;
import org.jeecg.modules.oa.webservices.soap.customDoc.DocAttachment;
import org.jeecg.modules.oa.webservices.soap.customDoc.DocInfo;
import org.jeecg.modules.oa.webservices.soap.workflow.*;
import org.jeecg.modules.publicManagement.service.IPublicManagementService;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.FormFieldType;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.jeecg.modules.regulation.constant.BiiOaWorkflowStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipInputStream;

import static org.jeecg.modules.oa.webservices.soap.workflow.WorkflowUtils.toTableFieldArray;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BiiRequestServiceSpecialAuditTest {
    @Autowired private WorkflowServiceImpl workflowClient;
    @Autowired private IQiqiaoService qiqiaoService;
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;
    @Value("${biisaas.biiRegulationInfo.applicationId}") private String biiRegulationInfoApplicationId;
    @Value("${biisaas.biiRegulationInfo.formModelId}") private String biiRegulationInfoFormModelId;
    @Value("${oa-workflow.bii-special-audit}") private String biiSpecialAuditWorkflowId;
    @Value("${oa-workflow.bii-special-audit-name}") private String biiSpecialAuditWorkflowNameTemplate;
    @Autowired @Qualifier("biiContentManagementService") private IContentManagementService contentManagementService;
    @Autowired private CustomDocServiceImpl customDocServiceImpl;
    @Autowired private IPublicManagementService publicManagementService;

    @Test public void createSpecialAudit() throws Exception {
        final String qiqiaoRegulationInfoId = "8230473145326379009";
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationInfoId);
        final JSONObject infoRecord = qiqiaoFormsService.queryById(recordVO);
        System.out.println("infoRecord: " + infoRecord);
        if (infoRecord == null) {
            return;
        }

        final String qiqiaoCreatorId = infoRecord.getString("author");
        final JSONObject usersInfoJson = qiqiaoService.usersInfo(qiqiaoCreatorId);
        final String wxid = usersInfoJson.getString("account");
        final JSONObject userInfoByWxid = publicManagementService.getUserInfoByWxid(wxid);
        System.out.println("userInfoByWxid: " + userInfoByWxid);
        Assert.assertNotNull(userInfoByWxid);

        final Integer creatorId = userInfoByWxid.getInteger("account");
        final String creatorName = userInfoByWxid.getString("nickName");
        final String createDeptId = userInfoByWxid.getString("orgOaId");

        final WorkflowRequestInfo requestInfo =
            createSpecialAuditRequestInfo(qiqiaoRegulationInfoId, creatorId, creatorName, createDeptId);
        System.out.println(requestInfo);
        if (requestInfo == null) {
            return;
        }
        final WorkflowServicePortType service = workflowClient.getWorkflowServiceHttpPort();
        final String requestId = service.doCreateWorkflowRequest(requestInfo, creatorId);
        System.out.println("qiqiaoRegulationInfoId: " + qiqiaoRegulationInfoId + ", requestId: " + requestId);
        if (StringUtils.isNotEmpty(requestId) && !requestId.startsWith("-")) {
            // 更新七巧计划单状态及流程ID
            approvingSpecialAudit(qiqiaoRegulationInfoId, requestId);
        }
    }

    @Test public void approveSpecialAudit() {
        final String requestId = "3378564";
        final String oaDocIds = "";
        approveSpecialAudit(requestId, oaDocIds);
    }

    @Test public void rejectSpecialAudit() {
        final String requestId = "3378564";
        rejectSpecialAudit(requestId);
    }

    private WorkflowRequestInfo createSpecialAuditRequestInfo(final String qiqiaoRegulationInfoId,
        final Integer creatorId, final String creatorName, final String createDeptId) {
        if (StringUtils.isEmpty(qiqiaoRegulationInfoId)) {
            return null;
        }

        final String createTime = DateUtils.getCurrentDateInBeijing();
        final String requestName =
            biiSpecialAuditWorkflowNameTemplate.replace("{creator}", creatorName).replace("{date}", createTime);
        final String workflowId = biiSpecialAuditWorkflowId;

        // 设置基础信息
        final WorkflowRequestInfo result = new WorkflowRequestInfo();
        result.setRequestName(requestName);
        result.setCreatorId(String.valueOf(creatorId));

        // 流程基本信息
        final WorkflowBaseInfo wbi = new WorkflowBaseInfo();
        wbi.setWorkflowId(String.valueOf(workflowId));
        result.setWorkflowBaseInfo(wbi);

        // 主表
        final List<WorkflowRequestTableField> regulationInfoTableFields =
            convertQiqiaoSpecialAudit(String.valueOf(creatorId), createDeptId, qiqiaoRegulationInfoId);
        final WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];
        wrtri[0] = new WorkflowRequestTableRecord();
        wrtri[0].setWorkflowRequestTableFields(toTableFieldArray(regulationInfoTableFields));
        final WorkflowMainTableInfo wmi = new WorkflowMainTableInfo();
        wmi.setRequestRecords(wrtri);
        result.setWorkflowMainTableInfo(wmi);

        result.setIsnextflow("0");

        return result;
    }

    private List<WorkflowRequestTableField> convertQiqiaoSpecialAudit(final String creatorId, final String createDeptId,
        final String qiqiaoRegulationInfoId) {
        if (StringUtils.isEmpty(qiqiaoRegulationInfoId) || StringUtils.isEmpty(createDeptId) || StringUtils.isEmpty(
            createDeptId)) {
            return null;
        }

        final List<WorkflowRequestTableField> result = new ArrayList<>();

        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationInfoId);

        // {
        //   "variables": {
        //     "制度编号": "我是修订后的编号",
        //     "制度跟进人": ["fa8c33dad56943b5ec479cec374c11a1"],
        //     "制度配合部门_pretty_value": [],
        //     "制度发布范围": "我是发布范围",
        //     "是否再次进行意见征集": "2",
        //     "制度归口管理部门": "1",
        //     "文件水印": "",
        //     "系统性规范性审核": "",
        //     "可编辑": "",
        //     "制度分类": "6",
        //     "法律合规审核结果": "",
        //     "办公室审核": "",
        //     "制度编写依据的相关文件": [
        //       {
        //         "uid": 1704701236784,
        //         "fileSize": 16797,
        //         "name": "意见单.docx",
        //         "hasUploadSuccess": true,
        //         "fileType": "/doc/docx/msword/.document/msword",
        //         "fileId": "8281646546787147776",
        //         "createDate": 1704701237103,
        //         "uploadUser": "85e123417e72221947a83c682f009567"
        //       }
        //     ],
        //     "制度分类计划版": "10",
        //     "制度建设类型": "2",
        //     "制度本次编制部门_pretty_value": "京投公司-信息数据管理部",
        //     "可删除": "",
        //     "法律合规审核": "",
        //     "发布要求": "",
        //     "可预览": "",
        //     "计划生效时间": 1701619200000,
        //     "可见范围": ["1", "2", "3", "4", "5"],
        //     "修订前的制度名称": "7940205064889843738",
        //     "制度版本": "A",
        //     "制度级别": "3",
        //     "制度跟进人_pretty_value": ["王璐瑶"],
        //     "制度审批备案程序拟完成时间": 1703692800000,
        //     "新制度编号": "我是修订后的编号",
        //     "制度建设编码": "202401ZDJS0026",
        //     "备注": "",
        //     "文件名称": "制度正文.docx",
        //     "制度建设编号文本": "",
        //     "流转选项": "",
        //     "制度级别计划版": "3",
        //     "外键": "8230465551824191488",
        //     "上传制度的上会版本": [],
        //     "是否为非原则修订": "",
        //     "会议报审结果": "1",
        //     "京投专项流程状态": "",
        //     "意见征求范围_pretty_value": ["京投公司-信息数据管理部"],
        //     "京投专项流程id": "",
        //     "制度主责部门_pretty_value": ["京投公司-管培生", "京投公司-信息数据管理部"],
        //     "修订前的制度编号": "我是修订前的编号",
        //     "制度唯一标识文本": "",
        //     "制度本次编制部门": "7c1c05ac71614da0a66decf0add9cac1",
        //     "内管文件编号": "659bac3ac31e123e37511d57",
        //     "制度状态": "1",
        //     "制度配合部门": [],
        //     "发文途径": "1",
        //     "上传打印的意见单": [
        //       {
        //         "uid": 1704443796118,
        //         "fileSize": 10007425,
        //         "name": "3-龙盈战队.pdf",
        //         "hasUploadSuccess": true,
        //         "fileType": "/pdf",
        //         "removeConfirm": true,
        //         "fileId": "8272801010101428224",
        //         "createDate": 1704443798648,
        //         "uploadUser": "85e123417e72221947a83c682f009567"
        //       }
        //     ],
        //     "征求意见截止时间": 1704988800000,
        //     "最终制度名称": "我是最终名称",
        //     "意见征求范围": ["7c1c05ac71614da0a66decf0add9cac1"],
        //     "制度主责部门": [
        //       "a1483c18902a4264a856745a366020fb",
        //       "7c1c05ac71614da0a66decf0add9cac1"
        //     ],
        //     "任务来源": "  分",
        //     "内管文档编号": "dd4305ac-3d69-4201-8c4f-02f632b69d67",
        //     "请上传专项审核稿": [],
        //     "主要内容修订内容": "而非",
        //     "制度名称": "我是最终名称"
        //   },
        //   "author": "fa8c33dad56943b5ec479cec374c11a1",
        //   "authorName": "王璐瑶",
        //   "formDefinitionId": "c3c41729af7c4fdda59163a4534d56b1",
        //   "lastModifierName": "凌童",
        //   "prettyValue": {
        //     "修订前的制度名称": "重现修订完成",
        //     "制度跟进人": "王璐瑶",
        //     "制度级别": "三级制度(J3)",
        //     "是否再次进行意见征集": "否，进行意见打印并上传",
        //     "流转选项": "",
        //     "制度级别计划版": "三级制度(J3)",
        //     "外键": "202312京投公司-信息数据管理部021",
        //     "是否为非原则修订": "",
        //     "会议报审结果": "同意",
        //     "京投专项流程状态": "",
        //     "制度归口管理部门": "京投办公室",
        //     "制度本次编制部门": "京投公司-信息数据管理部",
        //     "可编辑": "",
        //     "制度分类": "法律合规管理(FG)",
        //     "制度状态": "计划中",
        //     "制度配合部门": "",
        //     "法律合规审核结果": "",
        //     "办公室审核": "",
        //     "发文途径": "由京投办发文",
        //     "制度分类计划版": "安全管理(AQ)",
        //     "制度建设类型": "修订",
        //     "意见征求范围": "京投公司-信息数据管理部",
        //     "可删除": "",
        //     "制度主责部门": "京投公司-管培生,京投公司-信息数据管理部",
        //     "可预览": "",
        //     "可见范围": "京投本部,分公司,全资子公司,控股公司,参股公司"
        //   },
        //   "id": "8230473145326379009",
        //   "applicationId": "cd363f9220154b3dba4d4a4cbcaea9c6",
        //   "lastModifyDate": 1704701239000,
        //   "version": 6,
        //   "createDate": 1703211897000
        // }
        final JSONObject infoRecord = qiqiaoFormsService.queryById(recordVO);
        System.out.println("infoRecord: " + infoRecord);
        if (infoRecord == null) {
            return null;
        }

        final JSONObject variables = infoRecord.getJSONObject("variables");
        final JSONObject prettyValue = infoRecord.getJSONObject("prettyValue");

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("fwjbr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("bgsjbr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("bmdw");
            tableField.setFieldValue(createDeptId);
            result.add(tableField);
        }

        final String nameBefore = prettyValue.getString("修订前的制度名称");
        if (StringUtils.isNotEmpty(nameBefore)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xdfzq");
            tableField.setFieldValue(nameBefore);
            result.add(tableField);
        }

        final String nameAfter = variables.getString("制度名称");
        if (StringUtils.isNotEmpty(nameAfter)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xdxzh");
            tableField.setFieldValue(nameAfter);
            result.add(tableField);
        }

        final String type = prettyValue.getString("制度建设类型");
        if (StringUtils.isNotEmpty(type)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zdbhlx");
            tableField.setFieldValue(type);
            result.add(tableField);
        }

        final String createDept = variables.getString("制度本次编制部门_pretty_value");
        if (StringUtils.isNotEmpty(createDept)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("bm");
            tableField.setFieldValue(createDept);
            result.add(tableField);
        }

        final String content = variables.getString("主要内容修订内容");
        if (StringUtils.isNotEmpty(content)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zynr");
            tableField.setFieldValue(content);
            result.add(tableField);
        }

        final String remark = variables.getString("备注");
        if (StringUtils.isNotEmpty(remark)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zqxgyj");
            tableField.setFieldValue(remark);
            result.add(tableField);
        }

        // xdnr 修订内容
        // 制度编写依据的相关文件
        final JSONArray relatedFileArray = variables.getJSONArray("制度编写依据的相关文件");
        if (CollectionUtils.isNotEmpty(relatedFileArray)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xdnr");

            final StringBuilder fieldTypeSb = new StringBuilder();
            final StringBuilder fieldValueSb = new StringBuilder();
            for (int i = 0; i < relatedFileArray.size(); ++i) {
                final JSONObject relatedFile = relatedFileArray.getJSONObject(i);
                fieldTypeSb.append("http:").append(relatedFile.getString("name")).append("|");

                final RecordVO downloadRecordVO = new RecordVO();
                downloadRecordVO.setApplicationId(biiRegulationInfoApplicationId);
                downloadRecordVO.setFileId(relatedFile.getString("fileId"));
                fieldValueSb.append(qiqiaoFormsService.getDownloadUrl(downloadRecordVO)).append("|");
            }

            tableField.setFieldType(fieldTypeSb.substring(0, fieldTypeSb.length() - 1));
            tableField.setFieldValue(fieldValueSb.substring(0, fieldValueSb.length() - 1));
            result.add(tableField);
        }

        // fjsc 制度送审稿
        // 在线编辑1
        final String regulationDocId = variables.getString("内管文档编号");
        if (StringUtils.isNotEmpty(regulationDocId) && StringUtils.isNotEmpty(nameAfter)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("fjsc");

            tableField.setFieldType("http:" + nameAfter + ".docx");
            tableField.setFieldValue(contentManagementService.getDownloadNewestUrl(regulationDocId));
            result.add(tableField);
        }

        // sczdxtyjd 上传制度系统打印的意见单
        // 上传打印的意见单
        final JSONArray optionFileList = variables.getJSONArray("上传打印的意见单");
        if (CollectionUtils.isNotEmpty(optionFileList)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sczdxtyjd");

            final StringBuilder fieldTypeSb = new StringBuilder();
            final StringBuilder fieldValueSb = new StringBuilder();
            for (int i = 0; i < optionFileList.size(); ++i) {
                final JSONObject optionFile = optionFileList.getJSONObject(i);
                fieldTypeSb.append("http:").append(optionFile.getString("name")).append("|");

                final RecordVO downloadRecordVO = new RecordVO();
                downloadRecordVO.setApplicationId(biiRegulationInfoApplicationId);
                downloadRecordVO.setFileId(optionFile.getString("fileId"));
                fieldValueSb.append(qiqiaoFormsService.getDownloadUrl(downloadRecordVO)).append("|");
            }

            tableField.setFieldType(fieldTypeSb.substring(0, fieldTypeSb.length() - 1));
            tableField.setFieldValue(fieldValueSb.substring(0, fieldValueSb.length() - 1));
            result.add(tableField);
        }

        for (WorkflowRequestTableField workflowRequestTableField : result) {
            workflowRequestTableField.setEdit(true);
            workflowRequestTableField.setView(true);
        }

        return result;
    }

    private void approvingSpecialAudit(final String qiqiaoRegulationInfoId, final String requestId) {
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationInfoId);

        final Map<String, Object> data = new HashMap<>(2);
        data.put("京投专项流程id", requestId);
        data.put("京投专项流程状态", BiiOaWorkflowStatus.APPROVING);
        recordVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(recordVO);
        System.out.println("saveOrUpdate: " + jsonObject);
    }

    private void approveSpecialAudit(final String requestId, final String oaDocIds) {
        if (StringUtils.isEmpty(requestId)) {
            return;
        }

        final RecordVO queryVO = new RecordVO();
        queryVO.setApplicationId(biiRegulationInfoApplicationId);
        queryVO.setFormModelId(biiRegulationInfoFormModelId);

        final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        final FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("京投专项流程id");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(requestId);
        fieldFilterList.add(fieldFilter);
        queryVO.setFilter(fieldFilterList);

        final JSONObject pageRecord = qiqiaoFormsService.page(queryVO);
        System.out.println("pageRecord: " + pageRecord);
        if (pageRecord == null) {
            return;
        }
        final JSONArray list = pageRecord.getJSONArray("list");
        if (list == null || list.size() != 1) {
            System.out.println("WEIRD list=" + list);
            return;
        }

        final RecordVO updateVO = new RecordVO();
        updateVO.setApplicationId(biiRegulationInfoApplicationId);
        updateVO.setFormModelId(biiRegulationInfoFormModelId);

        final JSONObject regulationInfo = list.getJSONObject(0);

        updateVO.setId(regulationInfo.getString("id"));
        final Map<String, Object> data = new HashMap<>(1);
        data.put("京投专项流程状态", BiiOaWorkflowStatus.APPROVED);
        data.put("专项审核完成时间", new Date().getTime());

        // 获取经办人的id
        final String qiqiaoCreatorId = regulationInfo.getString("author");
        final JSONObject usersInfoJson = qiqiaoService.usersInfo(qiqiaoCreatorId);
        final String wxid = usersInfoJson.getString("account");
        final JSONObject userInfoByWxid = publicManagementService.getUserInfoByWxid(wxid);
        System.out.println("userInfoByWxid: " + userInfoByWxid);

        Integer creatorOaId = null;
        if (userInfoByWxid != null) {
            creatorOaId = userInfoByWxid.getInteger("account");
        }

        if (creatorOaId == null) {
            return;
        }
        List<File> files = getOaFileList(creatorOaId, oaDocIds);
        final JSONArray fileArray =
            contentManagementService.upload2Qiqiao(files, FormFieldType.FILE, biiRegulationInfoApplicationId,
                biiRegulationInfoFormModelId);
        data.put("上传制度的上会版本", fileArray);
        updateVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
        System.out.println("saveOrUpdate: " + jsonObject);
    }

    private void rejectSpecialAudit(final String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            return;
        }

        final RecordVO queryVO = new RecordVO();
        queryVO.setApplicationId(biiRegulationInfoApplicationId);
        queryVO.setFormModelId(biiRegulationInfoFormModelId);

        final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        final FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("京投专项流程id");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(requestId);
        fieldFilterList.add(fieldFilter);
        queryVO.setFilter(fieldFilterList);

        final JSONObject pageRecord = qiqiaoFormsService.page(queryVO);
        System.out.println("pageRecord: " + pageRecord);
        if (pageRecord == null) {
            return;
        }
        final JSONArray list = pageRecord.getJSONArray("list");
        if (list == null || list.size() != 1) {
            System.out.println("WEIRD list=" + list);
            return;
        }

        final RecordVO updateVO = new RecordVO();
        updateVO.setApplicationId(biiRegulationInfoApplicationId);
        updateVO.setFormModelId(biiRegulationInfoFormModelId);

        final JSONObject regulationInfo = list.getJSONObject(0);

        updateVO.setId(regulationInfo.getString("id"));
        final Map<String, Object> data = new HashMap<>(1);
        data.put("京投专项流程状态", BiiOaWorkflowStatus.REJECTED);
        updateVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
        System.out.println("saveOrUpdate: " + jsonObject);
    }

    List<File> getOaFileList(final int creatorId, final String oaDocIds) {
        if (creatorId < 0 || StringUtils.isEmpty(oaDocIds)) {
            return new ArrayList<>();
        }

        List<File> result = new ArrayList<>();
        try {
            final String[] docIdArray = oaDocIds.split(",");
            CustomDocServicePortType customDocServiceHttpPort = customDocServiceImpl.getCustomDocServiceHttpPort();
            for (final String docId : docIdArray) {
                String session = customDocServiceHttpPort.loginByOaId(creatorId);
                final DocInfo doc = customDocServiceHttpPort.getDoc(Integer.parseInt(docId), session);
                // 取得该文档的第一个附件
                DocAttachment da = doc.getAttachments()[0];
                // 得到附件内容
                byte[] content = Base64.decode(da.getFilecontent());
                final File file = new File(da.getFilename());
                int byteread;
                byte[] data = new byte[1024];
                InputStream inputStream = null;
                if (da.getIszip() == 1) {
                    ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(content));
                    if (zin.getNextEntry() != null) {
                        inputStream = new BufferedInputStream(zin);
                    }
                }

                // Tong: 接口文档有点问题，这里加了一步判断
                if (inputStream == null) {
                    inputStream = new ByteArrayInputStream(content);
                }
                OutputStream out = Files.newOutputStream(file.toPath());
                while ((byteread = inputStream.read(data)) != -1) {
                    out.write(data, 0, byteread);
                    out.flush();
                }
                inputStream.close();
                out.close();
                result.add(file);
            }
        } catch (Exception e) {
            System.out.println("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }

        return result;
    }
}
