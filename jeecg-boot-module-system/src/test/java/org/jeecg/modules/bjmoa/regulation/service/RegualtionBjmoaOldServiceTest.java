package org.jeecg.modules.bjmoa.regulation.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.dto.EcmFileDTO;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.regulation.constant.RegulationType;
import org.jeecg.modules.regulation.dto.RegulationTempQueryDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaDept;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaHistory;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulationBjmoa;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaDeptService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaHistoryService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaService;
import org.jeecg.modules.regulation.service.IZyRelatedRegulationBjmoaService;
import org.jeecg.modules.regulation.vo.ZyRegulationTempBjmoaVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegualtionBjmoaOldServiceTest {
    @Autowired @Qualifier("bjmoaContentManagementService") private IContentManagementService contentManagementService;
    @Autowired private IZyRegulationBjmoaService zyRegulationBjmoaService;
    @Autowired private IZyRegulationBjmoaDeptService zyRegulationBjmoaDeptService;
    @Autowired private IZyRegulationBjmoaHistoryService zyRegulationBjmoaHistoryService;
    @Autowired private IZyRelatedRegulationBjmoaService zyRelatedRegulationBjmoaService;
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;

    @Value("${content-management.bjmoaAppId}") private String bjmoaAppId;
    @Value("${biisaas.bjmoaRegulationInfo.applicationId}") private String bjmoaRegulationInfoApplicationId;
    @Value("${biisaas.bjmoaRegulationInfo.formModelId}") private String bjmoaRegulationInfoFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realHistoryFormModelId}") private String bjmoaRealHistoryFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realParentFormModelId}") private String bjmoRealParentFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realRelatedFormModelId}") private String bjmoaRealRelatedFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realFormModelId}") private String bjmoaRealRegulationInfoFormModelId;

    private static List<JSONObject> toList(final JSONArray jsonArray) {
        if (CollectionUtils.isEmpty(jsonArray)) {
            return new ArrayList<>();
        }
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); ++i) {
            list.add(jsonArray.getJSONObject(i));
        }
        return list;
    }

    @Test public void checkNull() {
        Assert.assertNotNull(qiqiaoFormsService);
    }

    @Test public void syncBjmoaOldAll() {
        // 获取所有【轨道运营制度发布单】的记录
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);

        int pageNo = 1;
        final int pageSize = 10;
        recordVO.setPageSize(pageSize);
        boolean finished = false;
        while (!finished) {
            recordVO.setPage(pageNo);
            final JSONObject pageJson = qiqiaoFormsService.page(recordVO);

            System.out.println(pageJson);
            Assert.assertNotNull(pageJson);

            final JSONArray realRegulationList = pageJson.getJSONArray("list");
            for (int i = 0; i < realRegulationList.size(); ++i) {
                final JSONObject realRegulationJson = realRegulationList.getJSONObject(i);
                if (realRegulationJson == null) {
                    continue;
                }
                final String qiqiaoRealRegulationId = realRegulationJson.getString("id");
                if (!syncBjmoaOld(qiqiaoRealRegulationId)) {
                    System.out.println("FAILED TO SYNCHRONIZE REGULATION OLD id=" + qiqiaoRealRegulationId);
                }
            }

            finished = CollectionUtils.isEmpty(realRegulationList);
            ++pageNo;
        }
    }

    @Test public void syncBjmoaOldById() {
        String qiqiaoRealRegulationId = "8159646523274584067";
        if (!syncBjmoaOld(qiqiaoRealRegulationId)) {
            System.out.println("FAILED TO SYNC " + qiqiaoRealRegulationId);
        }
    }

    @Test public void inactivateByIdentifier() {
        String identifier = "8a8fb7c38c1478cc018c1479b17d0048";
        zyRegulationBjmoaService.inactivateByIdentifier(identifier);
    }

    private boolean syncBjmoaOld(final String qiqiaoRealRegulationId) {
        final String prefix = "[syncBjmoaOld " + qiqiaoRealRegulationId + "] ";
        if (StringUtils.isEmpty(qiqiaoRealRegulationId)) {
            return false;
        }

        final ZyRegulationBjmoa zyRegulationBjmoa = new ZyRegulationBjmoa();
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRealRegulationId);
        final JSONObject regulationJson = qiqiaoFormsService.queryById(recordVO);

        final JSONObject variables = regulationJson.getJSONObject("variables");
        final JSONObject prettyValue = regulationJson.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            System.out.println(prefix + "variables or prettyValue is empty");
            return false;
        }
        final String status = variables.getString("制度状态");
        if (!"7".equals(status) && !"8".equals(status)) {
            System.out.println(prefix + "NO NEED TO SYNC REGULATION " + qiqiaoRealRegulationId);
            return false;
        }
        zyRegulationBjmoa.setActive("7".equals(status) ? 1 : 0);

        final String code = variables.getString("制度编号");
        if (StringUtils.isEmpty(code)) {
            System.out.println(prefix + "CODE IS EMPTY");
            return false;
        }
        zyRegulationBjmoa.setCode(code);

        zyRegulationBjmoa.setLevelId(variables.getString("制度级别"));
        zyRegulationBjmoa.setLevelName(prettyValue.getString("制度级别"));
        zyRegulationBjmoa.setLineId(variables.getString("线路"));
        zyRegulationBjmoa.setLineName(prettyValue.getString("线路"));
        zyRegulationBjmoa.setContingencyPlanCategoryId(variables.getString("预案分类"));
        zyRegulationBjmoa.setContingencyPlanCategoryId(prettyValue.getString("预案分类"));
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

        final String qiqiaoCreatorId = variables.getString("制度跟进人");
        final String qiqiaoCreatorName = variables.getString("制度跟进人_pretty_value");
        if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(qiqiaoCreatorName)
            && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
            zyRegulationBjmoa.setQiqiaoCreatorId(qiqiaoCreatorId);
            zyRegulationBjmoa.setQiqiaoCreatorName(qiqiaoCreatorName);
        }
        zyRegulationBjmoa.setQiqiaoRegulationId(qiqiaoRealRegulationId);
        if (!zyRegulationBjmoaService.save(zyRegulationBjmoa)) {
            System.out.println(prefix + "FAILED TO SAVE zyRegulationBjmoa=" + zyRegulationBjmoa);
            return false;
        }

        // save history and department
        if (!syncHistoryAndDepartment(qiqiaoRealRegulationId, variables, prettyValue)) {
            System.out.println(prefix + "FAILED TO SAVE history id=" + qiqiaoRealRegulationId);
            return false;
        }

        // save parent
        if (!syncParent(qiqiaoRealRegulationId, variables, prettyValue)) {
            System.out.println(prefix + "FAILED TO SAVE parent id=" + qiqiaoRealRegulationId);
            return false;
        }

        // save related
        if (!syncRelated(qiqiaoRealRegulationId, variables, prettyValue)) {
            System.out.println(prefix + "FAILED TO SAVE related id=" + qiqiaoRealRegulationId);
            return false;
        }

        return true;
    }

    private boolean syncHistoryAndDepartment(final String qiqiaoRealRegulationId, final JSONObject latestVariables,
        final JSONObject latestPrettyValue) {
        if (StringUtils.isEmpty(qiqiaoRealRegulationId) || latestVariables == null || latestPrettyValue == null) {
            return false;
        }

        final RecordVO historyRecordVO = new RecordVO();
        historyRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        historyRecordVO.setFormModelId(bjmoaRealHistoryFormModelId);
        final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        final FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("外键");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(qiqiaoRealRegulationId);
        fieldFilterList.add(fieldFilter);
        historyRecordVO.setFilter(fieldFilterList);

        historyRecordVO.setPage(1);
        historyRecordVO.setPageSize(1000);
        final JSONObject page = qiqiaoFormsService.page(historyRecordVO);
        if (page == null) {
            System.out.println("PAGE IS NULL FOR historyRecordVO=" + historyRecordVO);
            return false;
        }
        final JSONArray historyRegulationArray = page.getJSONArray("list");
        System.out.println("historyRegulationArray: " + historyRegulationArray);

        List<JSONObject> historyRegulationList = toList(historyRegulationArray);
        historyRegulationList = historyRegulationList.stream()
            .sorted(Comparator.comparing(o -> (o.getJSONObject("variables").getString("发布日期"))))
            .collect(Collectors.toList());

        System.out.println(historyRegulationList);

        // 由最早的版本开始添加
        final String identifier = latestVariables.getString("制度系统标识别文本");
        for (final JSONObject historyRegulation : historyRegulationList) {
            final JSONObject curVariables = historyRegulation.getJSONObject("variables");
            final JSONObject curPrettyValue = historyRegulation.getJSONObject("prettyValue");
            if (curVariables == null) {
                continue;
            }

            final String curCode = curVariables.getString("制度编号");
            final String curName = curVariables.getString("制度名称");
            final String curPublishDate = curVariables.getString("发布日期");
            final String curExecuteDate = curVariables.getString("实施日期");
            final String curAbolishDate = curVariables.getString("作废日期");

            final String curFileId = curVariables.getString("内管文件编号");
            final String curDocId = curVariables.getString("内管文档编号");

            final String curFileName = curVariables.getString("文件名称");
            final String curVersion = curPrettyValue.getString("版本");

            final String qiqiaoCreatorId = curVariables.getString("制度跟进人");
            final String qiqiaoCreatorName = curPrettyValue.getString("制度跟进人");

            final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
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
                e.printStackTrace();
            }

            zyRegulationBjmoaHistory.setContentFileId(curFileId);
            zyRegulationBjmoaHistory.setContentDocId(curDocId);
            zyRegulationBjmoaHistory.setVersion(curVersion);

            if (!zyRegulationBjmoaHistoryService.save(zyRegulationBjmoaHistory)) {
                System.out.println("FAILED TO SAVE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            }

            // 保存主责部门
            final String curMainDeptId = curVariables.getString("主责部门");
            final String curMainDeptName = curPrettyValue.getString("主责部门");
            if (StringUtils.isNotEmpty(curMainDeptId) && StringUtils.isNotEmpty(
                curMainDeptName) && !curMainDeptId.equals(curMainDeptName)) {
                final ZyRegulationBjmoaDept zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
                zyRegulationBjmoaDept.setCode(curCode);
                zyRegulationBjmoaDept.setVersion(curVersion);
                zyRegulationBjmoaDept.setName(curName);
                zyRegulationBjmoaDept.setIdentifier(identifier);
                zyRegulationBjmoaDept.setQiqiaoDeptId(curMainDeptId);
                zyRegulationBjmoaDept.setQiqiaoDeptName(curMainDeptName);

                if (!zyRegulationBjmoaDeptService.save(zyRegulationBjmoaDept)) {
                    System.out.println("FAILED TO SAVE zyRegulationBjmoaDept=" + zyRegulationBjmoaDept);
                }
            }
        }

        // 最新版本
        {
            final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
            zyRegulationBjmoaHistory.setIdentifier(identifier);

            final String qiqiaoCreatorId = latestVariables.getString("制度跟进人");
            final String qiqiaoCreatorName = latestVariables.getString("制度跟进人_pretty_value");
            if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(qiqiaoCreatorName)
                && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
                zyRegulationBjmoaHistory.setQiqiaoCreatorId(qiqiaoCreatorId);
                zyRegulationBjmoaHistory.setQiqiaoCreatorName(qiqiaoCreatorName);
            }

            final String curCode = latestVariables.getString("制度编号");
            zyRegulationBjmoaHistory.setCode(curCode);

            final String curName = latestVariables.getString("制度名称");
            zyRegulationBjmoaHistory.setName(curName);

            try {
                final Long publishTime = latestVariables.getLong("制度发布时间");
                final Date publishTimeDate = publishTime == null ? null : DateUtils.getDate(publishTime);
                zyRegulationBjmoaHistory.setPublishTime(publishTimeDate);

                final Long executeTime = latestVariables.getLong("制度实施时间");
                final Date executeTimeDate = executeTime == null ? null : DateUtils.getDate(executeTime);
                zyRegulationBjmoaHistory.setExecuteTime(executeTimeDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            zyRegulationBjmoaHistory.setContentFileId(latestVariables.getString("内管文件编号"));
            zyRegulationBjmoaHistory.setContentDocId(latestVariables.getString("内管文档编号"));

            final String curVersion = latestPrettyValue.getString("制度版本");
            zyRegulationBjmoaHistory.setVersion(curVersion);

            zyRegulationBjmoaHistory.setLevelId(latestVariables.getString("制度级别"));
            zyRegulationBjmoaHistory.setLevelName(latestPrettyValue.getString("制度级别"));
            zyRegulationBjmoaHistory.setLineId(latestVariables.getString("线路"));
            zyRegulationBjmoaHistory.setLineName(latestPrettyValue.getString("线路"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryId(latestVariables.getString("预案分类"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryId(latestPrettyValue.getString("预案分类"));
            zyRegulationBjmoaHistory.setCategoryId(latestVariables.getString("大类"));
            zyRegulationBjmoaHistory.setCategoryName(latestPrettyValue.getString("大类"));
            zyRegulationBjmoaHistory.setManagementCategoryId(latestVariables.getString("管理类别"));
            zyRegulationBjmoaHistory.setManagementCategoryName(latestPrettyValue.getString("管理类别"));
            zyRegulationBjmoaHistory.setSubCategoryId(latestVariables.getString("业务子类"));
            zyRegulationBjmoaHistory.setSubCategoryName(latestPrettyValue.getString("业务子类"));

            if (!zyRegulationBjmoaHistoryService.save(zyRegulationBjmoaHistory)) {
                System.out.println("FAILED TO SAVE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            }

            // 保存主责部门
            final String curMainDeptId = latestVariables.getString("制度主责部门");
            if (StringUtils.isNotEmpty(curMainDeptId)) {
                final String curMainDeptName = latestVariables.getString("制度主责部门_pretty_value");
                final ZyRegulationBjmoaDept zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
                zyRegulationBjmoaDept.setCode(curCode);
                zyRegulationBjmoaDept.setVersion(curVersion);
                zyRegulationBjmoaDept.setName(curName);
                zyRegulationBjmoaDept.setIdentifier(identifier);
                zyRegulationBjmoaDept.setQiqiaoDeptId(curMainDeptId);
                zyRegulationBjmoaDept.setQiqiaoDeptName(curMainDeptName);

                if (!zyRegulationBjmoaDeptService.save(zyRegulationBjmoaDept)) {
                    System.out.println("FAILED TO SAVE zyRegulationBjmoaDept=" + zyRegulationBjmoaDept);
                }
            }
        }

        return true;
    }

    private boolean syncParent(final String qiqiaoRealRegulationId, final JSONObject latestVariables,
        final JSONObject latestPrettyValue) {
        if (StringUtils.isEmpty(qiqiaoRealRegulationId) || latestVariables == null || latestPrettyValue == null) {
            return false;
        }
        final String identifierA = latestVariables.getString("制度系统标识别文本");
        final String codeA = latestVariables.getString("制度编号");
        final String versionA = latestPrettyValue.getString("制度版本");

        final RecordVO parentRecordVO = new RecordVO();
        parentRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId); // 制度系统【轨道运营】
        parentRecordVO.setFormModelId(bjmoRealParentFormModelId); // 发布上级制度明细
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("制度名称");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(qiqiaoRealRegulationId);
        fieldFilterList.add(fieldFilter);
        parentRecordVO.setFilter(fieldFilterList);

        final JSONObject page = qiqiaoFormsService.page(parentRecordVO);
        final JSONArray parentRegulationList = page.getJSONArray("list");
        System.out.println("parentRegulationList: " + parentRegulationList);

        Assert.assertNotNull(parentRegulationList);

        final ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa = new ZyRelatedRegulationBjmoa();
        zyRelatedRegulationBjmoa.setRegulationIdentifierA(identifierA);
        zyRelatedRegulationBjmoa.setCodeA(codeA);
        zyRelatedRegulationBjmoa.setVersionA(versionA);
        for (int i = 0; i < parentRegulationList.size(); ++i) {
            final JSONObject parentRegulation = parentRegulationList.getJSONObject(i);
            final JSONObject variables = parentRegulation.getJSONObject("variables");
            final String type = variables.getString("类型");
            zyRelatedRegulationBjmoa.setRegulationType(type);

            if (RegulationType.INTERNAL.equals(type)) {
                final String parentQiqiaoRegulationId = variables.getString("上级关联制度");
                final RecordVO tmpRecordVO = new RecordVO();
                tmpRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
                tmpRecordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
                tmpRecordVO.setId(parentQiqiaoRegulationId);
                final JSONObject parentRegulationJson = qiqiaoFormsService.queryById(tmpRecordVO);
                if (parentRegulationJson == null) {
                    continue;
                }
                final JSONObject parentVariables = parentRegulationJson.getJSONObject("variables");
                final JSONObject parentPrettyValue = parentRegulationJson.getJSONObject("prettyValue");

                final String identifierB = parentVariables.getString("制度系统标识别文本");
                final String codeB = parentVariables.getString("制度编号");
                final String versionB = parentPrettyValue.getString("制度版本");
                final String nameB = parentVariables.getString("制度名称");
                zyRelatedRegulationBjmoa.setRegulationIdentifierB(identifierB);
                zyRelatedRegulationBjmoa.setCodeB(codeB);
                zyRelatedRegulationBjmoa.setVersionB(versionB);
                zyRelatedRegulationBjmoa.setRegulationName(nameB);

                if (!zyRelatedRegulationBjmoaService.save(zyRelatedRegulationBjmoa)) {
                    System.out.println("FAILED TO SAVE zyRelatedRegulationBjmoa=" + zyRelatedRegulationBjmoa);
                }
            } else if (RegulationType.EXTERNAL.equals(type)) {
                // 外部文件
                // final String externalFileName = variables.getString("外部文件名称");
                final JSONArray externalFileList = variables.getJSONArray("外部文件上传");
                for (int j = 0; j < externalFileList.size(); ++j) {
                    final JSONObject externalFile = externalFileList.getJSONObject(j);

                    final String name = externalFile.getString("name");
                    zyRelatedRegulationBjmoa.setRegulationName(name);

                    final String fileId = externalFile.getString("fileId");
                    zyRelatedRegulationBjmoa.setRegulationIdentifierB(fileId);

                    if (!zyRelatedRegulationBjmoaService.save(zyRelatedRegulationBjmoa)) {
                        System.out.println("FAILED TO SAVE zyRelatedRegulationBjmoa=" + zyRelatedRegulationBjmoa);
                    }
                }
            }
        }

        return true;
    }

    private boolean syncRelated(final String qiqiaoRealRegulationId, final JSONObject latestVariables,
        final JSONObject latestPrettyValue) {
        if (StringUtils.isEmpty(qiqiaoRealRegulationId) || latestVariables == null || latestPrettyValue == null) {
            return false;
        }
        final String identifierA = latestVariables.getString("制度系统标识别文本");
        final String codeA = latestVariables.getString("制度编号");
        final String versionA = latestPrettyValue.getString("制度版本");

        final RecordVO relatedRecordVO = new RecordVO();
        relatedRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId); // 制度系统【轨道运营】
        relatedRecordVO.setFormModelId(bjmoaRealRelatedFormModelId); // 发布库关联记录明细
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("制度名称");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(qiqiaoRealRegulationId);
        fieldFilterList.add(fieldFilter);
        relatedRecordVO.setFilter(fieldFilterList);

        final JSONObject page = qiqiaoFormsService.page(relatedRecordVO);
        final JSONArray relatedRegulationList = page.getJSONArray("list");
        System.out.println("relatedRegulationList: " + relatedRegulationList);

        Assert.assertNotNull(relatedRegulationList);

        final ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa = new ZyRelatedRegulationBjmoa();
        zyRelatedRegulationBjmoa.setRegulationIdentifierA(identifierA);
        zyRelatedRegulationBjmoa.setCodeA(codeA);
        zyRelatedRegulationBjmoa.setVersionA(versionA);

        final String qiqiaoCreatorId = latestVariables.getString("制度跟进人");
        final String qiqiaoCreatorName = latestVariables.getString("制度跟进人_pretty_value");
        final String status = latestVariables.getString("制度状态");
        final int isActive = "7".equals(status) ? 1 : 0;
        for (int i = 0; i < relatedRegulationList.size(); ++i) {
            final JSONObject relateRegulation = relatedRegulationList.getJSONObject(i);
            final JSONObject variables = relateRegulation.getJSONObject("variables");
            final JSONObject prettyValue = relateRegulation.getJSONObject("prettyValue");
            final String relatedIdentifier = variables.getString("关联记录唯一标识");
            final String relatedName = variables.getString("关联记录文件名称");
            final String relatedCode = variables.getString("关联记录编号");
            final String relatedVersion = prettyValue.getString("版本号");

            final String levelId = "12";
            final String levelName = "5级（表单）";

            zyRelatedRegulationBjmoa.setRegulationType(RegulationType.RELATED);
            zyRelatedRegulationBjmoa.setRegulationIdentifierB(relatedIdentifier);
            zyRelatedRegulationBjmoa.setCodeB(relatedCode);
            zyRelatedRegulationBjmoa.setVersionB(relatedVersion);
            zyRelatedRegulationBjmoa.setRegulationName(relatedName);
            if (!zyRelatedRegulationBjmoaService.save(zyRelatedRegulationBjmoa)) {
                System.out.println("FAILED TO SAVE zyRelatedRegulationBjmoa=" + zyRelatedRegulationBjmoa);
            }

            final JSONArray externalFileList = variables.getJSONArray("关联记录上传");
            Assert.assertEquals(1, externalFileList.size());

            ZyRegulationBjmoa zyRegulationBjmoa =
                zyRegulationBjmoaService.lambdaQuery().eq(ZyRegulationBjmoa::getIdentifier, relatedIdentifier).one();
            if (zyRegulationBjmoa == null) {
                zyRegulationBjmoa = new ZyRegulationBjmoa();
            }
            zyRegulationBjmoa.setIdentifier(relatedIdentifier);
            zyRegulationBjmoa.setActive(isActive);
            zyRegulationBjmoa.setName(relatedName);
            zyRegulationBjmoa.setCode(relatedCode);
            zyRegulationBjmoa.setLevelId(levelId);
            zyRegulationBjmoa.setLevelName(levelName);
            if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(qiqiaoCreatorName)
                && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
                zyRegulationBjmoa.setQiqiaoCreatorName(qiqiaoCreatorName);
                zyRegulationBjmoa.setQiqiaoCreatorId(qiqiaoCreatorId);
            }
            final Long publishTime = latestVariables.getLong("制度发布时间");
            final Date publishTimeDate = publishTime == null ? null : DateUtils.getDate(publishTime);
            final Long executeTime = latestVariables.getLong("制度实施时间");
            final Date executeTimeDate = executeTime == null ? null : DateUtils.getDate(executeTime);

            for (int j = 0; j < externalFileList.size(); ++j) {
                final JSONObject externalFile = externalFileList.getJSONObject(j);
                final String name = externalFile.getString("name");
                final String fileId = externalFile.getString("fileId");

                RecordVO downloadRecordVO = new RecordVO();
                downloadRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId); // 制度系统【轨道运营】
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
                    // Trick: 如果之前添加过这条关联记录，只需要更新即可
                    if (!zyRegulationBjmoaService.saveOrUpdate(zyRegulationBjmoa)) {
                        System.out.println("FAILED TO UPDATE zyRegulationBjmoa=" + zyRegulationBjmoa);
                    }

                    final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
                    BeanUtils.copyProperties(zyRegulationBjmoa, zyRegulationBjmoaHistory);
                    zyRegulationBjmoaHistory.setVersion(relatedVersion);
                    zyRegulationBjmoaHistory.setPublishTime(publishTimeDate);
                    zyRegulationBjmoaHistory.setExecuteTime(executeTimeDate);
                    if (!zyRegulationBjmoaHistoryService.save(zyRegulationBjmoaHistory)) {
                        System.out.println("FAILED TO SAVE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

    @Test public void tempList() {
        Page<ZyRegulationTempBjmoaVO> page = new Page<>(1, 10);
        RegulationTempQueryDTO queryDTO = new RegulationTempQueryDTO();

        // @todo 权限控制
        page = zyRegulationBjmoaService.queryTempPageList(page, queryDTO);
    }

    @Test public void tempId() {
        String id = "8138996801551515648";
        String mark = "";
        zyRegulationBjmoaService.tempQueryById(id, mark);
    }
}
