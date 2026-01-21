package org.jeecg.modules.regulation.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.dto.EcmFileDTO;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.regulation.constant.RegulationType;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaDept;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaHistory;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulationBjmoa;
import org.jeecg.modules.regulation.mapper.ZyRegulationBjmoaMapper;
import org.jeecg.modules.regulation.mapper.ZyRelatedRegulationBjmoaMapper;
import org.jeecg.modules.regulation.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhouwei
 * @date 2024/10/29
 */
@Service
@Slf4j
public class ZyRegulationBjmoaSyncDataServiceImpl implements IZyRegulationBjmoaSyncDataService {

    @Autowired @Qualifier("bjmoaContentManagementService")
    private IContentManagementService contentManagementService;
    @Autowired
    private IQiqiaoFormsService qiqiaoFormsService;
    @Autowired
    private ZyRegulationBjmoaMapper zyRegulationBjmoaMapper;
    @Autowired
    private ZyRelatedRegulationBjmoaMapper zyRelatedRegulationBjmoaMapper;
    @Autowired
    private IZyRegulationBjmoaService zyRegulationBjmoaService;
    @Autowired
    private IZyRegulationBjmoaHistoryService zyRegulationBjmoaHistoryService;
    @Autowired
    private IZyRegulationBjmoaDeptService zyRegulationBjmoaDeptService;
    @Autowired
    private IZyRelatedRegulationBjmoaService zyRelatedRegulationBjmoaService;


