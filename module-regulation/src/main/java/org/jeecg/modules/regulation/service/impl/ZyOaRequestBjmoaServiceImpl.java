package org.jeecg.modules.regulation.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoCallBackService;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.regulation.constant.BiiOaWorkflowStatus;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaHistory;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulationBjmoa;
import org.jeecg.modules.regulation.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author MENG RAN
 * @date 2024-01-11
 */
@Service @Slf4j public class ZyOaRequestBjmoaServiceImpl implements IZyOaRequestBjmoaService {
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;
    @Autowired private IQiqiaoCallBackService qiqiaoCallBackService;
    @Autowired private IZyRegulationBjmoaHistoryService zyRegulationBjmoaHistoryService;
    @Autowired private IZyRegulationBjmoaService zyRegulationBjmoaService;
    @Autowired private IZyRelatedRegulationBjmoaService zyRelatedRegulationBjmoaService;
    @Autowired private IZyRegulationArchiveBjmoaService zyRegulationArchiveBjmoaService;
    @Value("${biisaas.bjmoaRegulationInfo.applicationId}") private String bjmoaRegulationInfoApplicationId;
    @Value("${biisaas.bjmoaRegulationInfo.formModelId}") private String bjmoaRegulationInfoFormModelId;//信息单
    @Value("${biisaas.bjmoaRegulationInfo.realFormModelId}") private String bjmoaRealRegulationInfoFormModelId;//发布单
    @Value("${biisaas.callbackTaskId.OACallBackTaskId}") private String OACallBackTaskId;
    @Value("${biisaas.callbackTaskId.BoardOACallBackTaskId}") private String BoardOACallBackTaskId;
    @Value("${biisaas.bjmoaRegulationInfo.boardRegulationFormModelId}") private String boardRegulationFormModelId;

