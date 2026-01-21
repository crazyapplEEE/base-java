package org.jeecg.modules.regulation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.constant.OperateConstant;
import org.jeecg.modules.content.constant.WpsOperateType;
import org.jeecg.modules.content.dto.*;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.publicManagement.service.IPublicManagementService;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoCallBackService;
import org.jeecg.modules.qiqiao.service.IQiqiaoDepartmentService;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.regulation.constant.RegulationType;
import org.jeecg.modules.regulation.dto.RegulationQueryDTO;
import org.jeecg.modules.regulation.dto.RegulationTempQueryDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaDept;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaHistory;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulationBjmoa;
import org.jeecg.modules.regulation.mapper.ZyRegulationBjmoaDeptMapper;
import org.jeecg.modules.regulation.mapper.ZyRegulationBjmoaHistoryMapper;
import org.jeecg.modules.regulation.mapper.ZyRegulationBjmoaMapper;
import org.jeecg.modules.regulation.service.*;
import org.jeecg.modules.regulation.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Service @Slf4j public class ZyRegulationBjmoaServiceImpl
    extends ServiceImpl<ZyRegulationBjmoaMapper, ZyRegulationBjmoa> implements IZyRegulationBjmoaService {
    @Autowired @Qualifier("bjmoaContentManagementService") private IContentManagementService contentManagementService;
    @Autowired private ZyRegulationBjmoaMapper zyRegulationBjmoaMapper;
    @Autowired private ZyRegulationBjmoaHistoryMapper zyRegulationBjmoaHistoryMapper;
    @Autowired private ZyRegulationBjmoaDeptMapper zyRegulationBjmoaDeptMapper;
    @Autowired private IZyRegulationBjmoaDeptService zyRegulationBjmoaDeptService;
    @Autowired private IZyRegulationBjmoaHistoryService zyRegulationBjmoaHistoryService;
    @Autowired private IZyRelatedRegulationBjmoaService zyRelatedRegulationBjmoaService;
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;
    @Autowired private RedisUtil redisUtil;
    @Autowired private IPublicManagementService publicManagementService;
    @Autowired private IQiqiaoDepartmentService qiqiaoDepartmentService;
    @Autowired private IQiqiaoCallBackService qiqiaoCallBackService;

    @Value("${content-management.bjmoaAppId}") private String bjmoaAppId;
    @Value("${biisaas.bjmoaRegulationInfo.applicationId}") private String bjmoaRegulationInfoApplicationId;
    @Value("${biisaas.bjmoaRegulationInfo.formModelId}") private String bjmoaRegulationInfoFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.parentRegulationFormModelId}") private String
        bjmoaParentRegulationInfoFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.relatedRegulationFormModelId}") private String
        bjmoaRelatedRegulationInfoFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realFormModelId}") private String bjmoaRealRegulationInfoFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realHistoryFormModelId}") private String bjmoaRealHistoryFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realParentFormModelId}") private String bjmoRealParentFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.realRelatedFormModelId}") private String bjmoaRealRelatedFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.managementToolEntryFormModelId}") private String
        bjmoaManagementToolEntryFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.bjmoaManagementToolEntryRedisKey}") private String
        bjmoaManagementToolEntryRedisKey;
    @Value("${biisaas.bjmoaRegulationInfo.signatureFormModelId}") private String signatureFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.tempTechnicalChangeRegulationModelId}") private String
        tempTechnicalChangeRegulationModelId;
    @Value("${biisaas.bjmoaRegulationInfo.tempRelatedRegulationId}") private String tempRelatedRegulationId;
    @Value("${biisaas.bjmoaRegulationInfo.permissionManagementFormModelId}") private String
        bjmoaPermissionManagementFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.pdfDownloadPermissionDeptFormModelId}") private String
        bjmoaPdfDownloadPermissionDeptFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.pdfRegulationFileFormModelId}") private String pdfRegulationFileFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.boardRegulationFormModelId}") private String boardRegulationFormModelId;

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

    @Override public ZyRegulationBjmoaVO queryById(final Integer id, final String mark) {
        if (id == null) {
            return null;
        }

        final ZyRegulationBjmoa zyRegulationBjmoa = getById(id);
        if (zyRegulationBjmoa == null) {
            log.error("CANNOT FIND REGULATION WITH ID " + id);
            return null;
        }

        final ZyRegulationBjmoaVO result = new ZyRegulationBjmoaVO();
        BeanUtils.copyProperties(zyRegulationBjmoa, result);
        result.setPreviewUrl(contentManagementService.getPreviewUrl(zyRegulationBjmoa.getContentFileId(), mark));

        final String identifier = zyRegulationBjmoa.getIdentifier();
        final String code = zyRegulationBjmoa.getCode();
        final List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList =
            zyRegulationBjmoaHistoryService.lambdaQuery().eq(ZyRegulationBjmoaHistory::getIdentifier, identifier)
                .list();
        zyRegulationBjmoaHistoryList.sort(Comparator.comparing(ZyRegulationBjmoaHistory::getPublishTime,
            Comparator.nullsFirst(Comparator.naturalOrder())));
        final String version = zyRegulationBjmoaHistoryList.get(zyRegulationBjmoaHistoryList.size() - 1).getVersion();

        final List<ZyRegulationBjmoaHistoryVO> historyList = new ArrayList<>(zyRegulationBjmoaHistoryList.size());
        final boolean isActive = Integer.valueOf(1).equals(zyRegulationBjmoa.getActive());
        for (final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory : zyRegulationBjmoaHistoryList) {
            final ZyRegulationBjmoaHistoryVO zyRegulationBjmoaHistoryVO = new ZyRegulationBjmoaHistoryVO();
            BeanUtils.copyProperties(zyRegulationBjmoaHistory, zyRegulationBjmoaHistoryVO);

            final List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList = zyRegulationBjmoaDeptService.lambdaQuery()
                .eq(ZyRegulationBjmoaDept::getCode, zyRegulationBjmoaHistory.getCode()).list();
            zyRegulationBjmoaHistoryVO.setDeptList(zyRegulationBjmoaDeptList);
            historyList.add(zyRegulationBjmoaHistoryVO);

            final String previewUrl =
                contentManagementService.getPreviewUrl(zyRegulationBjmoaHistory.getContentFileId(),
                    isActive ? mark : ("已作废 " + mark));
            zyRegulationBjmoaHistoryVO.setPreviewUrl(previewUrl);

            if (result.getCode().equals(zyRegulationBjmoaHistory.getCode())) {
                result.setVersion(zyRegulationBjmoaHistory.getVersion());
                result.setPublishTime(zyRegulationBjmoaHistory.getPublishTime());
                result.setAbolishTime(zyRegulationBjmoaHistory.getAbolishTime());
                result.setRequestId(zyRegulationBjmoaHistory.getRequestId());
                result.setDeptList(zyRegulationBjmoaDeptList.stream().map(ZyRegulationBjmoaDept::getQiqiaoDeptName)
                    .collect(Collectors.toList()));
            }

        }
        result.setHistoryList(historyList);
        final String versionA = result.getVersion();
        // 查询关联制度
        final List<ZyRelatedRegulationBjmoa> zyRelatedRegulationBjmoas =
            zyRelatedRegulationBjmoaService.queryByRegulationIdentifierAndVersion(identifier, versionA);
        if (CollectionUtils.isNotEmpty(zyRelatedRegulationBjmoas)) {
            final List<ZyRelatedRegulationVO> relatedRegulationVOS = new ArrayList<>(zyRelatedRegulationBjmoas.size());
            for (final ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa : zyRelatedRegulationBjmoas) {
                final String regulationIdentifierA = zyRelatedRegulationBjmoa.getRegulationIdentifierA();
                final String regulationIdentifierB = zyRelatedRegulationBjmoa.getRegulationIdentifierB();
                final String regulationType = zyRelatedRegulationBjmoa.getRegulationType();
                final String regulationName = zyRelatedRegulationBjmoa.getRegulationName();
                if (identifier.equals(regulationIdentifierA)) {
                    if (!code.equals(zyRelatedRegulationBjmoa.getCodeA()) || !version.equals(
                        zyRelatedRegulationBjmoa.getVersionA())) {
                        continue;
                    }

                    final ZyRelatedRegulationVO relatedRegulationVO =
                        generateRelatedRegulationVO(regulationType, regulationIdentifierB, regulationName);
                    if (relatedRegulationVO != null) {
                        relatedRegulationVOS.add(relatedRegulationVO);
                    }
                } else if (RegulationType.RELATED.equals(regulationType)) {
                    if (!code.equals(zyRelatedRegulationBjmoa.getCodeB()) || !version.equals(
                        zyRelatedRegulationBjmoa.getVersionB())) {
                        continue;
                    }

                    final ZyRelatedRegulationVO relatedRegulationVO =
                        generateRelatedRegulationVO(regulationType, regulationIdentifierA, regulationName);
                    if (relatedRegulationVO != null) {
                        relatedRegulationVOS.add(relatedRegulationVO);
                    }
                }
                result.setRelatedRegulationList(relatedRegulationVOS);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override public void createOrEdit(final String qiqiaoRegulationId, final String publishStatus) {
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            return;
        }
        final String timeStamp = DateUtils.getDate("yyyyMMddHHmmss");
        final String timePublishStatus = timeStamp + publishStatus;
        final String traceId = "bjmoa_create_edit_regulation" + "@" + qiqiaoRegulationId + "@" + timePublishStatus;
        // 根据七巧id 获取七巧表单
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        final JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        JSONObject variables = record.getJSONObject("variables");
        final JSONObject prettyValue = record.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(traceId + " CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }
        String regulationIdentifier = variables.getString("制度唯一标示");
        if ("2".equals(variables.getString("制度建设类型"))) {
            regulationIdentifier = variables.getString("制度唯一标识文本");
        }
        if (StringUtils.isEmpty(regulationIdentifier)) {
            log.warn(traceId + " CANNOT FIND regulationIdentifier!");
            return;
        }
        // 1 未发出 2 已发出未套打 3 已套打未转换 4 已转换未加水印 5 已加水印未oa发文
        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(regulationIdentifier);
        log.info(traceId + "zyRegularionBjmoa: " + zyRegulationBjmoa);

        if ("1".equals(publishStatus)) {
            log.info(traceId + " START FROM CREATE_OR_EDIT");
            createOrEditFirst(qiqiaoRegulationId, traceId);
        } else if ("2".equals(publishStatus)) {
            // doc套打
            // 注意：制度编号里面不能有下划线
            log.info(traceId + " START FROM WRAP HEADER");
            if (zyRegulationBjmoa == null) {
                log.warn(traceId + " zyRegulationBjmoa is NULL");
                return;
            }
            final String docId = zyRegulationBjmoa.getContentDocId();
            final String fileId = zyRegulationBjmoa.getContentFileId();
            final String taskId =
                bjmoaAppId + "@" + WpsOperateType.OFFICE_WRAP_HEADER + "@" + docId + "@" + fileId + "@"
                    + qiqiaoRegulationId + "@" + timePublishStatus;

            // 查询是否套打过
            final JSONObject jsonObject = contentManagementService.queryTask(taskId);
            final String downloadId = jsonObject.getString("download_id");
            //是否已经套打
            if (StringUtils.isEmpty(downloadId)) {
                final WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
                wpsFormatDTO.setTask_id(taskId);
                wpsFormatDTO.setScene_id(bjmoaAppId);
                // 注意：这里我们获取的文件最新版本的下载链接
                final String templateUrl = contentManagementService.getDownloadNewestUrl(docId);
                wpsFormatDTO.setTemplate_filename(variables.getString("文件名称").trim());
                wpsFormatDTO.setTemplate_url(templateUrl);

                // 获取套打内容
                final List<Sample> sampleList = getSampleList(variables);
                if (sampleList == null) {
                    log.warn(traceId + " STH MUST BE WRONG WITH SAMPLE LIST");
                    return;
                }
                wpsFormatDTO.setSample_list(sampleList);

                if (!contentManagementService.officeWrapheader(wpsFormatDTO)) {
                    log.warn(traceId + " FAILED WRAP HEADER CONVERT " + wpsFormatDTO);
                }
            } else {
                // 如果之前已经套打过了
                saveHeaderWrappedDocRegulation(qiqiaoRegulationId, downloadId, zyRegulationBjmoa, variables, traceId);
            }
        } else if ("3".equals(publishStatus)) {
            log.info(traceId + " START FROM PDF CONVERT");
            if (zyRegulationBjmoa == null) {
                log.warn(traceId + " zyRegulationBjmoa is NULL");
                return;
            }
            // 转换PDF文件
            final String docId = zyRegulationBjmoa.getContentDocId();
            final String fileId = zyRegulationBjmoa.getContentFileId();
            final String pdfConversionTaskId =
                bjmoaAppId + "@" + WpsOperateType.OFFICE_CONVERT + "@" + docId + "@" + fileId + "@" + qiqiaoRegulationId
                    + "@" + timePublishStatus;
            final JSONObject pdfConversionTask = contentManagementService.queryTask(pdfConversionTaskId);
            final String pdfConversionDownloadId = pdfConversionTask.getString("download_id");
            if (StringUtils.isEmpty(pdfConversionDownloadId)) {
                // 回调接口中实现，存一条制度历史版本的记录（PDF文件）
                final WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
                wpsFormatDTO.setTask_id(pdfConversionTaskId);
                wpsFormatDTO.setScene_id(bjmoaAppId);
                // 注意：这里我们获取的文件最新版本的下载链接
                final String docUrl = contentManagementService.getDownloadNewestUrl(docId);
                wpsFormatDTO.setDoc_url(docUrl);
                wpsFormatDTO.setDoc_filename(variables.getString("文件名称").trim());
                wpsFormatDTO.setTarget_file_format("pdf");
                if (!contentManagementService.officeConvert(wpsFormatDTO)) {
                    log.warn(traceId + " FAILED PDF CONVERT " + wpsFormatDTO);
                }
            } else {
                // 如果之前已经转换过了
                savePdfRegulation(qiqiaoRegulationId, pdfConversionDownloadId, zyRegulationBjmoa, variables, traceId);
            }
        } else if ("4".equals(publishStatus)) {
            log.info(traceId + " START FROM WATERMARK");
            if (zyRegulationBjmoa == null) {
                log.warn(traceId + " zyRegulationBjmoa is NULL");
                return;
            }
            String identifier = zyRegulationBjmoa.getIdentifier();
            List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList =
                zyRegulationBjmoaHistoryMapper.queryNewestRegulationByIdentifier(identifier);
            ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
            if (CollectionUtils.isNotEmpty(zyRegulationBjmoaHistoryList)) {
                zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0); //获取最新历史版本
            } else {
                log.warn(traceId + " zyRegulationBjmoaHistoryList FOR IDENTIFIER :" + identifier + "is null");
                return;
            }
            final String docId = zyRegulationBjmoaHistory.getContentDocId();
            final String fileId = zyRegulationBjmoaHistory.getContentFileId();
            addWatermark(qiqiaoRegulationId, fileId, docId, zyRegulationBjmoa, traceId);
        } else if ("5".equals(publishStatus)) {
            log.info(traceId + " START FROM OA PROCESS");
            initiateOAProcess(qiqiaoRegulationId);
        }
    }

    public void createOrEditFirst(final String qiqiaoRegulationId, final String traceId) {
        log.info(traceId + " createOrEditFirst qiqiaoRegulationId: {}", qiqiaoRegulationId);
        if (StringUtils.isEmpty(qiqiaoRegulationId) || StringUtils.isEmpty(traceId)) {
            log.warn("QIQIAOREGULATIONID or TRACEID IS NULL: qiqiaoregulationid " + qiqiaoRegulationId + " traceId "
                + traceId);
            return;
        }
        final String[] traceIdList = traceId.split("@");
        final String timePublishStatus = traceIdList[2];
        // 1. 查询七巧制度基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        final JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        JSONObject variables = record.getJSONObject("variables");
        final JSONObject prettyValue = record.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(traceId + " CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }

        // 2. 存一条制度记录
        String regulationIdentifier = variables.getString("制度唯一标示");
        if ("2".equals(variables.getString("制度建设类型"))) {
            regulationIdentifier = variables.getString("制度唯一标识文本");
        }
        if (StringUtils.isEmpty(regulationIdentifier)) {
            log.warn(traceId + " CANNOT FIND regulationIdentifier!");
            return;
        }

        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(regulationIdentifier);
        if (zyRegulationBjmoa == null) {
            log.info(traceId + " creating a new regulation");
            zyRegulationBjmoa = new ZyRegulationBjmoa();
        } else {
            log.info(traceId + " editing a regulation");
        }

        // 关联/上级制度
        // 删除之前的关联
        // 保存关联制度
         if (Objects.equals(qiqiaoRegulationId, zyRegulationBjmoa.getQiqiaoRegulationId())) {
             log.info(traceId + " Not first execute createOrEditFirst() Method");
         } else {
            log.info(traceId + " First execute createOrEditFirst() Method");
            RecordVO relatedRecordVO = new RecordVO();
            relatedRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
            relatedRecordVO.setFormModelId(bjmoaRelatedRegulationInfoFormModelId);
            relatedRecordVO.setPageSize(50);
            List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            FieldFilter fieldFilter = new FieldFilter();
            fieldFilter.setFieldName("外键");
            fieldFilter.setLogic("eq");
            fieldFilter.setValue(qiqiaoRegulationId);
            fieldFilterList.add(fieldFilter);
            relatedRecordVO.setFilter(fieldFilterList);
            final JSONObject page = qiqiaoFormsService.page(relatedRecordVO);
            final JSONArray relatedRegulationBjmoaList = page.getJSONArray("list");
            log.info(traceId + " relatedRegulationBjmoaList: " + relatedRegulationBjmoaList);
            for (int i = 0; i < relatedRegulationBjmoaList.size(); ++i) {
                final JSONObject realRegulationJson = relatedRegulationBjmoaList.getJSONObject(i);
                final JSONObject realVariables = realRegulationJson.getJSONObject("variables");
                if (realVariables == null) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
                String relatedRegulationIdentifier5 = realVariables.getString("关联记录唯一标识");
                if (StringUtils.isEmpty(relatedRegulationIdentifier5)) {
                    log.warn(traceId + " CANNOT FIND relatedRegulationIdentifier5!");
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
                ZyRegulationBjmoa zyRegulationBjmoaRelated = queryByIdentifier(relatedRegulationIdentifier5);
                if (zyRegulationBjmoaRelated == null) {
                    log.info(traceId + " creating a new 5-level regulation");
                    zyRegulationBjmoaRelated = new ZyRegulationBjmoa();
                } else {
                    log.info(traceId + " editing a 5-level regulation");
                }
                final JSONArray relatedFileList = realVariables.getJSONArray("关联记录上传");
                for (int j = 0; j < relatedFileList.size(); ++j) {
                    final JSONObject relatedFile = relatedFileList.getJSONObject(j);

                    final String name = relatedFile.getString("name");
                    final String fileId = relatedFile.getString("fileId");

                    RecordVO downloadRecordVO = new RecordVO();
                    downloadRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId); // 制度系统【轨道运营】
                    downloadRecordVO.setFileId(fileId);
                    qiqiaoFormsService.download(downloadRecordVO, name);
                    File relatedRegulationFile = new File(name);
                    List<File> relatedRegulationFileList = new ArrayList<>();
                    relatedRegulationFileList.add(relatedRegulationFile);
                    final List<EcmFileDTO> relatedRegulationFileDTOList =
                        contentManagementService.uploadFiles(relatedRegulationFileList);
                    if (relatedRegulationFile.exists()) {
                        relatedRegulationFile.delete();
                    }
                    if (relatedRegulationFileDTOList == null) {
                        log.warn(traceId + " 5-LEVEL REGULATION FAILED TO UPLOAD CONTENT MANAGEMENT");
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return;
                    }
                    zyRegulationBjmoaRelated.setContentFileId(relatedRegulationFileDTOList.get(0).getFileId());
                    zyRegulationBjmoaRelated.setContentDocId(relatedRegulationFileDTOList.get(0).getFileId());
                }
                final String regulationName = realVariables.getString("关联记录文件名称").trim();
                final String code5 = realVariables.getString("关联记录编号");
                final String code = variables.getString("制度编号");
                zyRegulationBjmoaRelated.setName(regulationName);
                zyRegulationBjmoaRelated.setCode(code5);
                zyRegulationBjmoaRelated.setIdentifier(relatedRegulationIdentifier5);
                zyRegulationBjmoaRelated.setLevelId(realVariables.getString("制度级别"));
                zyRegulationBjmoaRelated
                    .setLevelName(realRegulationJson.getJSONObject("prettyValue").getString("制度级别"));
                zyRegulationBjmoaRelated.setCategoryId(variables.getString("大类"));
                zyRegulationBjmoaRelated.setCategoryName(prettyValue.getString("大类"));
                zyRegulationBjmoaRelated.setManagementCategoryId(variables.getString("管理类别"));
                zyRegulationBjmoaRelated.setManagementCategoryName(prettyValue.getString("管理类别"));
                zyRegulationBjmoaRelated.setSubCategoryId(variables.getString("业务子类"));
                zyRegulationBjmoaRelated.setSubCategoryName(prettyValue.getString("业务子类"));
                zyRegulationBjmoaRelated.setContingencyPlanCategoryId(variables.getString("预案分类"));
                zyRegulationBjmoaRelated.setContingencyPlanCategoryName(prettyValue.getString("预案分类"));
                zyRegulationBjmoaRelated.setLineId(variables.getString("线路"));
                zyRegulationBjmoaRelated.setLineName(prettyValue.getString("线路"));
                zyRegulationBjmoaRelated.setQiqiaoCreatorId(variables.getString("制度跟进人"));
                zyRegulationBjmoaRelated.setQiqiaoCreatorName(variables.getString("制度跟进人_pretty_value"));

                if (!saveOrUpdate(zyRegulationBjmoaRelated)) {
                    log.warn(traceId + " FAILED TO SAVE 5-level zyRegulationBjmoa=" + zyRegulationBjmoaRelated);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
                log.info(traceId + " SAVE OR UPDATE 5-level zyRegulationBjmoa=" + zyRegulationBjmoaRelated);
                final String version = "A/" + (variables.getInteger("制度版本号") - 1);
                final String version5 = "A/" + (realVariables.getInteger("版本号") - 1);
                if (!zyRelatedRegulationBjmoaService
                    .saveRelation(regulationIdentifier, version, code, relatedRegulationIdentifier5, version5, code5,
                        RegulationType.RELATED, regulationName, traceId)) {
                    log.warn(traceId + " FAILED TO SAVE RELATION 5-LEVEL IDENTIFIER: " + relatedRegulationIdentifier5);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }

                // 存5级制度历史版本的记录
                ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
                BeanUtils.copyProperties(zyRegulationBjmoaRelated, zyRegulationBjmoaHistory);
                zyRegulationBjmoaHistory.setId(null);
                zyRegulationBjmoaHistory.setFileName(relatedFileList.getJSONObject(0).getString("name"));

                zyRegulationBjmoaHistory.setVersion(version5);
//                zyRegulationBjmoaHistory.setPublishTime(variables.getDate("制度发布日期"));
                if (!zyRegulationBjmoaHistoryService.save(zyRegulationBjmoaHistory)) {
                    log.warn(traceId + " FAILED TO SAVE 5-level REGULATION HISTORY: " + zyRegulationBjmoaHistory);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
            }
        }

        // 保存上级制度
        if (!Objects.equals(qiqiaoRegulationId, zyRegulationBjmoa.getQiqiaoRegulationId())) {
            RecordVO relatedRecordVO = new RecordVO();
            relatedRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
            relatedRecordVO.setFormModelId(bjmoaParentRegulationInfoFormModelId);
            List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            FieldFilter fieldFilter = new FieldFilter();
            fieldFilter.setFieldName("制度名称");
            fieldFilter.setLogic("eq");
            fieldFilter.setValue(qiqiaoRegulationId);
            fieldFilterList.add(fieldFilter);
            relatedRecordVO.setFilter(fieldFilterList);
            final JSONObject page = qiqiaoFormsService.page(relatedRecordVO);
            final JSONArray relatedRegulationBjmoaList = page.getJSONArray("list");
            log.info(traceId + " relatedRegulationBjmoaList: " + relatedRegulationBjmoaList);
            final String code = variables.getString("制度编号");
            Integer variablesInteger = variables.getInteger("制度版本号");
            String version = "";
            if (null != variablesInteger){
                version = "A/" + (variablesInteger - 1);
            }

            for (int i = 0; i < relatedRegulationBjmoaList.size(); ++i) {
                final JSONObject realRegulationJson = relatedRegulationBjmoaList.getJSONObject(i);
                RecordVO tmpRecordVO = new RecordVO();
                tmpRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
                tmpRecordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
                final JSONObject realVariables = realRegulationJson.getJSONObject("variables");
                if (realVariables != null) {
                    String regulationType = realVariables.getString("类型");
                    if (RegulationType.INTERNAL.equals(regulationType)) {
                        String category = realVariables.getString("大类");
                        String realRegulationId = null;
                        if (category != null && "4".equals(category)) {
                            String hasParent = realVariables.getString("是否有上级制度");
                            if (hasParent != null && "2".equals(hasParent)) {
                                continue;
                            } else {
                                realRegulationId = realVariables.getString("预案上级关联制度");
                            }
                        } else {
                            realRegulationId = realVariables.getString("上级关联制度");
                        }
                        tmpRecordVO.setId(realRegulationId);
                        final JSONObject realRegulation = qiqiaoFormsService.queryById(tmpRecordVO);
                        final String relatedRegulationIdentifier =
                            realRegulation.getJSONObject("variables").getString("制度系统标识别文本");
                        final String regulationName =
                            realRegulation.getJSONObject("variables").getString("制度名称").trim();
                        final String relatedRegulationCode =
                            realRegulation.getJSONObject("variables").getString("制度编号").trim();
                        // 党群及廉政管理类制度没有版本号
                        String relatedRegulaitonVersion = null;
                        final String categoryId = realRegulation.getJSONObject("variables").getString("大类");
                        if (!"2".equals(categoryId)) {
                            relatedRegulaitonVersion = realRegulation.getJSONObject("prettyValue").getString("制度版本").trim();
                        }
                        zyRelatedRegulationBjmoaService
                            .saveRelation(regulationIdentifier, version, code, relatedRegulationIdentifier,
                                relatedRegulaitonVersion, relatedRegulationCode, regulationType, regulationName,
                                traceId);
                    } else if (RegulationType.EXTERNAL.equals(regulationType)) {
                        String category = realVariables.getString("大类");
                        if (category != null && "4".equals(category)) {
                            String hasParent = realVariables.getString("是否有上级制度");
                            if ( hasParent != null && "2".equals(hasParent)) {
                                continue;
                            }
                        }
                        final JSONArray externalFileList = realVariables.getJSONArray("外部文件上传");
                        final String externalFileName = realVariables.getString("外部文件名称").trim();
                        for (int j = 0; j < externalFileList.size(); ++j) {
                            final JSONObject externalFile = externalFileList.getJSONObject(j);
                            final String fileId = externalFile.getString("fileId");
                            zyRelatedRegulationBjmoaService
                                .saveRelation(regulationIdentifier, version, code, fileId, null, null, regulationType,
                                    externalFileName, traceId);
                        }
                    }
                }
            }
        }

        zyRegulationBjmoa.setQiqiaoRegulationId(qiqiaoRegulationId);
        final String regulationName = variables.getString("最终制度名称").trim();
        if (StringUtils.isEmpty(regulationName)) {
            log.warn(traceId + " CANNOT FIND REGULATION NAME");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }
        zyRegulationBjmoa.setName(regulationName);

        final String docId = variables.getString("内管文档编号");
        String fileName = variables.getString("文件名称");
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            log.warn(traceId + " INCORRECT REGULATION FILE NAME FORMAT");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }
        String filePrefix = fileName.substring(0, index);
        String fileSuffix = fileName.substring(index);
        // 如果最终制度名称和制度文件名称不一致，修改七巧制度文件名称为最终制度名称
        if (!filePrefix.equals(regulationName)) {
            String newFileName = regulationName + fileSuffix;
            JSONObject jsonObject = contentManagementService.renameFile(docId, newFileName);
            if (jsonObject == null) {
                log.warn(traceId + " FAILED TO RENAME REGUALTION FILE");
                return;
            }
            int code = jsonObject.getInteger("code");
            if (code != 200) {
                log.warn(traceId + " FAILED TO RENAME REGUALTION FILE");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }

            // 更新七巧端制度文件信息
            JSONObject data = jsonObject.getJSONObject("data");
            String newFileId = data.getString("fileId");
            Map<String, Object> regulationData = new HashMap<>();
            regulationData.put("文件名称", newFileName);
            regulationData.put("内管文件编号", newFileId);
            recordVO.setData(regulationData);
            qiqiaoFormsService.saveOrUpdate(recordVO);

            // 更新variables信息
            variables.put("文件名称", newFileName);
            variables.put("内管文件编号", newFileId);
        }
        final String fileId = variables.getString("内管文件编号");
        zyRegulationBjmoa.setContentFileId(fileId);
        zyRegulationBjmoa.setContentDocId(docId);
        zyRegulationBjmoa.setCode(variables.getString("制度编号"));
        zyRegulationBjmoa.setIdentifier(regulationIdentifier);
        zyRegulationBjmoa.setQiqiaoCreatorId(variables.getString("制度跟进人"));
        zyRegulationBjmoa.setQiqiaoCreatorName(variables.getString("制度跟进人_pretty_value"));
        zyRegulationBjmoa.setLevelId(variables.getString("制度级别"));
        zyRegulationBjmoa.setLevelName(prettyValue.getString("制度级别"));
        zyRegulationBjmoa.setCategoryId(variables.getString("大类"));
        zyRegulationBjmoa.setCategoryName(prettyValue.getString("大类"));
        //add
        zyRegulationBjmoa.setManagementCategoryId(variables.getString("管理类别"));
        zyRegulationBjmoa.setManagementCategoryName(prettyValue.getString("管理类别"));
        zyRegulationBjmoa.setSubCategoryId(variables.getString("业务子类"));
        zyRegulationBjmoa.setSubCategoryName(prettyValue.getString("业务子类"));
        zyRegulationBjmoa.setContingencyPlanCategoryId(variables.getString("预案分类"));
        zyRegulationBjmoa.setContingencyPlanCategoryName(prettyValue.getString("预案分类"));
        zyRegulationBjmoa.setLineId(variables.getString("线路"));
        zyRegulationBjmoa.setLineName(prettyValue.getString("线路"));
        String managementCategory = variables.getString("管理类别");
        zyRegulationBjmoa.setActive(0);
        if (!saveOrUpdate(zyRegulationBjmoa)) {
            log.warn(traceId + " FAILED TO SAVE zyRegulationBjmoa=" + zyRegulationBjmoa);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }

        // 3. 将doc套打
        // 注意：制度编号里面不能有下划线
        log.info(traceId + " START WARP HEADER");

        final String taskId = bjmoaAppId + "@" + WpsOperateType.OFFICE_WRAP_HEADER + "@" + docId + "@" + fileId + "@"
            + qiqiaoRegulationId + "@" + timePublishStatus;
        log.info(traceId + " WARP HEADER TASK ID: " + taskId);

        // 查询是否套打
        final JSONObject jsonObject = contentManagementService.queryTask(taskId);
        final String downloadId = jsonObject.getString("download_id");
        //是否已经套打
        if (StringUtils.isEmpty(downloadId)) {
            final WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
            wpsFormatDTO.setTask_id(taskId);
            wpsFormatDTO.setScene_id(bjmoaAppId);
            // 注意：这里我们获取的文件最新版本的下载链接
            final String templateUrl = contentManagementService.getDownloadNewestUrl(docId);
            wpsFormatDTO.setTemplate_filename(variables.getString("文件名称").trim());
            wpsFormatDTO.setExpand_bookmark(true);
            wpsFormatDTO.setTemplate_url(templateUrl);


            // 获取套打内容
            final List<Sample> sampleList = getSampleList(variables);
            if (sampleList == null) {
                log.warn(traceId + " STH MUST BE WRONG WITH SAMPLE LIST");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }
            wpsFormatDTO.setSample_list(sampleList);
            if (!contentManagementService.officeWrapheader(wpsFormatDTO)) {
                log.warn(traceId + " taskId " + taskId + " FAILED WRAP HEADER " + wpsFormatDTO);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        } else {
            // 如果之前已经套打过了
            saveHeaderWrappedDocRegulation(qiqiaoRegulationId, downloadId, zyRegulationBjmoa, variables, traceId);
        }
    }

    // 党群及廉政建设管理类制度批准发布接口
    @Override
    public void create(String qiqiaoRegulationId) {
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            log.warn("QIQIAOREGULATIONID is NULL: " + qiqiaoRegulationId);
            return;
        }
        String timeStamp = DateUtils.getDate("yyyyMMddHHmmss");
        String traceId = "bjmoa_create_dangqun_regulation" + "@" + qiqiaoRegulationId + "@" + timeStamp;

        // 1. 查询七巧制度基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }

        JSONObject variables = record.getJSONObject("variables");
        JSONObject prettyValue = record.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(traceId + " CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }

        // 2. 存一条制度记录
        String regulationIdentifier = variables.getString("制度唯一标示");
        if (StringUtils.isEmpty(regulationIdentifier)) {
            log.warn(traceId + " CANNOT FIND regulationIdentifier!");
            return;
        }

        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(regulationIdentifier);
        if (zyRegulationBjmoa == null) {
            log.info(traceId + " creating a new regulation");
            zyRegulationBjmoa = new ZyRegulationBjmoa();
        } else {
            log.info(traceId + " editing a regulation");
        }

        // 保存上级制度
        List<ZyRelatedRegulationBjmoa> zyRelatedRegulationBjmoas = zyRelatedRegulationBjmoaService.queryByRegulationIdentifier(regulationIdentifier);
        if (zyRelatedRegulationBjmoas == null || CollectionUtils.isEmpty(zyRelatedRegulationBjmoas)) {
            RecordVO relatedRecordVO = new RecordVO();
            relatedRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
            relatedRecordVO.setFormModelId(bjmoaParentRegulationInfoFormModelId);
            List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            FieldFilter fieldFilter = new FieldFilter();
            fieldFilter.setFieldName("制度名称");
            fieldFilter.setLogic("eq");
            fieldFilter.setValue(qiqiaoRegulationId);
            fieldFilterList.add(fieldFilter);
            relatedRecordVO.setFilter(fieldFilterList);
            JSONObject page = qiqiaoFormsService.page(relatedRecordVO);
            JSONArray parentRegulationBjmoaList = page.getJSONArray("list");
            log.info(traceId + " parentRegulationBjmoaList: " + parentRegulationBjmoaList);
            String code = variables.getString("制度编号");

            for (int i = 0; i < parentRegulationBjmoaList.size(); ++i) {
                JSONObject parentRegulationJson = parentRegulationBjmoaList.getJSONObject(i);
                RecordVO tmpRecordVO = new RecordVO();
                tmpRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
                tmpRecordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
                JSONObject parentVariables = parentRegulationJson.getJSONObject("variables");
                if (parentVariables != null) {
                    String regulationType = parentVariables.getString("类型");
                    if (RegulationType.INTERNAL.equals(regulationType)) {
                        String realRegulationId = parentVariables.getString("上级关联制度");
                        tmpRecordVO.setId(realRegulationId);
                        JSONObject realRegulation = qiqiaoFormsService.queryById(tmpRecordVO);
                        JSONObject realVariables = realRegulation.getJSONObject("variables");
                        String relatedRegulationIdentifier = realVariables.getString("制度系统标识别文本");
                        String regulationName = realVariables.getString("制度名称").trim();
                        String relatedRegulationCode = realVariables.getString("制度编号").trim();
                        // 党群类制度没有版本号
                        String relatedRegulaitonVersion = null;
                        String categoryId = realVariables.getString("大类");
                        if (!"2".equals(categoryId)) {
                            relatedRegulaitonVersion = "A/" + (realVariables.getInteger("制度版本") - 1);
                        }
                        zyRelatedRegulationBjmoaService
                                .saveRelation(regulationIdentifier, null, code, relatedRegulationIdentifier,
                                        relatedRegulaitonVersion, relatedRegulationCode, regulationType, regulationName,
                                        traceId);
                    } else if (RegulationType.EXTERNAL.equals(regulationType)) {
                        final JSONArray externalFileList = parentVariables.getJSONArray("外部文件上传");
                        final String externalFileName = parentVariables.getString("外部文件名称").trim();
                        for (int j = 0; j < externalFileList.size(); ++j) {
                            final JSONObject externalFile = externalFileList.getJSONObject(j);
                            final String fileId = externalFile.getString("fileId");
                            zyRelatedRegulationBjmoaService
                                    .saveRelation(regulationIdentifier, null, code, fileId, null, null, regulationType,
                                            externalFileName, traceId);
                        }
                    }
                }
            }
        }

        String regulationName = variables.getString("最终制度名称").trim();
        String docId = variables.getString("内管文档编号");
        String fileName = variables.getString("文件名称");
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            log.warn(traceId + " INCORRECT REGULATION FILE NAME FORMAT");
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }
//        String filePrefix = fileName.substring(0, index);
//        String fileSuffix = fileName.substring(index);
//        // 如果最终制度名称和制度文件名称不一致，修改七巧制度文件名称为最终制度名称
//        if (!filePrefix.equals(regulationName)) {
//            String newFileName = regulationName + fileSuffix;
//            JSONObject jsonObject = contentManagementService.renameFile(docId, newFileName);
//            if (jsonObject == null) {
//                log.warn(traceId + " FAILED TO RENAME REGUALTION FILE");
//                return;
//            }
//            int code = jsonObject.getInteger("code");
//            if (code != 200) {
//                log.warn(traceId + " FAILED TO RENAME REGUALTION FILE");
//                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                return;
//            }
//
//            // 更新variables信息
//            JSONObject data = jsonObject.getJSONObject("data");
//            String newFileId = data.getString("fileId");
//            variables.put("文件名称", newFileName);
//            variables.put("内管文件编号", newFileId);
//        }

//        if (StringUtils.isEmpty(regulationName)) {
//            log.warn(traceId + " CANNOT FIND REGULATION NAME");
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            return;
//        }

        zyRegulationBjmoa.setQiqiaoRegulationId(qiqiaoRegulationId);
        zyRegulationBjmoa.setName(regulationName);
        final String fileId = variables.getString("内管文件编号");
        zyRegulationBjmoa.setContentFileId(fileId);
        zyRegulationBjmoa.setContentDocId(docId);
        zyRegulationBjmoa.setCode(variables.getString("制度编号"));
        zyRegulationBjmoa.setIdentifier(regulationIdentifier);
        zyRegulationBjmoa.setQiqiaoCreatorId(variables.getString("制度跟进人"));
        zyRegulationBjmoa.setQiqiaoCreatorName(variables.getString("制度跟进人_pretty_value"));
        zyRegulationBjmoa.setCategoryId(variables.getString("大类"));
        zyRegulationBjmoa.setCategoryName(prettyValue.getString("大类"));
        zyRegulationBjmoa.setManagementCategoryId(variables.getString("管理类别"));
        zyRegulationBjmoa.setManagementCategoryName(prettyValue.getString("管理类别"));
        zyRegulationBjmoa.setActive(0);
        if (!saveOrUpdate(zyRegulationBjmoa)) {
            log.warn(traceId + " FAILED TO SAVE zyRegulationBjmoa=" + zyRegulationBjmoa);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }

        // 保存一条制度历史版本的记录
        ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
        BeanUtils.copyProperties(zyRegulationBjmoa, zyRegulationBjmoaHistory);
        zyRegulationBjmoaHistory.setId(null);
        zyRegulationBjmoaHistory.setPublishTime(variables.getDate("制度发布日期"));

        List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryMapper.queryByIdentifier(regulationIdentifier);
        if (zyRegulationBjmoaHistoryList == null || zyRegulationBjmoaHistoryList.size() == 0) {
            if (!zyRegulationBjmoaHistoryService.save(zyRegulationBjmoaHistory)) {
                log.warn(traceId + " FAILED TO SAVE HISTORY: " + zyRegulationBjmoaHistory);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }
        }

        // 保存一条主责部门记录
        String regulationDeptId = variables.getString("制度主责部门");
        String regulationDeptName = variables.getString("制度主责部门_pretty_value");
        if (regulationDeptId == null || regulationDeptName == null || regulationDeptId.equals(regulationDeptName)) {
            log.warn(traceId + " CANNOT FIND REGULATION DEPT FOR REGULATION " + qiqiaoRegulationId
                    + ", regulationDeptId=" + regulationDeptId);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }

        ZyRegulationBjmoaDept zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
        BeanUtils.copyProperties(zyRegulationBjmoa, zyRegulationBjmoaDept);
        zyRegulationBjmoaDept.setQiqiaoDeptId(regulationDeptId);
        zyRegulationBjmoaDept.setQiqiaoDeptName(regulationDeptName);
        zyRegulationBjmoaDept.setId(null);

        List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList = zyRegulationBjmoaDeptMapper.getByIdentifier(regulationIdentifier);
        if (zyRegulationBjmoaDeptList == null || zyRegulationBjmoaDeptList.size() == 0) {
            if (!zyRegulationBjmoaDeptService.save(zyRegulationBjmoaDept)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }
        }

//        // 3. 添加PDF水印
//        final String addWatermarkTaskId =
//                bjmoaAppId + "@" + WpsOperateType.OFFICE_OPERATE + "@" + docId + "@" + fileId + "@" + qiqiaoRegulationId + "@" + timeStamp;
//        log.info(traceId + " ADD WATER MARK TASK ID: " + addWatermarkTaskId);
//        final String pdfFileName = zyRegulationBjmoa.getName() + ".pdf";
//        final WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
//        wpsFormatDTO.setTask_id(addWatermarkTaskId);
//        wpsFormatDTO.setScene_id(bjmoaAppId);
//
//        wpsFormatDTO.setDoc_url(contentManagementService.getDownloadNewestUrl(docId));
//        wpsFormatDTO.setDoc_filename(pdfFileName);
//
//        final List<Step> steps = new ArrayList<>();
//        final Step step = new Step();
//        step.setOperate(OperateConstant.OFFICE_WATERMARK);
//        final TextWatermark textWatermark = new TextWatermark();
//        textWatermark.setContent("北京市轨道交通运营管理有限公司");
//        textWatermark.setTilt(true);
//        textWatermark.setTransparent(0.45);
//
//        final Arg args = new Arg();
//        args.setText_watermark(textWatermark);
//        step.setArgs(args);
//        steps.add(step);
//
//        wpsFormatDTO.setSteps(steps);
//
//        if (!contentManagementService.officeOperate(wpsFormatDTO)) {
//            log.warn(traceId + " FAILED ADD WATERMARK " + wpsFormatDTO);
//        }

    }

    // 董事会制度发布接口
    @Override
    public void createOrEditBoardRegulation(String qiqiaoRegulationId) {
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            log.warn("QIQIAOREGULATIONID is NULL: " + qiqiaoRegulationId);
            return;
        }
        String timeStamp = DateUtils.getDate("yyyyMMddHHmmss");
        String traceId = "bjmoa_create_edit_dongshihui_regulation" + "@" + qiqiaoRegulationId + "@" + timeStamp;

        // 1. 查询七巧制度基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(boardRegulationFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }

        JSONObject variables = record.getJSONObject("variables");
        JSONObject prettyValue = record.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(traceId + " CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }

        // 2. 存一条制度记录
        String regulationIdentifier = variables.getString("制度唯一标示");
        if ("2".equals(variables.getString("制度建设类型"))) {
            regulationIdentifier = variables.getString("制度唯一标识文本");
        }
        if (StringUtils.isEmpty(regulationIdentifier)) {
            log.warn(traceId + " CANNOT FIND regulationIdentifier!");
            return;
        }

        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(regulationIdentifier);
        if (zyRegulationBjmoa == null) {
            log.info(traceId + " creating a new regulation");
            zyRegulationBjmoa = new ZyRegulationBjmoa();
        } else {
            log.info(traceId + " editing a regulation");
        }

        String fileId = variables.getString("内管文件编号");
        String docId = variables.getString("内管文档编号");
        zyRegulationBjmoa.setQiqiaoRegulationId(qiqiaoRegulationId);
        zyRegulationBjmoa.setName(variables.getString("制度名称").trim());
        zyRegulationBjmoa.setContentFileId(fileId);
        zyRegulationBjmoa.setContentDocId(docId);
        zyRegulationBjmoa.setCode(variables.getString("制度编号"));
        zyRegulationBjmoa.setIdentifier(regulationIdentifier);
        zyRegulationBjmoa.setQiqiaoCreatorId(variables.getString("制度跟进人"));
        zyRegulationBjmoa.setQiqiaoCreatorName(variables.getString("制度跟进人_pretty_value"));
        zyRegulationBjmoa.setCategoryId(variables.getString("大类"));
        zyRegulationBjmoa.setCategoryName(prettyValue.getString("大类"));
        zyRegulationBjmoa.setActive(0);
        if (!saveOrUpdate(zyRegulationBjmoa)) {
            log.warn(traceId + " FAILED TO SAVE zyRegulationBjmoa=" + zyRegulationBjmoa);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }

        // 保存一条制度历史版本的记录
        ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
        BeanUtils.copyProperties(zyRegulationBjmoa, zyRegulationBjmoaHistory);
        zyRegulationBjmoaHistory.setId(null);
        List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryMapper.queryByIdentifier(regulationIdentifier);
        if (zyRegulationBjmoaHistoryList == null || zyRegulationBjmoaHistoryList.size() == 0) {
            if (!zyRegulationBjmoaHistoryService.save(zyRegulationBjmoaHistory)) {
                log.warn(traceId + " FAILED TO SAVE HISTORY: " + zyRegulationBjmoaHistory);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }
        } else {
            ZyRegulationBjmoaHistory existingHistory = zyRegulationBjmoaHistoryList.get(0);
            zyRegulationBjmoa.setId(existingHistory.getId());
            if (!zyRegulationBjmoaHistoryService.updateById(zyRegulationBjmoaHistory)) {
                log.warn(traceId + " FAILED TO UPDATE HISTORY: " + zyRegulationBjmoaHistory);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }
        }

        // 保存一条主责部门记录
        String regulationDeptId = variables.getString("制度主责部门");
        String regulationDeptName = variables.getString("制度主责部门_pretty_value");
        if (regulationDeptId == null || regulationDeptName == null || regulationDeptId.equals(regulationDeptName)) {
            log.warn(traceId + " CANNOT FIND REGULATION DEPT FOR REGULATION " + qiqiaoRegulationId
                    + ", regulationDeptId=" + regulationDeptId);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }

        ZyRegulationBjmoaDept zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
        BeanUtils.copyProperties(zyRegulationBjmoa, zyRegulationBjmoaDept);
        zyRegulationBjmoaDept.setQiqiaoDeptId(regulationDeptId);
        zyRegulationBjmoaDept.setQiqiaoDeptName(regulationDeptName);
        zyRegulationBjmoaDept.setId(null);

        List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList = zyRegulationBjmoaDeptMapper.getByIdentifier(regulationIdentifier);
        if (zyRegulationBjmoaDeptList == null || zyRegulationBjmoaDeptList.size() == 0) {
            if (!zyRegulationBjmoaDeptService.save(zyRegulationBjmoaDept)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }
        }

        // 3. 添加PDF水印
        String addWatermarkTaskId =
                bjmoaAppId + "@" + WpsOperateType.OFFICE_OPERATE + "@" + docId + "@" + fileId + "@" + qiqiaoRegulationId + "@" + timeStamp;
        log.info(traceId + " ADD WATER MARK TASK ID: " + addWatermarkTaskId);
        String pdfFileName = zyRegulationBjmoa.getName() + ".pdf";
        WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
        wpsFormatDTO.setTask_id(addWatermarkTaskId);
        wpsFormatDTO.setScene_id(bjmoaAppId);

        wpsFormatDTO.setDoc_url(contentManagementService.getDownloadNewestUrl(docId));
        wpsFormatDTO.setDoc_filename(pdfFileName);

        final List<Step> steps = new ArrayList<>();
        final Step step = new Step();
        step.setOperate(OperateConstant.OFFICE_WATERMARK);
        final TextWatermark textWatermark = new TextWatermark();
        textWatermark.setContent("北京市轨道交通运营管理有限公司");
        textWatermark.setTilt(true);
        textWatermark.setTransparent(0.45);

        final Arg args = new Arg();
        args.setText_watermark(textWatermark);
        step.setArgs(args);
        steps.add(step);

        wpsFormatDTO.setSteps(steps);

        if (!contentManagementService.officeOperate(wpsFormatDTO)) {
            log.warn(traceId + " FAILED ADD WATERMARK " + wpsFormatDTO);
        }
    }

    @Override
    public void initiateOAProcess(final String qiqiaoRegulationId) {
        String traceId = "bjmoa_initiate_OA_process" + "@" + qiqiaoRegulationId + "@" + DateUtils.getDate("yyyyMMddHHmmss");
        log.info(traceId + " START INITIATE OA PROCESS");
        ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaMapper.queryByQiqiaoRegulationId(qiqiaoRegulationId);
        log.info(traceId + " zyRegulationBjmoa: " + zyRegulationBjmoa);
        if (zyRegulationBjmoa == null) {
            log.warn(traceId + " Fail to find regulation by qiqiaoRegulationId: " + qiqiaoRegulationId);
            return;
        }
        String identifier = zyRegulationBjmoa.getIdentifier();
        if (StringUtils.isEmpty(identifier)) {
            log.warn(traceId + " identifier is null, fail to initiate oa process!");
            return;
        }

        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn("CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        String categoryId = record.getJSONObject("variables").getString("大类");
        List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList = null;
        if ("2".equals(categoryId)) {
            zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryMapper.queryNewestRegulationByIdentifier(identifier);
        } else {
            String code = zyRegulationBjmoa.getCode();
            String version = "A/" + (record.getJSONObject("variables").getInteger("制度版本号") - 1);
            zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryService.queryByIdentifierAndVersionAndCode(identifier, version, code);
        }
        log.info(traceId + " zyRegulationBjmoaHistoryList: " + zyRegulationBjmoaHistoryList);
        if (CollectionUtils.isNotEmpty(zyRegulationBjmoaHistoryList)) {
            ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0); //获取最新历史版本
            log.info(traceId + " zyRegulationBjmoaHistory: " + zyRegulationBjmoaHistory);
            zyRegulationBjmoaHistoryService.createOaRequest(zyRegulationBjmoaHistory, traceId);
        } else {
            log.warn(traceId + " Regulation history is null, fail to initiate oa process!");
        }
    }

    @Override
    public void initiateBoardOAProcess(String qiqiaoRegulationId) {
        String traceId = "bjmoa_initiate_Board_OA_process" + "@" + qiqiaoRegulationId + "@" + DateUtils.getDate("yyyyMMddHHmmss");
        log.info(traceId + " START INITIATE BOARD OA PROCESS");
        ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaMapper.queryByQiqiaoRegulationId(qiqiaoRegulationId);
        log.info(traceId + " zyRegulationBjmoa: " + zyRegulationBjmoa);
        if (zyRegulationBjmoa == null) {
            log.warn(traceId + " Fail to find regulation by qiqiaoRegulationId: " + qiqiaoRegulationId);
            return;
        }
        String identifier = zyRegulationBjmoa.getIdentifier();
        if (StringUtils.isEmpty(identifier)) {
            log.warn(traceId + " identifier is null, fail to initiate oa process!");
            return;
        }

        List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryMapper.queryNewestRegulationByIdentifier(identifier);
        log.info(traceId + " zyRegulationBjmoaHistoryList: " + zyRegulationBjmoaHistoryList);
        if (CollectionUtils.isNotEmpty(zyRegulationBjmoaHistoryList)) {
            ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0); //获取最新历史版本
            log.info(traceId + " zyRegulationBjmoaHistory: " + zyRegulationBjmoaHistory);
            zyRegulationBjmoaHistoryService.createOaRequest(zyRegulationBjmoaHistory, traceId);
        } else {
            log.warn(traceId + " Regulation history is null, fail to initiate oa process!");
        }
    }

    @Override public void activateByQiqiaoRegulationId(String qiqiaoRegulationId) {
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            return;
        }
        final ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaMapper.queryByQiqiaoRegulationId(qiqiaoRegulationId);
        if (zyRegulationBjmoa == null || Integer.valueOf(1).equals(zyRegulationBjmoa.getActive())) {
            log.warn("NO NEED TO UPDATE zyRegulationBjmoa=" + zyRegulationBjmoa);
            return;
        }

        final int activateCnt = zyRegulationBjmoaMapper.activateByQiqiaoRegulationId(qiqiaoRegulationId);
        log.info("Activate total: " + activateCnt);
    }

    @Override public void inactivateByIdentifier(final String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return;
        }
        final ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(identifier);
        if (zyRegulationBjmoa == null || Integer.valueOf(0).equals(zyRegulationBjmoa.getActive())) {
            log.warn("NO NEED TO UPDATE zyRegulationBjmoa=" + zyRegulationBjmoa);
            return;
        }

        final int inactivateCnt = zyRegulationBjmoaMapper.inactivateByIdentifier(identifier);
        zyRegulationBjmoaHistoryService.inactivateByIdentifier(identifier);
        inactivateRelatedByIdentifier(identifier);
        log.info("Inactivate total: " + inactivateCnt);
    }

    @Override public void inactivateRelatedByIdentifier(final String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return;
        }
        final List<ZyRelatedRegulationBjmoa> zyRelatedRegulationBjmoaList =
            zyRelatedRegulationBjmoaService.lambdaQuery()
                .eq(ZyRelatedRegulationBjmoa::getRegulationIdentifierA, identifier)
                .eq(ZyRelatedRegulationBjmoa::getRegulationType, RegulationType.RELATED).list();
        for (final ZyRelatedRegulationBjmoa zyRelatedRegulationBjmoa : zyRelatedRegulationBjmoaList) {
            final String regulationIdentifierB = zyRelatedRegulationBjmoa.getRegulationIdentifierB();
            // Hope there is no endless loop :)
            inactivateByIdentifier(regulationIdentifierB);
        }
    }

    @Override public void wrapHeaderCallback(final JSONObject jsonObject) {
        log.info("wrapHeaderCallback: " + jsonObject);
        if (jsonObject == null) {
            log.warn("CANNOT FIND JSON OBJECT");
            return;
        }
        final String taskId = jsonObject.getString("task_id");
        final String downloadId = jsonObject.getString("download_id");
        if (StringUtils.isEmpty(taskId) || StringUtils.isEmpty(downloadId)) {
            log.warn("CANNOT FIND TASK ID OR DOWNLOAD ID");
            return;
        }

        final String[] taskIdList = taskId.split("@");
        final String fileId = taskIdList[3];
        final String qiqiaoRegulationId = taskIdList[4];
        final String timePublishStatus = taskIdList[5];
        final String traceId = "bjmoa_create_edit_regulation" + "@" + qiqiaoRegulationId + "@" + timePublishStatus;
        log.info(traceId + " wrapHeaderCallback jsonObject: {}", jsonObject);
        final ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaMapper.queryByContentFileId(fileId);
        if (zyRegulationBjmoa == null || StringUtils.isEmpty(qiqiaoRegulationId)) {
            log.warn(traceId + " CANNOT FIND REGULATION BY CONTENT FILE ID " + fileId);
            return;
        }

        // 查询七巧制度基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        final JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        final JSONObject variables = record.getJSONObject("variables");
        final JSONObject prettyValue = record.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(traceId + " CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
        }
        saveHeaderWrappedDocRegulation(qiqiaoRegulationId, downloadId, zyRegulationBjmoa, variables, traceId);
    }

    @Override public void pdfConversionCallback(final JSONObject jsonObject) {
        log.info("pdfConversionCallback: " + jsonObject);
        if (jsonObject == null) {
            log.warn("CANNOT FIND JSON OBJECT");
            return;
        }
        final String taskId = jsonObject.getString("task_id");
        final String downloadId = jsonObject.getString("download_id");
        if (StringUtils.isEmpty(taskId) || StringUtils.isEmpty(downloadId)) {
            log.warn("CANNOT FIND TASK ID OR DOWNLOAD ID");
            return;
        }

        final String[] taskIdList = taskId.split("@");
        final String fileId = taskIdList[3];
        final String qiqiaoRegulationId = taskIdList[4];
        final String timePublishStatus = taskIdList[5];
        final String traceId = "bjmoa_create_edit_regulation" + "@" + qiqiaoRegulationId + "@" + timePublishStatus;
        log.info(traceId + " pdfConversionCallback jsonObject: {}", jsonObject);
        final ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaMapper.queryByContentFileId(fileId);
        if (zyRegulationBjmoa == null || StringUtils.isEmpty(qiqiaoRegulationId)) {
            log.warn(traceId + " CANNOT FIND REGULATION BY CONTENT FILE ID " + fileId);
            return;
        }

        // 查询七巧制度基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        final JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        final JSONObject variables = record.getJSONObject("variables");
        final JSONObject prettyValue = record.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(traceId + " CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }

        savePdfRegulation(qiqiaoRegulationId, downloadId, zyRegulationBjmoa, variables, traceId);
    }

    @Override public void addWatermarkCallback(final JSONObject jsonObject) {
        log.info("addWatermarkCallback: " + jsonObject);
        if (jsonObject == null) {
            log.warn("CANNOT FIND JSON OBJECT");
            return;
        }
        final String taskId = jsonObject.getString("task_id");
        final String downloadId = jsonObject.getString("download_id");
        if (StringUtils.isEmpty(taskId) || StringUtils.isEmpty(downloadId)) {
            log.warn("CANNOT FIND TASK ID OR DOWNLOAD ID");
            return;
        }

        final String[] taskIdList = taskId.split("@");
        final String qiqiaoRegulationId = taskIdList[4];
        final String timePublishStatus = taskIdList[5];
        final String traceId = "bjmoa_create_edit_regulation" + "@" + qiqiaoRegulationId + "@" + timePublishStatus;
        log.info(traceId + " addWatermarkCallback jsonObject: {}", jsonObject);
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            log.warn(traceId + " CANNOT FIND QIQIAO REGULATION ID ");
            return;
        }
        final ZyRegulationBjmoa zyRegulationBjmoa =
            zyRegulationBjmoaMapper.queryByQiqiaoRegulationId(qiqiaoRegulationId);
        if (zyRegulationBjmoa == null) {
            log.warn(traceId + " CANNOT FIND REGULATION BY QIQIAO REGULATION ID " + qiqiaoRegulationId);
            return;
        }

        saveWatermarkPdfRegulation(qiqiaoRegulationId, downloadId, zyRegulationBjmoa, traceId);
    }

    @Override public Page<ZyRegulationBjmoaVO> queryNewestVersionPageList(Page<ZyRegulationBjmoaVO> page,
        RegulationQueryDTO queryDTO) {
        if (page == null) {
            page = new Page<>();
        }

        queryDTO = preprocessQueryDTO(queryDTO);
        if (null == queryDTO) {
            log.warn("NO RECORDS! queryDTO=" + queryDTO);
            return page;
        }
        final List<ZyRegulationBjmoaVO> records = zyRegulationBjmoaMapper.queryNewestVersionPageList(page, queryDTO);
        page.setRecords(postProcess(queryDTO, records));
        return page;
    }

    @Override public Page<ZyRegulationTempBjmoaVO> queryTempPageList(Page<ZyRegulationTempBjmoaVO> page,
        RegulationTempQueryDTO queryDTO) {
        //  查询临时技术变更表单
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(tempTechnicalChangeRegulationModelId);
        recordVO.setPage((int)page.getCurrent());
        recordVO.setPageSize((int)page.getSize());
        //pc端获取标题
        if (StringUtils.isNotEmpty(queryDTO.getName())) {
            List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            FieldFilter fieldFilterName = new FieldFilter();
            fieldFilterName.setFieldName("临时技术变更名称");
            fieldFilterName.setLogic("like");
            fieldFilterName.setValue(String.valueOf(queryDTO.getName()));
            fieldFilterList.add(fieldFilterName);
            recordVO.setFilter(fieldFilterList);
        }
        //移动端获取searchmessage
        if (StringUtils.isNotEmpty(queryDTO.getSearchMessage())) {
            List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            FieldFilter fieldFilterName = new FieldFilter();
            fieldFilterName.setFieldName("临时技术变更名称");
            fieldFilterName.setLogic("like");
            fieldFilterName.setValue(String.valueOf(queryDTO.getSearchMessage()));
            fieldFilterList.add(fieldFilterName);
            recordVO.setFilter(fieldFilterList);
        }
        //获取编号
        if (StringUtils.isNotEmpty(queryDTO.getCode())) {
            List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            FieldFilter fieldFilterName = new FieldFilter();
            fieldFilterName.setFieldName("变更编号");
            fieldFilterName.setLogic("like");
            fieldFilterName.setValue(String.valueOf(queryDTO.getCode()));
            fieldFilterList.add(fieldFilterName);
            recordVO.setFilter(fieldFilterList);
        }
        final JSONObject records = qiqiaoFormsService.page(recordVO);
        if (records == null) {
            log.warn("CANNOT FIND TEMP LIST ");
            return page;
        }
        JSONArray list = records.getJSONArray("list");
        List<ZyRegulationTempBjmoaVO> results = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            JSONObject record = list.getJSONObject(i);
            JSONObject variables = record.getJSONObject("variables");
            ZyRegulationTempBjmoaVO zyRegulationTempBjmoaVO = new ZyRegulationTempBjmoaVO();
            String name = variables.getString("临时技术变更名称");
            Date effectiveTime = variables.getDate("临时技术变更开始时间");
            Date dueTime = variables.getDate("临时技术变更到期时间");
            String id = record.getString("id");
            //String status = variables.getString("状态");
            String code = variables.getString("变更编号");
            zyRegulationTempBjmoaVO.setEffectiveTime(effectiveTime);
            zyRegulationTempBjmoaVO.setDueTime(dueTime);
            zyRegulationTempBjmoaVO.setName(name);
            zyRegulationTempBjmoaVO.setId(id);
            //zyRegulationTempBjmoaVO.setStatus(status);
            zyRegulationTempBjmoaVO.setCode(code);
            results.add(zyRegulationTempBjmoaVO);
        }
        page.setRecords(results);
        if (records.getInteger("totalCount") != null) {
            page.setTotal(records.getInteger("totalCount"));
        } else if (records.getInteger("total") != null) {
            page.setTotal(records.getInteger("total"));
        }
        return page;
    }

    @Override public ZyRegulationBjmoa queryByIdentifier(final String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return null;
        }
        return zyRegulationBjmoaMapper.queryByIdentifier(identifier);
    }

    @Override public Result<?> queryManagementToolEntryList() {
        Object bjmoaManagementToolEntryObj = redisUtil.get(bjmoaManagementToolEntryRedisKey);
        if (!ObjectUtil.isEmpty(bjmoaManagementToolEntryObj)) {
            return Result.OK(bjmoaManagementToolEntryObj);
        }

        // 查询七巧管理工具快捷入口信息
        RecordVO recordVO = new RecordVO();
        recordVO.setPageSize(50);   // 设置返回快捷入口列表信息数量，确保返回所有的信息
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaManagementToolEntryFormModelId);
        final JSONObject records = qiqiaoFormsService.page(recordVO);
        if (records == null) {
            log.warn("CANNOT FIND MANAGEMENT TOOL ENTRY INFO RECORDS");
            return null;
        }

        JSONArray list = records.getJSONArray("list");
        List<ZyRegulationBjmoaManagementEntryVO> results = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            JSONObject record = list.getJSONObject(i);
            JSONObject variables = record.getJSONObject("variables");
            boolean isHide = ("1").equals(variables.getString("是否隐藏"));
            if (isHide) {
                continue;
            }
            ZyRegulationBjmoaManagementEntryVO zyRegulationBjmoaManagementEntryVO =
                new ZyRegulationBjmoaManagementEntryVO();
            int sort = Integer.MAX_VALUE;
            if (variables.getInteger("sort") != null) {
                sort = variables.getInteger("sort");
            }
            String button = variables.getString("button");
            String url = variables.getString("url");
            String iconUrl = contentManagementService.getDownloadUrl(variables.getString("内管文件编号"));

            zyRegulationBjmoaManagementEntryVO.setSort(sort);
            zyRegulationBjmoaManagementEntryVO.setButton(button);
            zyRegulationBjmoaManagementEntryVO.setUrl(url);
            zyRegulationBjmoaManagementEntryVO.setIconUrl(iconUrl);
            results.add(zyRegulationBjmoaManagementEntryVO);
        }

        // 按照优先级排序
        Collections.sort(results);

        if (CollectionUtils.isNotEmpty(results)) {
            redisUtil.del(bjmoaManagementToolEntryRedisKey);
            redisUtil.set(bjmoaManagementToolEntryRedisKey, results, 60);
        }
        return Result.OK(results);
    }

    @Override public int queryTempTechnicalChangesRegulationNumber() {
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(tempTechnicalChangeRegulationModelId);
        Date now = new Date();
        List<FieldFilter> filters = new ArrayList<>(2);
        FieldFilter fieldFilter1 = new FieldFilter();
        fieldFilter1.setFieldName("临时技术变更开始时间");
        fieldFilter1.setLogic("le");
        fieldFilter1.setValue(String.valueOf(now.getTime()));
        filters.add(fieldFilter1);
        FieldFilter fieldFilter2 = new FieldFilter();
        fieldFilter2.setFieldName("临时技术变更到期时间");
        fieldFilter2.setLogic("gt");
        fieldFilter2.setValue(String.valueOf(now.getTime()));
        filters.add(fieldFilter2);
        recordVO.setFilter(filters);
        final JSONObject record = qiqiaoFormsService.page(recordVO);
        if (record == null) {
            log.warn("CANNOT FIND TEMP TECHNICAL CHANGES REGULATION RECORD");
            return 0;
        }
        int totalCount = 0;
        if (record.containsKey("totalCount")) {
            totalCount = record.getInteger("totalCount");
        } else if (record.containsKey("total")) {
            totalCount = record.getInteger("total");
        }
        return totalCount;
    }

    @Override public boolean queryDownloadWordPermission(String loginid, String responsibleDepartment) {
        log.info("loginid: {}, responsibleDepartment: {}", loginid, responsibleDepartment);
        if (StringUtils.isEmpty(loginid) || StringUtils.isEmpty(responsibleDepartment)) {
            return false;
        }

        boolean wordDownloadPermission = false;
        JSONObject userInfo = publicManagementService.getUserInfoByUserName(loginid);
        JSONObject qiqiaoData = null;
        JSONArray departmentIds = null;
        String qiqiaoLoginUserid = null;
        if (userInfo == null) {
            log.warn("userInfo IS NULL");
            return false;
        }
        if (userInfo.containsKey("qiqiaoData")) {
            qiqiaoData = userInfo.getJSONObject("qiqiaoData");
        } else {
            log.warn("CANNOT FIND KEY qiqiaoData IN userInfo");
            return false;
        }
        if (qiqiaoData == null) {
            log.warn("qiqiaoData IS NULL");
            return false;
        }
        if (qiqiaoData.containsKey("departmentIds")) {
            departmentIds = qiqiaoData.getJSONArray("departmentIds");
        } else {
            log.warn("CANNOT FIND KEY departmentIds IN qiqiaoData");
            return false;
        }
        if (departmentIds == null || departmentIds.size() == 0) {
            log.warn("departmentIds IS NULL");
            return false;
        }
        if (qiqiaoData.containsKey("id")) {
            qiqiaoLoginUserid = qiqiaoData.getString("id");
        } else {
            log.warn("CANNOT FIND KEY id IN qiqiaoData");
            return false;
        }
        if (StringUtils.isEmpty(qiqiaoLoginUserid)) {
            log.warn("qiqiaoLoginUserid IS NULL");
            return false;
        }

        // 查询制度主责部门文件管理员
        List<String> documentationSupervisors = queryDocumentationSupervisor(responsibleDepartment);
        if (documentationSupervisors == null || documentationSupervisors.size() == 0) {
            log.warn("documentationSupervisors IS NULL");
            return false;
        }
        for (int i = 0; i < departmentIds.size(); i++) {
            // 如果登录者属于制度主责部门且是部门文件管理员，提供下载Word权限
            if (responsibleDepartment.equals(departmentIds.getString(i)) && documentationSupervisors.contains(
                qiqiaoLoginUserid)) {
                wordDownloadPermission = true;
                break;
            }
        }

        return wordDownloadPermission;
    }

    @Override public boolean queryDownloadPdfPermission(String loginid, String levelId, String responsibleDepartment,
        String identifier) {
        log.info("loginid: {}, levelId: {}, responsibleDepartment: {}, identifier: {}", loginid, levelId,
            responsibleDepartment, identifier);
        if (StringUtils.isEmpty(loginid) || StringUtils.isEmpty(responsibleDepartment) || StringUtils.isEmpty(
            identifier)) {
            return false;
        }

        boolean pdfDownloadPermission = false;
        JSONObject userInfo = publicManagementService.getUserInfoByUserName(loginid);
        JSONObject qiqiaoData = null;
        JSONArray departmentIds = null; // 员工部门ID
        if (userInfo == null) {
            log.warn("userInfo IS NULL");
            return false;
        }
        if (userInfo.containsKey("qiqiaoData")) {
            qiqiaoData = userInfo.getJSONObject("qiqiaoData");
        } else {
            log.warn("CANNOT FIND KEY qiqiaoData IN userInfo");
            return false;
        }
        if (qiqiaoData == null) {
            log.warn("qiqiaoData IS NULL");
            return false;
        }
        if (qiqiaoData.containsKey("departmentIds")) {
            departmentIds = qiqiaoData.getJSONArray("departmentIds");
        } else {
            log.warn("CANNOT FIND KEY departmentIds IN qiqiaoData");
            return false;
        }
        if (departmentIds == null || departmentIds.size() == 0) {
            log.warn("departmentIds IS NULL");
            return false;
        }

        List<String> rootDepartmentIds = new ArrayList<>(departmentIds.size());    // 员工最上级部门ID
        for (int i = 0; i < departmentIds.size(); i++) {
            JSONObject departmentInfo = qiqiaoDepartmentService.getByDepartmentId(departmentIds.getString(i));
            String parentDepartmentId = null;
            if (departmentInfo != null && departmentInfo.containsKey("parentId")) {
                parentDepartmentId = departmentInfo.getString("parentId");
            } else {
                log.warn("CAN NOT FIND PARENT DEPARTMENT ID");
                return false;
            }

            // 一直找到最上层的部门信息
            while (!parentDepartmentId.equals("") && parentDepartmentId != null) {
                departmentInfo = qiqiaoDepartmentService.getByDepartmentId(parentDepartmentId);
                if (departmentInfo != null && departmentInfo.containsKey("parentId")) {
                    parentDepartmentId = departmentInfo.getString("parentId");
                } else {
                    log.warn("CAN NOT FIND PARENT DEPARTMENT ID");
                    return false;
                }
            }
            rootDepartmentIds.add(departmentInfo.getString("id"));
        }
        if (rootDepartmentIds == null || rootDepartmentIds.size() == 0) {
            log.warn("rootDepartmentIds IS NULL");
            return false;
        }

        // 如果制度为5级表单，下载权限随关联制度
        if ("12".equals(levelId)) {
            levelId = zyRegulationBjmoaMapper.queryRelatedRegulationLevelId(identifier);
        }

        // 如果制度级别为空，按`公司级`制度处理
        if (levelId == null) {
            levelId = "10";
        }

        switch (levelId) {
            case "1":
            case "2":
            case "3":
            case "4":
            case "7":
            case "8":
            case "9":
            case "10":
                // 1级、2级、3A级、3B级、综合应急预案、专项应急预案、公司级制度根据七巧`pdf带水印制度下载部门列表`表单配置进行权限控制
                List<String> canDownloadPdfDeptIdList = queryDownloadPermissionDeptIdList(levelId);
                if (canDownloadPdfDeptIdList == null) {
                    log.warn("canDownloadPdfDeptIdList IS NULL");
                    break;
                }
                for (int i = 0; i < rootDepartmentIds.size(); i++) {
                    if (canDownloadPdfDeptIdList.contains(rootDepartmentIds.get(i))) {
                        pdfDownloadPermission = true;
                        break;
                    }
                }
                for (int i = 0; i < departmentIds.size(); i++) {
                    if (canDownloadPdfDeptIdList.contains(departmentIds.get(i))) {
                        pdfDownloadPermission = true;
                        break;
                    }
                }
                break;
            case "5":
            case "6":
            case "11":
                // 3C、4级、分工会级制度根据主责部门进行权限控制
                for (int i = 0; i < departmentIds.size(); i++) {
                    if (responsibleDepartment.equals(departmentIds.getString(i))) {
                        pdfDownloadPermission = true;
                        break;
                    }
                }
                break;
            default:
                log.warn("ERROR LEVEL ID!");
                break;
        }

        return pdfDownloadPermission;
    }

    private List<String> queryDocumentationSupervisor(String deptId) {
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaPermissionManagementFormModelId);
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("所在部门");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(deptId);
        fieldFilterList.add(fieldFilter);
        recordVO.setFilter(fieldFilterList);
        final JSONObject record = qiqiaoFormsService.page(recordVO);
        if (record == null) {
            log.warn("CANNOT FIND DOCUMENTATION SUPERVISOR INFO RECORD");
            return null;
        }

        JSONArray list = null;
        JSONObject permissionManagementRecord = null;
        JSONArray documentationSupervisor = null;
        JSONObject permissionManagementVariables = null;
        if (record.containsKey("list")) {
            list = record.getJSONArray("list");
        } else {
            log.warn("CANNOT FIND KEY list IN SUPERVISOR INFO RECORD");
            return null;
        }
        if (CollectionUtils.isEmpty(list)) {
            log.warn("list IS NULL IN SUPERVISOR INFO RECORD");
            return null;
        }
        permissionManagementRecord = list.getJSONObject(0);
        if (permissionManagementRecord.containsKey("variables")) {
            permissionManagementVariables = permissionManagementRecord.getJSONObject("variables");
        } else {
            log.warn("CANNOT FIND KEY variables IN permissionManagementRecord");
            return null;
        }
        if (permissionManagementVariables == null) {
            log.warn("permissionManagementVariables IS NULL");
            return null;
        }
        if (permissionManagementVariables.containsKey("制度联系人")) {
            documentationSupervisor = permissionManagementVariables.getJSONArray("制度联系人");
        } else {
            log.warn("CANNOT FIND KEY '制度联系人' IN permissionManagementVariables");
            return null;
        }
        if (documentationSupervisor == null || documentationSupervisor.size() == 0) {
            log.warn("documentationSupervisor IS NULL");
            return null;
        }

        List<String> documentationSupervisorLists = new ArrayList<>(documentationSupervisor.size());
        for (int i = 0; i < documentationSupervisor.size(); i++) {
            documentationSupervisorLists.add(documentationSupervisor.getString(i));
        }
        return documentationSupervisorLists;
    }

    private List<String> queryDownloadPermissionDeptIdList(String levelId) {
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaPdfDownloadPermissionDeptFormModelId);
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("制度级别");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(levelId);
        fieldFilterList.add(fieldFilter);
        recordVO.setFilter(fieldFilterList);
        final JSONObject record = qiqiaoFormsService.page(recordVO);
        if (record == null) {
            log.warn("CANNOT FIND TEMP TECHNICAL CHANGES REGULATION RECORD");
            return null;
        }

        JSONArray list = null;
        if (record.containsKey("list")) {
            list = record.getJSONArray("list");
        } else {
            log.warn("CAN NOT FIND KEY list IN record");
            return null;
        }
        if (CollectionUtils.isEmpty(list)) {
            log.warn("list IS NULL");
            return null;
        }
        final JSONObject pdfDownloadDeptRecord = list.getJSONObject(0);
        JSONObject pdfDownloadDeptVariables = null;
        JSONArray pdfDownloadDeptIds = null;
        if (pdfDownloadDeptRecord.containsKey("variables")) {
            pdfDownloadDeptVariables = pdfDownloadDeptRecord.getJSONObject("variables");
        } else {
            log.warn("CAN NOT FIND KEY variables IN pdfDownloadDeptRecord");
            return null;
        }
        if (pdfDownloadDeptVariables.containsKey("pdf带水印制度下载部门")) {
            pdfDownloadDeptIds = pdfDownloadDeptVariables.getJSONArray("pdf带水印制度下载部门");
        } else {
            log.warn("CAN NOT FIND KEY pdf带水印制度下载部门 IN pdfDownloadDeptVariables");
            return null;
        }
        if (pdfDownloadDeptIds == null || pdfDownloadDeptIds.size() == 0) {
            log.warn("pdfDownloadDeptIds IS NULL");
            return null;
        }

        List<String> pdfDownloadDeptIdLists = new ArrayList<>(pdfDownloadDeptIds.size());
        for (int i = 0; i < pdfDownloadDeptIds.size(); i++) {
            pdfDownloadDeptIdLists.add(pdfDownloadDeptIds.getString(i));
        }
        return pdfDownloadDeptIdLists;
    }

    @Override public void updateQiqiaoRegulation(final String requestId) {
        log.info("updateQiqiaoRegulation: requestId=" + requestId);
        if (StringUtils.isEmpty(requestId)) {
            log.warn("EMPTY REQUEST ID");
            return;
        }
        final ZyRegulationBjmoaHistory zyRegulationBjmoaHistory =
            zyRegulationBjmoaHistoryService.queryByRequestId(requestId);
        if (zyRegulationBjmoaHistory == null) {
            log.warn("CANNOT FIND zyRegulationBjmoaHistory BY requestId=" + requestId);
            return;
        }

        final String categoryId = zyRegulationBjmoaHistory.getCategoryId();
        if (StringUtils.isEmpty(categoryId)) {
            log.warn("CATEGORY ID IS EMPTY!");
            return;
        }

        switch (categoryId) {
            case "1":
            case "2": {
                // 董事会管理制度、党群管理制度：实施日期=发布日期，无需套打。需OA回传实施日期和发布日期
                zyRegulationBjmoaHistory.setPublishTime(new Date());
                zyRegulationBjmoaHistory.setExecuteTime(zyRegulationBjmoaHistory.getPublishTime());
                break;
            }
            case "3":
            case "4": {
                // 经营层制度、应急预案：OA回传发布日期
                zyRegulationBjmoaHistory.setPublishTime(new Date());
                break;
            }
            default: {
                log.warn("WAS IST DAS DENN?");
            }
        }

        if (!zyRegulationBjmoaHistoryService.updateById(zyRegulationBjmoaHistory)) {
            log.info("FAILED TO UPDATE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            return;
        }

        // 更新七巧信息
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        final FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("制度系统标识别文本");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(zyRegulationBjmoaHistory.getIdentifier());
        final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        fieldFilterList.add(fieldFilter);
        recordVO.setFilter(fieldFilterList);

        final JSONObject records = qiqiaoFormsService.page(recordVO);
        if (records == null) {
            log.warn("CANNOT FIND records FOR recordVO=" + recordVO);
            return;
        }

        final JSONArray list = records.getJSONArray("list");
        if (CollectionUtils.isEmpty(list)) {
            log.warn("LIST IS EMPTY FOR recordVO=" + recordVO);
            return;
        }
        final JSONObject record = list.getJSONObject(0);

        // 更新记录
        final RecordVO updateRecordVO = new RecordVO();
        updateRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        updateRecordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        updateRecordVO.setId(record.getString("id"));
        final Map<String, Object> data = new HashMap<>(2);
        if (zyRegulationBjmoaHistory.getPublishTime() != null) {
            data.put("制度发布时间", zyRegulationBjmoaHistory.getPublishTime().getTime());
        }
        if (zyRegulationBjmoaHistory.getExecuteTime() != null) {
            data.put("制度实施时间", zyRegulationBjmoaHistory.getExecuteTime().getTime());
        }
        updateRecordVO.setData(data);

        log.info("updateRecordVO: " + updateRecordVO);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateRecordVO);
        log.info("qiqiaoFormsService.saveOrUpdate: " + jsonObject);
    }

    @Override public void pullRegulationFromQiqiqao() {
        log.info("pullRegulationFromQiqiqao start");
        zyRelatedRegulationBjmoaService.truncateTable();
        log.info("truncate table zy_related_regulation_bjmoa");
        // 获取所有【轨道运营制度发布单】的记录
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);

        int pageNo = 1;
        final int pageSize = 100;
        recordVO.setPageSize(pageSize);
        boolean finished = false;
        while (!finished) {
            recordVO.setPage(pageNo);
            final JSONObject pageJson = qiqiaoFormsService.page(recordVO);

            final JSONArray realRegulationList = pageJson.getJSONArray("list");
            for (int i = 0; i < realRegulationList.size(); ++i) {
                try {
                    final JSONObject realRegulationJson = realRegulationList.getJSONObject(i);
                    if (realRegulationJson == null) {
                        continue;
                    }
                    final String qiqiaoRealRegulationId = realRegulationJson.getString("id");
                    if (!syncBjmoaOld(qiqiaoRealRegulationId)) {
                        log.warn("FAILED TO SYNCHRONIZE REGULATION OLD id=" + qiqiaoRealRegulationId);
                    }
                } catch (Exception e) {
                    log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
                }
            }

            finished = CollectionUtils.isEmpty(realRegulationList);
            ++pageNo;
        }

        log.info("pullRegulationFromQiqiqao end");
    }

    private boolean syncBjmoaOld(final String qiqiaoRealRegulationId) {
        final String prefix = "[syncBjmoaOld " + qiqiaoRealRegulationId + "] ";
        if (StringUtils.isEmpty(qiqiaoRealRegulationId)) {
            return false;
        }
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRealRegulationId);
        JSONObject regulationJson = qiqiaoFormsService.queryById(recordVO);

        JSONObject variables = regulationJson.getJSONObject("variables");
        JSONObject prettyValue = regulationJson.getJSONObject("prettyValue");
        final String managementType = variables.getString("管理类别");
        final String categoryType = variables.getString("大类");
        if (variables == null || prettyValue == null) {
            log.warn(prefix + "variables or prettyValue is empty");
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
        if (managementType == null && !"4".equals(categoryType)) {
            //如果管理类别为空，且不为应急预案，说明信息未完善，不进行同步
            log.warn(qiqiaoRealRegulationId + " incomplete data");
            return false;
        }
        ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(identifier);
        final String status = variables.getString("制度状态");
        if (zyRegulationBjmoa == null) {
            zyRegulationBjmoa = new ZyRegulationBjmoa();
        } else {
            if ("8".equals(status)) {
                //如果已废止的制度在高代码数据库中状态为active，将其作废
                if (Integer.valueOf(1).equals(zyRegulationBjmoa.getActive())) {
                    inactivateByIdentifier(identifier);
                }
                return true;
            }
        }
        //只同步”已发布“状态的制度
        if (!"7".equals(status)) {
            log.warn(prefix + "NO NEED TO SYNC REGULATION " + qiqiaoRealRegulationId);
            return false;
        } else {
            //save regulation
            if (!syncRegulation(qiqiaoRealRegulationId, zyRegulationBjmoa, variables, prettyValue)) {
                log.warn(prefix + "FAILED TO SAVE regulation id=" + qiqiaoRealRegulationId);
                return false;
            }
            // save history and department
            if (!syncHistoryAndDepartment(qiqiaoRealRegulationId, variables, prettyValue)) {
                log.warn(prefix + "FAILED TO SAVE history id=" + qiqiaoRealRegulationId);
                return false;
            }
            // save parent
            if (!syncParent(qiqiaoRealRegulationId, variables, prettyValue)) {
                log.warn(prefix + "FAILED TO SAVE parent id=" + qiqiaoRealRegulationId);
                return false;
            }

            // save related
            if (!syncRelated(qiqiaoRealRegulationId, variables, prettyValue)) {
                log.warn(prefix + "FAILED TO SAVE related id=" + qiqiaoRealRegulationId);
                return false;
            }

            return true;
        }
    }

    private boolean syncRegulation(final String qiqiaoRealRegulationId, ZyRegulationBjmoa zyRegulationBjmoa,
        JSONObject variables, final JSONObject prettyValue) {

        zyRegulationBjmoa.setActive(1);

        final String code = variables.getString("制度编号");
        if (StringUtils.isEmpty(code)) {
            log.warn("CODE IS EMPTY id=" + qiqiaoRealRegulationId);
            return false;
        }
        zyRegulationBjmoa.setCode(code);
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

        final String qiqiaoCreatorId = variables.getString("制度跟进人");
        final String qiqiaoCreatorName = variables.getString("制度跟进人_pretty_value");
        if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(
            qiqiaoCreatorName) && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
            zyRegulationBjmoa.setQiqiaoCreatorId(qiqiaoCreatorId);
            zyRegulationBjmoa.setQiqiaoCreatorName(qiqiaoCreatorName);
        }
        zyRegulationBjmoa.setQiqiaoRegulationId(qiqiaoRealRegulationId);
        if (!saveOrUpdate(zyRegulationBjmoa)) {
            log.warn("FAILED TO SAVE OR UPDATE zyRegulationBjmoa=" + zyRegulationBjmoa);
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
            log.warn("PAGE IS NULL FOR historyRecordVO=" + historyRecordVO);
            return false;
        }
        final JSONArray historyRegulationArray = page.getJSONArray("list");
        log.info("historyRegulationArray: " + historyRegulationArray);

        List<JSONObject> historyRegulationList = toList(historyRegulationArray);
        historyRegulationList = historyRegulationList.stream()
            .sorted(Comparator.comparing(o -> (o.getJSONObject("variables").getString("发布日期"))))
            .collect(Collectors.toList());

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
            final String curMainDeptId = curVariables.getString("主责部门");
            final String curMainDeptName = curPrettyValue.getString("主责部门");
            if (StringUtils.isNotEmpty(curMainDeptId) && StringUtils.isNotEmpty(
                curMainDeptName) && !curMainDeptId.equals(curMainDeptName)) {
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
        {
            final String curVersion = latestPrettyValue.getString("制度版本");
            ZyRegulationBjmoaHistory zyRegulationBjmoaHistory;
            List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList =
                zyRegulationBjmoaHistoryService.queryByIdentifierAndVersion(identifier, curVersion);
            if (zyRegulationBjmoaHistoryList.size() == 0) {
                zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
            } else {
                zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0);
            }
            zyRegulationBjmoaHistory.setIdentifier(identifier);

            final String qiqiaoCreatorId = latestVariables.getString("制度跟进人");
            final String qiqiaoCreatorName = latestVariables.getString("制度跟进人_pretty_value");
            if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(
                qiqiaoCreatorName) && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
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
                log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            }

            zyRegulationBjmoaHistory.setContentFileId(latestVariables.getString("内管文件编号"));
            zyRegulationBjmoaHistory.setContentDocId(latestVariables.getString("内管文档编号"));

            zyRegulationBjmoaHistory.setVersion(curVersion);

            zyRegulationBjmoaHistory.setLevelId(latestVariables.getString("制度级别"));
            zyRegulationBjmoaHistory.setLevelName(latestPrettyValue.getString("制度级别"));
            zyRegulationBjmoaHistory.setLineId(latestVariables.getString("线路"));
            zyRegulationBjmoaHistory.setLineName(latestPrettyValue.getString("线路"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryId(latestVariables.getString("预案分类"));
            zyRegulationBjmoaHistory.setContingencyPlanCategoryName(latestPrettyValue.getString("预案分类"));
            zyRegulationBjmoaHistory.setCategoryId(latestVariables.getString("大类"));
            zyRegulationBjmoaHistory.setCategoryName(latestPrettyValue.getString("大类"));
            zyRegulationBjmoaHistory.setManagementCategoryId(latestVariables.getString("管理类别"));
            zyRegulationBjmoaHistory.setManagementCategoryName(latestPrettyValue.getString("管理类别"));
            zyRegulationBjmoaHistory.setSubCategoryId(latestVariables.getString("业务子类"));
            zyRegulationBjmoaHistory.setSubCategoryName(latestPrettyValue.getString("业务子类"));
            if (!zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory)) {
                log.warn("FAILED TO SAVE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            }

            // 保存主责部门

            final String curMainDeptId = latestVariables.getString("制度主责部门");
            if (StringUtils.isNotEmpty(curMainDeptId)) {
                final String curMainDeptName = latestVariables.getString("制度主责部门_pretty_value");
                if (curMainDeptId.equals(curMainDeptName)) {
                    log.warn(
                        "WEIRD DEPT NAME! qiqiaoRealRegulationId: " + qiqiaoRealRegulationId + ", curMainDeptName:" + curMainDeptName);
                } else {
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
        log.info("parentRegulationList: " + parentRegulationList);

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
                    log.warn("FAILED TO SAVE zyRelatedRegulationBjmoa=" + zyRelatedRegulationBjmoa);
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
                        log.warn("FAILED TO SAVE zyRelatedRegulationBjmoa=" + zyRelatedRegulationBjmoa);
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
        log.info("relatedRegulationList: " + relatedRegulationList);

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
                log.warn("FAILED TO SAVE zyRelatedRegulationBjmoa=" + zyRelatedRegulationBjmoa);
            }

            final JSONArray externalFileList = variables.getJSONArray("关联记录上传");

            ZyRegulationBjmoa zyRegulationBjmoa = queryByIdentifier(relatedIdentifier);
            if (zyRegulationBjmoa == null) {
                zyRegulationBjmoa = new ZyRegulationBjmoa();
            }
            zyRegulationBjmoa.setIdentifier(relatedIdentifier);
            zyRegulationBjmoa.setActive(isActive);
            zyRegulationBjmoa.setName(relatedName);
            zyRegulationBjmoa.setCode(relatedCode);
            zyRegulationBjmoa.setLevelId(levelId);
            zyRegulationBjmoa.setLevelName(levelName);
            zyRegulationBjmoa.setManagementCategoryId(latestVariables.getString("管理类别"));
            zyRegulationBjmoa.setManagementCategoryName(latestPrettyValue.getString("管理类别"));
            zyRegulationBjmoa.setSubCategoryId(latestVariables.getString("业务子类"));
            zyRegulationBjmoa.setSubCategoryName(latestPrettyValue.getString("业务子类"));
            zyRegulationBjmoa.setLineId(latestVariables.getString("线路"));
            zyRegulationBjmoa.setLineName(latestPrettyValue.getString("线路"));
            zyRegulationBjmoa.setCategoryId(latestVariables.getString("大类"));
            zyRegulationBjmoa.setCategoryName(latestPrettyValue.getString("大类"));
            if (StringUtils.isNotEmpty(qiqiaoCreatorId) && StringUtils.isNotEmpty(
                qiqiaoCreatorName) && !qiqiaoCreatorId.equals(qiqiaoCreatorName)) {
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
                    if (!saveOrUpdate(zyRegulationBjmoa)) {
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
                    // 保存主责部门
                    final String curMainDeptId = latestVariables.getString("制度主责部门");
                    final String curMainDeptName = latestPrettyValue.getString("制度主责部门");
                    if (StringUtils.isNotEmpty(curMainDeptId) && StringUtils.isNotEmpty(
                        curMainDeptName) && !curMainDeptId.equals(curMainDeptName)) {
                        List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList =
                            zyRegulationBjmoaDeptService.getByRegulationCodeAndVersion(relatedCode, relatedVersion);
                        ZyRegulationBjmoaDept zyRegulationBjmoaDept;
                        if (zyRegulationBjmoaDeptList.size() == 0) {
                            zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
                        } else {
                            zyRegulationBjmoaDept = zyRegulationBjmoaDeptList.get(0);
                        }
                        zyRegulationBjmoaDept.setCode(relatedCode);
                        zyRegulationBjmoaDept.setVersion(relatedVersion);
                        zyRegulationBjmoaDept.setName(relatedName);
                        zyRegulationBjmoaDept.setIdentifier(relatedIdentifier);
                        zyRegulationBjmoaDept.setQiqiaoDeptId(curMainDeptId);
                        zyRegulationBjmoaDept.setQiqiaoDeptName(curMainDeptName);

                        if (!zyRegulationBjmoaDeptService.saveOrUpdate(zyRegulationBjmoaDept)) {
                            log.warn("FAILED TO SAVE zyRegulationBjmoaDept=" + zyRegulationBjmoaDept);
                        }
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

    private void saveHeaderWrappedDocRegulation(final String qiqiaoRegulationId, final String downloadId,
        final ZyRegulationBjmoa zyRegulationBjmoa, final JSONObject variables, final String traceId) {
        log.info(traceId + " saveHeaderWrappedDocRegulation qiqiaoRegulationId: {}, downloadId: {}, zyRegulationBjmoa: {}, variables: {}", qiqiaoRegulationId,
            downloadId, zyRegulationBjmoa, variables);

        final String fileName = variables.getString("文件名称").trim();
        final String docId = zyRegulationBjmoa.getContentDocId();
        String fileId = zyRegulationBjmoa.getContentFileId();
        try {
            if (!contentManagementService.downloadConvertedFile(downloadId, fileName, null)) {
                log.warn(traceId + " FAILED DOWNLOAD FILE name: " + fileName + ", downloadId: " + downloadId);
                return;
            }

            final File newDocFile = new File(fileName);

            // 2. 上传套打文件到内管
            if (!newDocFile.exists()) {
                log.warn(traceId + " HEADER WRAPPED DOC FILE NOT EXIST: " + fileName);
                return;
            }
            final List<File> fileList = new ArrayList<>(1);
            fileList.add(newDocFile);
            final List<EcmFileDTO> ecmFileDtoList = contentManagementService.uploadFiles(fileList, docId);
            if (CollectionUtils.isEmpty(ecmFileDtoList)) {
                log.warn(traceId + " FAILED TO UPLOAD DOC FILE");
                return;
            }
            final EcmFileDTO ecmFileDTO = ecmFileDtoList.get(0);
            // 注意：这里更新了fileId
            fileId = ecmFileDTO.getFileId();
            zyRegulationBjmoa.setContentFileId(fileId);
            log.info(traceId + " update fileId, zyRegulationBjmoa: " + zyRegulationBjmoa);
            if (!updateById(zyRegulationBjmoa)) {
                log.warn(traceId + " FAILED TO UPDATE zyRegulationBjmoa=" + zyRegulationBjmoa);
                return;
            }
        } catch (Exception e) {
            log.error(traceId + " EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        } finally {
            final File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        }
        log.info(traceId + " HEADER-WRAPPED DOC SAVED SUCCESSFULLY");

        // 转换PDF文件
        log.info(traceId + " START PDF CONVERT");
        final String[] traceIdList = traceId.split("@");
        final String timePublishStatus = traceIdList[2];
        final String pdfConversionTaskId =
            bjmoaAppId + "@" + WpsOperateType.OFFICE_CONVERT + "@" + docId + "@" + fileId + "@" + qiqiaoRegulationId + "@" + timePublishStatus;
        log.info(traceId + " PDF CONVERT TASK ID: " + pdfConversionTaskId);
        final JSONObject pdfConversionTask = contentManagementService.queryTask(pdfConversionTaskId);
        final String pdfConversionDownloadId = pdfConversionTask.getString("download_id");
        if (StringUtils.isEmpty(pdfConversionDownloadId)) {
            // 回调接口中实现，存一条制度历史版本的记录（PDF文件）
            final WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
            wpsFormatDTO.setTask_id(pdfConversionTaskId);
            wpsFormatDTO.setScene_id(bjmoaAppId);
            // 注意：这里我们获取的文件最新版本的下载链接
            final String docUrl = contentManagementService.getDownloadNewestUrl(docId);
            wpsFormatDTO.setDoc_url(docUrl);
            wpsFormatDTO.setDoc_filename(variables.getString("文件名称").trim());
            wpsFormatDTO.setTarget_file_format("pdf");
            if (!contentManagementService.officeConvert(wpsFormatDTO)) {
                log.warn(traceId + " FAILED CONVERT " + wpsFormatDTO);
            }
        } else {
            // 如果之前已经转换过了
            savePdfRegulation(qiqiaoRegulationId, pdfConversionDownloadId, zyRegulationBjmoa, variables, traceId);
        }
    }

    private void savePdfRegulation(final String qiqiaoRegulationId, final String downloadId,
        final ZyRegulationBjmoa zyRegulationBjmoa, final JSONObject variables, final String traceId) {
        log.info(traceId
                + " savePdfRegulation: qiqiaoRegulationId: {}, downloadId: {}, zyRegulationBjmoa: {}, variables: {}",
            qiqiaoRegulationId, downloadId, zyRegulationBjmoa, variables);

        if (StringUtils.isEmpty(qiqiaoRegulationId) || StringUtils.isEmpty(downloadId) || zyRegulationBjmoa == null
            || variables == null) {
            return;
        }

        final String fileName = zyRegulationBjmoa.getName() + ".pdf";
        String contentFileId = null;
        String contentDocId = null;
        try {
            if (!contentManagementService.downloadConvertedFile(downloadId, fileName, null)) {
                log.warn(traceId + " FAILED DOWNLOAD FILE name: " + fileName + ", downloadId: " + downloadId);
                return;
            }

            final File pdfFile = new File(fileName);

            // 2. 上传PDF文件到内管
            if (!pdfFile.exists()) {
                log.warn(traceId + " PDF FILE NOT EXIST: " + fileName);
                return;
            }
            final List<File> fileList = new ArrayList<>(1);
            fileList.add(pdfFile);

            // 如果是版本更新，那么需要保持docId一致
            final String identifier = zyRegulationBjmoa.getIdentifier();
            final List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList =
                zyRegulationBjmoaHistoryService.queryByIdentifier(identifier);
            String docId = null;
            if (CollectionUtils.isNotEmpty(zyRegulationBjmoaHistoryList)) {
                log.info(traceId + " FOUND REGULATION HISTORY " + zyRegulationBjmoaHistoryList.get(0));
                docId = zyRegulationBjmoaHistoryList.get(0).getContentDocId();
            }
            final List<EcmFileDTO> ecmFileDtoList = contentManagementService.uploadFiles(fileList, docId);
            if (CollectionUtils.isEmpty(ecmFileDtoList)) {
                log.warn(traceId + " FAILED TO UPLOAD PDF FILE");
                return;
            }

            // 3. 存一条制度历史版本的记录
            final EcmFileDTO ecmFileDTO = ecmFileDtoList.get(0);
            ZyRegulationBjmoaHistory zyRegulationBjmoaHistory = new ZyRegulationBjmoaHistory();
            BeanUtils.copyProperties(zyRegulationBjmoa, zyRegulationBjmoaHistory);
            zyRegulationBjmoaHistory.setId(null);

            final String version = "A/" + (variables.getInteger("制度版本号") - 1);
            zyRegulationBjmoaHistory.setVersion(version);
//            zyRegulationBjmoaHistory.setPublishTime(variables.getDate("制度发布日期"));
            contentFileId = ecmFileDTO.getFileId();
            contentDocId = ecmFileDTO.getDocId();
            zyRegulationBjmoaHistory.setContentFileId(contentFileId);
            zyRegulationBjmoaHistory.setContentDocId(contentDocId);

            String code = zyRegulationBjmoa.getCode();
            List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList1 = zyRegulationBjmoaHistoryService.queryByIdentifierAndVersionAndCode(identifier, version, code);
            if (zyRegulationBjmoaHistoryList1 == null || zyRegulationBjmoaHistoryList1.size() == 0) {
                if (!zyRegulationBjmoaHistoryService.save(zyRegulationBjmoaHistory)) {
                    log.warn(traceId + " FAILED TO SAVE HISTORY: " + zyRegulationBjmoaHistory);
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
            } else {
                ZyRegulationBjmoaHistory zyRegulationBjmoaHistory1 = zyRegulationBjmoaHistoryList1.get(0);
                zyRegulationBjmoaHistory1.setContentFileId(contentFileId);
                zyRegulationBjmoaHistory1.setContentDocId(contentDocId);
                zyRegulationBjmoaHistoryMapper.updateById(zyRegulationBjmoaHistory1);
            }

            // 4. 存一条主责部门记录
            final String regulationDeptId = variables.getString("制度主责部门");
            final String regulationDeptName = variables.getString("制度主责部门_pretty_value");
            if (regulationDeptId == null || regulationDeptName == null || regulationDeptId.equals(regulationDeptName)) {
                log.warn(traceId + " CANNOT FIND REGULATION DEPT FOR REGULATION " + qiqiaoRegulationId
                    + ", regulationDeptId=" + regulationDeptId);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }

            final ZyRegulationBjmoaDept zyRegulationBjmoaDept = new ZyRegulationBjmoaDept();
            BeanUtils.copyProperties(zyRegulationBjmoaHistory, zyRegulationBjmoaDept);
            zyRegulationBjmoaDept.setQiqiaoDeptId(regulationDeptId);
            zyRegulationBjmoaDept.setQiqiaoDeptName(regulationDeptName);
            zyRegulationBjmoaDept.setId(null);

            List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList = zyRegulationBjmoaDeptMapper.getByIdentifierAndVersion(identifier, version);
            if (zyRegulationBjmoaDeptList == null || zyRegulationBjmoaDeptList.size() == 0) {
                if (!zyRegulationBjmoaDeptService.save(zyRegulationBjmoaDept)) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
            }
        } catch (Exception e) {
            log.error(traceId + " EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        } finally {
            final File pdfFile = new File(fileName);
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
        }

        log.info(traceId + " PDF REGULATION SAVED SUCCESSFULLY");

        // PDF转换完成后添加水印
        addWatermark(qiqiaoRegulationId, contentFileId, contentDocId, zyRegulationBjmoa, traceId);
    }

    private void addWatermark(final String qiqiaoRegulationId, final String contentFileId, final String contentDocId,
        final ZyRegulationBjmoa zyRegulationBjmoa, final String traceId) {
        log.info(traceId + " addWatermark qiqiaoRegulationId: {}, contentFileId: {}, contentDocId: {}, zyRegulationBjmoa: {}",
                qiqiaoRegulationId, contentFileId, contentDocId, zyRegulationBjmoa);

        // 添加PDF水印
        if (StringUtils.isEmpty(qiqiaoRegulationId) || StringUtils.isEmpty(traceId) || StringUtils
            .isEmpty(contentFileId) || StringUtils.isEmpty(contentDocId) || zyRegulationBjmoa == null) {
            return;
        }
        final String[] traceIdList = traceId.split("@");
        final String timePublishStatus = traceIdList[2];
        final String addWatermarkTaskId =
            bjmoaAppId + "@" + WpsOperateType.OFFICE_OPERATE + "@" + contentDocId + "@" + contentFileId + "@" + qiqiaoRegulationId + "@" +timePublishStatus;
        log.info(traceId + " ADD WATER MARK TASK ID: " + addWatermarkTaskId);
        final JSONObject addWatermarkTask = contentManagementService.queryTask(addWatermarkTaskId);
        final String fileName = zyRegulationBjmoa.getName() + ".pdf";
        String watermarkPdfDownloadId = null;
        if (addWatermarkTask != null && addWatermarkTask.containsKey("download_id")) {
            watermarkPdfDownloadId = addWatermarkTask.getString("download_id");
        }
        if (StringUtils.isEmpty(watermarkPdfDownloadId)) {
            final WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
            wpsFormatDTO.setTask_id(addWatermarkTaskId);
            wpsFormatDTO.setScene_id(bjmoaAppId);

            wpsFormatDTO.setDoc_url(contentManagementService.getDownloadNewestUrl(contentDocId));
            wpsFormatDTO.setDoc_filename(fileName);

            final List<Step> steps = new ArrayList<>();
            final Step step = new Step();
            step.setOperate(OperateConstant.OFFICE_WATERMARK);
            final TextWatermark textWatermark = new TextWatermark();
            textWatermark.setContent("北京市轨道交通运营管理有限公司");
            textWatermark.setTilt(true);
            textWatermark.setTransparent(0.45);

            final Arg args = new Arg();
            args.setText_watermark(textWatermark);
            step.setArgs(args);
            steps.add(step);

            wpsFormatDTO.setSteps(steps);

            if (!contentManagementService.officeOperate(wpsFormatDTO)) {
                log.warn(traceId + " FAILED ADD WATERMARK " + wpsFormatDTO);
            }
        } else {
            // 如果之前已经添加过水印
            saveWatermarkPdfRegulation(qiqiaoRegulationId, watermarkPdfDownloadId, zyRegulationBjmoa, traceId);
        }
    }

    private void saveWatermarkPdfRegulation(final String qiqiaoRegulationId, final String downloadId,
        final ZyRegulationBjmoa zyRegulationBjmoa, final String traceId) {
        log.info(traceId + " saveWatermarkPdfRegulation qiqiaoRegulationId: {}, downloadId: {}, zyRegulationBjmoa: {}", qiqiaoRegulationId, downloadId,
            zyRegulationBjmoa);

        if (StringUtils.isEmpty(qiqiaoRegulationId) || StringUtils.isEmpty(traceId) || StringUtils.isEmpty(downloadId)
            || zyRegulationBjmoa == null) {
            return;
        }

        final String fileName = zyRegulationBjmoa.getName() + ".pdf";
        try {
            if (!contentManagementService.downloadConvertedFile(downloadId, fileName, null)) {
                log.warn(traceId + " FAILED DOWNLOAD FILE name: " + fileName + ", downloadId: " + downloadId);
                return;
            }

            final File watermarkPdfFile = new File(fileName);

            // 上传带水印PDF文件到内管
            if (!watermarkPdfFile.exists()) {
                log.warn(traceId + " WATERMARK PDF FILE NOT EXIST: " + fileName);
                return;
            }
            final List<File> fileList = new ArrayList<>(1);
            fileList.add(watermarkPdfFile);
            final List<EcmFileDTO> ecmFileDtoList = contentManagementService.uploadFiles(fileList);
            if (CollectionUtils.isEmpty(ecmFileDtoList)) {
                log.warn(traceId + " FAILED TO UPLOAD WATERMARK PDF FILE");
                return;
            }

            final EcmFileDTO ecmFileDTO = ecmFileDtoList.get(0);
            zyRegulationBjmoa.setWatermarkPdfContentFileId(ecmFileDTO.getFileId());
            zyRegulationBjmoa.setWatermarkPdfContentDocId(ecmFileDTO.getDocId());
            if (!updateById(zyRegulationBjmoa)) {
                log.warn(traceId + " FAILED TO UPDATE zyRegulationBjmoa=" + zyRegulationBjmoa);
                return;
            }
        } catch (Exception e) {
            log.error(traceId + " EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        } finally {
            final File watermarkPdfFile = new File(fileName);
            if (watermarkPdfFile.exists()) {
                watermarkPdfFile.delete();
            }
        }

        log.info(traceId + " WATER MARK PDF REGULATION SAVED SUCCESSFULLY");

        // 推送OA前最终制度文件确认
        ZyRegulationBjmoa zyRegulationBjmoa1 = zyRegulationBjmoaMapper.queryByQiqiaoRegulationId(qiqiaoRegulationId);
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        Map<String, Object> data = new HashMap<>();
        data.put("最终制度文件名称", fileName);
        data.put("最终制度内管文件编号", zyRegulationBjmoa1.getWatermarkPdfContentFileId());
        data.put("最终制度内管文档编号", zyRegulationBjmoa1.getWatermarkPdfContentDocId());
        // 如果制度类型是党群及廉政建设管理类制度，且最终制度名称和制度文件名称不一致，修改七巧制度文件名称为最终制度名称
        if ("2".equals(zyRegulationBjmoa1.getCategoryId())) {
            JSONObject record = qiqiaoFormsService.queryById(recordVO);
            if (record == null) {
                log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
                return;
            }
            JSONObject variables = record.getJSONObject("variables");
            if (variables == null) {
                log.warn(traceId + " CANNOT FIND VARIABLES FOR REGULATION " + qiqiaoRegulationId);
                return;
            }
            String qiqiaoFileName = variables.getString("文件名称");
            int index = qiqiaoFileName.lastIndexOf(".");
            if (index == -1) {
                log.warn(traceId + " INCORRECT REGULATION FILE NAME FORMAT");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return;
            }
            String filePrefix = qiqiaoFileName.substring(0, index);
            String fileSuffix = qiqiaoFileName.substring(index);
            String regulationName = zyRegulationBjmoa1.getName();
            // 如果最终制度名称和制度文件名称不一致，修改七巧制度文件名称为最终制度名称
            if (!filePrefix.equals(regulationName)) {
                String newFileName = regulationName + fileSuffix;
                // 更新七巧端制度文件信息
                data.put("文件名称", newFileName);
                data.put("内管文件编号", zyRegulationBjmoa1.getContentFileId());
            }
        }
        recordVO.setData(data);
        JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(recordVO);
        log.info(traceId + " push final regulation file to qiqiao, jsonObject: {}", jsonObject);
    }

    private List<ZyRegulationBjmoaVO> postProcess(final RegulationQueryDTO queryDTO,
        final List<ZyRegulationBjmoaVO> zyRegulationBjmoaVoList) {
        if (CollectionUtils.isEmpty(zyRegulationBjmoaVoList)) {
            return new ArrayList<>();
        }

        for (final ZyRegulationBjmoaVO zyRegulationBjmoaVo : zyRegulationBjmoaVoList) {
            final String contentFileId = zyRegulationBjmoaVo.getContentFileId();
            zyRegulationBjmoaVo.setPreviewUrl(contentManagementService.getPreviewUrl(contentFileId));
            zyRegulationBjmoaVo.setDownloadUrl(contentManagementService.getDownloadUrl(contentFileId));

            final List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList =
                zyRegulationBjmoaDeptService.getByRegulationCodeAndVersion(zyRegulationBjmoaVo.getCode(),
                    zyRegulationBjmoaVo.getVersion());
            zyRegulationBjmoaVo.setDeptList(
                zyRegulationBjmoaDeptList.stream().map(ZyRegulationBjmoaDept::getQiqiaoDeptName)
                    .collect(Collectors.toList()));

            if (queryDTO != null) {
                final Map<String, String> docId2Content = queryDTO.getDocId2Content();
                if (MapUtils.isNotEmpty(docId2Content)) {
                    zyRegulationBjmoaVo.setFragment(docId2Content.get(zyRegulationBjmoaVo.getContentDocId()));
                }
            }
        }
        return zyRegulationBjmoaVoList;
    }

    private RegulationQueryDTO preprocessQueryDTO(RegulationQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new RegulationQueryDTO();
            return queryDTO;
        }
        final String searchMessage = queryDTO.getSearchMessage();

        if (StringUtils.isNotEmpty(searchMessage)) {
            final PageModel<FileModel> pageModel =
                contentManagementService.searchNew(1, Integer.MAX_VALUE, searchMessage);
            if (pageModel == null || pageModel.getTotal() == 0) {
                log.warn("[preprocessQueryDTO] NO FILE FOUND queryDTO: " + queryDTO);
                return null;
            }

            // 获取所有的制度编号
            final List<FileModel> records = pageModel.getRecords();
            queryDTO.setDocIdList(records.stream().map(FileModel::getDocId).distinct().collect(Collectors.toList()));

            final Map<String, String> docId2Version = new HashMap<>();
            final Map<String, String> docId2Content = new HashMap<>();
            for (final FileModel record : records) {
                final String docId = record.getDocId();
                if (docId2Version.containsKey(docId)) {
                    final String curVersion = docId2Version.get(docId);

                    if (curVersion.compareTo(record.getVersion()) < 0) {
                        docId2Version.put(docId, record.getVersion());

                        if (record.getContent().contains("<span")) {
                            docId2Content.put(docId, record.getContent());
                        } else {
                            docId2Content.put(docId, record.getTitle());
                        }
                    }
                } else {
                    docId2Version.put(docId, record.getVersion());
                    if (record.getContent().contains("<span")) {
                        docId2Content.put(docId, record.getContent());
                    } else {
                        docId2Content.put(docId, record.getTitle());
                    }
                }
            }
            queryDTO.setDocId2Version(docId2Version);
            queryDTO.setDocId2Content(docId2Content);
        }

        final List<String> deptIdList = queryDTO.getDeptIdList();
        if (CollectionUtils.isNotEmpty(deptIdList)) {
            final List<ZyRegulationBjmoaDept> zyRegulationBjmoaDeptList =
                zyRegulationBjmoaDeptService.getByQiqiaoDeptIdList(deptIdList);

            if (CollectionUtils.isEmpty(zyRegulationBjmoaDeptList)) {
                log.warn("[preprocessQueryDTO] NO zyRegulationBjmoaDept FOUND queryDTO: " + queryDTO);
                return null;
            }

            final List<String> codeList =
                zyRegulationBjmoaDeptList.stream().map(ZyRegulationBjmoaDept::getCode).distinct()
                    .collect(Collectors.toList());
            queryDTO.setCodeList(codeList);
        }

        return queryDTO;
    }

    private ZyRelatedRegulationVO generateRelatedRegulationVO(final String regulationType, final String identifier,
        final String regulationName) {
        final ZyRelatedRegulationVO relatedRegulationVO = new ZyRelatedRegulationVO();
        relatedRegulationVO.setType(regulationType);
        relatedRegulationVO.setIdentifier(identifier);
        if (regulationType.equals(RegulationType.INTERNAL) || regulationType.equals(RegulationType.RELATED)) {
            final ZyRegulationBjmoa zyRegulationBjmoaInternalOrRelated =
                zyRegulationBjmoaMapper.queryByIdentifier(identifier);
            if (zyRegulationBjmoaInternalOrRelated == null) {
                return null;
            }
            relatedRegulationVO.setId(zyRegulationBjmoaInternalOrRelated.getId());

            relatedRegulationVO.setName(zyRegulationBjmoaInternalOrRelated.getName());
            final String relatedRegulationDownloadUrl =
                contentManagementService.getDownloadUrl(zyRegulationBjmoaInternalOrRelated.getContentFileId());
            relatedRegulationVO.setDownloadUrl(relatedRegulationDownloadUrl);
        } else if (regulationType.equals(RegulationType.EXTERNAL)) {
            relatedRegulationVO.setName(regulationName);
            RecordVO downloadRecordVO = new RecordVO();
            downloadRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
            downloadRecordVO.setFileId(identifier);
            final String relatedRegulationDownloadUrl = qiqiaoFormsService.getDownloadUrl(downloadRecordVO);
            relatedRegulationVO.setDownloadUrl(relatedRegulationDownloadUrl);
        }
        return relatedRegulationVO;
    }

    @Override public List<ZyRegulationBjmoaStatisticsVO> queryRegulationStatistics(Integer year) {
        List<ZyRegulationBjmoaStatisticsVO> zyRegulationBjmoaStatisticsVO =
            zyRegulationBjmoaMapper.queryRegulationStatistics(year);
        return zyRegulationBjmoaStatisticsVO;
    }

    @Override public ZyRegulationTempBjmoaVO tempQueryById(final String id, final String mark) {
        if (id == null) {
            return null;
        }

        //  查询临时技术变更表单
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(tempTechnicalChangeRegulationModelId);
        recordVO.setId(id);
        final JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn("CANNOT FIND TEMP RECORD WITH QIQIAO ID " + id);
            return null;
        }
        JSONObject variables = record.getJSONObject("variables");
        JSONObject prettyValue = record.getJSONObject("prettyValue");
        String name = variables.getString("临时技术变更名称");
        String creator = prettyValue.getString("发起人");
        String createDepartment = prettyValue.getString("发起部门");
        String jointDepartment = prettyValue.getString("联合发文部门");
        String receiveDepartment = prettyValue.getString("收文部门");
        String code = variables.getString("变更编号");
        Date effectiveTime = variables.getDate("临时技术变更开始时间");
        Date dueTime = variables.getDate("临时技术变更到期时间");
        List<ZyRegulationBjmoa> relatedRegulationList = new ArrayList<>();
        //获得关联制度名称
        {
            RecordVO relatedRecordVO = new RecordVO();
            relatedRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
            relatedRecordVO.setFormModelId(tempRelatedRegulationId);
            List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            FieldFilter fieldFilter = new FieldFilter();
            fieldFilter.setFieldName("外键");
            fieldFilter.setLogic("eq");
            fieldFilter.setValue(id);
            fieldFilterList.add(fieldFilter);
            relatedRecordVO.setFilter(fieldFilterList);
            final JSONObject page = qiqiaoFormsService.page(relatedRecordVO);
            final JSONArray relatedRegulationBjmoaList = page.getJSONArray("list");
            log.info("relatedRegulationBjmoaList: " + relatedRegulationBjmoaList);
            for (int i = 0; i < relatedRegulationBjmoaList.size(); ++i) {
                final JSONObject realRegulationJson = relatedRegulationBjmoaList.getJSONObject(i);
                final String relatedRegulationPublishID =
                    realRegulationJson.getJSONObject("variables").getString("制度名称");
                RecordVO relatedPublishRecordVO = new RecordVO();
                relatedPublishRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
                relatedPublishRecordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
                relatedPublishRecordVO.setId(relatedRegulationPublishID);
                final JSONObject recordPublish = qiqiaoFormsService.queryById(relatedPublishRecordVO);
                final String relatedRegulationIdentifier =
                    recordPublish.getJSONObject("variables").getString("制度系统标识别文本");
                if (StringUtils.isNotEmpty(relatedRegulationIdentifier)) {
                    final ZyRegulationBjmoa relatedRegulation = queryByIdentifier(relatedRegulationIdentifier);
                    if (relatedRegulation != null) {
                        relatedRegulationList.add(relatedRegulation);
                    }
                }
            }
        }

        final ZyRegulationTempBjmoaVO result = new ZyRegulationTempBjmoaVO();
        result.setId(id);
        result.setName(name);
        result.setCreator(creator);
        result.setCreateDepartment(createDepartment);
        result.setJointDepartment(jointDepartment);
        result.setReceiveDepartment(receiveDepartment);
        result.setCode(code);
        result.setEffectiveTime(effectiveTime);
        result.setDueTime(dueTime);
        result.setRelatedRegulationList(relatedRegulationList);
        result.setPreviewUrl(contentManagementService.getPreviewUrl(variables.getString("内管文件编号"), mark));
        return result;
    }

    private List<Sample> getSampleList(final JSONObject variables) {
        log.info("getSampleList: variables=" + variables);
        if (variables == null) {
            log.warn("EMPTY INPUT");
            return null;
        }

        final String categoryId = variables.getString("大类");
        if (StringUtils.isEmpty(categoryId)) {
            log.warn("EMPTY CATEGORY ID");
            return null;
        }

        final List<Sample> result = new ArrayList<>();
        switch (categoryId) {
            case "1":
            case "2": {
                // 董事会管理制度、党群管理制度：无需套打
                final Sample sample = new Sample();
                sample.setBookmark("JUST SOME NONSENSE");
                sample.setType(SampleType.TEXT.toString());
                sample.setText("");
                result.add(sample);
                break;
            }
            case "3":
            case "4": {
                // 经营层制度、应急预案：需制度系统套打实施日期及批准人电子签名
                //delete 实施日期
                //                final Date executeDate = variables.getDate("制度实施日期");
                //                if (executeDate == null) {
                //                    log.warn("executeDate IS EMPTY");
                //                    return null;
                //                }
                //                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
                //                final String executeDateStr = dateFormat.format(executeDate);

                //                // 首页实施日期
                //                if (StringUtils.isNotEmpty(executeDateStr)) {
                //                    final Sample sample = new Sample();
                //                    sample.setBookmark("首页实施日期");
                //                    sample.setType(SampleType.TEXT.toString());
                //                    sample.setText(executeDateStr);
                //                    result.add(sample);
                //                }

                // 批准人签名
                {
                    final Sample sample = new Sample();
                    sample.setBookmark("批准人签名");
                    sample.setType(SampleType.IMAGE.toString());

                    RecordVO tmpRecordVO = new RecordVO();
                    tmpRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
                    tmpRecordVO.setFormModelId(signatureFormModelId);
                    final List<FieldFilter> fieldFilterList = new ArrayList<>(1);
                    final FieldFilter fieldFilter = new FieldFilter();
                    fieldFilter.setFieldName("人员");
                    fieldFilter.setLogic("eq");

                    final String meinFuehrer = variables.getString("最高决策机构负责人");
                    if (StringUtils.isEmpty(meinFuehrer)) {
                        log.warn("WER IST MEIN FUEHRER DENN?!");
                        return null;
                    }
                    fieldFilter.setValue(meinFuehrer);
                    fieldFilterList.add(fieldFilter);
                    tmpRecordVO.setFilter(fieldFilterList);
                    final JSONObject records = qiqiaoFormsService.page(tmpRecordVO);
                    if (records == null) {
                        log.warn("CANNOT FIND SIGNATURE INFO RECORDS");
                        return null;
                    }

                    final JSONArray list = records.getJSONArray("list");
                    if (CollectionUtils.isEmpty(list)) {
                        return null;
                    }
                    final JSONObject signatureRecord = list.getJSONObject(0);
                    final JSONObject signatureVariables = signatureRecord.getJSONObject("variables");
                    sample.setSample_filename(signatureVariables.getString("文件名称").trim());
                    final String signatureDownloadUrl =
                        contentManagementService.getDownloadUrl(signatureVariables.getString("内管文件编号"));
                    sample.setSample_url(signatureDownloadUrl);
                    result.add(sample);
                }

                break;
            }
            default: {
                log.warn("WAS IST DAS DENN?");
                return null;
            }
        }

        return result;
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

        final RecordVO updateRecordVO = new RecordVO();
        updateRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        updateRecordVO.setFormModelId(bjmoaRealRegulationInfoFormModelId);
        updateRecordVO.setId(qiqiaoRealRegulationId);
        final Map<String, Object> data = new HashMap<>(2);
        data.put("制度系统标识别文本", identifier);
        updateRecordVO.setData(data);

        log.info("updateRecordVO: " + updateRecordVO);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(updateRecordVO);
        log.info("qiqiaoFormsService.saveOrUpdate: " + jsonObject);
        return identifier;
    }

    @Override
    public void replaceRegulationFile(String qiqiaoRegulationId, String contentFileId, String contentDocId) {
        if (StringUtils.isEmpty(qiqiaoRegulationId) || StringUtils.isEmpty(contentFileId) || StringUtils.isEmpty(contentDocId)) {
            return;
        }

        final String timeStamp = DateUtils.getDate("yyyyMMddHHmmss");
        String traceId = "bjmoa_replace_regulation_file" + "@" + qiqiaoRegulationId + "@" + timeStamp;
        log.info(traceId + " replaceRegulationFile qiqiaoRegulationId: {}, contentFileId: {}, contentDocId: {}",
                qiqiaoRegulationId, contentFileId, contentDocId);

        // 更新数据库中制度正文
        ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaMapper.queryByQiqiaoRegulationId(qiqiaoRegulationId);
        zyRegulationBjmoa.setContentFileId(contentFileId);
        zyRegulationBjmoa.setContentDocId(contentDocId);
        if (!updateById(zyRegulationBjmoa)) {
            log.warn(traceId + " FAILED TO UPDATE zyRegulationBjmoa=" + zyRegulationBjmoa);
            return;
        }

        // 更新七巧端制度正文
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        final JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        JSONObject variables = record.getJSONObject("variables");
        final JSONObject prettyValue = record.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn(traceId + " CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }
        String fileName = variables.getString("文件名称").trim();
        Map<String, Object> data = new HashMap<>(2);
        data.put("内管文件编号", contentFileId);
        data.put("内管文档编号", contentDocId);
        recordVO.setData(data);
        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(recordVO);
        log.info(traceId + " qiqiaoFormsService.saveOrUpdate: " + jsonObject);

        // 继续走转PDF流程
        log.info(traceId + " START PDF CONVERT");
        final String docId = contentDocId;
        final String fileId = contentFileId;
        final String pdfConversionTaskId =
                bjmoaAppId + "@" + WpsOperateType.OFFICE_CONVERT + "@" + docId + "@" + fileId + "@" + qiqiaoRegulationId
                        + "@" + timeStamp;
        log.info(traceId + " PDF CONVERT TASK ID: " + pdfConversionTaskId);
        final JSONObject pdfConversionTask = contentManagementService.queryTask(pdfConversionTaskId);
        final String pdfConversionDownloadId = pdfConversionTask.getString("download_id");
        if (StringUtils.isEmpty(pdfConversionDownloadId)) {
            // 回调接口中实现，存一条制度历史版本的记录（PDF文件）
            final WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
            wpsFormatDTO.setTask_id(pdfConversionTaskId);
            wpsFormatDTO.setScene_id(bjmoaAppId);
            // 注意：这里我们获取的文件最新版本的下载链接
            final String docUrl = contentManagementService.getDownloadNewestUrl(docId);
            wpsFormatDTO.setDoc_url(docUrl);
            wpsFormatDTO.setDoc_filename(fileName);
            wpsFormatDTO.setTarget_file_format("pdf");
            if (!contentManagementService.officeConvert(wpsFormatDTO)) {
                log.warn(traceId + " FAILED PDF CONVERT " + wpsFormatDTO);
            }
        } else {
            // 如果之前已经转换过了
            savePdfRegulation(qiqiaoRegulationId, pdfConversionDownloadId, zyRegulationBjmoa, variables, traceId);
        }
    }

    @Override
    public void replacePDFRegulationFile(String qiqiaoRegulationId) {
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            return;
        }
        String timeStamp = DateUtils.getDate("yyyyMMddHHmmss");
        String traceId = "bjmoa_replace_pdf_regulation_file" + "@" + qiqiaoRegulationId + "@" + timeStamp;
        log.info(traceId + " replacePDFRegulationFile qiqiaoRegulationId: {}", qiqiaoRegulationId);

        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(pdfRegulationFileFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        JSONObject variables = record.getJSONObject("variables");
        if (variables == null) {
            log.warn(traceId + " CANNOT FIND VARIABLES FOR REGULATION " + qiqiaoRegulationId);
            return;
        }
        String regulationIdentifier = variables.getString("新建制度唯一标识");
        if ("2".equals(variables.getString("制度建设类型"))) {
            regulationIdentifier = variables.getString("修订制度唯一标识");
        }
        if (StringUtils.isEmpty(regulationIdentifier)) {
            log.warn(traceId + " CANNOT FIND regulationIdentifier!");
            return;
        }
        String code = variables.getString("制度编号");
        String version = "A/" + (variables.getInteger("制度版本号") - 1);
        String contentFileId = variables.getString("pdf制度内管文件编号");
        String contentDocId = variables.getString("pdf制度内管文档编号");

        // 更新数据库中PDF制度正文
        ZyRegulationBjmoaHistory zyRegulationBjmoaHistory;
        List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryMapper.queryByIdentifierAndVersionAndCode(regulationIdentifier, version, code);
        if (zyRegulationBjmoaHistoryList.size() == 0) {
            log.warn(traceId + " CANNOT FIND zyRegulationBjmoaHistory WITH IDENTIFIER " + regulationIdentifier + " VERSION " + version + " CODE " + code);
            return;
        } else {
            zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0);
        }

        zyRegulationBjmoaHistory.setContentFileId(contentFileId);
        zyRegulationBjmoaHistory.setContentDocId(contentDocId);

        if (!zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory)) {
            log.warn(traceId + " FAILED TO UPDATE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            return;
        }

        // 继续走加水印流程
        log.info(traceId + " START FROM WATERMARK");
        String qiqiaoinfoRegulationId = variables.getString("请选择制度");
        ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaMapper.queryByQiqiaoRegulationId(qiqiaoinfoRegulationId);
        addWatermark(qiqiaoinfoRegulationId, contentFileId, contentDocId, zyRegulationBjmoa, traceId);
    }

    @Override
    public void replaceFinalPDFRegulationFile(String qiqiaoRegulationId) {
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            return;
        }
        String timeStamp = DateUtils.getDate("yyyyMMddHHmmss");
        String traceId = "bjmoa_replace_final_pdf_regulation_file" + "@" + qiqiaoRegulationId + "@" + timeStamp;
        log.info(traceId + " replaceFinalPDFRegulationFile qiqiaoRegulationId: {}", qiqiaoRegulationId);

        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn(traceId + " CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        JSONObject variables = record.getJSONObject("variables");
        if (variables == null) {
            log.warn(traceId + " CANNOT FIND VARIABLES FOR REGULATION " + qiqiaoRegulationId);
            return;
        }
        String regulationIdentifier = variables.getString("制度唯一标示");
        if ("2".equals(variables.getString("制度建设类型"))) {
            regulationIdentifier = variables.getString("制度唯一标识文本");
        }
        if (StringUtils.isEmpty(regulationIdentifier)) {
            log.warn(traceId + " CANNOT FIND regulationIdentifier!");
            return;
        }
        String code = variables.getString("制度编号");
        String version = "A/" + (variables.getInteger("制度版本号") - 1);
        String contentFileId = variables.getString("pdf制度内管文件编号");
        String contentDocId = variables.getString("pdf制度内管文档编号");

        // 更新数据库中PDF制度正文
        ZyRegulationBjmoaHistory zyRegulationBjmoaHistory;
        List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryMapper.queryByIdentifierAndVersionAndCode(regulationIdentifier, version, code);
        if (zyRegulationBjmoaHistoryList.size() == 0) {
            log.warn(traceId + " CANNOT FIND zyRegulationBjmoaHistory WITH IDENTIFIER " + regulationIdentifier + " VERSION " + version + " CODE " + code);
            return;
        } else {
            zyRegulationBjmoaHistory = zyRegulationBjmoaHistoryList.get(0);
        }

        zyRegulationBjmoaHistory.setContentFileId(contentFileId);
        zyRegulationBjmoaHistory.setContentDocId(contentDocId);

        if (!zyRegulationBjmoaHistoryService.saveOrUpdate(zyRegulationBjmoaHistory)) {
            log.warn(traceId + " FAILED TO UPDATE zyRegulationBjmoaHistory=" + zyRegulationBjmoaHistory);
            return;
        }

        // 继续走加水印流程
        log.info(traceId + " START FROM WATERMARK");
        ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaMapper.queryByQiqiaoRegulationId(qiqiaoRegulationId);
        addWatermark(qiqiaoRegulationId, contentFileId, contentDocId, zyRegulationBjmoa, traceId);
    }

    @Override
    public JSONObject qiqiaoCallback(String taskId, Map data) {
        return qiqiaoCallBackService.callBack(bjmoaRegulationInfoApplicationId, taskId, data);
    }
}