    @Value("${biisaas.bjmoaRegulationInfo.applicationId}")
    private String bjmoaRegulationInfoApplicationId;
    @Value("${biisaas.bjmoaRegulationInfo.realFormModelId}")
    private String bjmoaRealRegulationInfoFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realRelatedFormModelId}")
    private String bjmoaRealRelatedFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realHistoryFormModelId}")
    private String bjmoaRealHistoryFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realParentFormModelId}")
    private String bjmoRealParentFormModelId;


    @Override
    public void syncEmergencyRegulationFromQiqiaoToDatabase() {
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("大类");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue("4");
        fieldFilterList.add(fieldFilter);
        recordVO.setFilter(fieldFilterList);

        int pageNo = 1;
        int pageSize = 20;
        recordVO.setPageSize(pageSize);
        boolean finished = false;
        while (!finished) {
            recordVO.setPage(pageNo);
            JSONObject page = qiqiaoFormsService.page(recordVO);

            JSONArray regulationList = page.getJSONArray("list");
            for (int i = 0; i < regulationList.size(); i++) {

                JSONObject regulationJson = regulationList.getJSONObject(i);
                if (regulationJson == null) {
                    continue;
                }
                String qiqiaoRealRegulationId = regulationJson.getString("id");
                if (!syncRegulationFromQiqiaoToDatabase(qiqiaoRealRegulationId)) {
                    log.info("FAIL TO SYNC REGULATION BY QiqiaoRealRegulationID : " + qiqiaoRealRegulationId);
                }

            }

            finished = CollectionUtils.isEmpty(regulationList);
            pageNo++;

        }
    }

    @Override
    public void syncPublishedEmergencyRegulation(String qiqiaoEmergencyId) {
        log.info("syncPublishedEmergencyRegulation qiqiaoEmergencyId: {}", qiqiaoEmergencyId);
        if (StringUtils.isEmpty(qiqiaoEmergencyId)) {
            log.warn("qiqiaoEmergencyId IS NULL");
            return;
        }
        // 1. 查询七巧制度基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        recordVO.setId(qiqiaoEmergencyId);
        final JSONObject regulationJson = qiqiaoFormsService.queryById(recordVO);
        if (regulationJson == null) {
            log.warn(" CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoEmergencyId);
            return;
        }
        JSONObject variables = regulationJson.getJSONObject("variables");
        final JSONObject prettyValue = regulationJson.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(" CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoEmergencyId);
            return;
        }

        // 2. 存一条预案记录
        String identifier = variables.getString("制度系统标识别文本");
        if (StringUtils.isEmpty(identifier)) {
            log.warn("CANNOT FIND regulationIdentifier!");
            return;
        }
        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(identifier);
        if (zyRegulationBjmoa == null) {
            zyRegulationBjmoa = new ZyRegulationBjmoa();
        }

        //save regulation
        if (!syncRegulation(qiqiaoEmergencyId, zyRegulationBjmoa, variables, prettyValue)) {
            log.warn("FAILED TO SAVE regulation id=" + qiqiaoEmergencyId);
        }
        // save history and department
        if (!syncHistoryAndDepartment(qiqiaoEmergencyId, variables, prettyValue)) {
            log.warn("FAILED TO SAVE regulation id=" + qiqiaoEmergencyId);
        }
    }

    @Override
    public void syncModifiedRegulation(String qiqiaoRegulationId) {
        log.info("syncModifiedRegulation qiqiaoRegulationId: {}", qiqiaoRegulationId);
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            log.warn("qiqiaoRegulationId IS NULL");
            return;
        }
        // 1. 查询七巧制度基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        final JSONObject regulationJson = qiqiaoFormsService.queryById(recordVO);
        if (regulationJson == null) {
            log.warn(" CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        JSONObject variables = regulationJson.getJSONObject("variables");
        final JSONObject prettyValue = regulationJson.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(" CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }

        // 2. 存一条制度记录
        String identifier = variables.getString("制度系统标识别文本");
        if (StringUtils.isEmpty(identifier)) {
            log.warn("CANNOT FIND regulationIdentifier!");
            return;
        }
        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(identifier);
        if (zyRegulationBjmoa == null) {
            log.warn("CANNOT FIND regulation!");
            return;
        }

        //save regulation
        if (!syncRegulation(qiqiaoRegulationId, zyRegulationBjmoa, variables, prettyValue)) {
            log.warn("FAILED TO SAVE regulation id=" + qiqiaoRegulationId);
        }
        // save history and department
        if (!syncModifiedHistoryAndDepartment(qiqiaoRegulationId, variables, prettyValue)) {
            log.warn("FAILED TO SAVE regulation id=" + qiqiaoRegulationId);
        }
    }

    @Override
    public void syncPublishedRegulation(String qiqiaoRegulationId) {
        log.info("syncPublishedRegulation qiqiaoRegulationId: {}", qiqiaoRegulationId);
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            log.warn("qiqiaoRegulationId IS NULL");
            return;
        }
        // 1. 查询七巧制度基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        final JSONObject regulationJson = qiqiaoFormsService.queryById(recordVO);
        if (regulationJson == null) {
            log.warn(" CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        JSONObject variables = regulationJson.getJSONObject("variables");
        final JSONObject prettyValue = regulationJson.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(" CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }

        // 2. 存一条制度记录
        String identifier = variables.getString("制度系统标识别文本");
        if (StringUtils.isEmpty(identifier)) {
            log.warn("CANNOT FIND regulationIdentifier!");
            return;
        }
        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(identifier);
        if (zyRegulationBjmoa == null) {
            log.warn("CANNOT FIND regulation!");
            return;
        }

        //save regulation
        if (!syncRegulation(qiqiaoRegulationId, zyRegulationBjmoa, variables, prettyValue)) {
            log.warn("FAILED TO SAVE regulation id=" + qiqiaoRegulationId);
        }
        // save history and department
        if (!syncHistoryAndDepartment(qiqiaoRegulationId, variables, prettyValue)) {
            log.warn("FAILED TO SAVE regulation id=" + qiqiaoRegulationId);
        }
        // save related regulation
        if (!syncRelated(qiqiaoRegulationId, variables, prettyValue)) {
            log.warn("FAILED TO SAVE regulation id=" + qiqiaoRegulationId);
        }
    }

    @Override
    public void syncRegulationToDatabase(String qiqiaoRegulationId) {
        log.info("syncRegulationToDatabase qiqiaoRegulationId: {}", qiqiaoRegulationId);
        // 根据七巧ID获取【轨道运营制度发布单】的记录
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        JSONObject regulationJson = qiqiaoFormsService.queryById(recordVO);

        JSONObject variables = regulationJson.getJSONObject("variables");
        JSONObject prettyValue = regulationJson.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn("variables or prettyValue is empty");
            return;
        }
        String identifier = variables.getString("制度系统标识别文本");
        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(identifier);
        if (zyRegulationBjmoa == null) {
            zyRegulationBjmoa = new ZyRegulationBjmoa();
        }

        //save regulation
        if (!syncRegulation(qiqiaoRegulationId, zyRegulationBjmoa, variables, prettyValue)) {
            log.warn("FAILED TO SAVE regulation id=" + qiqiaoRegulationId);
            return;
        }
        // save history and department
        if (!syncAllHistoryAndDepartment(qiqiaoRegulationId, variables, prettyValue)) {
            log.warn("FAILED TO SAVE history id=" + qiqiaoRegulationId);
            return;
        }

        // save related
        if (!syncRelated(qiqiaoRegulationId, variables, prettyValue)) {
            log.warn("FAILED TO SAVE related id=" + qiqiaoRegulationId);
            return;
        }

        // save parent
        if (!syncParent(qiqiaoRegulationId, variables, prettyValue)) {
            log.warn("FAILED TO SAVE parent id=" + qiqiaoRegulationId);
            return;
        }
    }

    private boolean syncRegulationFromQiqiaoToDatabase(String qiqiaoRealRegulationId) {
        // 根据七巧ID获取【轨道运营制度发布单】的记录
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRealRegulationId);
        JSONObject regulationJson = qiqiaoFormsService.queryById(recordVO);

        JSONObject variables = regulationJson.getJSONObject("variables");
        JSONObject prettyValue = regulationJson.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn("variables or prettyValue is empty");
            return false;
        }
        String identifier = variables.getString("制度系统标识别文本");
        if (StringUtils.isEmpty(identifier)) {
            log.info("CREATE IDENTIFIER! qiqiaoRealRegulationId=" + qiqiaoRealRegulationId);
            identifier = createIdentifier(qiqiaoRealRegulationId);
            regulationJson = qiqiaoFormsService.queryById(recordVO);
            variables = regulationJson.getJSONObject("variables");
            prettyValue = regulationJson.getJSONObject("prettyValue");
            if (StringUtils.isEmpty(variables.getString("制度系统标识别文本"))) {
                log.warn("FAIL TO CREATE IDENTIFIER FOR QIQIAOID:" + qiqiaoRealRegulationId);
                return false;
            }
        }

        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(identifier);
        String regulationStatus = variables.getString("制度状态");
        if (zyRegulationBjmoa == null) {
            zyRegulationBjmoa = new ZyRegulationBjmoa();
        }

        if (!"7".equals(regulationStatus)) {
            log.warn("NO NEED TO SYNC REGULATION " + qiqiaoRealRegulationId);
            return false;
        } else {
            //save regulation
            if (!syncRegulation(qiqiaoRealRegulationId, zyRegulationBjmoa, variables, prettyValue)) {
                log.warn("FAILED TO SAVE regulation id=" + qiqiaoRealRegulationId);
                return false;
            }
            // save history and department
            if (!syncHistoryAndDepartment(qiqiaoRealRegulationId, variables, prettyValue)) {
                log.warn("FAILED TO SAVE regulation id=" + qiqiaoRealRegulationId);
                return false;
            }

        }

        return true;
    }

    private boolean syncRegulation(String qiqiaoRealRegulationId, ZyRegulationBjmoa zyRegulationBjmoa, JSONObject variables, JSONObject prettyValue) {
        log.info("syncRegulation qiqiaoRegulationId: {}, zyRegulationBjmoa: {}, variables: {}, prettyValue: {}", qiqiaoRealRegulationId,
                zyRegulationBjmoa, variables, prettyValue);
        zyRegulationBjmoa.setActive(1);
        zyRegulationBjmoa.setCode(variables.getString("制度编号"));
        zyRegulationBjmoa.setQiqiaoCreatorId(variables.getString("制度跟进人"));
        zyRegulationBjmoa.setQiqiaoCreatorName(prettyValue.getString("制度跟进人"));
        zyRegulationBjmoa.setLevelId(variables.getString("制度级别"));
        zyRegulationBjmoa.setLevelName(prettyValue.getString("制度级别"));
        zyRegulationBjmoa.setLineId(variables.getString("线路"));
        zyRegulationBjmoa.setLineName(prettyValue.getString("线路"));
        zyRegulationBjmoa.setContingencyPlanCategoryId(variables.getString("预案分类"));
        zyRegulationBjmoa.setContingencyPlanCategoryName(prettyValue.getString("预案分类"));
        zyRegulationBjmoa.setCategoryId(variables.getString("大类"));
        zyRegulationBjmoa.setCategoryName(prettyValue.getString("大类"));
        zyRegulationBjmoa.setManagementCategoryId(variables.getString("管理类别"));
        zyRegulationBjmoa.setManagementCategoryName(prettyValue.getString("管理类别"));
        zyRegulationBjmoa.setSubCategoryId(variables.getString("业务子类"));
        zyRegulationBjmoa.setSubCategoryName(prettyValue.getString("业务子类"));
        zyRegulationBjmoa.setContentFileId(variables.getString("内管文件编号"));
        zyRegulationBjmoa.setContentDocId(variables.getString("内管文档编号"));
        zyRegulationBjmoa.setIdentifier(variables.getString("制度系统标识别文本"));
        zyRegulationBjmoa.setName(variables.getString("制度名称"));
        zyRegulationBjmoa.setQiqiaoRegulationId(qiqiaoRealRegulationId);
        if (!zyRegulationBjmoaService.saveOrUpdate(zyRegulationBjmoa)) {
            log.warn("FAILED TO SAVE OR UPDATE zyRegulationBjmoa=" + zyRegulationBjmoa);
            return false;
        }
        return true;
    }

    private boolean syncHistoryAndDepartment(String qiqiaoRealRegulationId, JSONObject variables, JSONObject prettyValue) {
        log.info("syncHistoryAndDepartment qiqiaoRegulationId: {}, variables: {}, prettyValue: {}", qiqiaoRealRegulationId,
                variables, prettyValue);
        if (StringUtils.isEmpty(qiqiaoRealRegulationId) || variables == null || prettyValue == null) {
            return false;
        }

        String identifier = variables.getString("制度系统标识别文本");
        String code = variables.getString("制度编号");
        // 最新版本
        {
            String curVersion = prettyValue.getString("制度版本");
            ZyRegulationBjmoaHistory zyRegulationBjmoaHistory;
            List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList =
                    zyRegulationBjmoaHistoryService.queryByIdentifierAndVersionAndCode(identifier, curVersion, code);
            if (zyRegulationBjmoaHistoryList.size() == 0) {
                zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
            } else {
                zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0);
            }
            zyRegulationBjmoaHistory.setIdentifier(identifier);

            String qiqiaoCreatorId = variables.getString("制度跟进人");
            final String qiqiaoCreatorName = variables.getString("制度跟进人_pretty_value");
            if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(
                    qiqiaoCreatorName) && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
                zyRegulationBjmoaHistory.setQiqiaoCreatorId(qiqiaoCreatorId);
                zyRegulationBjmoaHistory.setQiqiaoCreatorName(qiqiaoCreatorName);
            }

            zyRegulationBjmoaHistory.setCode(code);
            zyRegulationBjmoaHistory.setName(variables.getString("制度名称"));
            zyRegulationBjmoaHistory.setPublishTime(DateUtils.getDate(variables.getLong("制度发布时间")));

            try {
                Long executeTime = variables.getLong("制度实施时间");
                Date executeTimeDate = executeTime == null ? null : DateUtils.getDate(executeTime);
                zyRegulationBjmoaHistory.setExecuteTime(executeTimeDate);
            } catch (Exception e) {
                log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            }

            zyRegulationBjmoaHistory.setContentFileId(variables.getString("内管文件编号"));
            zyRegulationBjmoaHistory.setContentDocId(variables.getString("内管文档编号"));

            zyRegulationBjmoaHistory.setVersion(curVersion);

            zyRegulationBjmoaHistory.setLevelId(variables.getString("制度级别"));
            zyRegulationBjmoaHistory.setLevelName(prettyValue.getString("制度级别"));
            zyRegulationBjmoaHistory.setLineId(variables.getString("线路"));
            zyRegulationBjmoaHistory.setLineName(prettyValue.getString("线路"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryId(variables.getString("预案分类"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryName(prettyValue.getString("预案分类"));
            zyRegulationBjmoaHistory.setCategoryId(variables.getString("大类"));
            zyRegulationBjmoaHistory.setCategoryName(prettyValue.getString("大类"));
            zyRegulationBjmoaHistory.setManagementCategoryId(variables.getString("管理类别"));
            zyRegulationBjmoaHistory.setManagementCategoryName(prettyValue.getString("管理类别"));
            zyRegulationBjmoaHistory.setSubCategoryId(variables.getString("业务子类"));
            zyRegulationBjmoaHistory.setSubCategoryName(prettyValue.getString("业务子类"));
            if (!zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory)) {
                log.warn("FAILED TO SAVE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            }

            // 保存主责部门
            final String curMainDeptId = variables.getString("制度主责部门");
            if (StringUtils.isNotEmpty(curMainDeptId)) {
                final String curMainDeptName = variables.getString("制度主责部门_pretty_value");
                if (curMainDeptId.equals(curMainDeptName)) {
                    log.warn(
                            "WEIRD DEPT NAME! qiqiaoRealRegulationId: " + qiqiaoRealRegulationId + ", curMainDeptName:" + curMainDeptName);
                } else {
                    List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList =
                            zyRegulationBjmoaDeptService.getByRegulationCodeAndVersion(code, curVersion);
                    ZyRegulationBjmoaDept zyRegulationBjmoaDept;
                    if (zyRegulationBjmoaDeptList.size() == 0) {
                        zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
                    } else {
                        zyRegulationBjmoaDept = zyRegulationBjmoaDeptList.get(0);
                    }
                    zyRegulationBjmoaDept.setCode(code);
                    zyRegulationBjmoaDept.setVersion(curVersion);
                    zyRegulationBjmoaDept.setName(variables.getString("制度名称"));
                    zyRegulationBjmoaDept.setIdentifier(identifier);
                    zyRegulationBjmoaDept.setQiqiaoDeptId(curMainDeptId);
                    zyRegulationBjmoaDept.setQiqiaoDeptName(curMainDeptName);

                    if (!zyRegulationBjmoaDeptService.saveOrUpdate(zyRegulationBjmoaDept)) {
                        log.warn("FAILED TO SAVE zyRegulationBjmoaDept=" + zyRegulationBjmoaDept);
                    }
                }
            }
        }

        return true;
    }

    private boolean syncModifiedHistoryAndDepartment(String qiqiaoRealRegulationId, JSONObject variables, JSONObject prettyValue) {
        log.info("syncModifiedHistoryAndDepartment qiqiaoRegulationId: {}, variables: {}, prettyValue: {}", qiqiaoRealRegulationId,
                variables, prettyValue);
        if (StringUtils.isEmpty(qiqiaoRealRegulationId) || variables == null || prettyValue == null) {
            return false;
        }

        String identifier = variables.getString("制度系统标识别文本");
        String code = variables.getString("制度编号");
        // 最新版本
        {
            String curVersion = prettyValue.getString("制度版本");
            ZyRegulationBjmoaHistory zyRegulationBjmoaHistory;
            List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList =
                    zyRegulationBjmoaHistoryService.queryByIdentifier(identifier);
            if (zyRegulationBjmoaHistoryList.size() == 0) {
                zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
            } else {
                zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0);
            }
            zyRegulationBjmoaHistory.setIdentifier(identifier);

            String qiqiaoCreatorId = variables.getString("制度跟进人");
            final String qiqiaoCreatorName = variables.getString("制度跟进人_pretty_value");
            if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(
                    qiqiaoCreatorName) && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
                zyRegulationBjmoaHistory.setQiqiaoCreatorId(qiqiaoCreatorId);
                zyRegulationBjmoaHistory.setQiqiaoCreatorName(qiqiaoCreatorName);
            }

            zyRegulationBjmoaHistory.setCode(code);
            zyRegulationBjmoaHistory.setName(variables.getString("制度名称"));
            zyRegulationBjmoaHistory.setPublishTime(DateUtils.getDate(variables.getLong("制度发布时间")));

            try {
                Long executeTime = variables.getLong("制度实施时间");
                Date executeTimeDate = executeTime == null ? null : DateUtils.getDate(executeTime);
                zyRegulationBjmoaHistory.setExecuteTime(executeTimeDate);
            } catch (Exception e) {
                log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            }

            zyRegulationBjmoaHistory.setContentFileId(variables.getString("内管文件编号"));
            zyRegulationBjmoaHistory.setContentDocId(variables.getString("内管文档编号"));

            zyRegulationBjmoaHistory.setVersion(curVersion);

            zyRegulationBjmoaHistory.setLevelId(variables.getString("制度级别"));
            zyRegulationBjmoaHistory.setLevelName(prettyValue.getString("制度级别"));
            zyRegulationBjmoaHistory.setLineId(variables.getString("线路"));
            zyRegulationBjmoaHistory.setLineName(prettyValue.getString("线路"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryId(variables.getString("预案分类"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryName(prettyValue.getString("预案分类"));
            zyRegulationBjmoaHistory.setCategoryId(variables.getString("大类"));
            zyRegulationBjmoaHistory.setCategoryName(prettyValue.getString("大类"));
            zyRegulationBjmoaHistory.setManagementCategoryId(variables.getString("管理类别"));
            zyRegulationBjmoaHistory.setManagementCategoryName(prettyValue.getString("管理类别"));
            zyRegulationBjmoaHistory.setSubCategoryId(variables.getString("业务子类"));
            zyRegulationBjmoaHistory.setSubCategoryName(prettyValue.getString("业务子类"));
            if (!zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory)) {
                log.warn("FAILED TO SAVE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            }

            // 保存主责部门
            final String curMainDeptId = variables.getString("制度主责部门");
            if (StringUtils.isNotEmpty(curMainDeptId)) {
                final String curMainDeptName = variables.getString("制度主责部门_pretty_value");
                if (curMainDeptId.equals(curMainDeptName)) {
                    log.warn(
                            "WEIRD DEPT NAME! qiqiaoRealRegulationId: " + qiqiaoRealRegulationId + ", curMainDeptName:" + curMainDeptName);
                } else {
                    List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList =
                            zyRegulationBjmoaDeptService.getByRegulationIdentifier(identifier);
                    ZyRegulationBjmoaDept zyRegulationBjmoaDept;
                    if (zyRegulationBjmoaDeptList.size() == 0) {
                        zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
                    } else {
                        zyRegulationBjmoaDept = zyRegulationBjmoaDeptList.get(0);
                    }
                    zyRegulationBjmoaDept.setCode(code);
                    zyRegulationBjmoaDept.setVersion(curVersion);
                    zyRegulationBjmoaDept.setName(variables.getString("制度名称"));
                    zyRegulationBjmoaDept.setIdentifier(identifier);
                    zyRegulationBjmoaDept.setQiqiaoDeptId(curMainDeptId);
                    zyRegulationBjmoaDept.setQiqiaoDeptName(curMainDeptName);

                    if (!zyRegulationBjmoaDeptService.saveOrUpdate(zyRegulationBjmoaDept)) {
                        log.warn("FAILED TO SAVE zyRegulationBjmoaDept=" + zyRegulationBjmoaDept);
                    }
                }
            }
        }

        return true;
    }

    private boolean syncAllHistoryAndDepartment(String qiqiaoRegulationId, JSONObject variables, JSONObject prettyValue) {
        log.info("syncAllHistoryAndDepartment qiqiaoRegulationId: {}, variables: {}, prettyValue: {}", qiqiaoRegulationId,
                variables, prettyValue);
        if (StringUtils.isEmpty(qiqiaoRegulationId) || variables == null || prettyValue == null) {
            return false;
        }

        RecordVO historyRecordVO = new RecordVO();
        historyRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        historyRecordVO.setFormModelId(bjmoaRealHistoryFormModelId);
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("外键");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(qiqiaoRegulationId);
        fieldFilterList.add(fieldFilter);
        historyRecordVO.setFilter(fieldFilterList);

        historyRecordVO.setPage(1);
        historyRecordVO.setPageSize(50);
        JSONObject page = qiqiaoFormsService.page(historyRecordVO);
        if (page == null) {
            log.warn("PAGE IS NULL FOR historyRecordVO=" + historyRecordVO);
            return false;
        }
        JSONArray historyRegulationArray = page.getJSONArray("list");
        log.info("historyRegulationArray: " + historyRegulationArray);

        List<JSONObject> historyRegulationList = toList(historyRegulationArray);
        historyRegulationList = historyRegulationList.stream()
                .sorted(Comparator.comparing(o -> (o.getJSONObject("variables").getString("发布日期"))))
                .collect(Collectors.toList());

        // 由最早的版本开始添加
        String identifier = variables.getString("制度系统标识别文本");
        for (JSONObject historyRegulation : historyRegulationList) {
            JSONObject curVariables = historyRegulation.getJSONObject("variables");
            JSONObject curPrettyValue = historyRegulation.getJSONObject("prettyValue");
            if (curVariables == null) {
                continue;
            }

            String curCode = curVariables.getString("制度编号");
            String curName = curVariables.getString("制度名称");
            String curPublishDate = curVariables.getString("发布日期");
            String curExecuteDate = curVariables.getString("实施日期");
            String curAbolishDate = curVariables.getString("作废日期");

            String curFileId = curVariables.getString("内管文件编号");
            String curDocId = curVariables.getString("内管文档编号");

            String curFileName = curVariables.getString("文件名称");
            String curVersion = curPrettyValue.getString("版本");

            String qiqiaoCreatorId = curVariables.getString("制度跟进人");
            String qiqiaoCreatorName = curPrettyValue.getString("制度跟进人");

            ZyRegulationBjmoaHistory zyRegulationBjmoaHistory;
            List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList =
                    zyRegulationBjmoaHistoryService.queryByIdentifierAndVersion(identifier, curVersion);
            if (zyRegulationBjmoaHistoryList.size() == 0) {
                zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
            } else {
                zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0);
            }
            zyRegulationBjmoaHistory.setIdentifier(identifier);
            zyRegulationBjmoaHistory.setCode(curCode);
            zyRegulationBjmoaHistory.setName(curName);
            zyRegulationBjmoaHistory.setFileName(curFileName);

            if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(
                    qiqiaoCreatorName) && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
                zyRegulationBjmoaHistory.setQiqiaoCreatorId(qiqiaoCreatorId);
                zyRegulationBjmoaHistory.setQiqiaoCreatorName(qiqiaoCreatorName);
            }

            final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                zyRegulationBjmoaHistory.setPublishTime(dateFormat.parse(curPublishDate));
                zyRegulationBjmoaHistory.setExecuteTime(dateFormat.parse(curExecuteDate));
                zyRegulationBjmoaHistory.setAbolishTime(dateFormat.parse(curAbolishDate));
            } catch (Exception e) {
                log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            }

            zyRegulationBjmoaHistory.setContentFileId(curFileId);
            zyRegulationBjmoaHistory.setContentDocId(curDocId);
            zyRegulationBjmoaHistory.setVersion(curVersion);

            if (!zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory)) {
                log.warn("FAILED TO SAVE OR UPDATE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            }

            // 保存主责部门
            String curMainDeptId = curVariables.getString("主责部门");
            String curMainDeptName = curPrettyValue.getString("主责部门");
            if (StringUtils.isNotEmpty(curMainDeptId) && StringUtils.isNotEmpty(
                curMainDeptName) && !curMainDeptId.equals(curMainDeptName)) {
//
                List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList =
                    zyRegulationBjmoaDeptService.getByRegulationCodeAndVersion(curCode, curVersion);
                ZyRegulationBjmoaDept zyRegulationBjmoaDept;
                if (zyRegulationBjmoaDeptList.size() == 0) {
                    zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
                } else {
                    zyRegulationBjmoaDept = zyRegulationBjmoaDeptList.get(0);
                }
                zyRegulationBjmoaDept.setCode(curCode);
                zyRegulationBjmoaDept.setVersion(curVersion);
                zyRegulationBjmoaDept.setName(curName);
                zyRegulationBjmoaDept.setIdentifier(identifier);
                zyRegulationBjmoaDept.setQiqiaoDeptId(curMainDeptId);
                zyRegulationBjmoaDept.setQiqiaoDeptName(curMainDeptName);

                if (!zyRegulationBjmoaDeptService.saveOrUpdate(zyRegulationBjmoaDept)) {
                    log.warn("FAILED TO SAVE zyRegulationBjmoaDept=" + zyRegulationBjmoaDept);
                }
            }
        }


        // 最新版本
        String code = variables.getString("制度编号");
        {
            String curVersion = prettyValue.getString("制度版本");
            ZyRegulationBjmoaHistory zyRegulationBjmoaHistory;
            List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList =
                    zyRegulationBjmoaHistoryService.queryByIdentifierAndVersionAndCode(identifier, curVersion, code);
            if (zyRegulationBjmoaHistoryList.size() == 0) {
                zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
            } else {
                zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0);
            }
            zyRegulationBjmoaHistory.setIdentifier(identifier);

            String qiqiaoCreatorId = variables.getString("制度跟进人");
            final String qiqiaoCreatorName = variables.getString("制度跟进人_pretty_value");
            if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(
                    qiqiaoCreatorName) && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
                zyRegulationBjmoaHistory.setQiqiaoCreatorId(qiqiaoCreatorId);
                zyRegulationBjmoaHistory.setQiqiaoCreatorName(qiqiaoCreatorName);
            }

            zyRegulationBjmoaHistory.setCode(code);
            zyRegulationBjmoaHistory.setName(variables.getString("制度名称"));
            zyRegulationBjmoaHistory.setPublishTime(DateUtils.getDate(variables.getLong("制度发布时间")));

            try {
                Long executeTime = variables.getLong("制度实施时间");
                Date executeTimeDate = executeTime == null ? null : DateUtils.getDate(executeTime);
                zyRegulationBjmoaHistory.setExecuteTime(executeTimeDate);
            } catch (Exception e) {
                log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            }

            zyRegulationBjmoaHistory.setContentFileId(variables.getString("内管文件编号"));
            zyRegulationBjmoaHistory.setContentDocId(variables.getString("内管文档编号"));

            zyRegulationBjmoaHistory.setVersion(curVersion);

            zyRegulationBjmoaHistory.setLevelId(variables.getString("制度级别"));
            zyRegulationBjmoaHistory.setLevelName(prettyValue.getString("制度级别"));
            zyRegulationBjmoaHistory.setLineId(variables.getString("线路"));
            zyRegulationBjmoaHistory.setLineName(prettyValue.getString("线路"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryId(variables.getString("预案分类"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryName(prettyValue.getString("预案分类"));
            zyRegulationBjmoaHistory.setCategoryId(variables.getString("大类"));
            zyRegulationBjmoaHistory.setCategoryName(prettyValue.getString("大类"));
            zyRegulationBjmoaHistory.setManagementCategoryId(variables.getString("管理类别"));
            zyRegulationBjmoaHistory.setManagementCategoryName(prettyValue.getString("管理类别"));
            zyRegulationBjmoaHistory.setSubCategoryId(variables.getString("业务子类"));
            zyRegulationBjmoaHistory.setSubCategoryName(prettyValue.getString("业务子类"));
            if (!zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory)) {
                log.warn("FAILED TO SAVE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            }

            // 保存主责部门
            final String curMainDeptId = variables.getString("制度主责部门");
            if (StringUtils.isNotEmpty(curMainDeptId)) {
                final String curMainDeptName = variables.getString("制度主责部门_pretty_value");
                if (curMainDeptId.equals(curMainDeptName)) {
                    log.warn(
                            "WEIRD DEPT NAME! qiqiaoRealRegulationId: " + qiqiaoRegulationId + ", curMainDeptName:" + curMainDeptName);
                } else {
                    List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList =
                            zyRegulationBjmoaDeptService.getByRegulationCodeAndVersion(code, curVersion);
                    ZyRegulationBjmoaDept zyRegulationBjmoaDept;
                    if (zyRegulationBjmoaDeptList.size() == 0) {
                        zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
                    } else {
                        zyRegulationBjmoaDept = zyRegulationBjmoaDeptList.get(0);
                    }
                    zyRegulationBjmoaDept.setCode(code);
                    zyRegulationBjmoaDept.setVersion(curVersion);
                    zyRegulationBjmoaDept.setName(variables.getString("制度名称"));
                    zyRegulationBjmoaDept.setIdentifier(identifier);
                    zyRegulationBjmoaDept.setQiqiaoDeptId(curMainDeptId);
                    zyRegulationBjmoaDept.setQiqiaoDeptName(curMainDeptName);

                    if (!zyRegulationBjmoaDeptService.saveOrUpdate(zyRegulationBjmoaDept)) {
                        log.warn("FAILED TO SAVE zyRegulationBjmoaDept=" + zyRegulationBjmoaDept);
                    }
                }
            }
        }

        return true;
    }

    private boolean syncRelated(String qiqiaoRealRegulationId, JSONObject variables, JSONObject prettyValue) {
        log.info("syncRelated qiqiaoRegulationId: {}, variables: {}, prettyValue: {}",
                qiqiaoRealRegulationId, variables, prettyValue);
        final String identifierA = variables.getString("制度系统标识别文本");
        final String codeA = variables.getString("制度编号");
        final String versionA = prettyValue.getString("制度版本");

        RecordVO relatedRecordVO = new RecordVO();
        relatedRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        relatedRecordVO.setFormModelId(bjmoaRealRelatedFormModelId);
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("制度名称");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(qiqiaoRealRegulationId);
        fieldFilterList.add(fieldFilter);
        relatedRecordVO.setFilter(fieldFilterList);

        JSONObject page = qiqiaoFormsService.page(relatedRecordVO);
        JSONArray relatedRegulationList = page.getJSONArray("list");
        log.info("relatedRegulationList: " + relatedRegulationList);

        ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa = new ZyRelatedRegulationBjmoa();
        zyRelatedRegulationBjmoa.setRegulationIdentifierA(identifierA);
        zyRelatedRegulationBjmoa.setCodeA(codeA);
        zyRelatedRegulationBjmoa.setVersionA(versionA);

        final String qiqiaoCreatorId = variables.getString("制度跟进人");
        final String qiqiaoCreatorName = variables.getString("制度跟进人_pretty_value");

        for (int i = 0; i < relatedRegulationList.size(); ++i) {
            final JSONObject relateRegulation = relatedRegulationList.getJSONObject(i);
            final JSONObject relatedVariables = relateRegulation.getJSONObject("variables");
            final JSONObject relatedPrettyValue = relateRegulation.getJSONObject("prettyValue");
            final String relatedIdentifier = relatedVariables.getString("关联记录唯一标识");
            final String relatedName = relatedVariables.getString("关联记录文件名称");
            final String relatedCode = relatedVariables.getString("关联记录编号");
            final String relatedVersion = relatedPrettyValue.getString("版本号");

            final String levelId = "12";
            final String levelName = "5级";

            zyRelatedRegulationBjmoa.setRegulationType(RegulationType.RELATED);
            zyRelatedRegulationBjmoa.setRegulationIdentifierB(relatedIdentifier);
            zyRelatedRegulationBjmoa.setCodeB(relatedCode);
            zyRelatedRegulationBjmoa.setVersionB(relatedVersion);
            zyRelatedRegulationBjmoa.setRegulationName(relatedName);
            if (!zyRelatedRegulationBjmoaService.save(zyRelatedRegulationBjmoa)) {
                log.warn("FAILED TO SAVE zyRelatedRegulationBjmoa=" + zyRelatedRegulationBjmoa);
            }

            JSONArray externalFileList = relatedVariables.getJSONArray("关联记录上传");

            ZyRegulationBjmoa zyRegulationBjmoa = new ZyRegulationBjmoa();
            zyRegulationBjmoa.setIdentifier(relatedIdentifier);
            zyRegulationBjmoa.setActive(1);
            zyRegulationBjmoa.setName(relatedName);
            zyRegulationBjmoa.setCode(relatedCode);
            zyRegulationBjmoa.setLevelId(levelId);
            zyRegulationBjmoa.setLevelName(levelName);
            zyRegulationBjmoa.setManagementCategoryId(variables.getString("管理类别"));
            zyRegulationBjmoa.setManagementCategoryName(prettyValue.getString("管理类别"));
            zyRegulationBjmoa.setSubCategoryId(variables.getString("业务子类"));
            zyRegulationBjmoa.setSubCategoryName(prettyValue.getString("业务子类"));
            zyRegulationBjmoa.setLineId(variables.getString("线路"));
            zyRegulationBjmoa.setLineName(prettyValue.getString("线路"));
            zyRegulationBjmoa.setCategoryId(variables.getString("大类"));
            zyRegulationBjmoa.setCategoryName(prettyValue.getString("大类"));
            if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(
                    qiqiaoCreatorName) && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
                zyRegulationBjmoa.setQiqiaoCreatorName(qiqiaoCreatorName);
                zyRegulationBjmoa.setQiqiaoCreatorId(qiqiaoCreatorId);
            }
            final Long publishTime = variables.getLong("制度发布时间");
            final Date publishTimeDate = publishTime == null ? null : DateUtils.getDate(publishTime);
            final Long executeTime = variables.getLong("制度实施时间");
            final Date executeTimeDate = executeTime == null ? null : DateUtils.getDate(executeTime);

            for (int j = 0; j < externalFileList.size(); ++j) {
                final JSONObject externalFile = externalFileList.getJSONObject(j);
                final String name = externalFile.getString("name");
                final String fileId = externalFile.getString("fileId");

                RecordVO downloadRecordVO = new RecordVO();
                downloadRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
                downloadRecordVO.setFileId(fileId);
                qiqiaoFormsService.download(downloadRecordVO, name);

                final File file = new File(name);
                try {
                    if (file.exists()) {
                        final List<File> fileToUpload = new ArrayList<>();
                        fileToUpload.add(file);

                        final List<EcmFileDTO> ecmFileDTOs =
                                contentManagementService.uploadFiles(fileToUpload, zyRegulationBjmoa.getContentDocId());
                        if (CollectionUtils.isNotEmpty(ecmFileDTOs)) {
                            final EcmFileDTO ecmFileDTO = ecmFileDTOs.get(0);

                            final String contentDocId = ecmFileDTO.getDocId();
                            final String contentFileId = ecmFileDTO.getFileId();

                            zyRegulationBjmoa.setContentDocId(contentDocId);
                            zyRegulationBjmoa.setContentFileId(contentFileId);
                        }
                    }

                    // 将关联记录保存为一条制度
                    if (!zyRegulationBjmoaService.saveOrUpdate(zyRegulationBjmoa)) {
                        log.warn("FAILED TO UPDATE zyRegulationBjmoa=" + zyRegulationBjmoa);
                    }

                    ZyRegulationBjmoaHistory zyRegulationBjmoaHistory;
                    List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList =
                            zyRegulationBjmoaHistoryService.queryByIdentifierAndVersion(relatedIdentifier, relatedVersion);
                    if (zyRegulationBjmoaHistoryList.size() == 0) {
                        zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
                    } else {
                        zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0);
                        for (int n = 1; n < zyRegulationBjmoaHistoryList.size(); n++) {
                            zyRegulationBjmoaHistoryService.removeById(zyRegulationBjmoaHistoryList.get(n).getId());
                        }
                    }
                    zyRegulationBjmoaHistory.setIdentifier(relatedIdentifier);

                    if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(qiqiaoCreatorName)
                            && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
                        zyRegulationBjmoaHistory.setQiqiaoCreatorId(qiqiaoCreatorId);
                        zyRegulationBjmoaHistory.setQiqiaoCreatorName(qiqiaoCreatorName);
                    }

                    zyRegulationBjmoaHistory.setPublishTime(publishTimeDate);
                    zyRegulationBjmoaHistory.setExecuteTime(executeTimeDate);

                    zyRegulationBjmoaHistory.setContentFileId(zyRegulationBjmoa.getContentFileId());
                    zyRegulationBjmoaHistory.setContentDocId(zyRegulationBjmoa.getContentDocId());

                    zyRegulationBjmoaHistory.setVersion(relatedVersion);
                    zyRegulationBjmoaHistory.setName(relatedName);
                    zyRegulationBjmoaHistory.setCode(relatedCode);
                    zyRegulationBjmoaHistory.setLevelId(zyRegulationBjmoa.getLevelId());
                    zyRegulationBjmoaHistory.setLevelName(zyRegulationBjmoa.getLevelName());
                    zyRegulationBjmoaHistory.setLineId(zyRegulationBjmoa.getLineId());
                    zyRegulationBjmoaHistory.setLineName(zyRegulationBjmoa.getLineName());
                    zyRegulationBjmoaHistory
                            .setContingencyPlanCategoryId(zyRegulationBjmoa.getContingencyPlanCategoryId());
                    zyRegulationBjmoaHistory
                            .setContingencyPlanCategoryName(zyRegulationBjmoa.getContingencyPlanCategoryName());
                    zyRegulationBjmoaHistory.setCategoryId(zyRegulationBjmoa.getCategoryId());
                    zyRegulationBjmoaHistory.setCategoryName(zyRegulationBjmoa.getCategoryName());
                    zyRegulationBjmoaHistory.setManagementCategoryId(zyRegulationBjmoa.getManagementCategoryId());
                    zyRegulationBjmoaHistory.setManagementCategoryName(zyRegulationBjmoa.getManagementCategoryName());
                    zyRegulationBjmoaHistory.setSubCategoryId(zyRegulationBjmoa.getSubCategoryId());
                    zyRegulationBjmoaHistory.setSubCategoryName(zyRegulationBjmoa.getSubCategoryName());
                    if (!zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory)) {
                        log.warn("FAILED TO SAVE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
                    }
                } catch (Exception e) {
                    log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
                } finally {
                    if (file.exists()) {
                        file.delete();
                    }
                }

                // 只能有一个文件
                break;
            }
        }
        return true;
    }

    private boolean syncParent(String qiqiaoRegulationId, JSONObject variables, JSONObject prettyValue) {
        log.info("syncParent qiqiaoRegulationId: {}, variables: {}, prettyValue: {}",
                qiqiaoRegulationId, variables, prettyValue);
        if (StringUtils.isEmpty(qiqiaoRegulationId) || variables == null || prettyValue == null) {
            return false;
        }
        String identifierA = variables.getString("制度系统标识别文本");
        String codeA = variables.getString("制度编号");
        String versionA = prettyValue.getString("制度版本");
        RecordVO parentRecordVO = new RecordVO();
        parentRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId); // 制度系统【轨道运营】
        parentRecordVO.setFormModelId(bjmoRealParentFormModelId); // 发布上级制度明细
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("制度名称");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(qiqiaoRegulationId);
        fieldFilterList.add(fieldFilter);
        parentRecordVO.setFilter(fieldFilterList);

        JSONObject page = qiqiaoFormsService.page(parentRecordVO);
        JSONArray parentRegulationList = page.getJSONArray("list");
        log.info("parentRegulationList: " + parentRegulationList);

        ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa;
        for (int i = 0; i < parentRegulationList.size(); ++i) {
            JSONObject parentRegulation = parentRegulationList.getJSONObject(i);
            JSONObject parentVariables = parentRegulation.getJSONObject("variables");
            String type = parentVariables.getString("类型");

            if (RegulationType.INTERNAL.equals(type)) {
                String parentQiqiaoRegulationId = parentVariables.getString("上级关联制度");
                RecordVO tmpRecordVO = new RecordVO();
                tmpRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
                tmpRecordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
                tmpRecordVO.setId(parentQiqiaoRegulationId);
                JSONObject parentRegulationJson = qiqiaoFormsService.queryById(tmpRecordVO);
                if (parentRegulationJson == null) {
                    continue;
                }
                JSONObject internalParentVariables = parentRegulationJson.getJSONObject("variables");
                JSONObject internalParentPrettyValue = parentRegulationJson.getJSONObject("prettyValue");

                String identifierB = internalParentVariables.getString("制度系统标识别文本");
                String codeB = internalParentVariables.getString("制度编号");
                String versionB = internalParentPrettyValue.getString("制度版本");
                String nameB = internalParentVariables.getString("制度名称");

                List<ZyRelatedRegulationBjmoa> zyRelatedRegulationBjmoaList = zyRelatedRegulationBjmoaMapper.queryInternalRelatedeRegulations(identifierA,
                        versionA, codeA, identifierB, versionB, codeB);
                if (zyRelatedRegulationBjmoaList.size() == 0) {
                    zyRelatedRegulationBjmoa = new ZyRelatedRegulationBjmoa();
                } else {
                    zyRelatedRegulationBjmoa = zyRelatedRegulationBjmoaList.get(0);
                }

                zyRelatedRegulationBjmoa.setRegulationIdentifierA(identifierA);
                zyRelatedRegulationBjmoa.setCodeA(codeA);
                zyRelatedRegulationBjmoa.setVersionA(versionA);
                zyRelatedRegulationBjmoa.setRegulationType(type);
                zyRelatedRegulationBjmoa.setRegulationIdentifierB(identifierB);
                zyRelatedRegulationBjmoa.setCodeB(codeB);
                zyRelatedRegulationBjmoa.setVersionB(versionB);
                zyRelatedRegulationBjmoa.setRegulationName(nameB);

                if (!zyRelatedRegulationBjmoaService.save(zyRelatedRegulationBjmoa)) {
                    log.warn("FAILED TO SAVE zyRelatedRegulationBjmoa=" + zyRelatedRegulationBjmoa);
                }
            } else if (RegulationType.EXTERNAL.equals(type)) {
                // 外部文件
                JSONArray externalFileList = parentVariables.getJSONArray("外部文件上传");
                for (int j = 0; j < externalFileList.size(); ++j) {
                    JSONObject externalFile = externalFileList.getJSONObject(j);
                    String identifierB = externalFile.getString("fileId");
                    String name = externalFile.getString("name");

                    List<ZyRelatedRegulationBjmoa> zyRelatedRegulationBjmoaList = zyRelatedRegulationBjmoaMapper.queryExternalRelatedeRegulations(identifierA,
                            versionA, codeA, identifierB);
                    if (zyRelatedRegulationBjmoaList.size() == 0) {
                        zyRelatedRegulationBjmoa = new ZyRelatedRegulationBjmoa();
                    } else {
                        zyRelatedRegulationBjmoa = zyRelatedRegulationBjmoaList.get(0);
                    }

                    zyRelatedRegulationBjmoa.setRegulationIdentifierA(identifierA);
                    zyRelatedRegulationBjmoa.setCodeA(codeA);
                    zyRelatedRegulationBjmoa.setVersionA(versionA);
                    zyRelatedRegulationBjmoa.setRegulationName(name);
                    zyRelatedRegulationBjmoa.setRegulationIdentifierB(identifierB);

                    if (!zyRelatedRegulationBjmoaService.save(zyRelatedRegulationBjmoa)) {
                        log.warn("FAILED TO SAVE zyRelatedRegulationBjmoa=" + zyRelatedRegulationBjmoa);
                    }
                }
            }
        }

        return true;
    }

    private String createIdentifier(String qiqiaoRealRegulationId) {
        // 更新记录
        String identifier = null;
        do {
            identifier = UUIDGenerator.generate();   // 生成唯一ID标识
            ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(identifier);
            log.info("create identifier " + identifier + " for QIQIAOID: " + qiqiaoRealRegulationId);
            if (zyRegulationBjmoa != null) {
                identifier = null;
            }
        } while (StringUtils.isEmpty(identifier));

        RecordVO updateRecordVO = new RecordVO();
        updateRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        updateRecordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        updateRecordVO.setId(qiqiaoRealRegulationId);
        Map<String, Object> data = new HashMap<>(1);
        data.put("制度系统标识别文本", identifier);
        updateRecordVO.setData(data);

        log.info("updateRecordVO: " + updateRecordVO);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateRecordVO);
        log.info("qiqiaoFormsService.saveOrUpdate: " + jsonObject);
        return identifier;
    }

    private ZyRegulationBjmoa queryByIdentifier(String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return null;
        }
        return zyRegulationBjmoaMapper.queryByIdentifier(identifier);
    }

    private List<JSONObject> toList(final JSONArray jsonArray) {
        if (CollectionUtils.isEmpty(jsonArray)) {
            return new ArrayList<>();
        }
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); ++i) {
            list.add(jsonArray.getJSONObject(i));
        }
        return list;
    }

}