    @Override public void approveRegulationPublish(final String requestId, String yfrq) {
        String traceId = "bjmoa_OA_callback" + "@" + requestId + "@" + DateUtils.getDate("yyyyMMddHHmmss");
        log.info(traceId + " requestId: {}, yfrq: {}", requestId, yfrq);
        if (StringUtils.isEmpty(requestId)) {
            return;
        }

        // 更新【制度计划立项单】

        final RecordVO queryVO = new RecordVO();
        queryVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        queryVO.setFormModelId(bjmoaRegulationInfoFormModelId);

        final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        final FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("oaid");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(requestId);
        fieldFilterList.add(fieldFilter);
        queryVO.setFilter(fieldFilterList);

        JSONObject regulationInfoRecord = qiqiaoFormsService.page(queryVO);
        log.info(traceId + " regulationInfoRecord: " + regulationInfoRecord);
        if (regulationInfoRecord == null || regulationInfoRecord.getJSONArray("list").size() == 0
        || regulationInfoRecord.getIntValue("totalCount") == 0) {
            queryVO.setFormModelId(boardRegulationFormModelId);
            regulationInfoRecord = qiqiaoFormsService.page(queryVO);
            log.info(traceId + "board regulationInfoRecord: " + regulationInfoRecord);
            if (regulationInfoRecord == null  || regulationInfoRecord.getJSONArray("list").size() == 0
                    || regulationInfoRecord.getIntValue("totalCount") == 0) {
                log.warn(traceId + " CANNOT FIND regulationInfoRecord BY OAID : " + requestId);
                return;
            }
        }
        final JSONArray regulationPlanList = regulationInfoRecord.getJSONArray("list");
        if (regulationPlanList == null || regulationPlanList.size() != 1) {
            log.warn(traceId + " WEIRD regulationPlanList=" + regulationPlanList);
            return;
        }

        final JSONObject regulationPlan = regulationPlanList.getJSONObject(0);
        String qiqiaoRegulationPlanId = regulationPlan.getString("id");
        traceId = traceId + "@" + qiqiaoRegulationPlanId;
        JSONObject variables = regulationPlan.getJSONObject("variables");

        String categoryId = variables.getString("大类");
        String regulationIdentifier = variables.getString("制度唯一标示");
        if ("2".equals(variables.getString("制度建设类型"))) {
            regulationIdentifier = variables.getString("制度唯一标识文本");
        }

        if (StringUtils.isEmpty(regulationIdentifier)) {
            log.warn(traceId + " CANNOT FIND regulationIdentifier!");
            return;
        }
        String curVersion = null;
        String code = null;
        // 3：经营层制度 4：应急预案
        if ("3".equals(categoryId) || "4".equals(categoryId)) {
            curVersion = regulationPlan.getJSONObject("prettyValue").getString("制度版本号");
            code = variables.getString("制度编号");
            if (StringUtils.isEmpty(curVersion)) {
                log.warn(traceId + " CANNOT FIND curVersion!");
                return;
            }
        }

        //获取制度发布日期
        Date publishDate = null;
        try {
            if (StringUtils.isEmpty(yfrq)) {
                log.warn(traceId + " EMPTY yfrq");
                return;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            publishDate = formatter.parse(yfrq);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(publishDate);

            List<ZyRegulationBjmoaHistory> regulationHistoryList = null;
            if ("1".equals(categoryId) || "2".equals(categoryId)) {
                regulationHistoryList = zyRegulationBjmoaHistoryService.queryByIdentifier(regulationIdentifier);
            } else if ("3".equals(categoryId) || "4".equals(categoryId)) {
                regulationHistoryList = zyRegulationBjmoaHistoryService.queryByIdentifierAndVersionAndCode(regulationIdentifier, curVersion, code);
            }
            if (CollectionUtils.isNotEmpty(regulationHistoryList)) {
                log.info(traceId + " FOUND REGULATION HISTORY " + regulationHistoryList.get(0));
                ZyRegulationBjmoaHistory regulationBjmoaHistory = regulationHistoryList.get(0);
                regulationBjmoaHistory.setPublishTime(publishDate);
                zyRegulationBjmoaHistoryService.saveOrUpdate(regulationBjmoaHistory);
            }

            // 将经营层制度旧版的关联记录作废
            if ("3".equals(categoryId)) {
                List<ZyRelatedRegulationBjmoa> zyRelatedRegulationBjmoas = zyRelatedRegulationBjmoaService.queryByRegulationIdentifier(regulationIdentifier);
                if (CollectionUtils.isNotEmpty(zyRelatedRegulationBjmoas)) {
                    for (int i = 0; i < zyRelatedRegulationBjmoas.size(); i++) {
                        ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa = zyRelatedRegulationBjmoas.get(i);
                        if ("3".equals(zyRelatedRegulationBjmoa.getRegulationType())) {
                            String regulationIdentifierB = zyRelatedRegulationBjmoa.getRegulationIdentifierB();

                            ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaService.queryByIdentifier(regulationIdentifierB);
                            zyRegulationBjmoa.setActive(0);
                            zyRegulationBjmoaService.saveOrUpdate(zyRegulationBjmoa);

                            List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryService.queryByIdentifier(regulationIdentifierB);
                            if (CollectionUtils.isNotEmpty(zyRegulationBjmoaHistoryList)) {
                                for (int j = 0; j < zyRegulationBjmoaHistoryList.size(); j++) {
                                    ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(j);
                                    zyRegulationBjmoaHistory.setAbolishTime(publishDate);
                                    zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory);
                                }
                            }
                        }
                    }
                }
            }

            // 保存5级表单发布日期
            if ("3".equals(categoryId)) {
                List<ZyRelatedRegulationBjmoa> zyRelatedRegulationBjmoas = zyRelatedRegulationBjmoaService.queryByRegulationIdentifierAndVersionAndCode(
                        regulationIdentifier, curVersion, code);
                if (CollectionUtils.isNotEmpty(zyRelatedRegulationBjmoas)) {
                    for (int i = 0; i < zyRelatedRegulationBjmoas.size(); i++) {
                        ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa = zyRelatedRegulationBjmoas.get(i);

                        if ("3".equals(zyRelatedRegulationBjmoa.getRegulationType())) {
                            String regulationIdentifierB = zyRelatedRegulationBjmoa.getRegulationIdentifierB();
                            String versionB = zyRelatedRegulationBjmoa.getVersionB();
                            String codeB = zyRelatedRegulationBjmoa.getCodeB();

                            ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaService.queryByIdentifier(regulationIdentifierB);
                            zyRegulationBjmoa.setActive(1);
                            zyRegulationBjmoaService.saveOrUpdate(zyRegulationBjmoa);

                            List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryService.queryByIdentifierAndVersionAndCode(
                                    regulationIdentifierB, versionB, codeB);
                            if (CollectionUtils.isNotEmpty(zyRegulationBjmoaHistoryList)) {
                                ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0);
                                zyRegulationBjmoaHistory.setPublishTime(publishDate);
                                zyRegulationBjmoaHistory.setAbolishTime(null);
                                zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }

        ZyRegulationBjmoa regulationBjmoa = zyRegulationBjmoaService.queryByIdentifier(regulationIdentifier);
        if (regulationBjmoa != null) {
            regulationBjmoa.setActive(1);
            zyRegulationBjmoaService.saveOrUpdate(regulationBjmoa);
        } else {
            log.warn("CANNOT FIND regulationBjmoa!");
        }

        // 董事会和其他三类制度不是一个回调任务
        HashMap<String, String> data = new HashMap<>(2);
        data.put("qiqiaoRegulationId", qiqiaoRegulationPlanId);
        data.put("publishTime", yfrq);
        if ("1".equals(categoryId)) {
            JSONObject jsonObject = qiqiaoCallBackService.callBack(bjmoaRegulationInfoApplicationId, BoardOACallBackTaskId, data);
            log.info("board qiqiaocallback: " + jsonObject);
        } else if ("2".equals(categoryId) || "3".equals(categoryId) || "4".equals(categoryId)) {
            JSONObject jsonObject = qiqiaoCallBackService.callBack(bjmoaRegulationInfoApplicationId, OACallBackTaskId, data);
            log.info("qiqiaocallback: " + jsonObject);
        }

        // 发起制度归档
//        zyRegulationArchiveBjmoaService.filed(regulationIdentifier); // 档案系统正式上线后开启此接口调用
    }

    @Override public void rejectRegulationPublish(final String requestId) {
        if (StringUtils.isEmpty(requestId)) {
            return;
        }

        final RecordVO queryVO = new RecordVO();
        queryVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        queryVO.setFormModelId(bjmoaRegulationInfoFormModelId);

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
        JSONObject variables = regulationPlan.getJSONObject("variables");
        String regulationIdentifier = variables.getString("制度唯一标示");
        if ("2".equals(variables.getString("制度建设类型"))) {
            regulationIdentifier = variables.getString("制度唯一标识文本");
        }
        if (StringUtils.isEmpty(regulationIdentifier)) {
            log.warn("CANNOT FIND regulationIdentifier!");
            return;
        }
        //更新信息单
        final RecordVO updateVO = new RecordVO();
        updateVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        updateVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        updateVO.setId(regulationPlan.getString("id"));
        final Map<String, Object> data = new HashMap<>(1);
        data.put("oa流程状态", BiiOaWorkflowStatus.REJECTED);
        //"9"制度状态 已终止
        data.put("制度状态", "9");
        updateVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateVO);
        log.info("saveOrUpdate: " + jsonObject);
        //获取发布单id
        final RecordVO queryPublishVO = new RecordVO();
        queryPublishVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        queryPublishVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);

        final List<FieldFilter> fieldFilterPublishList = new ArrayList<>(1);
        final FieldFilter fieldPublishFilter = new FieldFilter();
        fieldPublishFilter.setFieldName("制度系统标识别文本");
        fieldPublishFilter.setLogic("eq");
        fieldPublishFilter.setValue(regulationIdentifier);
        fieldFilterPublishList.add(fieldPublishFilter);
        queryPublishVO.setFilter(fieldFilterPublishList);

        final JSONObject regulationRealInfoRecord = qiqiaoFormsService.page(queryPublishVO);
        log.info("regulationRealInfoRecord: " + regulationRealInfoRecord);
        if (regulationRealInfoRecord == null) {
            return;
        }
        final JSONArray regulationRealList = regulationRealInfoRecord.getJSONArray("list");
        if (regulationRealList == null || regulationPlanList.size() != 1) {
            log.warn("WEIRD regulationRealList=" + regulationRealList);
            return;
        }
        final JSONObject regulationRealPlan = regulationRealList.getJSONObject(0);
        String qiqiaoRealRegulationPlanId = regulationRealPlan.getString("id");

        //更新发布单
        final RecordVO updatePublishVO = new RecordVO();
        updatePublishVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        updatePublishVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        updatePublishVO.setId(qiqiaoRealRegulationPlanId);
        final Map<String, Object> dataPublish = new HashMap<>(1);
        //七巧发布单“制度状态”字段“已终止”为9
        dataPublish.put("制度状态", "9");
        updatePublishVO.setData(dataPublish);
        final JSONObject jsonObjectPublish = qiqiaoFormsService.saveOrUpdate(updatePublishVO);
        log.info("saveOrUpdate: " + jsonObjectPublish);
    }

}
