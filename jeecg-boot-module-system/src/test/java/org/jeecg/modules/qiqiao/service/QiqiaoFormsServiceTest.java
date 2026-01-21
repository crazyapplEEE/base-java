package org.jeecg.modules.qiqiao.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.regulation.entity.ZyRegulationBii;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiDept;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiHistory;
import org.jeecg.modules.regulation.service.IZyRegulationBiiDeptService;
import org.jeecg.modules.regulation.service.IZyRegulationBiiHistoryService;
import org.jeecg.modules.regulation.service.IZyRegulationBiiService;
import org.jeecg.modules.regulation.vo.ZyRegulationBiiHistoryVO;
import org.jeecg.modules.regulation.vo.ZyRegulationBiiVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JeecgSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QiqiaoFormsServiceTest {
    @Autowired private IZyRegulationBiiService zyRegulationBiiService;
    @Autowired private IZyRegulationBiiHistoryService zyRegulationBiiHistoryService;
    @Autowired private IZyRegulationBiiDeptService zyRegulationBiiDeptService;
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;
    @Value("${biisaas.bjmoaRegulationInfo.applicationId}") private String bjmoaRegulationInfoApplicationId;
    @Value("${biisaas.bjmoaRegulationInfo.formModelId}") private String bjmoaRegulationInfoFormModelId;
    @Value("${biisaas.biiRegulationInfo.applicationId}") private String biiRegulationInfoApplicationId;
    @Value("${biisaas.biiRegulationInfo.formModelId}") private String biiRegulationInfoFormModelId;
    @Value("${biisaas.biiRegulationInfo.realFormModelId}") private String biiRealRegulationInfoFormModelId;
    @Value("${biisaas.biiRegulationInfo.realHistoryFormModelId}")
    private String biiRealHistoryRegulationInfoFormModelId;
    @Value("${biisaas.biiRegulationInfo.parentRegulationFormModelId}")
    private String biiParentRegulationInfoFormModelId;

    @Test public void checkNull() {
        Assert.assertNotNull(qiqiaoFormsService);
    }

    @Test public void getBjmoaById() {
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);

        String id = "7586454996995211274";
        recordVO.setId(id);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        Assert.assertNotNull(record);
        System.out.println(record);

        // 有上级制度
        id = "7604799764389101578";
        recordVO.setId(id);
        record = qiqiaoFormsService.queryById(recordVO);
        Assert.assertNotNull(record);
        System.out.println(record);
    }

    @Test public void getBiiById() {
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationInfoFormModelId);

        String id = "7940043642838990859";
        recordVO.setId(id);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        Assert.assertNotNull(record);
        System.out.println(record);
    }

    @Test public void getBiiRealById() {
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRealRegulationInfoFormModelId);

        String id = "7871693670729326618";
        recordVO.setId(id);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        Assert.assertNotNull(record);
        System.out.println(record);
    }

    @Test public void getRelatedRegulations() {
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationInfoFormModelId);
        recordVO.setPage(1);
        recordVO.setPageSize(1000);

        String id = "7850843288134369291";
        recordVO.setId(id);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        System.out.println(record);
        Assert.assertNotNull(record);
        JSONObject variables = record.getJSONObject("variables");
        String regulationIdentifier = variables.getString("制度唯一标示");
        if (StringUtils.isEmpty(regulationIdentifier)) {
            regulationIdentifier = variables.getString("制度唯一标识文本");
        }
        System.out.println("当前制度编号: " + regulationIdentifier);

        recordVO.setFormModelId(biiParentRegulationInfoFormModelId);

        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("制度名称");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(id);
        fieldFilterList.add(fieldFilter);
        recordVO.setFilter(fieldFilterList);

        final JSONObject page = qiqiaoFormsService.page(recordVO);
        System.out.println(page);
        Assert.assertNotNull(page);

        final JSONArray relatedRegulationList = page.getJSONArray("list");
        for (int i = 0; i < relatedRegulationList.size(); ++i) {
            final JSONObject realRegulationJson = relatedRegulationList.getJSONObject(i);
            RecordVO tmpRecordVO = new RecordVO();
            tmpRecordVO.setApplicationId(biiRegulationInfoApplicationId);
            tmpRecordVO.setFormModelId(biiRealRegulationInfoFormModelId);
            variables = realRegulationJson.getJSONObject("variables");
            if (variables != null) {
                final String realRegulationId = variables.getString("上级关联制度");
                tmpRecordVO.setId(realRegulationId);

                final JSONObject realRegulation = qiqiaoFormsService.queryById(tmpRecordVO);
                System.out.println(realRegulation);
                Assert.assertNotNull(realRegulation);

                regulationIdentifier = realRegulation.getJSONObject("variables").getString("制度系统标识别文本");
                Assert.assertNotNull(regulationIdentifier);
                System.out.println("上级制度: " + regulationIdentifier);
            }
        }
    }

    @Test public void insertBiiRealById() {
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRealRegulationInfoFormModelId);

        Integer id = 808;
        insertToQiqiao(id);
    }

    void insertToQiqiao(final Integer regulationBiiId) {
        final ZyRegulationBiiVO zyRegulationBiiVO = zyRegulationBiiService.queryById(regulationBiiId, "");
        System.out.println(zyRegulationBiiVO);

        final String levelId = level2Qiqiao(zyRegulationBiiVO.getLevelId());
        if (StringUtils.isEmpty(levelId)) {
            return;
        }

        final List<ZyRegulationBiiHistoryVO> historyList = zyRegulationBiiVO.getHistoryList();
        String version = "";
        for (final ZyRegulationBiiHistoryVO history : historyList) {
            if (history.getCode().equals(zyRegulationBiiVO.getCode())) {
                version = history.getVersion();
                break;
            }
        }

        // 更新制度发布单
        Map<String, Object> data = new HashMap<>();
        data.put("制度名称", zyRegulationBiiVO.getName());
        data.put("制度编号", version);
        data.put("制度版本", version);
        data.put("制度级别", levelId);
        data.put("发布文号", zyRegulationBiiVO.getPublishNo());
        data.put("制度状态", zyRegulationBiiVO.getActive() == 1 ? "7" : "8");
        data.put("制度系统标识别文本", zyRegulationBiiVO.getIdentifier());
        data.put("制度分类", subCategory2Qiqiao(zyRegulationBiiVO.getSubCategoryName()));

        final Date publishTime = zyRegulationBiiVO.getPublishTime();
        if (publishTime != null) {
            data.put("制度发布时间", publishTime.getTime());
        } else {
            System.out.println("发布时间为空！" + regulationBiiId);
        }

        // 查询所有的部门
        JSONArray mainDeptList = new JSONArray();
        final List<ZyRegulationBiiDept> zyRegulationBiiDeptList =
                zyRegulationBiiDeptService.lambdaQuery().eq(ZyRegulationBiiDept::getCode,
                        zyRegulationBiiVO.getCode()).list();
        for (final ZyRegulationBiiDept zyRegulationBiiDept : zyRegulationBiiDeptList) {
            mainDeptList.add(zyRegulationBiiDept.getQiqiaoDeptId());
        }
        data.put("制度主责部门", mainDeptList);

        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRealRegulationInfoFormModelId);
        recordVO.setData(data);

        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(recordVO);
        System.out.println(jsonObject);
        if (jsonObject == null) {
            System.out.println("FAILED TO SAVE OR UPDATE recordVO " + recordVO);
            return;
        }

        final String qiqiaoRegulationId = jsonObject.getString("id");
        final ZyRegulationBii zyRegulationBii =
                zyRegulationBiiService.queryByIdentifier(zyRegulationBiiVO.getIdentifier());
        zyRegulationBii.setQiqiaoRegulationId(qiqiaoRegulationId);
        if (!zyRegulationBiiService.updateById(zyRegulationBii)) {
            System.out.println("FAILED TO UPDATE " + zyRegulationBii);
        }

        // 更新历史版本明细
        updateHistory(qiqiaoRegulationId, zyRegulationBiiVO.getCode(), historyList);

        // @todo 更新上级制度发布明细 （暂时不用做，旧制度系统不存在关联制度）
    }

    void updateHistory(final String qiqiaoRegulationId, final String currentCode,
                       final List<ZyRegulationBiiHistoryVO> historyList) {
        if (StringUtils.isEmpty(qiqiaoRegulationId) || StringUtils.isEmpty(currentCode) || CollectionUtils.isEmpty(historyList)) {
            System.out.println("WRONG INPUT");
            return;
        }

        for (final ZyRegulationBiiHistoryVO history : historyList) {
            final String code = history.getCode();
            if (currentCode.equals(code)) {
                final ZyRegulationBiiHistory zyRegulationBiiHistory =
                        zyRegulationBiiHistoryService.lambdaQuery().eq(ZyRegulationBiiHistory::getCode, code).one();
                zyRegulationBiiHistory.setQiqiaoRegulationId(qiqiaoRegulationId);
                if (!zyRegulationBiiHistoryService.updateById(zyRegulationBiiHistory)) {
                    System.out.println("FAILED TO UPDATE " + zyRegulationBiiHistory);
                }
                continue;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("外键", qiqiaoRegulationId);
            data.put("制度名称", history.getName());
            data.put("制度编号", history.getVersion());
            data.put("制度版本", history.getVersion());
            data.put("制度级别", level2Qiqiao(history.getLevelId()));
            data.put("制度分类", subCategory2Qiqiao(history.getSubCategoryName()));

            final Date publishTime = history.getPublishTime();
            if (publishTime != null) {
                data.put("发布日期", publishTime.getTime());
            }
            final Date abolishTime = history.getAbolishTime();
            if (abolishTime != null) {
                data.put("作废日期", abolishTime.getTime());
            }
            data.put("发布文号", history.getPublishNo());

            JSONArray mainDeptList = new JSONArray();
            final List<ZyRegulationBiiDept> zyRegulationBiiDeptList =
                    zyRegulationBiiDeptService.lambdaQuery().eq(ZyRegulationBiiDept::getCode, code).list();
            for (final ZyRegulationBiiDept zyRegulationBiiDept : zyRegulationBiiDeptList) {
                mainDeptList.add(zyRegulationBiiDept.getQiqiaoDeptId());
            }
            data.put("主责部门", mainDeptList);
            data.put("制度唯一标识", history.getIdentifier());

            RecordVO recordVO = new RecordVO();
            recordVO.setApplicationId(biiRegulationInfoApplicationId);
            recordVO.setFormModelId(biiRealHistoryRegulationInfoFormModelId);
            recordVO.setData(data);

            final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(recordVO);
            final String qiqiaoHistoryRegulationId = jsonObject.getString("id");
            final ZyRegulationBiiHistory zyRegulationBiiHistory =
                    zyRegulationBiiHistoryService.lambdaQuery().eq(ZyRegulationBiiHistory::getCode, code).one();
            zyRegulationBiiHistory.setQiqiaoRegulationId(qiqiaoHistoryRegulationId);
            if (!zyRegulationBiiHistoryService.updateById(zyRegulationBiiHistory)) {
                System.out.println("FAILED TO UPDATE " + zyRegulationBiiHistory);
            }

        }
    }

    String level2Qiqiao(final String level) {
        if (StringUtils.isEmpty(level)) {
            return null;
        }

        String result = null;
        switch (level) {
            case "lvl1": {
                result = "1";
                break;
            }
            case "lvl2":
            case "lvl2*": {
                result = "2";
                break;
            }
            case "lvl3": {
                result = "3";
                break;
            }
            case "lvl4": {
                result = "4";
                break;
            }
            default: {
                break;
            }
        }

        return result;
    }

    String subCategory2Qiqiao(final String subCategoryName) {
        String result = null;
        if (StringUtils.isEmpty(subCategoryName)) {
            return result;
        }

        switch (subCategoryName) {
            case "董事会管理": {
                result = "1";
                break;
            }
            case "行政管理": {
                result = "2";
                break;
            }
            case "人力资源管理": {
                result = "3";
                break;
            }
            case "财务管理": {
                result = "4";
                break;
            }
            case "审计管理": {
                result = "5";
                break;
            }
            case "法律合规管理": {
                result = "6";
                break;
            }
            case "采购合同管理": {
                result = "7";
                break;
            }
            case "信息数据管理": {
                result = "8";
                break;
            }
            case "科研管理": {
                result = "9";
                break;
            }
            case "安全管理": {
                result = "10";
                break;
            }
            case "综合管理": {
                result = "11";
                break;
            }
            case "出资企业管理": {
                result = "12";
                break;
            }
            case "业务管理": {
                result = "13";
                break;
            }
            case "党群管理": {
                result = "14";
                break;
            }
            case "廉政建设管理": {
                result = "15";
                break;
            }
            default: {
                break;
            }
        }

        return result;
    }

    @Test public void getFile() {
        RecordVO relatedRecordVO = new RecordVO();
        relatedRecordVO.setApplicationId("aaeb1362800048ee82c3d4dc13fd186b"); // 制度系统【轨道运营】
        relatedRecordVO.setFormModelId("65027ff58cc544365b8f460f"); // 发布上级制度明细
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("制度名称");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue("7939507356042543127");
        fieldFilterList.add(fieldFilter);
        relatedRecordVO.setFilter(fieldFilterList);

        final JSONObject page = qiqiaoFormsService.page(relatedRecordVO);
        final JSONArray relatedRegulationList = page.getJSONArray("list");
        System.out.println("relatedRegulationList: " + relatedRegulationList);

        Assert.assertNotNull(relatedRegulationList);
        for (int i = 0; i < relatedRegulationList.size(); ++i) {
            final JSONObject jsonObject = relatedRegulationList.getJSONObject(i);
            final JSONObject variables = jsonObject.getJSONObject("variables");
            final String type = variables.getString("类型");
            if ("2".equals(type)) {
                // 外部文件
                final String externalFileName = variables.getString("外部文件名称");
                final JSONArray externalFileList = variables.getJSONArray("外部文件上传");
                System.out.println(externalFileName);
                for (int j = 0; j < externalFileList.size(); ++j) {
                    final JSONObject externalFile = externalFileList.getJSONObject(j);
                    System.out.println(externalFile);

                    final String name = externalFile.getString("name");
                    final String fileId = externalFile.getString("fileId");

                    RecordVO downloadRecordVO = new RecordVO();
                    downloadRecordVO.setApplicationId("aaeb1362800048ee82c3d4dc13fd186b"); // 制度系统【轨道运营】
                    downloadRecordVO.setFileId(fileId);
                    qiqiaoFormsService.download(downloadRecordVO, name);
                }
            }
        }
    }
}
