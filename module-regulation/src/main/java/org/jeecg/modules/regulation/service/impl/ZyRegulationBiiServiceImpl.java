package org.jeecg.modules.regulation.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.common.constant.ApplicationProfile;
import org.jeecg.modules.common.utils.ProxyUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.constant.WpsOperateType;
import org.jeecg.modules.content.dto.EcmFileDTO;
import org.jeecg.modules.content.dto.FileModel;
import org.jeecg.modules.content.dto.PageModel;
import org.jeecg.modules.content.dto.WpsFormatDTO;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.publicManagement.service.IPublicManagementService;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoDepartmentService;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.jeecg.modules.regulation.dto.RegulationFileOld;
import org.jeecg.modules.regulation.dto.RegulationOld;
import org.jeecg.modules.regulation.dto.RegulationQueryDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationBii;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiDept;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiHistory;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulation;
import org.jeecg.modules.regulation.mapper.ZyRegulationBiiMapper;
import org.jeecg.modules.regulation.service.*;
import org.jeecg.modules.regulation.vo.ZyRegulationBiiHistoryVO;
import org.jeecg.modules.regulation.vo.ZyRegulationBiiVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Service @Slf4j public class ZyRegulationBiiServiceImpl extends ServiceImpl<ZyRegulationBiiMapper, ZyRegulationBii>
    implements IZyRegulationBiiService {
    @Autowired private ZyRegulationBiiMapper zyRegulationBiiMapper;
    @Autowired private IZyRegulationBiiHistoryService zyRegulationBiiHistoryService;
    @Autowired private IZyRegulationBiiDeptService zyRegulationBiiDeptService;
    @Autowired private IZyRelatedRegulationService zyRelatedRegulationService;
    @Autowired @Qualifier("biiContentManagementService") private IContentManagementService contentManagementService;
    @Autowired private IPublicManagementService publicManagementService;

    @Autowired private IQiqiaoService qiqiaoService;
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;
    @Autowired private IQiqiaoDepartmentService qiqiaoDepartmentService;

    @Autowired @Lazy private RegulationAiService regulationAiService;
    @Value("${spring.profiles.active}") private String profile;
    @Value("${content-management.biiAppId}") private String biiAppId;
    @Value("${chat-service.knowledge_base.file_path}") private String regulationDirectory;
    @Value("${biisaas.biiRegulationInfo.applicationId}") private String biiRegulationInfoApplicationId;
    @Value("${biisaas.biiRegulationInfo.formModelId}") private String biiRegulationInfoFormModelId;
    @Value("${biisaas.biiRegulationInfo.parentRegulationFormModelId}") private String
        biiParentRegulationInfoFormModelId;
    @Value("${biisaas.biiRegulationInfo.realFormModelId}") private String biiRealRegulationInfoFormModelId;
    @Value("${biisaas.biiRegulationInfo.realHistoryFormModelId}") private String
        biiRealHistoryRegulationInfoFormModelId;

    @Override public void syncOldRegulationList() {
        final List<RegulationOld> regulationOldList = getRegulationOldList();
        log.info("Start to sync regulationOldList (size: " + regulationOldList.size() + ")");

        // 是否需要重新建立聊天机器人索引
        Set<String> fileIdentifiersToAdd = new HashSet<>();
        Set<String> fileIdentifiersToUpdate = new HashSet<>();
        Set<String> fileIdentifiersToDelete = new HashSet<>();
        Set<String> currentRegulationCodeSet = new HashSet<>(regulationOldList.size());
        for (final RegulationOld regulationOld : regulationOldList) {
            final List<RegulationOld> regulationOldDetail = getRegulationOldDetail(regulationOld.getId());
            if (CollectionUtils.isEmpty(regulationOldDetail)) {
                continue;
            }

            final RegulationOld curRegulationOldDetail = regulationOldDetail.get(0);
            currentRegulationCodeSet.add(curRegulationOldDetail.getId());

            final List<String> recordList = curRegulationOldDetail.getRecordList();
            if (CollectionUtils.isEmpty(recordList)) {
                log.warn("recordList is empty for regulationOld " + regulationOld);
                continue;
            }
            final String identifier = recordList.get(0);
            final String code = curRegulationOldDetail.getId();
            // 判断是否需要更新
            final ZyRegulationBii oldRegulationRecord =
                lambdaQuery().eq(ZyRegulationBii::getIdentifier, identifier).eq(ZyRegulationBii::getCode, code).one();
            if (oldRegulationRecord != null) {
                // 判断是否是active
                if (oldRegulationRecord.getActive() == 0) {
                    // 需要重新activate一下
                    oldRegulationRecord.setActive(1);
                    if (!updateById(oldRegulationRecord)) {
                        log.warn("FAILED ACTIVATE oldRegulationRecord=" + oldRegulationRecord);
                        fileIdentifiersToUpdate.add(identifier);
                    }
                } else {
                    log.warn("NO NEED TO UPDATE identifier " + identifier + " code " + code);
                    updateSpecialAuditTime(curRegulationOldDetail);
                }

                continue;
            }

            final String title = curRegulationOldDetail.getTitle();
            if (StringUtils.isNotEmpty(title) && !title.contains("京投公司管理制度汇编")) {
                fileIdentifiersToAdd.add(identifier);
            }
            if (!syncRegulationOld(curRegulationOldDetail)) {
                log.warn("FAILED TO SYNC REGULATION OLD " + regulationOld);
                continue;
            }
        }

        // 查看当前数据库的记录
        final List<ZyRegulationBii> activeRegulationList = lambdaQuery().eq(ZyRegulationBii::getActive, 1).list();
        if (CollectionUtils.isNotEmpty(activeRegulationList)) {
            for (final ZyRegulationBii zyRegulationBii : activeRegulationList) {
                final String code = zyRegulationBii.getCode();

                // @todo 如果之后七巧也需要把旧系统的所有制度存储一份，那么这里的判断逻辑需要修改
                if (zyRegulationBii.getQiqiaoRegulationId() == null && !currentRegulationCodeSet.contains(code)) {
                    fileIdentifiersToDelete.add(zyRegulationBii.getIdentifier());
                    if (inactivateById(zyRegulationBii.getId()) <= 0) {
                        log.warn("FAILED TO INACTIVATE " + zyRegulationBii);
                    } else {
                        log.info("SUCCEEDED TO INACTIVE " + zyRegulationBii);
                    }
                }
            }
        }

        rebuildIndex(fileIdentifiersToAdd, fileIdentifiersToUpdate, fileIdentifiersToDelete);
    }

    private void prepareFilesForKnowledgeBase(Set<String> fileIdentifiersToAdd, Set<String> fileIdentifiersToUpdate,
        Set<String> fileIdentifiersToDelete) {
        log.info(
            "[prepareFilesForKnowledgeBase] fileIdentifiersToAdd: " + fileIdentifiersToAdd + ", fileIdentifiersToUpdate: " + fileIdentifiersToUpdate + ", fileIdentifiersToDelete:" + fileIdentifiersToDelete);
        if (fileIdentifiersToAdd == null) {
            fileIdentifiersToAdd = new HashSet<>();
        }
        if (fileIdentifiersToUpdate == null) {
            fileIdentifiersToUpdate = new HashSet<>();
        }
        if (fileIdentifiersToDelete == null) {
            fileIdentifiersToDelete = new HashSet<>();
        }
        if (fileIdentifiersToAdd.isEmpty() && fileIdentifiersToUpdate.isEmpty() && fileIdentifiersToDelete.isEmpty()) {
            log.info("NO NEED TO UPDATE");
            return;
        }

        File directory = new File(regulationDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            log.warn("CANNOT FIND VALID DIRECTORY UNDER " + regulationDirectory);
            return;
        }

        // 删除文件
        final File[] files = directory.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.exists()) {
                    final String name = file.getName();
                    final String[] s = name.split("_");
                    if (fileIdentifiersToUpdate.contains(s[0]) || fileIdentifiersToDelete.contains(s[0])) {
                        log.info("Delete file " + name);
                        if (!file.delete()) {
                            log.warn("FAILED TO DELETE FILE " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }

        // 添加文件
        Set<String> tmpFileIdentifiers = new HashSet<>();
        tmpFileIdentifiers.addAll(fileIdentifiersToAdd);
        tmpFileIdentifiers.addAll(fileIdentifiersToUpdate);
        for (final String identifier : tmpFileIdentifiers) {
            // 下载文件到文件夹
            // 文件名开头是identifier
            final ZyRegulationBii zyRegulationBii = zyRegulationBiiMapper.queryByIdentifier(identifier);
            if (zyRegulationBii == null) {
                log.error("CANNOT FIND zyRegulationBii with identifier " + identifier);
                continue;
            }

            final String regulationBiiName = zyRegulationBii.getName();
            if (regulationBiiName.contains("京投公司管理制度汇编")) {
                continue;
            }
            log.info("Add file " + regulationBiiName);

            final String name =
                regulationDirectory + zyRegulationBii.getIdentifier() + "_" + regulationBiiName + ".pdf";
            final String downloadUrl = contentManagementService.getDownloadUrl(zyRegulationBii.getContentFileId());
            HttpGet httpGet = new HttpGet(downloadUrl);
            // httpGet.setConfig(ProxyUtils.generateRequestConfig());
            try (CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse httpResponse = client.execute(httpGet)) {
                File file = new File(name);
                try (OutputStream out = Files.newOutputStream(file.toPath())) {
                    httpResponse.getEntity().writeTo(out);
                }
            } catch (Exception e) {
                log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    @Override public Page<ZyRegulationBiiVO> queryNewestVersionPageList(Page<ZyRegulationBiiVO> page,
        RegulationQueryDTO queryDTO) {
        if (page == null) {
            page = new Page<>();
        }

        queryDTO = preprocessQueryDTO(queryDTO);
        if (null == queryDTO) {
            log.warn("NO RECORDS! queryDTO=" + queryDTO);
            return page;
        }
        final List<ZyRegulationBiiVO> records = zyRegulationBiiMapper.queryNewestVersionPageList(page, queryDTO);
        page.setRecords(postProcess(queryDTO, records));
        return page;
    }

    @Override public int inactivateById(final Integer id) {
        if (id == null) {
            return 0;
        }
        return zyRegulationBiiMapper.inactivateById(id);
    }

    @Override public void inactivateByIdentifier(final String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return;
        }
        final int inactivateCnt = zyRegulationBiiMapper.inactivateByIdentifier(identifier);
        log.info("Inactivate total: " + inactivateCnt);
    }

    @Override public void rebuildIndex(Set<String> fileIdentifiersToAdd, Set<String> fileIdentifiersToUpdate,
        Set<String> fileIdentifiersToDelete) {
        if (fileIdentifiersToAdd == null || fileIdentifiersToUpdate == null || fileIdentifiersToDelete == null) {
            log.warn("INPUT IS NULL!");
            return;
        }

        if (fileIdentifiersToAdd.isEmpty() && fileIdentifiersToUpdate.isEmpty() && fileIdentifiersToDelete.isEmpty()) {
            log.warn("INPUT IS EMPTY!");
            return;
        }

        // 更新聊天机器人文件库索引
        prepareFilesForKnowledgeBase(fileIdentifiersToAdd, fileIdentifiersToUpdate, fileIdentifiersToDelete);

        final String fileIdentifiersToAddStr = buildIdentifierStringFromSet(fileIdentifiersToAdd);
        final String fileIdentifiersToUpdateStr = buildIdentifierStringFromSet(fileIdentifiersToUpdate);
        final String fileIdentifiersToDeleteStr = buildIdentifierStringFromSet(fileIdentifiersToDelete);

        regulationAiService.updateDb(regulationDirectory, fileIdentifiersToAddStr, fileIdentifiersToUpdateStr,
            fileIdentifiersToDeleteStr);
    }

    private String buildIdentifierStringFromSet(final Set<String> fileIdentifiers) {
        if (fileIdentifiers == null || fileIdentifiers.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (final String fileIdentifier : fileIdentifiers) {
            sb.append(fileIdentifier).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    @Override public ZyRegulationBiiVO queryById(final Integer id, final String mark) {
        if (id == null) {
            return null;
        }

        final ZyRegulationBii zyRegulationBii = getById(id);
        if (zyRegulationBii == null) {
            log.error("CANNOT FIND REGULATION WITH ID " + id);
            return null;
        }

        ZyRegulationBiiVO result = new ZyRegulationBiiVO();
        BeanUtils.copyProperties(zyRegulationBii, result);
        result.setPreviewUrl(contentManagementService.getPreviewUrl(zyRegulationBii.getContentFileId(), mark));

        final String identifier = zyRegulationBii.getIdentifier();
        final List<ZyRegulationBiiHistory> zyRegulationBiiHistoryList =
            zyRegulationBiiHistoryService.lambdaQuery().eq(ZyRegulationBiiHistory::getIdentifier, identifier).list();

        List<ZyRegulationBiiHistoryVO> historyList = new ArrayList<>(zyRegulationBiiHistoryList.size());
        for (final ZyRegulationBiiHistory zyRegulationBiiHistory : zyRegulationBiiHistoryList) {
            final ZyRegulationBiiHistoryVO zyRegulationBiiHistoryVO = new ZyRegulationBiiHistoryVO();
            BeanUtils.copyProperties(zyRegulationBiiHistory, zyRegulationBiiHistoryVO);

            final List<ZyRegulationBiiDept> zyRegulationBiiDeptList = zyRegulationBiiDeptService.lambdaQuery()
                .eq(ZyRegulationBiiDept::getCode, zyRegulationBiiHistory.getCode()).list();
            zyRegulationBiiHistoryVO.setDeptList(zyRegulationBiiDeptList);
            historyList.add(zyRegulationBiiHistoryVO);

            final String previewUrl =
                contentManagementService.getPreviewUrl(zyRegulationBiiHistory.getContentFileId(), mark);
            zyRegulationBiiHistoryVO.setPreviewUrl(previewUrl);

            if (result.getCode().equals(zyRegulationBiiHistory.getCode())) {
                result.setVersion(zyRegulationBiiHistory.getVersion());
                result.setPublishNo(zyRegulationBiiHistory.getPublishNo());
                result.setSpecialAuditTime(zyRegulationBiiHistory.getSpecialAuditTime());
                result.setPublishTime(zyRegulationBiiHistory.getPublishTime());
                result.setAbolishTime(zyRegulationBiiHistory.getAbolishTime());
                result.setDeptList(zyRegulationBiiDeptList.stream().map(ZyRegulationBiiDept::getQiqiaoDeptName)
                    .collect(Collectors.toList()));
            }

        }
        result.setHistoryList(historyList);

        // 查询关联制度
        final List<ZyRelatedRegulation> zyRelatedRegulations =
            zyRelatedRegulationService.queryByRegulationIdentifier(identifier);
        if (CollectionUtils.isNotEmpty(zyRelatedRegulations)) {
            final List<String> relatedRegulationIdentifierList = new ArrayList<>(zyRelatedRegulations.size());
            for (final ZyRelatedRegulation zyRelatedRegulation : zyRelatedRegulations) {
                final String regulationIdentifierA = zyRelatedRegulation.getRegulationIdentifierA();
                final String regulationIdentifierB = zyRelatedRegulation.getRegulationIdentifierB();
                if (identifier.equals(regulationIdentifierA)) {
                    relatedRegulationIdentifierList.add(regulationIdentifierB);
                } else {
                    relatedRegulationIdentifierList.add(regulationIdentifierA);
                }
            }

            final RegulationQueryDTO regulationQueryDTO = new RegulationQueryDTO();
            regulationQueryDTO.setActive(1);
            regulationQueryDTO.setIdentifierList(relatedRegulationIdentifierList);
            final List<ZyRegulationBii> zyRegulationBiis = zyRegulationBiiMapper.queryList(regulationQueryDTO);
            result.setRelatedRegulationList(zyRegulationBiis);
        }

        return result;
    }

    @Override @Transactional(rollbackFor = Exception.class) public void createOrEdit(final String qiqiaoRegulationId) {
        if (StringUtils.isEmpty(qiqiaoRegulationId)) {
            return;
        }

        // 1. 查询七巧制度基本信息
        final RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        final JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn("CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        log.info("record: " + record);
        final String author = record.getString("author");
        final String authorName = record.getString("authorName");
        final JSONObject variables = record.getJSONObject("variables");
        final JSONObject prettyValue = record.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn("CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }

        // 2. 存一条制度记录
        String regulationIdentifier = variables.getString("制度唯一标示");
        if (StringUtils.isEmpty(regulationIdentifier)) {
            regulationIdentifier = variables.getString("制度唯一标识文本");
        }
        if (StringUtils.isEmpty(regulationIdentifier)) {
            log.warn("CANNOT FIND regulationIdentifier!");
            return;
        }

        ZyRegulationBii zyRegulationBii = queryByIdentifier(regulationIdentifier);
        if (zyRegulationBii == null) {
            log.info("creating a new regulation");
            zyRegulationBii = new ZyRegulationBii();
        } else {
            log.info("editing a regulation");
        }

        // 保存上级/关联制度
        {
            // 删除之前的关联
            zyRelatedRegulationService.deleteByRegulationIdentifier(regulationIdentifier);

            RecordVO relatedRecordVO = new RecordVO();
            relatedRecordVO.setApplicationId(biiRegulationInfoApplicationId);
            relatedRecordVO.setFormModelId(biiParentRegulationInfoFormModelId);
            List<FieldFilter> fieldFilterList = new ArrayList<>(1);
            FieldFilter fieldFilter = new FieldFilter();
            fieldFilter.setFieldName("制度名称");
            fieldFilter.setLogic("eq");
            fieldFilter.setValue(qiqiaoRegulationId);
            fieldFilterList.add(fieldFilter);
            relatedRecordVO.setFilter(fieldFilterList);
            final JSONObject page = qiqiaoFormsService.page(relatedRecordVO);
            final JSONArray relatedRegulationList = page.getJSONArray("list");
            log.info("relatedRegulationList: " + relatedRegulationList);
            for (int i = 0; i < relatedRegulationList.size(); ++i) {
                final JSONObject realRegulationJson = relatedRegulationList.getJSONObject(i);
                RecordVO tmpRecordVO = new RecordVO();
                tmpRecordVO.setApplicationId(biiRegulationInfoApplicationId);
                tmpRecordVO.setFormModelId(biiRealRegulationInfoFormModelId);
                final JSONObject realVariables = realRegulationJson.getJSONObject("variables");
                if (realVariables != null) {
                    final String realRegulationId = realVariables.getString("上级关联制度");
                    tmpRecordVO.setId(realRegulationId);
                    final JSONObject realRegulation = qiqiaoFormsService.queryById(tmpRecordVO);
                    final String relatedRegulationIdentifier =
                        realRegulation.getJSONObject("variables").getString("制度系统标识别文本");
                    zyRelatedRegulationService.saveRelation(regulationIdentifier, relatedRegulationIdentifier);
                }
            }
        }

        zyRegulationBii.setQiqiaoRegulationId(qiqiaoRegulationId);
        zyRegulationBii.setName(variables.getString("制度名称").trim());

        final String fileId = variables.getString("内管文件编号");
        final String docId = variables.getString("内管文档编号");
        zyRegulationBii.setContentFileId(fileId);
        zyRegulationBii.setContentDocId(docId);
        zyRegulationBii.setCode(variables.getString("制度编号"));
        zyRegulationBii.setIdentifier(regulationIdentifier);
        zyRegulationBii.setQiqiaoCreatorId(author);
        zyRegulationBii.setQiqiaoCreatorName(authorName);

        final String levelId = variables.getString("制度级别");
        zyRegulationBii.setLevelName("--");
        if (StringUtils.isNotEmpty(levelId)) {
            zyRegulationBii.setLevelId("lvl" + levelId);
            switch (levelId) {
                case "1": {
                    zyRegulationBii.setLevelName("一级");
                    break;
                }
                case "2": {
                    zyRegulationBii.setLevelName("二级");
                    break;
                }
                case "3": {
                    zyRegulationBii.setLevelName("三级");
                    break;
                }
                case "4": {
                    zyRegulationBii.setLevelName("四级");
                    break;
                }
                case "5": {
                    zyRegulationBii.setLevelName("无级别");
                    break;
                }
                default: {
                    break;
                }
            }
        }
        final String qiqiaoSubCategoryId = variables.getString("制度分类");
        final String subCategoryId = convertSubCategoryId(qiqiaoSubCategoryId);
        final String categoryId = convertCategoryId(subCategoryId);
        zyRegulationBii.setSubCategoryId(subCategoryId);
        zyRegulationBii.setSubCategoryName(convertSubCategoryName(categoryId, subCategoryId));

        final String categoryName = convertCategoryName(categoryId);
        zyRegulationBii.setCategoryId(categoryId);
        zyRegulationBii.setCategoryName(categoryName);

        if (!saveOrUpdate(zyRegulationBii)) {
            log.warn("FAILED TO SAVE zyRegulationBii=" + zyRegulationBii);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return;
        }

        // 3. 将doc转换成pdf
        // 注意：制度编号里面不能有下划线
        final String taskId =
            biiAppId + "@" + WpsOperateType.OFFICE_CONVERT + "@" + docId + "@" + fileId + "@" + qiqiaoRegulationId;

        // 查询是否转换过
        final JSONObject jsonObject = contentManagementService.queryTask(taskId);
        final String downloadId = jsonObject.getString("download_id");
        if (StringUtils.isEmpty(downloadId)) {
            WpsFormatDTO wpsFormatDTO = new WpsFormatDTO();
            wpsFormatDTO.setTask_id(taskId);
            wpsFormatDTO.setScene_id(biiAppId);
            // 注意：这里我们获取的文件最新版本的下载链接
            final String docUrl = contentManagementService.getDownloadNewestUrl(docId);
            wpsFormatDTO.setDoc_url(docUrl);
            wpsFormatDTO.setDoc_filename(variables.getString("文件名称"));
            wpsFormatDTO.setTarget_file_format("pdf");
            if (!contentManagementService.officeConvert(wpsFormatDTO)) {
                log.warn("FAILED CONVERT " + wpsFormatDTO);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }

            // 回调接口中实现，存一条制度历史版本的记录（PDF文件）
        } else {
            // 如果之前已经转换过了
            savePdfRegulation(qiqiaoRegulationId, downloadId, zyRegulationBii, variables);
        }
    }

    @Override public ZyRegulationBii queryByIdentifier(final String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            return null;
        }
        return zyRegulationBiiMapper.queryByIdentifier(identifier);
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
        final ZyRegulationBii zyRegulationBii = zyRegulationBiiMapper.queryByContentFileId(fileId);
        if (zyRegulationBii == null || StringUtils.isEmpty(qiqiaoRegulationId)) {
            log.warn("CANNOT FIND REGULATION BY CONTENT FILE ID " + fileId);
            return;
        }

        // 查询七巧制度基本信息
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        final JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn("CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return;
        }
        final JSONObject variables = record.getJSONObject("variables");
        final JSONObject prettyValue = record.getJSONObject("prettyValue");
        if (variables == null || prettyValue == null) {
            log.warn("CANNOT FIND VARIABLES OR PRETTY VALUE FOR REGULATION " + qiqiaoRegulationId);
            return;
        }

        savePdfRegulation(qiqiaoRegulationId, downloadId, zyRegulationBii, variables);
    }

    @Override public void syncToQiqiao(final Integer minId, final Integer maxId) {
        final List<ZyRegulationBii> zyRegulationBiiList = lambdaQuery().list();
        for (final ZyRegulationBii zyRegulationBii : zyRegulationBiiList) {
            final Integer id = zyRegulationBii.getId();
            if (minId != null && id < minId) {
                continue;
            }

            if (maxId != null && id > maxId) {
                continue;
            }

            insertRegulationToQiqiao(id);
        }
    }

    @Override public void insertRegulationToQiqiao(final Integer regulationBiiId) {
        log.info("insertRegulationToQiqiao: " + regulationBiiId);
        final ZyRegulationBiiVO zyRegulationBiiVO = queryById(regulationBiiId, "");
        if (zyRegulationBiiVO == null) {
            return;
        }

        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRealRegulationInfoFormModelId);
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("制度名称");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(zyRegulationBiiVO.getName());
        fieldFilterList.add(fieldFilter);
        recordVO.setFilter(fieldFilterList);

        final JSONObject page = qiqiaoFormsService.page(recordVO);
        final JSONArray foundRegulationList = page == null ? null : page.getJSONArray("list");

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
        data.put("内管文档编号", zyRegulationBiiVO.getContentDocId());
        data.put("内管文件编号", zyRegulationBiiVO.getContentFileId());
        data.put("文件名称", zyRegulationBiiVO.getName() + ".pdf");
        data.put("可编辑", "2");
        data.put("可预览", "1");
        data.put("可删除", "2");

        final Date publishTime = zyRegulationBiiVO.getPublishTime();
        if (publishTime != null) {
            data.put("制度发布时间", publishTime.getTime());
        } else {
            log.warn("发布时间为空！" + regulationBiiId);
        }

        // 查询所有的部门
        JSONArray mainDeptList = new JSONArray();
        final List<ZyRegulationBiiDept> zyRegulationBiiDeptList =
            zyRegulationBiiDeptService.lambdaQuery().eq(ZyRegulationBiiDept::getCode, zyRegulationBiiVO.getCode())
                .list();
        for (final ZyRegulationBiiDept zyRegulationBiiDept : zyRegulationBiiDeptList) {
            mainDeptList.add(zyRegulationBiiDept.getQiqiaoDeptId());
        }
        data.put("制度主责部门", mainDeptList);

        recordVO = new RecordVO();
        recordVO.setApplicationId(biiRegulationInfoApplicationId);
        recordVO.setFormModelId(biiRealRegulationInfoFormModelId);
        recordVO.setData(data);
        if (CollectionUtils.isNotEmpty(foundRegulationList)) {
            recordVO.setId(foundRegulationList.getJSONObject(0).getString("id"));
        }

        final JSONObject jsonObject = qiqiaoFormsService.saveOrUpdate(recordVO);
        if (jsonObject == null) {
            log.warn("FAILED TO SAVE OR UPDATE recordVO " + recordVO);
            return;
        }

        final String qiqiaoRegulationId = jsonObject.getString("id");
        final ZyRegulationBii zyRegulationBii = queryByIdentifier(zyRegulationBiiVO.getIdentifier());
        zyRegulationBii.setQiqiaoRegulationId(qiqiaoRegulationId);
        if (!updateById(zyRegulationBii)) {
            log.warn("FAILED TO UPDATE " + zyRegulationBii);
        }

        // 更新历史版本明细
        updateHistory(qiqiaoRegulationId, zyRegulationBiiVO.getCode(), historyList);

        // @todo 更新上级制度发布明细 （暂时不用做，旧制度系统不存在关联制度）
    }

    private void updateHistory(final String qiqiaoRegulationId, final String currentCode,
        final List<ZyRegulationBiiHistoryVO> historyList) {
        if (StringUtils.isEmpty(qiqiaoRegulationId) || StringUtils.isEmpty(
            currentCode) || org.springframework.util.CollectionUtils.isEmpty(historyList)) {
            log.warn("WRONG INPUT");
            return;
        }

        for (final ZyRegulationBiiHistoryVO history : historyList) {
            final String code = history.getCode();
            if (currentCode.equals(code)) {
                final ZyRegulationBiiHistory zyRegulationBiiHistory =
                    zyRegulationBiiHistoryService.lambdaQuery().eq(ZyRegulationBiiHistory::getCode, code).one();
                zyRegulationBiiHistory.setQiqiaoRegulationId(qiqiaoRegulationId);
                if (!zyRegulationBiiHistoryService.updateById(zyRegulationBiiHistory)) {
                    log.warn("FAILED TO UPDATE " + zyRegulationBiiHistory);
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
            data.put("专项审核完成时间", history.getSpecialAuditTime());

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
                log.warn("FAILED TO UPDATE " + zyRegulationBiiHistory);
            }

        }
    }

    private String level2Qiqiao(final String level) {
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

    private String subCategory2Qiqiao(final String subCategoryName) {
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

    private List<RegulationOld> getRegulationOldList() {
        final String searchUrl = "https://jtregulation.bii.com.cn/bii/regulation/search";

        JSONObject data = new JSONObject();
        JSONObject query = new JSONObject();
        query.put("isActive", "1");
        query.put("type", "2");
        query.put("isLite", "1");
        query.put("flag", "1");
        data.put("query", query);

        JSONObject user = new JSONObject();
        user.put("loginId", "lingtong");
        user.put("companyType", "0");
        user.put("level", "1");

        final JSONArray result = search(searchUrl, data.toJSONString(), user);
        try {
            return new ObjectMapper().readValue(result.toJSONString(), new TypeReference<List<RegulationOld>>() {
            });
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }
        return new ArrayList<>();
    }

    private List<RegulationOld> getRegulationOldDetail(final String regulationOldId) {
        final String searchUrl = "https://jtregulation.bii.com.cn/bii/regulation/search";

        JSONObject data = new JSONObject();
        JSONObject query = new JSONObject();
        query.put("isActive", "3");
        query.put("id", regulationOldId);
        data.put("query", query);

        JSONObject user = new JSONObject();
        user.put("loginId", "lingtong");
        user.put("companyType", "0");
        user.put("level", "1");

        final JSONArray result = search(searchUrl, data.toJSONString(), user);
        try {
            return new ObjectMapper().readValue(result.toJSONString(), new TypeReference<List<RegulationOld>>() {
            });
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }
        return new ArrayList<>();
    }

    private boolean syncRegulationOld(final RegulationOld regulationOld) {
        if (regulationOld == null) {
            return false;
        }

        final String docId = UUIDGenerator.generate();
        if (!syncHistoryInfo(regulationOld, docId)) {
            log.warn("FAILED TO SYNC REGULATION OLD HISTORY INFO");
            return false;
        }

        if (!syncCurrentInfo(regulationOld)) {
            log.warn("FAILED TO SYNC REGULATION OLD CURRENT INFO");
            return false;
        }

        return true;
    }

    private JSONArray search(final String url, final String data, final JSONObject user) {
        // log.info("url: " + url);
        // log.info("data: " + data);
        // log.info("user: " + user);

        if (StringUtils.isEmpty(url)) {
            return null;
        }

        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.addHeader("User", user.toJSONString());
        httpPost.setEntity(new StringEntity(data, "UTF-8"));
        httpPost.setConfig(ProxyUtils.generateRequestConfig());
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
                EntityUtils.consume(entity);
                JSONObject resObj = JSONObject.parseObject(result);
                return resObj.getJSONArray("result");
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }

        return new JSONArray();
    }

    private boolean syncCurrentInfo(final RegulationOld regulationOld) {
        if (regulationOld == null) {
            log.warn("regulationOld is null");
            return false;
        }

        final String regulationOldId = regulationOld.getId();
        final List<ZyRegulationBii> zyRegulationBiiList =
            lambdaQuery().eq(ZyRegulationBii::getCode, regulationOldId).list();
        if (CollectionUtils.isNotEmpty(zyRegulationBiiList)) {
            log.warn("ALREADY ADDED " + regulationOld);
            return true;
        }

        final List<ZyRegulationBiiHistory> zyRegulationBiiHistoryList =
            zyRegulationBiiHistoryService.lambdaQuery().eq(ZyRegulationBiiHistory::getCode, regulationOldId).list();

        if (CollectionUtils.isNotEmpty(zyRegulationBiiHistoryList)) {
            ZyRegulationBii zyRegulationBii = new ZyRegulationBii();
            BeanUtils.copyProperties(zyRegulationBiiHistoryList.get(0), zyRegulationBii);

            // 如果这条已经添加过了，就不用添加
            final List<String> recordList = regulationOld.getRecordList();
            if (recordList.size() > 1) {
                for (int i = 0; i < recordList.size() - 1; ++i) {
                    final String recordId = recordList.get(i);
                    zyRegulationBiiMapper.removeByCode(recordId);
                }
            }

            if (!save(zyRegulationBii)) {
                log.warn("FAILED TO SAVE REGULATION BII " + zyRegulationBii);
                return false;
            }

            return true;
        } else {
            log.warn("FAILED TO ADD " + regulationOld);
            return false;
        }
    }

    private boolean syncHistoryInfo(final RegulationOld regulationOld, String docId) {
        // sync history info
        // sync responsible departments
        List<String> recordList = regulationOld.getRecordList();
        if (CollectionUtils.isEmpty(recordList)) {
            return true;
        }

        // recordList默认是从旧到新的顺序
        for (final String recordId : recordList) {
            final List<ZyRegulationBiiHistory> zyRegulationBiiHistoryList =
                zyRegulationBiiHistoryService.lambdaQuery().eq(ZyRegulationBiiHistory::getCode, recordId).list();
            if (CollectionUtils.isNotEmpty(zyRegulationBiiHistoryList)) {
                // 说明之前已经上传过了
                docId = zyRegulationBiiHistoryList.get(0).getContentDocId();
                continue;
            }

            final List<RegulationOld> regulationOldDetailList = getRegulationOldDetail(recordId);
            if (CollectionUtils.isEmpty(regulationOldDetailList)) {
                continue;
            }

            final RegulationOld regulationOldHistory = regulationOldDetailList.get(0);
            final ZyRegulationBii zyRegulationBii = convert(regulationOldHistory, docId);
            if (zyRegulationBii == null) {
                log.warn("FAILED TO CONVERT " + regulationOldHistory);
                return false;
            }

            if (zyRegulationBii.getContentFileId() == null) {
                log.warn("CONTENT FILE ID IS EMPTY " + regulationOldHistory);
                return false;
            }

            if (!updateHistoryAndDept(regulationOldHistory, zyRegulationBii)) {
                log.warn("FAILED TO UPDATE HISTORY AND DEPT " + regulationOldHistory + ", " + zyRegulationBii);
                return false;
            }
        }

        return true;
    }

    private ZyRegulationBii convert(final RegulationOld regulationOld, final String docId) {
        if (regulationOld == null) {
            return null;
        }

        final List<String> recordList = regulationOld.getRecordList();
        if (CollectionUtils.isEmpty(recordList)) {
            log.warn("RECORD LIST IS EMPTY " + regulationOld);
            return null;
        }

        ZyRegulationBii zyRegulationBii = new ZyRegulationBii();
        zyRegulationBii.setIdentifier(recordList.get(0));
        zyRegulationBii.setCode(regulationOld.getId());
        zyRegulationBii.setName(regulationOld.getTitle());
        final String isActive = regulationOld.getIsActive();
        if (StringUtils.isEmpty(isActive)) {
            zyRegulationBii.setActive(0);
        } else {
            zyRegulationBii.setActive(Integer.parseInt(regulationOld.getIsActive()));
        }

        // 上传文件
        final List<RegulationFileOld> fileList = regulationOld.getFileList();
        if (CollectionUtils.isEmpty(fileList)) {
            log.warn("fileList is empty (regulationOld: " + regulationOld + ")");
        } else {
            if (fileList.size() > 1) {
                log.warn("fileList is too many (regulationOld: " + regulationOld + ")");
            }
            final RegulationFileOld regulationFileOld = fileList.get(0);

            final String downloadUrl = "https://jtregulation.bii.com.cn/bii/file/download/{id}";
            String fileId = regulationFileOld.getId();
            String fileName = regulationFileOld.getName();
            downloadFile(downloadUrl, fileId, fileName);

            File file = new File(fileName);
            if (file.exists()) {
                List<File> files = new ArrayList<File>() {{
                    add(file);
                }};
                final List<EcmFileDTO> ecmFileDTOList = contentManagementService.uploadFiles(files, docId);

                if (CollectionUtils.isEmpty(ecmFileDTOList)) {
                    log.warn("uploadFiles is empty (file: " + file + ")");
                } else {
                    final EcmFileDTO ecmFileDTO = ecmFileDTOList.get(0);
                    zyRegulationBii.setContentDocId(ecmFileDTO.getDocId());
                    zyRegulationBii.setContentFileId(ecmFileDTO.getFileId());
                }
                file.delete();
            }

            // @todo 七巧上面是否需要存一份？
            // zyRegulationBii.setQiqiaoRegulationId();
            final JSONObject userInfo = publicManagementService.getUserInfoByUserName(regulationOld.getUserId());
            if (userInfo == null) {
                log.warn("CANNOT FIND USER WITH USER ID " + regulationOld.getUserId());
            } else {
                zyRegulationBii.setCreatorId(userInfo.getInteger("account"));
                zyRegulationBii.setCreateBy(userInfo.getString("nickName"));
                zyRegulationBii.setCreateDeptId(userInfo.getInteger("orgOaId"));
                zyRegulationBii.setCreateDept(userInfo.getString("orgName"));
                zyRegulationBii.setCreateSubCompanyId(userInfo.getInteger("companyOrgOaId"));
                zyRegulationBii.setCreateSubCompany(userInfo.getString("companyOrgName"));
                zyRegulationBii.setCreateMpUserId(userInfo.getString("userId"));

                String wxid = userInfo.getString("wxid");
                final JSONObject qiqiaoUser = qiqiaoService.usersAccount(wxid);
                if (qiqiaoUser == null) {
                    log.warn("CANNOT FIND qiqiaoUser for wxid " + wxid);
                } else {
                    zyRegulationBii.setQiqiaoCreatorId(qiqiaoUser.getString("id"));
                    zyRegulationBii.setQiqiaoCreatorName(qiqiaoUser.getString("name"));
                }
            }

            zyRegulationBii.setLevelId(regulationOld.getLevel());
            zyRegulationBii.setLevelName(convertLevelName(regulationOld.getLevel()));

            final List<String> categories = regulationOld.getCategories();
            if (CollectionUtils.isNotEmpty(categories)) {
                zyRegulationBii.setCategoryId(categories.get(0));
                zyRegulationBii.setCategoryName(convertCategoryName(zyRegulationBii.getCategoryId()));

                if (categories.size() > 1) {
                    zyRegulationBii.setSubCategoryId(categories.get(1));
                    zyRegulationBii.setSubCategoryName(
                        convertSubCategoryName(zyRegulationBii.getCategoryId(), zyRegulationBii.getSubCategoryId()));
                }
            }
        }

        return zyRegulationBii;
    }

    private void downloadFile(String url, String fileId, String fileName) {
        HttpGet httpGet = new HttpGet(url.replace("{id}", fileId));
        httpGet.setConfig(ProxyUtils.generateRequestConfig());
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = client.execute(httpGet)) {
            File file = new File(fileName);
            try (OutputStream out = Files.newOutputStream(file.toPath())) {
                httpResponse.getEntity().writeTo(out);
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private String convertCategoryId(final String subCategoryId) {
        String result = "";
        if (StringUtils.isEmpty(subCategoryId)) {
            return result;
        }

        switch (subCategoryId) {
            case "dq":
            case "lz": {
                result = "dghlz";
                break;
            }
            case "xz":
            case "rl":
            case "cw":
            case "sj":
            case "flhg":
            case "cght":
            case "xxsj":
            case "ky":
            case "aq":
            case "zh":
            case "czqi":
            case "yw": {
                result = "jlcgl";
                break;
            }
            default: {
                result = "none";
                break;
            }
        }

        return result;
    }

    private String convertCategoryName(final String categoryId) {
        if (StringUtils.isEmpty(categoryId)) {
            return "--";
        }

        switch (categoryId) {
            case "none":
                return "--";

            case "dqhlz":
                return "党群和廉政制度";

            case "dshgl":
                return "董事会管理制度";

            case "jlcgl":
                return "经理层管理制度";
        }
        return "--";
    }

    private String convertSubCategoryName(final String categoryId, final String subCategoryId) {
        if (StringUtils.isEmpty(categoryId)) {
            return "--";
        }

        switch (categoryId) {
            case "none": {
                switch (subCategoryId) {
                    case "dq":
                        return "党群管理";
                    case "lz":
                        return "廉政建设管理";
                    case "dshgl":
                        return "董事会管理";
                    case "xz":
                        return "行政管理";
                    case "rl":
                        return "人力资源管理";
                    case "cw":
                        return "财务管理";
                    case "sj":
                        return "审计管理";
                    case "flhg":
                        return "法律合规管理";
                    case "cght":
                        return "采购合同管理";
                    case "xxsj":
                        return "信息数据管理";
                    case "ky":
                        return "科研管理";
                    case "aq":
                        return "安全管理";
                    case "zh":
                        return "综合管理";
                    case "czqi":
                        return "出资企业管理";
                    case "yw":
                        return "业务管理";
                }
                break;
            }
            case "dghlz": {
                switch (subCategoryId) {
                    case "dq":
                        return "党群管理";
                    case "lz":
                        return "廉政建设管理";
                }
            }
            case "jlcgl": {
                switch (subCategoryId) {
                    case "xz":
                        return "行政管理";
                    case "rl":
                        return "人力资源管理";
                    case "cw":
                        return "财务管理";
                    case "sj":
                        return "审计管理";
                    case "flhg":
                        return "法律合规管理";
                    case "cght":
                        return "采购合同管理";
                    case "xxsj":
                        return "信息数据管理";
                    case "ky":
                        return "科研管理";
                    case "aq":
                        return "安全管理";
                    case "zh":
                        return "综合管理";
                    case "czqi":
                        return "出资企业管理";
                    case "yw":
                        return "业务管理";
                }
                break;
            }
        }
        return "--";
    }

    private String convertLevelName(final String levelId) {
        if (StringUtils.isEmpty(levelId)) {
            return "无级别";
        }

        switch (levelId) {
            case "none":
                return "无级别";
            case "lvl1":
                return "一级";
            case "lvl2":
                return "二级";
            case "lvl2*":
                return "二级*";
            case "lvl3":
                return "三级";
            case "lvl4":
                return "四级";
            default:
                return "无级别";
        }
    }

    private String convertQiqiaoDeptId(final String departmentId) {
        if (StringUtils.isEmpty(departmentId)) {
            return "--";
        }
        // 转换成七巧ID（注意测试和生产的区别）
        switch (departmentId) {
            case "1":
                return "edc3eb37-30a1-4bfc-9975-49a85d9b9418";
            case "3":
                return ApplicationProfile.PROD.equals(profile) ? "4c7bddf7ede8478d8afa9c86e87d28da"
                    : "a4a12143a8fa471fa3898557e4ea140c";
            case "4":
                return ApplicationProfile.PROD.equals(profile) ? "a5debc73242b4a1b9a98adad3e9985f8"
                    : "6b8760da9f1f4caf8a2d6bbf813f2756";
            case "5":
                return ApplicationProfile.PROD.equals(profile) ? "5f698d1972ec4e7cbf661551008de49c"
                    : "ca062971d32b4b7da94f8e02af8201ad";
            case "6":
                return ApplicationProfile.PROD.equals(profile) ? "74dfaac7d05243349175a44506b6739c"
                    : "17530d0d217146b88686b1008d7547c6";
            case "101":
                return ApplicationProfile.PROD.equals(profile) ? "296964369d5b43eeb4a6c7ec4ddf6aec"
                    : "833878d192be46c4ba6d4ce4942646fb";
            case "7":
                return ApplicationProfile.PROD.equals(profile) ? "b19bbb8947574c2daa7dd3ab6dd00fad"
                    : "36c903b8731a4e96a7a6be1928844949";
            case "8":
                return ApplicationProfile.PROD.equals(profile) ? "1c95aecfe72949baa5b176fe7c871467"
                    : "f31a14ca8eac48919a90e80cce22f050";

            case "9":
                return ApplicationProfile.PROD.equals(profile) ? "6445bd3e913849399fd162595669a16a"
                    : "7c1c05ac71614da0a66decf0add9cac1";

            case "10":
                return ApplicationProfile.PROD.equals(profile) ? "1c1aaf02a52b48b8b825f7cbd668515c"
                    : "ddd5727fb883427fbc2d827a2ae34818";

            case "11":
                return ApplicationProfile.PROD.equals(profile) ? "c34755e60a934eec82c45fafaa909e3a"
                    : "7c7d0563356242959309321e3ac018bb";

            case "12":
                return ApplicationProfile.PROD.equals(profile) ? "67958880988e49759941bcb96a040eeb"
                    : "c2c6bf49c73c41abb7c4ac140075b990";
            case "13":
                return ApplicationProfile.PROD.equals(profile) ? "a9459711f85840efb38b9d5e6de3cb8b"
                    : "87af57ba273447ce8f28ff4d1219b316";
            case "14":
                return ApplicationProfile.PROD.equals(profile) ? "abbde7e75d0146f0b983ca985da01036"
                    : "660bc33863bc416fb56aef1c3521f997";
            case "15":
                return ApplicationProfile.PROD.equals(profile) ? "3e893d92533e42abba3912020db3961a"
                    : "cabfb277df7140cf85f110aaf1218cf5";
            case "16":
                return ApplicationProfile.PROD.equals(profile) ? "983a04c42a3f4e1491d8ed873f9cd63d"
                    : "c5ca507e7e384258a881b6679d18e43b";
            case "17":
                return ApplicationProfile.PROD.equals(profile) ? "cf19af293a8e485da122566400b8f6ca"
                    : "dfb1b26804ad4a668a782e38551741f0";
            case "18":
                return ApplicationProfile.PROD.equals(profile) ? "26990334d5c144cf9c84235992c80dfa"
                    : "bce8b8b2182d4805b50c812fef353961";

            case "19":
                return ApplicationProfile.PROD.equals(profile) ? "ccd46a16b5e141a7ade2689017636307"
                    : "0432cc1873a34bbfaf008b27f7edb3d0";
            case "20":
                return ApplicationProfile.PROD.equals(profile) ? "bd6eb5fb9f3d4bc3b264d7a44d4e1594"
                    : "0dab87658b9c4c5fb64e89ada4d6ab4f";
            case "21":
                return ApplicationProfile.PROD.equals(profile) ? "8a80a188845d4d02905690123ffc028b"
                    : "2b37a5894fb647c2840e3fa931931abf";
            case "22":
                return ApplicationProfile.PROD.equals(profile) ? "0fdbcea9ab774592bf73fc581e5b88e4"
                    : "a1b7ce8943084dbcaf1112cd976a933f";
            case "23":
                return ApplicationProfile.PROD.equals(profile) ? "4f4548e60a6e48f6b9553088ca93e6e0"
                    : "8f898b555cd740f7bab3b718963b34f7";
            case "24":
                return ApplicationProfile.PROD.equals(profile) ? "cb506389902d402f99288cf1774d50c3"
                    : "c19e51b5cb3e44759e27a1cab3ab11a2";

            case "25":
                return ApplicationProfile.PROD.equals(profile) ? "5a73abdb59b0432bb2ef902bc7226988"
                    : "d437aa926fdd40e49bdc48c8ca25ff84";
            case "26":
                return ApplicationProfile.PROD.equals(profile) ? "cf46e22dc177479eb3bcef52ecb6be89"
                    : "6a8e60ddf6144b4c99ee590913e1d89d";
            case "27":
                return ApplicationProfile.PROD.equals(profile) ? "f3217333f2ab4b7d8d6b228116295839"
                    : "f7c9dcbc856f4aaaa4b4db0f1561a8b3";
            case "28":
                return ApplicationProfile.PROD.equals(profile) ? "756962e1fed6465e8da1f3161f382aa2"
                    : "f614ddc3900749f7900017755a2b4a08";

            case "29":
                return ApplicationProfile.PROD.equals(profile) ? "dcbfe714236045f7a035a63e7ee952c1"
                    : "9f10ac3335214ae99dccb298246d9b95";
            case "30":
                return ApplicationProfile.PROD.equals(profile) ? "eb8b7def34be46ee8413833c4b59a574"
                    : "cb4f6c77fbc6481ab32ab1d87a9915dc";
            case "31":
                return ApplicationProfile.PROD.equals(profile) ? "11daa6dc60e34de193e0115d121dab7c"
                    : "9ab438ad43f749e49b24a30298cd4968";
            case "32":
                return ApplicationProfile.PROD.equals(profile) ? "bff467d0951b4cd7994f10f983e249b1"
                    : "9532e70afb2b4cb9811cf71ce6482fdb";
            case "33":
                return ApplicationProfile.PROD.equals(profile) ? "58980e0f386b48349c74f1ae3832dd24"
                    : "62489763dd004b5e8ab2cdede6143060";
            case "34":
                return ApplicationProfile.PROD.equals(profile) ? "bb5033b663b14703ae85dfb022810915"
                    : "97bf226f582c4e9b9185d3112abac738";
            default:
                break;
        }

        return "--";
    }

    private boolean updateHistoryAndDept(final RegulationOld regulationOld, final ZyRegulationBii zyRegulationBii) {
        if (regulationOld == null || zyRegulationBii == null) {
            log.warn("INPUT IS EMPTY!");
            return false;
        }

        // 更新该制度的历史记录
        final ZyRegulationBiiHistory zyRegulationBiiHistory = new ZyRegulationBiiHistory();
        BeanUtils.copyProperties(zyRegulationBii, zyRegulationBiiHistory);
        zyRegulationBiiHistory.setId(null);
        zyRegulationBiiHistory.setVersion(regulationOld.getVersion());
        zyRegulationBiiHistory.setPublishNo(regulationOld.getPublishNo());
        zyRegulationBiiHistory.setPublishTime(regulationOld.getPublishTime());
        zyRegulationBiiHistory.setAbolishTime(regulationOld.getAbolishTime());
        // 法律审核时间 --> 专项审核完成时间
        zyRegulationBiiHistory.setSpecialAuditTime(regulationOld.getLawTime());
        if (!zyRegulationBiiHistoryService.save(zyRegulationBiiHistory)) {
            log.warn("FAILED TO SAVE REGULATION BII HISTORY " + zyRegulationBiiHistory);
            return false;
        }

        // 更新该制度的主责部门
        ZyRegulationBiiDept zyRegulationBiiDept = new ZyRegulationBiiDept();
        BeanUtils.copyProperties(zyRegulationBiiHistory, zyRegulationBiiDept);
        final List<String> departmentIds = regulationOld.getDepartmentIds();
        final List<String> departments = regulationOld.getDepartments();
        for (int i = 0; i < departments.size(); ++i) {
            final String departmentId = departmentIds.get(i);
            String departmentName = departments.get(i);
            zyRegulationBiiDept.setId(null);

            final String qiqiaoDeptId = convertQiqiaoDeptId(departmentId);
            zyRegulationBiiDept.setQiqiaoDeptId(qiqiaoDeptId);

            // 获取名字
            final JSONObject departmentJson = qiqiaoDepartmentService.getByDepartmentId(qiqiaoDeptId);
            if (departmentJson != null) {
                departmentName = departmentJson.getString("name");
            }
            zyRegulationBiiDept.setQiqiaoDeptName(departmentName);
            if (!zyRegulationBiiDeptService.save(zyRegulationBiiDept)) {
                log.warn("FAILED TO SAVE REGULATION BII DEPT " + zyRegulationBiiDept);
                return false;
            }
        }

        return true;
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
            final List<ZyRegulationBiiDept> zyRegulationBiiDeptList =
                zyRegulationBiiDeptService.getByQiqiaoDeptIdList(deptIdList);

            if (CollectionUtils.isEmpty(zyRegulationBiiDeptList)) {
                log.warn("[preprocessQueryDTO] NO zyRegulationBiiDept FOUND queryDTO: " + queryDTO);
                return null;
            }

            final List<String> codeList = zyRegulationBiiDeptList.stream().map(ZyRegulationBiiDept::getCode).distinct()
                .collect(Collectors.toList());
            queryDTO.setCodeList(codeList);
        }

        return queryDTO;
    }

    private List<ZyRegulationBiiVO> postProcess(final RegulationQueryDTO queryDTO,
        final List<ZyRegulationBiiVO> zyRegulationBiiVoList) {
        if (CollectionUtils.isEmpty(zyRegulationBiiVoList)) {
            return new ArrayList<>();
        }

        for (final ZyRegulationBiiVO zyRegulationBiiVo : zyRegulationBiiVoList) {
            final String contentFileId = zyRegulationBiiVo.getContentFileId();
            zyRegulationBiiVo.setPreviewUrl(contentManagementService.getPreviewUrl(contentFileId));
            // zyRegulationBiiVo.setDownloadUrl(contentManagementService.getDownloadUrl(contentFileId));

            final List<ZyRegulationBiiDept> zyRegulationBiiDeptList =
                zyRegulationBiiDeptService.getByRegulationCodeAndVersion(zyRegulationBiiVo.getCode(),
                    zyRegulationBiiVo.getVersion());
            zyRegulationBiiVo.setDeptList(zyRegulationBiiDeptList.stream().map(ZyRegulationBiiDept::getQiqiaoDeptName)
                .collect(Collectors.toList()));

            if (queryDTO != null) {
                final Map<String, String> docId2Content = queryDTO.getDocId2Content();
                if (MapUtils.isNotEmpty(docId2Content)) {
                    zyRegulationBiiVo.setFragment(docId2Content.get(zyRegulationBiiVo.getContentDocId()));
                }
            }
        }
        return zyRegulationBiiVoList;
    }

    private void savePdfRegulation(final String qiqiaoRegulationId, final String downloadId,
        final ZyRegulationBii zyRegulationBii, final JSONObject variables) {
        log.info("qiqiaoRegulationId: {}, downloadId: {}, zyRegulationBii: {}, variables: {}", qiqiaoRegulationId,
            downloadId, zyRegulationBii, variables);

        if (StringUtils.isEmpty(qiqiaoRegulationId) || StringUtils.isEmpty(
            downloadId) || zyRegulationBii == null || variables == null) {
            return;
        }

        final String fileName = zyRegulationBii.getName() + ".pdf";
        try {
            if (!contentManagementService.downloadConvertedFile(downloadId, fileName, null)) {
                log.warn("FAILED DOWNLOAD FILE name: " + fileName + ", downloadId: " + downloadId);
                return;
            }

            final File pdfFile = new File(fileName);

            // 2. 上传PDF文件到内管
            if (!pdfFile.exists()) {
                log.warn("PDF FILE NOT EXIST: " + fileName);
                return;
            }
            List<File> fileList = new ArrayList<>();
            fileList.add(pdfFile);

            // 如果是版本更新，那么需要保持docId一致
            final String identifier = zyRegulationBii.getIdentifier();
            final List<ZyRegulationBiiHistory> zyRegulationBiiHistoryList =
                zyRegulationBiiHistoryService.queryByIdentifier(identifier);
            String docId = null;
            if (CollectionUtils.isNotEmpty(zyRegulationBiiHistoryList)) {
                log.info("FOUND REGULATION HISTORY " + zyRegulationBiiHistoryList.get(0));
                docId = zyRegulationBiiHistoryList.get(0).getContentDocId();
            }
            final List<EcmFileDTO> ecmFileDtoList = contentManagementService.uploadFiles(fileList, docId);
            if (CollectionUtils.isEmpty(ecmFileDtoList)) {
                log.warn("FAILED TO UPLOAD PDF FILE");
                return;
            }

            // 3. 存一条制度历史版本的记录
            final EcmFileDTO ecmFileDTO = ecmFileDtoList.get(0);
            ZyRegulationBiiHistory zyRegulationBiiHistory = new ZyRegulationBiiHistory();
            BeanUtils.copyProperties(zyRegulationBii, zyRegulationBiiHistory);
            zyRegulationBiiHistory.setId(null);
            zyRegulationBiiHistory.setVersion(variables.getString("制度版本"));
            zyRegulationBiiHistory.setContentFileId(ecmFileDTO.getFileId());
            zyRegulationBiiHistory.setContentDocId(ecmFileDTO.getDocId());
            zyRegulationBiiHistory.setPublishNo(variables.getString("发布文号"));
            final Date specialAuditTime = variables.getDate("专项审核完成时间");
            zyRegulationBiiHistory.setSpecialAuditTime(specialAuditTime);

            final Long publishTime = variables.getLong("制度发布时间");
            log.info("publishTime: " + publishTime);
            final Date publishTimeDate = publishTime == null ? new Date() : DateUtils.getDate(publishTime);
            log.info("publishTimeDate: " + publishTimeDate);
            zyRegulationBiiHistory.setPublishTime(publishTimeDate);

            // 把历史版本的废止时间更新为当前最新版本的发布时间
            zyRegulationBiiHistoryService.updateAbolishTime(identifier, publishTimeDate);
            if (!zyRegulationBiiHistoryService.save(zyRegulationBiiHistory)) {
                log.warn("FAILED TO SAVE zyRegulationBiiHistory " + zyRegulationBiiHistory);
            }

            // 4. 存一条或多条主责部门记录
            final JSONArray regulationDeptIdList = variables.getJSONArray("制度主责部门");
            final JSONArray regulationDeptNameList = variables.getJSONArray("制度主责部门_pretty_value");
            if (CollectionUtils.isEmpty(regulationDeptIdList) || CollectionUtils.isEmpty(
                regulationDeptNameList) || regulationDeptIdList.size() != regulationDeptNameList.size()) {
                log.warn("CANNOT FIND REGULATION DEPT LIST FOR REGULATION " + qiqiaoRegulationId);
                return;
            }

            ZyRegulationBiiDept zyRegulationBiiDept = new ZyRegulationBiiDept();
            BeanUtils.copyProperties(zyRegulationBiiHistory, zyRegulationBiiDept);
            for (int i = 0; i < regulationDeptIdList.size(); i++) {
                final String deptId = regulationDeptIdList.getString(i);
                final String deptName = regulationDeptNameList.getString(i);
                zyRegulationBiiDept.setQiqiaoDeptId(deptId);
                zyRegulationBiiDept.setQiqiaoDeptName(deptName);
                zyRegulationBiiDept.setId(null);
                if (!zyRegulationBiiDeptService.save(zyRegulationBiiDept)) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return;
                }
            }
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        } finally {
            File pdfFile = new File(fileName);
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
        }

        log.info("PDF REGULATION SAVED SUCCESSFULLY");
    }

    private String convertSubCategoryId(final String qiqiaoCategoryId) {
        String result = "--";
        if (StringUtils.isEmpty(qiqiaoCategoryId)) {
            return result;
        }
        switch (qiqiaoCategoryId) {
            case "1": {
                result = "dshgl";
                break;
            }
            case "2": {
                result = "xz";
                break;
            }
            case "3": {
                result = "rl";
                break;
            }
            case "4": {
                result = "cw";
                break;
            }
            case "5": {
                result = "sj";
                break;
            }
            case "6": {
                result = "flhg";
                break;
            }
            case "7": {
                result = "cght";
                break;
            }
            case "8": {
                result = "xxsj";
                break;
            }
            case "9": {
                result = "ky";
                break;
            }
            case "10": {
                result = "aq";
                break;
            }
            case "11": {
                result = "zh";
                break;
            }
            case "12": {
                result = "czqi";
                break;
            }
            case "13": {
                result = "yw";
                break;
            }
            case "14": {
                result = "dq";
                break;
            }
            case "15": {
                result = "lz";
                break;
            }
            default: {
                break;
            }
        }
        return result;
    }

    private void updateSpecialAuditTime(final RegulationOld regulationOld) {
        if (regulationOld == null) {
            return;
        }

        final List<String> recordList = regulationOld.getRecordList();
        if (CollectionUtils.isEmpty(recordList)) {
            return;
        }
        for (final String recordId : recordList) {
            final List<ZyRegulationBiiHistory> zyRegulationBiiHistoryList =
                zyRegulationBiiHistoryService.lambdaQuery().eq(ZyRegulationBiiHistory::getCode, recordId).list();
            if (CollectionUtils.isNotEmpty(zyRegulationBiiHistoryList)) {
                // 说明之前已经上传过了
                final ZyRegulationBiiHistory zyRegulationBiiHistory = zyRegulationBiiHistoryList.get(0);

                // 更新法律审核时间（专项审核完成时间）
                if (zyRegulationBiiHistory.getSpecialAuditTime() != null) {
                    continue;
                }
                final List<RegulationOld> regulationOldDetailList = getRegulationOldDetail(recordId);
                if (CollectionUtils.isEmpty(regulationOldDetailList)) {
                    continue;
                }
                final RegulationOld regulationOldHistory = regulationOldDetailList.get(0);
                final Date lawTime = regulationOldHistory.getLawTime();
                if (lawTime == null) {
                    continue;
                }
                zyRegulationBiiHistory.setSpecialAuditTime(lawTime);
                if (!zyRegulationBiiHistoryService.updateById(zyRegulationBiiHistory)) {
                    log.warn("FAILED TO UPDATE zyRegulationBiiHistory=" + zyRegulationBiiHistory);
                }
            }
        }
    }
}
