package org.jeecg.modules.bjmoa.regulation.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaDept;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaHistory;
import org.jeecg.modules.regulation.mapper.ZyRegulationBjmoaMapper;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaDeptService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaHistoryService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * @author zhouwei
 * @date 2024/10/29
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class RegulationBjmoaSyncDataServiceTest {

    @Autowired
    private IQiqiaoFormsService qiqiaoFormsService;
    @Autowired
    private ZyRegulationBjmoaMapper zyRegulationBjmoaMapper;
    @Autowired
    private IZyRegulationBjmoaService zyRegulationBjmoaService;
    @Autowired
    private IZyRegulationBjmoaHistoryService zyRegulationBjmoaHistoryService;
    @Autowired
    private IZyRegulationBjmoaDeptService zyRegulationBjmoaDeptService;


    @Value("${biisaas.bjmoaRegulationInfo.applicationId}")
    private String bjmoaRegulationInfoApplicationId;
    @Value("${biisaas.bjmoaRegulationInfo.realFormModelId}")
    private String bjmoaRealRegulationInfoFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realHistoryFormModelId}")
    private String bjmoaRealHistoryFormModelId;


    @Test
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
//        int pageSize = 100;
        int pageSize = 20;
        recordVO.setPageSize(pageSize);
        boolean finished = false;
        while (!finished) {
            recordVO.setPage(pageNo);
            JSONObject page = qiqiaoFormsService.page(recordVO);
            System.out.println(page);

            JSONArray regulationList = page.getJSONArray("list");
            for (int i = 0; i < regulationList.size(); i++) {

                JSONObject regulationJson = regulationList.getJSONObject(i);
                if (regulationJson == null) {
                    continue;
                }
                System.out.println(regulationJson);
                String qiqiaoRealRegulationId = regulationJson.getString("id");
                if (!syncRegulationFromQiqiaoToDatabase(qiqiaoRealRegulationId)) {
                    log.info("FAIL TO SYNC REGULATION BY QiqiaoRealRegulationID : " + qiqiaoRealRegulationId);
                }

            }

            finished = CollectionUtils.isEmpty(regulationList);
//            finished = true;
            pageNo++;

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
        System.out.println(variables);
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

        /*String categoryType = variables.getString("大类");
        if ("4".equals(categoryType)) {
        }*/

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

//    {"制度编号":"12522","制度跟进人":"6da99a0438801fdafe6b2cb48a7bbd0b","制度版本":"1","制度级别":"3",
//    "制度跟进人_pretty_value":"林友景","文件名称":"员工工服配置管理规定.docx","制度最新评审时间":1728462112000,
//    "制度发布时间":1722873600000,"制度归口管理部门":"7","文件水印":"","是否为基本管理制度":"2",
//    "制度主责部门_pretty_value":"京投公司-信息数据管理部","线路":"1","管理类别":"1","可编辑":"1",
//    "内管文件编号":"6728811f8183ab1b2405d5ce","制度状态":"7","业务子类":"17","制度实施时间":1605801600000,
//    "制度系统标识别文本":"8a8fb7c38c155c1b018c15fb753c1553","可删除":"2","制度主责部门":"7c1c05ac71614da0a66decf0add9cac1",
//    "内管文档编号":"3e715fbd-345f-40b3-8507-26273eeb429d","可预览":"1","制度名称":"安全生产风险管理规定","大类":"3"}


//    "variables":{"制度编号":"BJMOA-YJ-TY-XC-GJ-010","制度跟进人":"c209cec8bac32d7072642ed037c7e7bd",
//    "制度版本":"1","可下载":"2","制度级别":"9","文件名称":"BJMOA-YJ-TY-XC-GJ-010-A0 供电机电部雪天现场处置方案.pdf",
//    "制度发布时间":"2024-05-14","制度归口管理部门":"9","文件水印":"","预案分类":"9","线路":"1","可编辑":"2",
//    "内管文件编号":"6752a43a07917219e52faca8","制度状态":"7","制度实施时间":"2024-05-14","制度系统标识别文本":"","可删除":"2",
//    "制度主责部门":"3141389b190c4d6ea26b7d6acc55c9c4","内管文档编号":"2a975103-eec9-40a7-8f01-bf70391ad888",
//    "可预览":"1","制度名称":"供电机电部雪天现场处置方案","大类":"4"}

    private boolean syncRegulation(String qiqiaoRealRegulationId, ZyRegulationBjmoa zyRegulationBjmoa, JSONObject variables, JSONObject prettyValue) {
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
        if (StringUtils.isEmpty(qiqiaoRealRegulationId) || variables == null || prettyValue == null) {
            return false;
        }

        System.out.println("prettyValue"  + prettyValue);

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
            System.out.println("curMainDeptId" + curMainDeptId);
            if (StringUtils.isNotEmpty(curMainDeptId)) {
                final String curMainDeptName = variables.getString("制度主责部门_pretty_value");
                System.out.println("curMainDeptName" + curMainDeptName);
                if (curMainDeptId.equals(curMainDeptName)) {
                    log.warn(
                            "WEIRD DEPT NAME! qiqiaoRealRegulationId: " + qiqiaoRealRegulationId + ", curMainDeptName:" + curMainDeptName);
                } else {
                    List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList =
                            zyRegulationBjmoaDeptService.getByRegulationCodeAndVersion(code, curVersion);
                    System.out.println("zyRegulationBjmoaDeptList" + zyRegulationBjmoaDeptList);
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

    public ZyRegulationBjmoa queryByIdentifier(String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return null;
        }
        return zyRegulationBjmoaMapper.queryByIdentifier(identifier);
    }


}
