package org.jeecg.modules.regulation.service.impl;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
import org.jeecg.modules.regulation.service.IZyOaRequestBiiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipInputStream;

import static org.jeecg.modules.oa.webservices.soap.workflow.WorkflowUtils.*;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Service @Slf4j public class ZyOaRequestBiiServiceImpl implements IZyOaRequestBiiService {
    @Autowired private WorkflowServiceImpl workflowClient;
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;
    @Autowired private IQiqiaoService qiqiaoService;
    @Autowired private IPublicManagementService publicManagementService;
    @Value("${biisaas.biiRegulationInfo.applicationId}") private String biiRegulationInfoApplicationId;
    @Value("${biisaas.biiRegulationInfo.formModelId}") private String biiRegulationInfoFormModelId;
    @Value("${biisaas.biiRegulationInfo.regulationPlanFormModelId}") private String biiRegulationPlanFormModelId;
    @Value("${oa-workflow.bii-regulation-plan}") private String biiRegulationPlanWorkflowId;
    @Value("${oa-workflow.bii-regulation-plan-name}") private String biiRegulationPlanWorkflowNameTemplate;
    @Value("${oa-workflow.bii-special-audit}") private String biiSpecialAuditWorkflowId;
    @Value("${oa-workflow.bii-special-audit-name}") private String biiSpecialAuditWorkflowNameTemplate;
    @Value("${oa-workflow.shiyebu-special-audit}") private String shiyebuSpecialAuditWorkflowId;
    @Value("${oa-workflow.shiyebu-special-audit-name}") private String shiyebuSpecialAuditWorkflowNameTemplate;
    @Autowired @Qualifier("biiContentManagementService") private IContentManagementService contentManagementService;
    @Autowired private CustomDocServiceImpl customDocServiceImpl;

    @Override public String createRegulationPlan(final String qiqiaoUserId, final String qiqiaoRegulationPlanId) {
        if (StringUtils.isEmpty(qiqiaoUserId) || StringUtils.isEmpty(qiqiaoRegulationPlanId)) {
            log.warn("EMPTY INPUT!");
            return null;
        }

        String requestId = null;
        try {
            // 获取用户信息
            // 验证七巧用户
            final JSONObject usersInfoJson = qiqiaoService.usersInfo(qiqiaoUserId);
            log.info("usersInfoJson: " + usersInfoJson);
            if (usersInfoJson == null) {
                log.warn("CANNOT FIND USER OA ID FOR qiqiaoUserId " + qiqiaoUserId);
                return null;
            }
            final String wxid = usersInfoJson.getString("account");
            final JSONObject userInfoByWxid = publicManagementService.getUserInfoByWxid(wxid);
            if (userInfoByWxid == null) {
                log.warn("CANNOT FIND USER OA ID FOR wxid " + wxid);
                return null;
            }
            final String oaId = userInfoByWxid.getString("account");
            if ("0".equals(oaId) || StringUtils.isEmpty(oaId)) {
                log.warn("CANNOT FIND USER OA ID FOR qiqiaoUserId " + qiqiaoUserId);
                return null;
            }

            final String creatorName = userInfoByWxid.getString("nickName");
            final WorkflowRequestInfo regulationPlan =
                createRegulationPlan(Integer.parseInt(oaId), creatorName, qiqiaoRegulationPlanId);
            if (regulationPlan == null) {
                return null;
            }
            final WorkflowServicePortType service = workflowClient.getWorkflowServiceHttpPort();
            requestId = service.doCreateWorkflowRequest(regulationPlan, Integer.parseInt(oaId));
            log.info("qiqiaoRegulationPlanId: " + qiqiaoRegulationPlanId + ", requestId: " + requestId);
            if (StringUtils.isNotEmpty(requestId) && !requestId.startsWith("-")) {
                // 更新七巧计划单状态及流程ID
                approvingRegulationPlan(qiqiaoRegulationPlanId, requestId);
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }

        return requestId;
    }

    @Override public void approveRegulationPlan(final String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            return;
        }

        // 更新【制度计划立项单】
        String qiqiaoRegulationPlanId = null;
        {
            final RecordVO queryVO = new RecordVO();
            queryVO.setApplicationId(biiRegulationInfoApplicationId);
            queryVO.setFormModelId(biiRegulationPlanFormModelId);

            final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            final FieldFilter fieldFilter = new FieldFilter();
            fieldFilter.setFieldName("oaid");
            fieldFilter.setLogic("eq");
            fieldFilter.setValue(requestId);
            fieldFilterList.add(fieldFilter);
            queryVO.setFilter(fieldFilterList);

            final JSONObject regulationInfoRecord = qiqiaoFormsService.page(queryVO);
            log.info("regulationInfoRecord: " + regulationInfoRecord);
            if (regulationInfoRecord == null) {
                return;
            }
            final JSONArray regulationPlanList = regulationInfoRecord.getJSONArray("list");
            if (regulationPlanList == null || regulationPlanList.size() != 1) {
                log.warn("WEIRD regulationPlanList=" + regulationPlanList);
                return;
            }

            final JSONObject regulationPlan = regulationPlanList.getJSONObject(0);

            final RecordVO updateVO = new RecordVO();
            updateVO.setApplicationId(biiRegulationInfoApplicationId);
            updateVO.setFormModelId(biiRegulationPlanFormModelId);

            qiqiaoRegulationPlanId = regulationPlan.getString("id");
            updateVO.setId(qiqiaoRegulationPlanId);

            final Map<String, Object> data = new HashMap<>(1);
            data.put("制度计划流程状态", BiiOaWorkflowStatus.APPROVED);
            updateVO.setData(data);

            log.info("updateVO: " + updateVO);

            final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
            log.info("saveOrUpdate: " + jsonObject);
        }

        // 更新【制度信息单】
        if (StringUtils.isNotEmpty(qiqiaoRegulationPlanId)) {
            final RecordVO queryVO = new RecordVO();
            queryVO.setApplicationId(biiRegulationInfoApplicationId);
            queryVO.setFormModelId(biiRegulationInfoFormModelId);
            // MORE THAN 1000 PLANS? NO ONE WOULD BE THAT CRAZY...
            queryVO.setPageSize(1000);

            final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            final FieldFilter fieldFilter = new FieldFilter();
            fieldFilter.setFieldName("外键");
            fieldFilter.setLogic("eq");
            fieldFilter.setValue(qiqiaoRegulationPlanId);
            fieldFilterList.add(fieldFilter);
            queryVO.setFilter(fieldFilterList);

            final JSONObject regulationInfoRecord = qiqiaoFormsService.page(queryVO);
            log.info("regulationInfoRecord: " + regulationInfoRecord);
            if (regulationInfoRecord == null) {
                return;
            }
            final JSONArray regulationInfoList = regulationInfoRecord.getJSONArray("list");

            final RecordVO updateVO = new RecordVO();
            updateVO.setApplicationId(biiRegulationInfoApplicationId);
            updateVO.setFormModelId(biiRegulationInfoFormModelId);
            for (int i = 0; i < regulationInfoList.size(); ++i) {
                final JSONObject regulationInfo = regulationInfoList.getJSONObject(i);
                log.info("regulationInfo: " + regulationInfo);

                final JSONObject variables = regulationInfo.getJSONObject("variables");
                final JSONObject prettyValue = regulationInfo.getJSONObject("prettyValue");

                final String id = regulationInfo.getString("id");
                updateVO.setId(id);

                final Map<String, Object> data = new HashMap<>();
                data.put("制度归口管理部门", "1");
                data.put("可编辑", "1");
                data.put("可删除", "1");
                data.put("可预览", "1");

                final String type = variables.getString("制度分类计划版");
                if (StringUtils.isNotEmpty(type)) {
                    data.put("制度分类", type);
                }
                final String rank = variables.getString("制度级别计划版");
                if (StringUtils.isNotEmpty(rank)) {
                    data.put("制度级别", rank);
                }
                final String num = variables.getString("制度建设编码");
                if (StringUtils.isNotEmpty(num)) {
                    data.put("制度建设编号文本", num);
                }

                final String name1 = prettyValue.getString("修订前的制度名称");
                final String name2 = variables.getString("修订或新增后的制度名称");

                String name = null;
                if (StringUtils.isNotEmpty(name1)) {
                    name = name1;
                } else if (StringUtils.isNotEmpty(name2)) {
                    name = name2;
                }
                data.put("制度名称", name);

                final String buildType = variables.getString("制度建设类型");
                if ("1".equals(buildType) || "2".equals(buildType) || "3".equals(buildType) || "4".equals(buildType)) {
                    data.put("制度状态", "2");
                }

                updateVO.setData(data);
                log.info("updateVO: " + updateVO);
                final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
                log.info("saveOrUpdate: " + jsonObject);
            }
        }
    }

    @Override public void rejectRegulationPlan(final String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            return;
        }

        final RecordVO queryVO = new RecordVO();
        queryVO.setApplicationId(biiRegulationInfoApplicationId);
        queryVO.setFormModelId(biiRegulationPlanFormModelId);

        final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        final FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("oaid");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(requestId);
        fieldFilterList.add(fieldFilter);
        queryVO.setFilter(fieldFilterList);

        final JSONObject regulationInfoRecord = qiqiaoFormsService.page(queryVO);
        log.info("regulationInfoRecord: " + regulationInfoRecord);
        if (regulationInfoRecord == null) {
            return;
        }
        final JSONArray regulationPlanList = regulationInfoRecord.getJSONArray("list");
        if (regulationPlanList == null || regulationPlanList.size() != 1) {
            log.warn("WEIRD regulationPlanList=" + regulationPlanList);
            return;
        }

        final RecordVO updateVO = new RecordVO();
        updateVO.setApplicationId(biiRegulationInfoApplicationId);
        updateVO.setFormModelId(biiRegulationPlanFormModelId);

        final JSONObject regulationPlan = regulationPlanList.getJSONObject(0);

        updateVO.setId(regulationPlan.getString("id"));
        final Map<String, Object> data = new HashMap<>(1);
        data.put("制度计划流程状态", BiiOaWorkflowStatus.REJECTED);
        updateVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
        log.info("saveOrUpdate: " + jsonObject);
    }

    @Override public String createSpecialAudit(final String qiqiaoUserId, final String qiqiaoRegulationInfoId) {
        if (StringUtils.isEmpty(qiqiaoUserId) || StringUtils.isEmpty(qiqiaoRegulationInfoId)) {
            log.error("EMPTY INPUT!");
            return null;
        }
        final JSONObject usersInfoJson = qiqiaoService.usersInfo(qiqiaoUserId);
        if (usersInfoJson == null) {
            log.warn("usersInfoJson IS EMPTY");
            return null;
        }

        final String wxid = usersInfoJson.getString("account");
        final JSONObject userInfoByWxid = publicManagementService.getUserInfoByWxid(wxid);
        log.info("userInfoByWxid: " + userInfoByWxid);

        if (userInfoByWxid == null) {
            log.warn("userInfoByWxid iS EMPTY");
            return null;
        }

        final Integer creatorId = userInfoByWxid.getInteger("account");
        final String creatorName = userInfoByWxid.getString("nickName");
        final String createDeptId = userInfoByWxid.getString("orgOaId");

        String requestId = null;
        try {
            final WorkflowRequestInfo requestInfo =
                createSpecialAuditRequestInfo(qiqiaoRegulationInfoId, creatorId, creatorName, createDeptId);
            if (requestInfo == null) {
                return null;
            }
            final WorkflowServicePortType service = workflowClient.getWorkflowServiceHttpPort();
            requestId = service.doCreateWorkflowRequest(requestInfo, creatorId);
            log.info("qiqiaoRegulationInfoId: " + qiqiaoRegulationInfoId + ", requestId: " + requestId);
            if (StringUtils.isNotEmpty(requestId) && !requestId.startsWith("-")) {
                // 更新七巧计划单状态及流程ID
                approvingSpecialAudit(qiqiaoRegulationInfoId, requestId);
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }
        return requestId;
    }

    @Override public void approveSpecialAudit(final String requestId, final String creatorId, final String oaDocIds) {
        if (StringUtils.isEmpty(requestId) || StringUtils.isEmpty(creatorId) || StringUtils.isEmpty(oaDocIds)) {
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
        log.info("pageRecord: " + pageRecord);
        if (pageRecord == null) {
            return;
        }
        final JSONArray list = pageRecord.getJSONArray("list");
        if (list == null || list.size() != 1) {
            log.info("WEIRD list=" + list);
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

        final List<File> files = getOaFileList(creatorId, oaDocIds);
        try {
            if (CollectionUtils.isEmpty(files)) {
                log.warn("files IS EMPTY!");
                return;
            }

            final JSONArray fileArray =
                contentManagementService.upload2Qiqiao(files, FormFieldType.FILE, biiRegulationInfoApplicationId,
                    biiRegulationInfoFormModelId);
            data.put("上传制度的上会版本", fileArray);
            updateVO.setData(data);
            final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
            log.info("saveOrUpdate: " + jsonObject);
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        } finally {
            if (CollectionUtils.isNotEmpty(files)) {
                for (final File file : files) {
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }
    }

    @Override public void rejectSpecialAudit(final String requestId) {
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
        log.info("pageRecord: " + pageRecord);
        if (pageRecord == null) {
            return;
        }
        final JSONArray list = pageRecord.getJSONArray("list");
        if (list == null || list.size() != 1) {
            log.info("WEIRD list=" + list);
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
        log.info("saveOrUpdate: " + jsonObject);
    }

    @Override public String createShiyebuSpecialAudit(final String qiqiaoUserId, final String qiqiaoRegulationInfoId) {
        if (StringUtils.isEmpty(qiqiaoUserId) || StringUtils.isEmpty(qiqiaoRegulationInfoId)) {
            log.error("EMPTY INPUT!");
            return null;
        }
        final JSONObject usersInfoJson = qiqiaoService.usersInfo(qiqiaoUserId);
        if (usersInfoJson == null) {
            log.warn("usersInfoJson IS EMPTY");
            return null;
        }

        final String wxid = usersInfoJson.getString("account");
        final JSONObject userInfoByWxid = publicManagementService.getUserInfoByWxid(wxid);
        log.info("userInfoByWxid: " + userInfoByWxid);

        if (userInfoByWxid == null) {
            log.warn("userInfoByWxid iS EMPTY");
            return null;
        }

        final Integer creatorId = userInfoByWxid.getInteger("account");
        final String creatorName = userInfoByWxid.getString("nickName");
        final String createDeptId = userInfoByWxid.getString("orgOaId");

        String requestId = null;
        try {
            final WorkflowRequestInfo requestInfo =
                createShiyebuSpecialAuditRequestInfo(qiqiaoRegulationInfoId, creatorId, creatorName, createDeptId);
            if (requestInfo == null) {
                return null;
            }
            final WorkflowServicePortType service = workflowClient.getWorkflowServiceHttpPort();
            requestId = service.doCreateWorkflowRequest(requestInfo, creatorId);
            log.info("qiqiaoRegulationInfoId: " + qiqiaoRegulationInfoId + ", requestId: " + requestId);
            if (StringUtils.isNotEmpty(requestId) && !requestId.startsWith("-")) {
                // 更新七巧计划单状态及流程ID
                approvingShiyebuSpecialAudit(qiqiaoRegulationInfoId, requestId);
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }

        return requestId;
    }

    @Override public void approveShiyebuSpecialAudit(final String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            return;
        }

        final RecordVO queryVO = new RecordVO();
        queryVO.setApplicationId(biiRegulationInfoApplicationId);
        queryVO.setFormModelId(biiRegulationInfoFormModelId);

        final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        final FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("多经专项流程id");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(requestId);
        fieldFilterList.add(fieldFilter);
        queryVO.setFilter(fieldFilterList);

        final JSONObject pageRecord = qiqiaoFormsService.page(queryVO);
        log.info("pageRecord: " + pageRecord);
        if (pageRecord == null) {
            return;
        }
        final JSONArray list = pageRecord.getJSONArray("list");
        if (list == null || list.size() != 1) {
            log.info("WEIRD list=" + list);
            return;
        }

        final RecordVO updateVO = new RecordVO();
        updateVO.setApplicationId(biiRegulationInfoApplicationId);
        updateVO.setFormModelId(biiRegulationInfoFormModelId);

        final JSONObject regulationInfo = list.getJSONObject(0);

        updateVO.setId(regulationInfo.getString("id"));
        final Map<String, Object> data = new HashMap<>(1);
        data.put("多经专项流程状态", BiiOaWorkflowStatus.APPROVED);
        updateVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
        log.info("saveOrUpdate: " + jsonObject);
    }

    @Override public void rejectShiyebuSpecialAudit(final String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            return;
        }

        final RecordVO queryVO = new RecordVO();
        queryVO.setApplicationId(biiRegulationInfoApplicationId);
        queryVO.setFormModelId(biiRegulationInfoFormModelId);

        final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        final FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("多经专项流程id");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(requestId);
        fieldFilterList.add(fieldFilter);
        queryVO.setFilter(fieldFilterList);

        final JSONObject pageRecord = qiqiaoFormsService.page(queryVO);
        log.info("pageRecord: " + pageRecord);
        if (pageRecord == null) {
            return;
        }
        final JSONArray list = pageRecord.getJSONArray("list");
        if (list == null || list.size() != 1) {
            log.info("WEIRD list=" + list);
            return;
        }

        final RecordVO updateVO = new RecordVO();
        updateVO.setApplicationId(biiRegulationInfoApplicationId);
        updateVO.setFormModelId(biiRegulationInfoFormModelId);

        final JSONObject regulationInfo = list.getJSONObject(0);

        updateVO.setId(regulationInfo.getString("id"));
        final Map<String, Object> data = new HashMap<>(1);
        data.put("多经专项流程状态", BiiOaWorkflowStatus.REJECTED);
        updateVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
        log.info("saveOrUpdate: " + jsonObject);
    }

    private WorkflowRequestInfo createRegulationPlan(final Integer creatorId, final String creatorName,
        final String qiqiaoRegulationPlanId) {
        if (StringUtils.isEmpty(qiqiaoRegulationPlanId)) {
            return null;
        }

        final String createTime = DateUtils.getCurrentDateInBeijing();
        final String requestName =
            biiRegulationPlanWorkflowNameTemplate.replace("{creator}", creatorName).replace("{date}", createTime);

        // 设置基础信息
        final WorkflowRequestInfo result = new WorkflowRequestInfo();
        result.setRequestName(requestName);
        result.setCreatorId(String.valueOf(creatorId));

        // 流程基本信息
        final WorkflowBaseInfo wbi = new WorkflowBaseInfo();
        wbi.setWorkflowId(String.valueOf(biiRegulationPlanWorkflowId));
        result.setWorkflowBaseInfo(wbi);

        // 主表
        final List<WorkflowRequestTableField> regulationInfoTableFields =
            convertQiqiaoRegulationPlan(qiqiaoRegulationPlanId);
        final WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];
        wrtri[0] = new WorkflowRequestTableRecord();
        wrtri[0].setWorkflowRequestTableFields(toTableFieldArray(regulationInfoTableFields));
        final WorkflowMainTableInfo wmi = new WorkflowMainTableInfo();
        wmi.setRequestRecords(wrtri);
        result.setWorkflowMainTableInfo(wmi);

        // 副表
        final List<WorkflowDetailTableInfo> wdti = new ArrayList<>();
        final WorkflowDetailTableInfo workflowDetailTableInfo = new WorkflowDetailTableInfo();
        final List<WorkflowRequestTableRecord> workflowRequestTableRecords =
            convertQiqiaoRegulationInfoListFromPlan(qiqiaoRegulationPlanId);
        workflowDetailTableInfo.setWorkflowRequestTableRecords(toTableRecordArray(workflowRequestTableRecords));
        wdti.add(workflowDetailTableInfo);
        result.setWorkflowDetailTableInfos(toDetailTableInfoArray(wdti));

        return result;
    }

    private List<WorkflowRequestTableField> convertQiqiaoRegulationPlan(final String qiqiaoRegulationPlanId) {
        if (StringUtils.isEmpty(qiqiaoRegulationPlanId)) {
            return null;
        }

        final List<WorkflowRequestTableField> result = new ArrayList<>();

        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationPlanFormModelId);
        recordVO.setId(qiqiaoRegulationPlanId);

        //        {
        //            "variables": {
        //                "立项编号": "202312京投公司-信息数据管理部015",
        //                "发起人_pretty_value": "凌童",
        //                "发起人": "85e123417e72221947a83c682f009567",
        //                "立项年度": 1672502400000,
        //                "发起人部门_pretty_value": "京投公司-信息数据管理部",
        //                "发起人部门": "7c1c05ac71614da0a66decf0add9cac1",
        //                "计划类型": "1",
        //                "申请日期": 1701878400000
        //            },
        //            "author": "85e123417e72221947a83c682f009567",
        //            "authorName": "凌童",
        //            "formDefinitionId": "e419f347a3294b578b5fa936c2d8832b",
        //            "lastModifierName": "凌童",
        //            "prettyValue": {
        //                "发起人": "凌童",
        //                "发起人部门": "京投公司-信息数据管理部",
        //                "计划类型": "年度计划"
        //            },
        //            "id": "8186487560614092800",
        //            "applicationId": "cd363f9220154b3dba4d4a4cbcaea9c6",
        //            "lastModifyDate": 1701931924000,
        //            "version": 1,
        //            "createDate": 1701931924000
        //        }
        final JSONObject planRecord = qiqiaoFormsService.queryById(recordVO);
        log.info("planRecord: " + planRecord);
        if (planRecord == null) {
            return null;
        }

        final JSONObject variables = planRecord.getJSONObject("variables");
        final JSONObject prettyValue = planRecord.getJSONObject("prettyValue");

        final String creator = prettyValue.getString("发起人");
        if (StringUtils.isNotEmpty(creator)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("fqr");
            tableField.setFieldValue(creator);
            result.add(tableField);
        }

        final String createDept = prettyValue.getString("发起人部门");
        if (StringUtils.isNotEmpty(createDept)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("fqbm");
            tableField.setFieldValue(createDept);
            result.add(tableField);
        }

        final Date createTime = variables.getDate("申请日期");
        final String createTimeStr = DateUtils.formatDate(createTime);
        if (StringUtils.isNotEmpty(createTimeStr)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sqrq");
            tableField.setFieldValue(createTimeStr);
            result.add(tableField);
        }

        if (StringUtils.isNotEmpty(createTimeStr)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("jhnd");
            tableField.setFieldValue(createTimeStr.substring(0, 4));
            result.add(tableField);
        }

        final String planIdentifier = variables.getString("立项编号");
        if (StringUtils.isNotEmpty(planIdentifier)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("jhbh");
            tableField.setFieldValue(planIdentifier);
            result.add(tableField);
        }

        final String planType = prettyValue.getString("计划类型");
        if (StringUtils.isNotEmpty(planType)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("jhlx");
            tableField.setFieldValue(planType);
            result.add(tableField);
        }

        for (WorkflowRequestTableField workflowRequestTableField : result) {
            workflowRequestTableField.setEdit(true);
            workflowRequestTableField.setView(true);
        }

        return result;
    }

    private List<WorkflowRequestTableRecord> convertQiqiaoRegulationInfoListFromPlan(
        final String qiqiaoRegulationPlanId) {
        final List<WorkflowRequestTableRecord> result = new ArrayList<>();

        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationInfoFormModelId);
        // MORE THAN 1000 PLANS? NO ONE WOULD BE THAT CRAZY...
        recordVO.setPageSize(1000);

        final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        final FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("外键");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(qiqiaoRegulationPlanId);
        fieldFilterList.add(fieldFilter);
        recordVO.setFilter(fieldFilterList);

        // {
        //   "currPage": 1,
        //   "totalPage": 1,
        //   "pageSize": 1000,
        //   "list": [
        //     {
        //       "variables": {
        //         "制度跟进人": "[\"fa8c33dad56943b5ec479cec374c11a1\"]",
        //         "制度归口管理部门": "1",
        //         "制度分类计划版": "1",
        //         "制度建设类型": "2",
        //         "修订前的制度名称": "8073686704214810626",
        //         "制度审批备案程序拟完成时间": "2023-12-21",
        //         "制度建设编码": "202312ZDJS0021",
        //         "备注": "备注一下吧",
        //         "制度级别计划版": "3",
        //         "外键": "8186487560614092800",
        //         "制度唯一标识文本": "202310ZDBS0009",
        //         "制度状态": "1",
        //         "制度主责部门": "[\"7c1c05ac71614da0a66decf0add9cac1\"]",
        //         "任务来源": "还是任务来源",
        //         "主要内容修订内容": "这是我的内容",
        //         "制度名称": ""
        //       },
        //       "author": "85e123417e72221947a83c682f009567",
        //       "authorName": "凌童",
        //       "formDefinitionId": "",
        //       "lastModifierName": "凌童",
        //       "prettyValue": {
        //         "修订前的制度名称": "测试1",
        //         "制度跟进人": "王璐瑶",
        //         "制度级别计划版": "三级制度(J3)",
        //         "外键": "202312京投公司-信息数据管理部015",
        //         "制度归口管理部门": "京投办公室",
        //         "制度状态": "计划中",
        //         "制度分类计划版": "董事会管理(DS)",
        //         "制度建设类型": "修订",
        //         "制度主责部门": "京投公司-信息数据管理部"
        //       },
        //       "id": "8186493676647530507",
        //       "applicationId": "",
        //       "lastModifyDate": 1701931924000,
        //       "version": 1,
        //       "createDate": 1701931926000
        //     },
        //     {
        //       "variables": {
        //         "制度跟进人": "[\"fa8c33dad56943b5ec479cec374c11a1\", \"85e123417e72221947a83c682f009567\"]",
        //         "制度唯一标示": "202312ZDBS0014",
        //         "修订或新增后的制度名称": "我的建议制度",
        //         "制度分类计划版": "6",
        //         "制度建设类型": "1",
        //         "制度审批备案程序拟完成时间": "2023-12-14",
        //         "制度建设编码": "202312ZDJS0020",
        //         "备注": "这是备注",
        //         "制度级别计划版": "2",
        //         "外键": "8186487560614092800",
        //         "制度状态": "1",
        //         "制度主责部门": "[\"7c1c05ac71614da0a66decf0add9cac1\"]",
        //         "任务来源": "这是任务来源",
        //         "主要内容修订内容": "这条是主要内容",
        //         "制度名称": "我的建议制度"
        //       },
        //       "author": "85e123417e72221947a83c682f009567",
        //       "authorName": "凌童",
        //       "formDefinitionId": "",
        //       "lastModifierName": "凌童",
        //       "prettyValue": {
        //         "修订前的制度名称": "",
        //         "制度跟进人": "王璐瑶,凌童",
        //         "制度级别计划版": "二级制度(J2)",
        //         "外键": "202312京投公司-信息数据管理部015",
        //         "制度状态": "计划中",
        //         "制度分类计划版": "法律合规管理(FG)",
        //         "制度建设类型": "新建",
        //         "制度主责部门": "京投公司-信息数据管理部"
        //       },
        //       "id": "8186493676647530506",
        //       "applicationId": "",
        //       "lastModifyDate": 1701931924000,
        //       "version": 1,
        //       "createDate": 1701931925000
        //     }
        //   ],
        //   "totalCount": 2
        // }
        final JSONObject regulationInfoRecord = qiqiaoFormsService.page(recordVO);
        log.info("regulationInfoRecord: " + regulationInfoRecord);
        if (regulationInfoRecord == null) {
            return null;
        }
        final JSONArray regulationInfoList = regulationInfoRecord.getJSONArray("list");
        for (int i = 0; i < regulationInfoList.size(); ++i) {
            final JSONObject regulationInfo = regulationInfoList.getJSONObject(i);
            final List<WorkflowRequestTableField> workflowRequestTableFieldList = convertRegulationInfo(regulationInfo);
            if (CollectionUtils.isEmpty(workflowRequestTableFieldList)) {
                log.info("FAILED TO CONVERT REGULATION INFO regulationInfo=" + regulationInfo);
                continue;
            }

            final WorkflowRequestTableRecord workflowRequestTableRecord = new WorkflowRequestTableRecord();
            workflowRequestTableRecord.setWorkflowRequestTableFields(toTableFieldArray(workflowRequestTableFieldList));
            result.add(workflowRequestTableRecord);
        }

        return result;
    }

    private List<WorkflowRequestTableField> convertRegulationInfo(final JSONObject regulationInfo) {
        final List<WorkflowRequestTableField> result = new ArrayList<>();
        if (regulationInfo == null) {
            return null;
        }

        //     {
        //       "variables": {
        //         "制度跟进人": "[\"fa8c33dad56943b5ec479cec374c11a1\"]",
        //         "制度归口管理部门": "1",
        //         "制度分类计划版": "1",
        //         "制度建设类型": "2",
        //         "修订前的制度名称": "8073686704214810626",
        //         "制度审批备案程序拟完成时间": "2023-12-21",
        //         "制度建设编码": "202312ZDJS0021",
        //         "备注": "备注一下吧",
        //         "制度级别计划版": "3",
        //         "外键": "8186487560614092800",
        //         "制度唯一标识文本": "202310ZDBS0009",
        //         "制度状态": "1",
        //         "制度主责部门": "[\"7c1c05ac71614da0a66decf0add9cac1\"]",
        //         "任务来源": "还是任务来源",
        //         "主要内容修订内容": "这是我的内容",
        //         "制度名称": ""
        //       },
        //       "author": "85e123417e72221947a83c682f009567",
        //       "authorName": "凌童",
        //       "formDefinitionId": "",
        //       "lastModifierName": "凌童",
        //       "prettyValue": {
        //         "修订前的制度名称": "测试1",
        //         "制度跟进人": "王璐瑶",
        //         "制度级别计划版": "三级制度(J3)",
        //         "外键": "202312京投公司-信息数据管理部015",
        //         "制度归口管理部门": "京投办公室",
        //         "制度状态": "计划中",
        //         "制度分类计划版": "董事会管理(DS)",
        //         "制度建设类型": "修订",
        //         "制度主责部门": "京投公司-信息数据管理部"
        //       },
        //       "id": "8186493676647530507",
        //       "applicationId": "",
        //       "lastModifyDate": 1701931924000,
        //       "version": 1,
        //       "createDate": 1701931926000
        //     },
        final JSONObject variables = regulationInfo.getJSONObject("variables");
        final JSONObject prettyValue = regulationInfo.getJSONObject("prettyValue");

        {
            final String type = prettyValue.getString("制度建设类型");
            if (StringUtils.isNotEmpty(type)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("zdjslx");
                tableField.setFieldValue(type);
                result.add(tableField);
            }
        }
        {
            final String name = variables.getString("修订或新增后的制度名称");
            if (StringUtils.isNotEmpty(name)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("jyzdmc");
                tableField.setFieldValue(name);
                result.add(tableField);
            }
        }
        {
            final String oldName = prettyValue.getString("修订前的制度名称");
            if (StringUtils.isNotEmpty(oldName)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("yzdmc");
                tableField.setFieldValue(oldName);
                result.add(tableField);
            }
        }
        {
            final String mainDept = prettyValue.getString("制度主责部门");
            if (StringUtils.isNotEmpty(mainDept)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("zdzzbm");
                tableField.setFieldValue(mainDept);
                result.add(tableField);
            }
        }
        {
            final String planedFinishTime = variables.getString("制度审批备案程序拟完成时间");
            if (StringUtils.isNotEmpty(planedFinishTime)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("nwcsj");
                tableField.setFieldValue(planedFinishTime);
                result.add(tableField);
            }
        }
        {
            final String responsiblePerson = prettyValue.getString("制度跟进人");
            if (StringUtils.isNotEmpty(responsiblePerson)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("zdbzr");
                tableField.setFieldValue(responsiblePerson);
                result.add(tableField);
            }
        }
        {
            final String category = prettyValue.getString("制度分类计划版");
            if (StringUtils.isNotEmpty(category)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("jyzdfl");
                tableField.setFieldValue(category);
                result.add(tableField);
            }
        }
        {
            final String name = prettyValue.getString("制度级别计划版");
            if (StringUtils.isNotEmpty(name)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("jyzdjb");
                tableField.setFieldValue(name);
                result.add(tableField);
            }
        }
        {
            final String content = variables.getString("主要内容修订内容");
            if (StringUtils.isNotEmpty(content)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("zdzynr");
                tableField.setFieldValue(content);
                result.add(tableField);
            }
        }
        {
            final String source = variables.getString("任务来源");
            if (StringUtils.isNotEmpty(source)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("rwly");
                tableField.setFieldValue(source);
                result.add(tableField);
            }
        }
        {
            final String planedStartTime = variables.getString("计划生效时间");
            if (StringUtils.isNotEmpty(planedStartTime)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("jhsxsj");
                tableField.setFieldValue(planedStartTime);
                result.add(tableField);
            }
        }
        {
            final String remark = variables.getString("备注");
            if (StringUtils.isNotEmpty(remark)) {
                final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
                tableField.setFieldName("bz");
                tableField.setFieldValue(remark);
                result.add(tableField);
            }
        }

        for (WorkflowRequestTableField workflowRequestTableField : result) {
            workflowRequestTableField.setEdit(true);
            workflowRequestTableField.setView(true);
        }

        return result;
    }

    private void approvingRegulationPlan(final String qiqiaoRegulationPlanId, final String requestId) {
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationPlanFormModelId);
        recordVO.setId(qiqiaoRegulationPlanId);

        final Map<String, Object> data = new HashMap<>(2);
        data.put("oaid", requestId);
        data.put("制度计划流程状态", BiiOaWorkflowStatus.APPROVING);
        recordVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(recordVO);
        log.info("saveOrUpdate: " + jsonObject);
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
        log.info("infoRecord: " + infoRecord);
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
        log.info("saveOrUpdate: " + jsonObject);
    }

    private void approvingShiyebuSpecialAudit(final String qiqiaoRegulationInfoId, final String requestId) {
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationInfoId);

        final Map<String, Object> data = new HashMap<>(2);
        data.put("多经专项流程id", requestId);
        data.put("多经专项流程状态", BiiOaWorkflowStatus.APPROVING);
        recordVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(recordVO);
        log.info("saveOrUpdate: " + jsonObject);
    }

    List<File> getOaFileList(final String creatorId, final String oaDocIds) {
        log.info("[getOaFileList] creatorId: " + creatorId + ", oaDocIds: " + oaDocIds);
        if (StringUtils.isEmpty(creatorId) || StringUtils.isEmpty(oaDocIds)) {
            return new ArrayList<>();
        }

        List<File> result = new ArrayList<>();
        try {
            final String[] docIdArray = oaDocIds.split(",");
            CustomDocServicePortType customDocServiceHttpPort = customDocServiceImpl.getCustomDocServiceHttpPort();
            for (final String docId : docIdArray) {
                String session = customDocServiceHttpPort.loginByOaId(Integer.parseInt(creatorId));
                final DocInfo doc = customDocServiceHttpPort.getDoc(Integer.parseInt(docId), session);
                // 取得该文档的第一个附件
                final DocAttachment da = doc.getAttachments()[0];
                // 得到附件内容
                byte[] content = Base64.decode(da.getFilecontent());
                final String filename = da.getFilename();
                final File file = new File(filename);
                int byteread;
                byte[] data = new byte[1024];
                InputStream inputStream = null;
                if (filename.endsWith("zip") || filename.endsWith("rar") || filename.endsWith(
                    "7z") || filename.endsWith("tar") || filename.endsWith("gz")) {
                    ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(content));
                    if (zin.getNextEntry() != null) {
                        inputStream = new BufferedInputStream(zin);
                    }
                } else {
                    inputStream = new ByteArrayInputStream(content);
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
            log.info("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }

        return result;
    }

    private WorkflowRequestInfo createShiyebuSpecialAuditRequestInfo(final String qiqiaoRegulationInfoId,
        final Integer creatorId, final String creatorName, final String createDeptId) {
        if (StringUtils.isEmpty(qiqiaoRegulationInfoId)) {
            return null;
        }

        final String createTime = DateUtils.getCurrentDateInBeijing();
        final String requestName =
            shiyebuSpecialAuditWorkflowNameTemplate.replace("{creator}", creatorName).replace("{date}", createTime);
        final String workflowId = shiyebuSpecialAuditWorkflowId;

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
            convertShiyebuQiqiaoSpecialAudit(String.valueOf(creatorId), createDeptId, qiqiaoRegulationInfoId);
        final WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];
        wrtri[0] = new WorkflowRequestTableRecord();
        wrtri[0].setWorkflowRequestTableFields(toTableFieldArray(regulationInfoTableFields));
        final WorkflowMainTableInfo wmi = new WorkflowMainTableInfo();
        wmi.setRequestRecords(wrtri);
        result.setWorkflowMainTableInfo(wmi);

        result.setIsnextflow("0");

        return result;
    }

    private List<WorkflowRequestTableField> convertShiyebuQiqiaoSpecialAudit(final String creatorId,
        final String createDeptId, final String qiqiaoRegulationInfoId) {
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
        log.info("infoRecord: " + infoRecord);
        if (infoRecord == null) {
            return null;
        }

        final JSONObject variables = infoRecord.getJSONObject("variables");
        final JSONObject prettyValue = infoRecord.getJSONObject("prettyValue");

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zebm");
            tableField.setFieldValue(createDeptId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("jbr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xgbmfzr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        final String responsiblePerson = prettyValue.getString("制度跟进人");
        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zdzzbmjbr");
            tableField.setFieldValue(responsiblePerson);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("fkrq");
            tableField.setFieldValue(DateUtils.getCurrentDateInBeijing());
            result.add(tableField);
        }

        final String mainDept = prettyValue.getString("制度主责部门");
        if (StringUtils.isNotEmpty(mainDept)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zqyjbm");
            tableField.setFieldValue(mainDept);
            result.add(tableField);
        }

        final String content = variables.getString("主要内容修订内容");
        if (StringUtils.isNotEmpty(content)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zdtzyy");
            tableField.setFieldValue(content);
            result.add(tableField);
        }

        final String remark = variables.getString("备注");
        if (StringUtils.isNotEmpty(content)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zdmc2");
            tableField.setFieldValue(remark);
            result.add(tableField);
        }

        final String type = prettyValue.getString("制度建设类型");
        if (StringUtils.isNotEmpty(type)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zdbhlx");
            tableField.setFieldValue(type);
            result.add(tableField);
        }

        // scfj 事项内容-附件上传
        final JSONArray relatedFileArray = variables.getJSONArray("制度编写依据的相关文件");
        if (CollectionUtils.isNotEmpty(relatedFileArray)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("scfj");

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

        // sczdxtyjd 意见单
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

        // gzzdmc 规章制度名称
        final String nameAfter = variables.getString("制度名称");
        if (StringUtils.isNotEmpty(nameAfter)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("gzzdmc");
            tableField.setFieldValue(nameAfter);
            result.add(tableField);
        }

        // zdssg 制度送审稿
        final String regulationDocId = variables.getString("内管文档编号");
        if (StringUtils.isNotEmpty(regulationDocId) && StringUtils.isNotEmpty(nameAfter)) {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zdssg");

            tableField.setFieldType("http:" + nameAfter + ".docx");
            tableField.setFieldValue(contentManagementService.getDownloadNewestUrl(regulationDocId));
            result.add(tableField);
        }

        for (WorkflowRequestTableField workflowRequestTableField : result) {
            workflowRequestTableField.setEdit(true);
            workflowRequestTableField.setView(true);
        }

        return result;
    }

}
