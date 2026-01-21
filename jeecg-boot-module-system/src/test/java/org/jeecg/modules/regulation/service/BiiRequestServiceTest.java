package org.jeecg.modules.regulation.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.oa.webservices.soap.workflow.*;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.regulation.constant.BiiOaWorkflowStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.jeecg.modules.oa.webservices.soap.workflow.WorkflowUtils.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BiiRequestServiceTest {
    @Autowired private WorkflowServiceImpl workflowClient;
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;
    @Value("${biisaas.biiRegulationInfo.applicationId}") private String biiRegulationInfoApplicationId;
    @Value("${biisaas.biiRegulationInfo.formModelId}") private String biiRegulationInfoFormModelId;
    @Value("${biisaas.biiRegulationInfo.regulationPlanFormModelId}") private String biiRegulationPlanFormModelId;

    @Test public void regulationPlan() throws Exception {
        final int creatorId = 6052;
        final String qiqiaoRegulationPlanId = "8188802651065851904";
        final WorkflowRequestInfo regulationPlan = createRegulationPlan(qiqiaoRegulationPlanId);
        System.out.println(regulationPlan);
        if (regulationPlan == null) {
            return;
        }
        final WorkflowServicePortType service = workflowClient.getWorkflowServiceHttpPort();
        final String requestId = service.doCreateWorkflowRequest(regulationPlan, creatorId);
        System.out.println("qiqiaoRegulationPlanId: " + qiqiaoRegulationPlanId + ", requestId: " + requestId);
        if (StringUtils.isNotEmpty(requestId) && !"-1".equals(requestId)) {
            // 更新七巧计划单状态及流程ID
            approvingRegulationPlan(qiqiaoRegulationPlanId, requestId);
        }
    }

    @Test public void approveRegulationPlan() {
        final String requestId = "3377912";
        approveRegulationPlan(requestId);
    }

    @Test public void rejectRegulationPlan() {
        final String requestId = "3377912";
        rejectRegulationPlan(requestId);
    }

    private WorkflowRequestInfo createRegulationPlan(final String qiqiaoRegulationPlanId) {
        if (StringUtils.isEmpty(qiqiaoRegulationPlanId)) {
            return null;
        }

        final Integer creatorId = 6052;
        final String creatorName = "王璐瑶";
        final String createTime = "2023-12-07";
        final String requestName = "京投-制度系统计划报送流程-" + creatorName + "-" + createTime;
        final Integer workflowId = 1710;

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
        System.out.println("planRecord: " + planRecord);
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
        System.out.println("regulationInfoRecord: " + regulationInfoRecord);
        if (regulationInfoRecord == null) {
            return null;
        }
        final JSONArray regulationInfoList = regulationInfoRecord.getJSONArray("list");
        for (int i = 0; i < regulationInfoList.size(); ++i) {
            final JSONObject regulationInfo = regulationInfoList.getJSONObject(i);
            final List<WorkflowRequestTableField> workflowRequestTableFieldList = convertRegulationInfo(regulationInfo);
            if (CollectionUtils.isEmpty(workflowRequestTableFieldList)) {
                System.out.println("FAILED TO CONVERT REGULATION INFO regulationInfo=" + regulationInfo);
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
        System.out.println("saveOrUpdate: " + jsonObject);
    }

    private void approveRegulationPlan(final String requestId) {
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
            System.out.println("regulationInfoRecord: " + regulationInfoRecord);
            if (regulationInfoRecord == null) {
                return;
            }
            final JSONArray regulationPlanList = regulationInfoRecord.getJSONArray("list");
            Assert.assertNotNull(regulationPlanList);
            Assert.assertEquals(1, regulationPlanList.size());

            final JSONObject regulationPlan = regulationPlanList.getJSONObject(0);

            final RecordVO updateVO = new RecordVO();
            updateVO.setApplicationId(biiRegulationInfoApplicationId);
            updateVO.setFormModelId(biiRegulationPlanFormModelId);

            qiqiaoRegulationPlanId = regulationPlan.getString("id");
            updateVO.setId(qiqiaoRegulationPlanId);

            final Map<String, Object> data = new HashMap<>(1);
            data.put("制度计划流程状态", BiiOaWorkflowStatus.APPROVED);
            updateVO.setData(data);

            System.out.println(updateVO);

            final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
            System.out.println("saveOrUpdate: " + jsonObject);
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
            System.out.println("regulationInfoRecord: " + regulationInfoRecord);
            if (regulationInfoRecord == null) {
                return;
            }
            final JSONArray regulationInfoList = regulationInfoRecord.getJSONArray("list");

            final RecordVO updateVO = new RecordVO();
            updateVO.setApplicationId(biiRegulationInfoApplicationId);
            updateVO.setFormModelId(biiRegulationInfoFormModelId);
            for (int i = 0; i < regulationInfoList.size(); ++i) {
                final JSONObject regulationInfo = regulationInfoList.getJSONObject(i);
                System.out.println(regulationInfo);

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
                System.out.println("updateVO: " + updateVO);
                final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
                System.out.println("saveOrUpdate: " + jsonObject);
            }
        }
    }

    private void rejectRegulationPlan(final String requestId) {
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
        System.out.println("regulationInfoRecord: " + regulationInfoRecord);
        if (regulationInfoRecord == null) {
            return;
        }
        final JSONArray regulationPlanList = regulationInfoRecord.getJSONArray("list");
        Assert.assertNotNull(regulationPlanList);
        Assert.assertEquals(1, regulationPlanList.size());

        final RecordVO updateVO = new RecordVO();
        updateVO.setApplicationId(biiRegulationInfoApplicationId);
        updateVO.setFormModelId(biiRegulationPlanFormModelId);

        final JSONObject regulationPlan = regulationPlanList.getJSONObject(0);

        updateVO.setId(regulationPlan.getString("id"));
        final Map<String, Object> data = new HashMap<>(1);
        data.put("制度计划流程状态", BiiOaWorkflowStatus.REJECTED);
        updateVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
        System.out.println("saveOrUpdate: " + jsonObject);
    }
}
