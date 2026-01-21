package org.jeecg.modules.regulation.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.common.constant.ApplicationProfile;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.oa.webservices.soap.workflow.*;
import org.jeecg.modules.publicManagement.service.IPublicManagementService;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.jeecg.modules.regulation.constant.BiiOaWorkflowStatus;
import org.jeecg.modules.regulation.constant.RegulationType;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaHistory;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulationBjmoa;
import org.jeecg.modules.regulation.mapper.ZyRegulationBjmoaHistoryMapper;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaHistoryService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaService;
import org.jeecg.modules.regulation.service.IZyRelatedRegulationBjmoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.jeecg.modules.oa.webservices.soap.workflow.WorkflowUtils.toTableFieldArray;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Service @Slf4j public class ZyRegulationBjmoaHistoryServiceImpl
    extends ServiceImpl<ZyRegulationBjmoaHistoryMapper, ZyRegulationBjmoaHistory>
    implements IZyRegulationBjmoaHistoryService {
    @Autowired private ZyRegulationBjmoaHistoryMapper zyRegulationBjmoaHistoryMapper;
    @Autowired private IQiqiaoService qiqiaoService;
    @Autowired private IPublicManagementService publicManagementService;
    @Autowired private WorkflowServiceImpl workflowService;
    @Autowired @Qualifier("bjmoaContentManagementService") private IContentManagementService contentManagementService;
    @Autowired private IZyRegulationBjmoaService zyRegulationBjmoaService;
    @Autowired private IZyRelatedRegulationBjmoaService zyRelatedRegulationBjmoaService;
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;

    @Value("${oa-workflow.bjmoa-publish-company}") private String bjmoaPublishCompanyWorkflowId;
    @Value("${oa-workflow.bjmoa-publish-department}") private String bjmoaPublishDepartmentWorkflowId;
    @Value("${spring.profiles.active}") private String profile;
    @Value("${biisaas.bjmoaRegulationInfo.applicationId}") private String bjmoaRegulationInfoApplicationId;
    @Value("${biisaas.bjmoaRegulationInfo.formModelId}") private String bjmoaRegulationInfoFormModelId;

    @Override public List<ZyRegulationBjmoaHistory> queryByIdentifier(final String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return new ArrayList<>();
        }
        return zyRegulationBjmoaHistoryMapper.queryByIdentifier(identifier);
    }

    @Override public List<ZyRegulationBjmoaHistory> queryByIdentifierAndVersion(String identifier, String version) {

        if (StringUtils.isEmpty(identifier) || StringUtils.isEmpty(version)) {
            return null;
        }
        return zyRegulationBjmoaHistoryMapper.queryByIdentifierAndVersion(identifier, version);

    }

    @Override
    public List<ZyRegulationBjmoaHistory> queryByIdentifierAndVersionAndCode(String identifier, String version, String code) {
        if (StringUtils.isEmpty(identifier) || StringUtils.isEmpty(version) || StringUtils.isEmpty(code)) {
            return null;
        }
        return zyRegulationBjmoaHistoryMapper.queryByIdentifierAndVersionAndCode(identifier, version, code);
    }

    @Override public void inactivateByIdentifier(final String identifier) {
        log.info("inactivate by identifier " + identifier);
        if (StringUtils.isEmpty(identifier)) {
            return;
        }

        final List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistories = queryByIdentifier(identifier);
        if (CollectionUtils.isNotEmpty(zyRegulationBjmoaHistories)) {
            final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = zyRegulationBjmoaHistories.get(0);
            zyRegulationBjmoaHistory.setAbolishTime(new Date());
            if (updateById(zyRegulationBjmoaHistory)) {
                log.info("Abolish zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            } else {
                log.warn("FAILED TO UPDATE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            }
        } else {
            log.warn("CANNOT FIND REGULATION WITH IDENTIFIER " + identifier);
        }
    }

    @Override public void createOaRequest(final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory, final String traceId) {
        log.info(traceId + " create oa request zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
        if (zyRegulationBjmoaHistory == null || zyRegulationBjmoaHistory.getId() == null) {
            log.warn(traceId + " INPUT IS EMPTY!");
            return;
        }

        // 获取创建人信息
        final String qiqiaoCreatorId = zyRegulationBjmoaHistory.getQiqiaoCreatorId();
        final JSONObject usersInfoJson = qiqiaoService.usersInfo(qiqiaoCreatorId);
        log.info(traceId + " usersInfoJson: " + usersInfoJson);

        String createBy = null;
        String creatorId = null;
        String createDept = null;
        String createDeptId = null;
        final boolean isProd = ApplicationProfile.PROD.equals(profile);
        if (usersInfoJson == null) {
            log.warn(traceId + " CANNOT FIND USER INFO FOR qiqiaoCreatorId=" + qiqiaoCreatorId);
        } else {
            final String wxid = usersInfoJson.getString("account");
            final JSONObject userInfoByWxid = publicManagementService.getUserInfoByWxid(wxid);
            log.info(traceId + " userInfoByWxid: " + userInfoByWxid);
            if (userInfoByWxid != null) {
                creatorId = userInfoByWxid.getString("account");
                createBy = userInfoByWxid.getString("nickName");
                createDept = userInfoByWxid.getString("orgName");
                createDeptId = userInfoByWxid.getString("orgOaId");
            }
        }

        if (StringUtils.isEmpty(creatorId) || "0".equals(creatorId)) {
            log.warn(traceId + " CANNOT FIND USER INFO FOR qiqiaoCreatorId=" + qiqiaoCreatorId);
            return;
        }

        zyRegulationBjmoaHistory.setCreateBy(createBy);
        zyRegulationBjmoaHistory.setCreatorId(Integer.parseInt(creatorId));
        zyRegulationBjmoaHistory.setCreateDept(createDept);
        zyRegulationBjmoaHistory.setCreateDeptId(Integer.parseInt(createDeptId));

        // 发起OA流程
        try {
            final int daiyiOaId = 7244;
            final int daiyiDeptOaId = 849;
            final WorkflowServicePortType workflowServiceHttpPort = workflowService.getWorkflowServiceHttpPort();
            final WorkflowRequestInfo workflowRequestInfo =
                createWorkflowRequestInfo(isProd ? creatorId : String.valueOf(daiyiOaId),
                    isProd ? createDeptId : String.valueOf(daiyiDeptOaId), zyRegulationBjmoaHistory, traceId);
            if (workflowRequestInfo == null) {
                log.warn(traceId + " FAILED TO CREATE WORKFLOW REQUEST INFO");
                return;
            }
            // 让流程保持在第一个节点
            workflowRequestInfo.setIsnextflow("0");

            final String requestId = workflowServiceHttpPort
                .doCreateWorkflowRequest(workflowRequestInfo, isProd ? Integer.parseInt(creatorId) : daiyiOaId);
            if (StringUtils.isEmpty(requestId)) {
                log.warn(traceId + " FAILED TO CREATE OA REQUEST FOR zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
                // @todo 发送一个企业微信消息
            } else {
                zyRegulationBjmoaHistory.setRequestId(requestId);
                if (updateById(zyRegulationBjmoaHistory)) {
                    log.info(traceId + " SUCCEEDED TO UPDATE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
                } else {
                    log.warn(traceId + " FAILED TO UPDATE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
                }
                final String qiqiaoRegulationId = zyRegulationBjmoaHistory.getQiqiaoRegulationId();
                if (StringUtils.isEmpty(qiqiaoRegulationId)) {
                    log.warn(traceId + " FAILED TO GET QIQIAOID OF zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
                } else {
                    final RecordVO recordVO = new RecordVO();
                    recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
                    recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
                    recordVO.setId(qiqiaoRegulationId);

                    final Map<String, Object> data = new HashMap<>(2);
                    data.put("oaid", requestId);
                    data.put("oa流程状态", BiiOaWorkflowStatus.APPROVING);
                    recordVO.setData(data);
                    final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(recordVO);
                    if (jsonObject == null) {
                        log.warn(traceId + " FAILED TO UPDATE QIQIAO OA INFORMATION");
                        return;
                    }
                    log.info(traceId + " saveOrUpdate: " + jsonObject);
                }
            }
        } catch (Exception e) {
            log.error(traceId + " EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override public ZyRegulationBjmoaHistory queryByRequestId(final String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            log.warn("REQUEST ID IS EMPTY");
            return null;
        }
        return lambdaQuery().eq(ZyRegulationBjmoaHistory::getRequestId, requestId).one();
    }

    private WorkflowRequestInfo createWorkflowRequestInfo(final String creatorId, final String createDeptId,
        final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory, final String traceId) {
        if (StringUtils.isEmpty(creatorId) || StringUtils.isEmpty(createDeptId) || zyRegulationBjmoaHistory == null) {
            log.warn(traceId + " INPUT IS EMPTY");
            return null;
        }

        final String requestName = "关于印发《" + zyRegulationBjmoaHistory.getName() + "》的通知";
        final WorkflowRequestInfo result = new WorkflowRequestInfo();
        result.setRequestLevel("0");
        result.setRequestName(requestName);
        result.setCreatorId(creatorId);

        // 流程基本信息
        final WorkflowBaseInfo wbi = new WorkflowBaseInfo();
        wbi.setWorkflowId(getBjmoaWorkflowId(zyRegulationBjmoaHistory, traceId));
        if (StringUtils.isEmpty(wbi.getWorkflowId())) {
            log.warn(traceId + " WORKFLOW ID IS EMPTY! creatorId=" + creatorId + ", createDeptId=" + createDeptId
                + ", zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            return null;
        }

        result.setWorkflowBaseInfo(wbi);

        final List<WorkflowRequestTableField> tableFieldList =
            convert2WorkflowRequestTableFieldList(creatorId, createDeptId, zyRegulationBjmoaHistory, traceId);
        final WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];
        wrtri[0] = new WorkflowRequestTableRecord();
        wrtri[0].setWorkflowRequestTableFields(toTableFieldArray(tableFieldList));
        final WorkflowMainTableInfo wmi = new WorkflowMainTableInfo();
        wmi.setRequestRecords(wrtri);
        result.setWorkflowMainTableInfo(wmi);

        return result;
    }

    private List<WorkflowRequestTableField> convert2WorkflowRequestTableFieldList(final String creatorId,
        final String createDeptId, final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory, final String traceId) {
        if (StringUtils.isEmpty(creatorId) || StringUtils.isEmpty(createDeptId) || zyRegulationBjmoaHistory == null) {
            log.warn(traceId + " INPUT IS EMPTY");
            return null;
        }

        final List<WorkflowRequestTableField> result = new ArrayList<>(19);
        final String curDate = DateUtils.formatDate(new Date());
        final String year = curDate.substring(0, 4);
        final String regulationName = zyRegulationBjmoaHistory.getName();

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sqbm");
            tableField.setFieldValue(createDeptId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("nf");
            tableField.setFieldValue(year);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sqr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sqsj");
            tableField.setFieldValue(curDate);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("yfrq");
            tableField.setFieldValue(curDate);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("sqr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xgbmld");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("ejbmld");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zbfz");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zbzz");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("gszgld");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("zxswy");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xlkz");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("thrq");
            tableField.setFieldValue(curDate);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xgbmfzrhq");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("txwjglbmjbr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("fwbmjbr");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("xgfgldhq");
            tableField.setFieldValue(creatorId);
            result.add(tableField);
        }

        {
            // 附件
            final StringBuilder fieldTypeSb = new StringBuilder();
            final StringBuilder fieldValueSb = new StringBuilder();

            fieldTypeSb.append("http:").append(regulationName).append(".pdf").append("|");
            ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaService.queryByIdentifier(zyRegulationBjmoaHistory.getIdentifier());
            final String contentFileId = zyRegulationBjmoa.getWatermarkPdfContentFileId();
            fieldValueSb.append(contentManagementService.getDownloadUrl(contentFileId)).append("|");

            // @todo 需要添加关联记录
            final String regulationVersion = zyRegulationBjmoaHistory.getVersion();
            final String regulationIdentifier = zyRegulationBjmoaHistory.getIdentifier();
            final List<ZyRelatedRegulationBjmoa> relatedRegulationBjmoas = zyRelatedRegulationBjmoaService
                .queryByRegulationIdentifierAndVersion(regulationIdentifier, regulationVersion);
            log.info(traceId + " relatedRegulationBjmoas: " + relatedRegulationBjmoas);
            for (final ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa : relatedRegulationBjmoas) {
                final String regulationIdentifierA = zyRelatedRegulationBjmoa.getRegulationIdentifierA();
                final String regulationIdentifierB = zyRelatedRegulationBjmoa.getRegulationIdentifierB();
                final String regulationVersionB = zyRelatedRegulationBjmoa.getVersionB();
                final String regulationType = zyRelatedRegulationBjmoa.getRegulationType();
                if (regulationIdentifier.equals(regulationIdentifierA)) {
                    if (RegulationType.RELATED.equals(regulationType)) {
                        final List<ZyRegulationBjmoaHistory> relatedRegulationHistory =
                            queryByIdentifierAndVersion(regulationIdentifierB, regulationVersionB);
                        final String relatedContentFileId = relatedRegulationHistory.get(0).getContentFileId();
                        final String relatedFileName = relatedRegulationHistory.get(0).getFileName();
                        if (StringUtils.isNotEmpty(relatedContentFileId)) {
                            fieldTypeSb.append("http:").append(relatedFileName).append("|");
                            fieldValueSb.append(contentManagementService.getDownloadUrl(relatedContentFileId))
                                .append("|");
                        }
                    }
                }
            }

            final WorkflowRequestTableField tableField = new WorkflowRequestTableField();
            tableField.setFieldName("fj");
            tableField.setFieldType(fieldTypeSb.substring(0, fieldTypeSb.length() - 1));
            tableField.setFieldValue(fieldValueSb.substring(0, fieldValueSb.length() - 1));
            result.add(tableField);
        }

        for (final WorkflowRequestTableField workflowRequestTableField : result) {
            workflowRequestTableField.setEdit(true);
            workflowRequestTableField.setView(true);
        }

        return result;
    }

    private String getBjmoaWorkflowId(final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory, final String traceId) {
        if (zyRegulationBjmoaHistory == null) {
            log.warn(traceId + " INPUT IS EMPTY");
            return null;
        }
        log.info(traceId + " getBjmoaWorkflowId! zyRegulationBjmoaHistory: " + zyRegulationBjmoaHistory);

        // 所有党群及廉政建设管理制度发文均为部门级
        final String categoryId = zyRegulationBjmoaHistory.getCategoryId();
        if ("1".equals(categoryId)) {
            return bjmoaPublishCompanyWorkflowId;
        } else if ("2".equals(categoryId)) {
            return bjmoaPublishDepartmentWorkflowId;
        }

        final String levelId = zyRegulationBjmoaHistory.getLevelId();
        if (StringUtils.isEmpty(levelId)) {
            return null;
        }

        String result = null;
        switch (levelId) {
            case "1": // 1级
            case "2": // 2级
            case "3": // 3A级（公司级）
            case "7": // 综合应急预案
            case "8": // 专项应急预案
            case "10": // 公司级
            case "11": { // 分工会级
                result = bjmoaPublishCompanyWorkflowId;
                break;
            }
            case "4": // 3B级（设备设施及运营板块）
            case "5": // 3C级（部门级）
            case "6": // 4级（作业指导书）
            case "9": { // 现场处置方案
                result = bjmoaPublishDepartmentWorkflowId;
                break;
            }
            default:
                break;
        }

        return result;
    }

}
