package org.jeecg.modules.regulation.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.common.utils.EncryptionUtils;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.dto.EcmFileDTO;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.publicManagement.service.IPublicManagementService;
import org.jeecg.modules.qiqiao.constants.FieldFilter;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.jeecg.modules.regulation.dto.RegulationFileMessageDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationArchive;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaHistory;
import org.jeecg.modules.regulation.mapper.ZyRegulationArchiveMapper;
import org.jeecg.modules.regulation.mapper.ZyRegulationBjmoaHistoryMapper;
import org.jeecg.modules.regulation.mapper.ZyRegulationBjmoaMapper;
import org.jeecg.modules.regulation.service.IZyRegulationArchiveBjmoaService;
import org.jeecg.modules.regulation.service.IZyRegulationBjmoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.springframework.data.redis.connection.ReactiveListCommands.LRemCommand.last;

/**
 * @author zhouwei
 * @date 2024/1/30
 */
@Service
@Slf4j
public class ZyRegulationArchiveBjmoaServiceImpl implements IZyRegulationArchiveBjmoaService {

    private static final Calendar calendar = Calendar.getInstance();

    @Autowired
    @Qualifier("bjmoaContentManagementService")
    private IContentManagementService contentManagementService;
    @Autowired
    private ZyRegulationBjmoaMapper zyRegulationBjmoaMapper;
    @Autowired
    private ZyRegulationBjmoaHistoryMapper zyRegulationBjmoaHistoryMapper;
    @Autowired
    private IQiqiaoFormsService qiqiaoFormsService;
    @Autowired
    private IQiqiaoService qiqiaoService;
    @Autowired
    private IPublicManagementService publicManagementService;
    @Autowired
    private ZyRegulationArchiveMapper zyRegulationArchiveMapper;
    @Autowired
    private IZyRegulationBjmoaService zyRegulationBjmoaService;


    @Value("${biisaas.bjmoaRegulationInfo.relatedRegulationFormModelId}")
    private String bjmoaRelatedRegulationInfoFormModelId;
    @Value("${biisaas.bjmoaRegulationInfo.applicationId}")
    private String bjmoaRegulationInfoApplicationId;
    @Value("${biisaas.bjmoaRegulationInfo.formModelId}")
    private String bjmoaRegulationInfoFormModelId;
    @Value("${archive.appid}")
    private String appid;
    @Value("${archive.secret}")
    private String secret;
    @Value("${archive.libcode}")
    private String libcode;
    @Value("${archive.unitcode}")
    private String unitcode;
    @Value("${archive.tokenUrl}")
    private String tokenUrl;
    @Value("${archive.addFileUrl}")
    private String addFileUrl;

    @Override
    public JSONObject filed(String qiqiaoRegulationId) {
        log.info("filed: " + qiqiaoRegulationId);
        ZyRegulationBjmoa zyRegulationBjmoa = zyRegulationBjmoaMapper.queryByQiqiaoRegulationId(qiqiaoRegulationId);
        if (zyRegulationBjmoa == null) {
            log.warn("Fail to find bjmoa regulation by qiqiaoRegulationId :" + qiqiaoRegulationId);
            return null;
        }

        // 1.创建文件夹
        String filePath = qiqiaoRegulationId;
        File file = new File(filePath);
        file.mkdirs();

        // 2.下载制度文件并进行数据封装
        boolean isCreateXmlSuccess = createXml(zyRegulationBjmoa, filePath);
        if (isCreateXmlSuccess == false) {
            log.warn("Fail to implement data encapsulation!");
            return null;
        }

        // 3.打包成压缩包
        String zipPath = appid + "-" + DateUtils.getDate("yyyyMMdd") + "-" + qiqiaoRegulationId + ".zip";
        boolean isZipSuccess = false;
        try {
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath));
            isZipSuccess = zip(filePath, zipOut);
        } catch (Exception e) {
            log.warn("Fail to compress folder!");
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return null;
        }

        // 4.如果压缩成功，上传内管平台生成http下载链接
        String downloadUrl = null;
        if (isZipSuccess) {
            File archiveFile = new File(zipPath);
            List<File> archiveInfoZip = new ArrayList<>();
            archiveInfoZip.add(archiveFile);

            List<EcmFileDTO> ecmFileDTOList = contentManagementService.uploadFiles(archiveInfoZip);
            if (CollectionUtils.isNotEmpty(ecmFileDTOList)) {
                EcmFileDTO ecmFileDTO = ecmFileDTOList.get(0);
                String fileId = ecmFileDTO.getFileId();

                downloadUrl = contentManagementService.getDownloadUrl(fileId);
                log.info("zip包下载地址: {}", downloadUrl);
            } else {
                log.warn("ecmFileDTOList is NULL");
                return null;
            }
            deleteDirectory(file); // 删除文件夹
        } else {
            log.warn("Fail to compress folder!");
            return null;
        }

        // 5.开始与档案系统对接
        log.info("Start Regulation Filed!");
        JSONObject filedResult = null;
        String reqid = qiqiaoRegulationId + "@" + DateUtils.getDate("yyyyMMddHHmmss");
        filedResult = startFiled(reqid, zipPath, downloadUrl);
        // 打印最终的档案系统对接结果
        log.info("档案系统对接完成 - 制度ID: {}, 请求ID: {}, 结果: {}",
                qiqiaoRegulationId, reqid,
                filedResult != null ? filedResult.toJSONString() : "null");
        // 删除压缩包
        File zipFile = new File(zipPath);
        if (zipFile.exists()) {
            zipFile.delete();
        }
        log.info("filedResult" + filedResult);

        return filedResult;
    }



    /**
     * 遍历所有七巧表单
     *
     *
     *
     */
    @Override
    public JSONObject syncPublishedRegulations() {
        log.info("[syncPublishedRegulations] start");

        JSONObject result = new JSONObject();
        int total = 0;
        int inserted = 0;
        int skipped = 0;

        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);

        int pageNo = 1;
        int pageSize = 100;
        boolean hasMore = true;

        while (hasMore) {
            recordVO.setPage(pageNo);
            recordVO.setPageSize(pageSize);

            JSONObject pageResult = qiqiaoFormsService.page(recordVO);
            if (pageResult == null) {
                log.warn("Failed to query qiqiao, page: {}", pageNo);
                break;
            }

            JSONArray list = pageResult.getJSONArray("list");
            if (list == null || list.isEmpty()) {
                hasMore = false;
                break;
            }

            for (int i = 0; i < list.size(); i++) {
                JSONObject record = list.getJSONObject(i);
                JSONObject variables = record.getJSONObject("variables");

                if (variables == null) {
                    continue;
                }

                // 筛选已发布状态 = 7
                String status = variables.getString("制度状态");
                if (!"7".equals(status)) {
                    continue;
                }
                // 筛选制度类型不为适用
                String buildType = variables.getString("制度建设类型");
                if ("3".equals(buildType)) {
                    continue;
                }

                total++;

                String qiqiaoRegulationId = record.getString("id");

                // 检查新表是否已存在
                ZyRegulationArchive existing = zyRegulationArchiveMapper.selectByQiqiaoRegulationId(qiqiaoRegulationId);
                if (existing != null) {
                    skipped++;
                    continue;
                }

                // 写入新表
                ZyRegulationArchive archive = new ZyRegulationArchive();
                archive.setQiqiaoRegulationId(qiqiaoRegulationId);

                String identifier = variables.getString("制度唯一标示");
                if ("2".equals(variables.getString("制度建设类型"))) {
                    identifier = variables.getString("制度唯一标识文本");
                }
                archive.setIdentifier(identifier);

                archive.setName(variables.getString("最终制度名称"));
                archive.setCode(variables.getString("制度编号"));
                archive.setCategoryId(variables.getString("大类"));
                archive.setManagementCategoryId(variables.getString("管理类别"));
                archive.setLevelId(variables.getString("制度级别"));
                archive.setContentFileId(variables.getString("最终制度内管文件编号"));
                archive.setQiqiaoCreatorId(variables.getString("制度跟进人"));

                // 从 prettyValue 取名称
                JSONObject prettyValue = record.getJSONObject("prettyValue");
                if (prettyValue != null) {
                    archive.setCategoryName(prettyValue.getString("大类"));
                    archive.setManagementCategoryName(prettyValue.getString("管理类别"));
                    archive.setLevelName(prettyValue.getString("制度级别"));
                    archive.setQiqiaoCreatorName(prettyValue.getString("制度跟进人"));
                }

                // 发布时间
                Long publishTimeLong = variables.getLong("制度发布日期");
                if (publishTimeLong != null) {
                    Date publishTime = new Date(publishTimeLong);
                    archive.setPublishTime(publishTime);
                    calendar.setTime(publishTime);
                    archive.setPublishYear(calendar.get(Calendar.YEAR));
                }

                archive.setArchiveStatus(0); // 未归档
//                archive.setCreateTime(new Date());
//                archive.setUpdateTime(new Date());

                zyRegulationArchiveMapper.insert(archive);
                inserted++;

                log.info("Inserted archive: {}", qiqiaoRegulationId);
            }

            // 判断下一页
            Integer totalCount = pageResult.getInteger("totalCount");
            if (totalCount == null) {
                totalCount = pageResult.getInteger("total");
            }
            if (totalCount == null || pageNo * pageSize >= totalCount) {
                hasMore = false;
            } else {
                pageNo++;
            }
        }

        result.put("total", total);
        result.put("inserted", inserted);
        result.put("skipped", skipped);

        log.info("[syncPublishedRegulations] result: {}", result);
        return result;
    }


    // ZyRegulationArchiveBjmoaServiceImpl.java 添加：

    @Override
    public JSONObject batchArchiveByYear(Integer publishYear) {
        log.info("[batchArchiveByYear] publishYear: {}", publishYear);

        JSONObject result = new JSONObject();
        result.put("publishYear", publishYear);

        int total = 0;
        int success = 0;
        int fail = 0;
        List<String> successList = new ArrayList<>();
        List<JSONObject> failList = new ArrayList<>();   // 失败列表存对象

        // 查询指定年份、未归档的记录
        LambdaQueryWrapper<ZyRegulationArchive> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ZyRegulationArchive::getPublishYear, publishYear)
                .in(ZyRegulationArchive::getArchiveStatus, 0, 2);// 0 和 2 都查
//                .last("LIMIT 1");
        List<ZyRegulationArchive> archiveList = zyRegulationArchiveMapper.selectList(queryWrapper);

        if (archiveList == null || archiveList.isEmpty()) {
            log.info("No pending records for year: {}", publishYear);
            result.put("total", 0);
            result.put("success", 0);
            result.put("fail", 0);
            return result;
        }

        total = archiveList.size();
        log.info("Found {} records to archive", total);

        for (ZyRegulationArchive archive : archiveList) {
            String qiqiaoRegulationId = archive.getQiqiaoRegulationId();
            try {
                // 判断 managementCategoryId 是否为 3
                String managementCategoryId = archive.getManagementCategoryId();
                String operation = "1"; // 默认使用编辑
                // 党群制度
                if ("3".equals(managementCategoryId) || "4".equals(managementCategoryId)) {
                    zyRegulationBjmoaService.create(qiqiaoRegulationId);
                }else {
                // 其他类型存到主表里
                zyRegulationBjmoaService.createOrEdit(qiqiaoRegulationId, operation);
                }
                // 调用 filed 方法推送
                JSONObject filedResult = filed(qiqiaoRegulationId);

                if (filedResult != null && "0".equals(filedResult.getString("code"))) {
                    // --- 成功逻辑 ---
                    archive.setArchiveStatus(1);
                    archive.setArchiveTime(new Date());
                    archive.setArchiveResult(filedResult.toJSONString());

                    zyRegulationArchiveMapper.updateById(archive);

                    success++;
                    successList.add(qiqiaoRegulationId);
                    log.info("Archive success: {}", qiqiaoRegulationId);
                } else {
                    // --- 失败逻辑 ---
                    String errorMsg = "未知错误";
                    if (filedResult != null) {
                        // 尝试获取错误信息，根据你实际接口返回的字段取值，可能是 msg 也可能是 retdesc
                        errorMsg = filedResult.containsKey("msg") ? filedResult.getString("msg") : filedResult.getString("retdesc");
                        if (errorMsg == null) errorMsg = filedResult.toJSONString();
                    }

                    archive.setArchiveStatus(2);
                    archive.setArchiveResult(filedResult != null ? filedResult.toJSONString() : "null");

                    zyRegulationArchiveMapper.updateById(archive);

                    fail++;

                    // 【改动】记录详细失败原因
                    JSONObject failItem = new JSONObject();
                    failItem.put("id", qiqiaoRegulationId);
                    failItem.put("reason", errorMsg);
                    failList.add(failItem);

                    log.warn("Archive failed: {}, result: {}", qiqiaoRegulationId, filedResult);
                }
            } catch (Exception e) {
                // --- 异常逻辑 ---
                // 【改动】同样需要手动设置更新时间
//                archive.setUpdateTime(new Date());

                archive.setArchiveStatus(2);
                // 截取部分异常信息存入数据库，防止太长存不下
                String exceptionMsg = e.getMessage();
                if (exceptionMsg != null && exceptionMsg.length() > 500) {
                    exceptionMsg = exceptionMsg.substring(0, 500);
                }
                archive.setArchiveResult("Exception: " + exceptionMsg);

                zyRegulationArchiveMapper.updateById(archive);

                fail++;

                // 【改动】记录详细异常原因
                JSONObject failItem = new JSONObject();
                failItem.put("id", qiqiaoRegulationId);
                failItem.put("reason", "系统异常: " + e.getMessage());
                failList.add(failItem);

                log.error("Archive exception: {}", qiqiaoRegulationId, e);
            }
        }

        result.put("total", total);
        result.put("success", success);
        result.put("fail", fail);
        result.put("successList", successList);
        result.put("failList", failList);

        log.info("[batchArchiveByYear] result: {}", result);
        return result;
    }





    /**
     * 数据封装
     *
     * @param zyRegulationBjmoa 制度信息
     * @param filePath          制度文件下载路径
     */
    private boolean createXml(ZyRegulationBjmoa zyRegulationBjmoa, String filePath) {
        log.info("zyRegulationBjmoa: {}, filePath: {}", zyRegulationBjmoa, filePath);
        if (zyRegulationBjmoa == null || StringUtils.isEmpty(filePath)) {
            return false;
        }
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("电子文件封装包");
        root.addAttribute("xmlns", "http://www.saac.gov.cn/standards/ERM/encapsulation");
        root.addElement("封装包格式描述").setText("本EEP根据中华人民共和国档案行业标准DA/T 48-2009《基于XML的电子文件封装规范》生成");
        root.addElement("版本").setText("2009");

        Element sign = root.addElement("被签名对象");
        sign.addAttribute("eep版本", "2009");
        sign.addElement("封装包类型").setText("原始型");
        sign.addElement("封装包类型描述").setText("本封装包包含电子文件数据及其元数据，原始封装，未经修改");
        sign.addElement("封装包创建时间").setText(DateUtils.getDate("yyyy-MM-dd HH:mm:ss").replace(" ", "T"));
        sign.addElement("封装包创建单位").setText("规章制度系统");

        Element content = sign.addElement("封装内容");
        Element fileBlock = content.addElement("文件实体块");
        Element fileEntity = fileBlock.addElement("文件实体");
        content.addElement("业务实体块");
        content.addElement("机构人员实体块");

        List<RegulationFileMessageDTO> fileMessageList = downloadAttachment(zyRegulationBjmoa, filePath);
        if (fileMessageList == null) {
            log.warn("Fail to download regulation archive file!");
            return false;
        }

        // 设置文件实体
        Element element = addFileEntity(fileEntity, zyRegulationBjmoa, fileMessageList);
        if (element == null) {
            log.warn("Fail to set file entity!");
            return false;
        }

        // 导出XML文件
        try {
            FileOutputStream fos = new FileOutputStream(filePath + File.separator + "eep.xml");
            OutputFormat format = new OutputFormat(" ", true);
            XMLWriter xw = new XMLWriter(fos, format);
            xw.write(doc);
            xw.close();
        } catch (Exception e) {
            log.warn("Fail to export xml file!");
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return false;
        }

        return true;
    }

    /**
     * 设置文件实体
     *
     * @param fileEntity        文件实体
     * @param zyRegulationBjmoa 制度信息
     * @param fileMessageList   文件属性
     * @return 文件实体
     */
    private Element addFileEntity(Element fileEntity, ZyRegulationBjmoa zyRegulationBjmoa, List<RegulationFileMessageDTO> fileMessageList) {
        log.info("zyRegulationBjmoa: {}, fileMessageList: {}", zyRegulationBjmoa, fileMessageList);
        if (zyRegulationBjmoa == null || fileMessageList == null) {
            return null;
        }
        String qiqiaoRegulationId = zyRegulationBjmoa.getQiqiaoRegulationId();
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn("Fail to find regulation record by qiqiaoregulationid : " + qiqiaoRegulationId);
            return null;
        }
        JSONObject variables = record.getJSONObject("variables");
        if (variables == null) {
            log.warn("Fail to find variables by qiqiaoregulationid : " + qiqiaoRegulationId);
        }

        Integer variablesInteger = variables.getInteger("制度版本号");
        String curVersion = "";
        if (null != variablesInteger){
            curVersion = "A/" + (variablesInteger - 1);
        }
        String regulationPrincipal = variables.getString("制度跟进人");
        String departmentName = variables.getString("制度主责部门_pretty_value");

        fileEntity.addElement("聚合层次").setText("文件");
        fileEntity.addElement("来源").addElement("立档单位名称").setText("北京市轨道交通运营管理有限公司");
        fileEntity.addElement("电子文件号").setText(zyRegulationBjmoa.getQiqiaoRegulationId());  // 电子文件号取值：标识制度的唯一ID
        // 设置档号
        Element fileNumber = fileEntity.addElement("档号");
        fileNumber.addElement("全宗号").setText(unitcode);
        // 制度发布年度
        ZyRegulationArchive archive = zyRegulationArchiveMapper.selectByQiqiaoRegulationId(qiqiaoRegulationId);
        Date publishTime = null;
        if (archive != null && archive.getPublishTime() != null) {
            publishTime = archive.getPublishTime();
        }
//        List<ZyRegulationBjmoaHistory> zyRegulationBjmoaHistoryList = zyRegulationBjmoaHistoryMapper.queryByIdentifierAndVersion(zyRegulationBjmoa.getIdentifier(), curVersion);
//        Date publishTime = zyRegulationBjmoaHistoryList.get(0).getPublishTime();
        calendar.setTime(publishTime);
        fileNumber.addElement("年度").setText(String.valueOf(calendar.get(Calendar.YEAR)));
        fileNumber.addElement("保管期限").setText("永久");
        JSONObject usersInfoJson = qiqiaoService.usersInfo(regulationPrincipal);
        if (usersInfoJson == null) {
            log.warn("CANNOT FIND USER INFO FOR qiqiaoCreatorId: " + zyRegulationBjmoa.getQiqiaoCreatorId() );
            fileNumber.addElement("机构或问题").setText(" ");  // 设置空字符串
        } else {
            String wxUserId = usersInfoJson.getString("wxUserId");
            JSONObject userInfoByWxid = publicManagementService.getUserInfoByWxid(wxUserId);
            log.info("userInfoByWxid: " + userInfoByWxid);
            if (userInfoByWxid != null) {
                String departmentId = userInfoByWxid.getString("deptId");
                fileNumber.addElement("机构或问题").setText(departmentId);
            }else {
                fileNumber.addElement("机构或问题").setText(" ");  // 设置空字符串
            }
        }

        // 设置内容描述
        Element contentDescription = fileEntity.addElement("内容描述");
        contentDescription.addElement("题名").setText(zyRegulationBjmoa.getName());//制度名称
        contentDescription.addElement("文件编号").setText(zyRegulationBjmoa.getCode());
        contentDescription.addElement("责任者").setText(departmentName.substring(departmentName.lastIndexOf("-") + 1));//起草部门
        contentDescription.addElement("日期").setText(DateUtils.formatDate(calendar, "yyyy-MM-dd"));//制度发布日期
        contentDescription.addElement("密级").setText("非涉密");
        contentDescription.addElement("归档份数").setText("0");
        contentDescription.addElement("备注");
        contentDescription.addElement("目录是否开放").setText("是");
        contentDescription.addElement("允许电子借阅").setText("是");
        contentDescription.addElement("载体类型").setText("电子");
        contentDescription.addElement("互见号");
        // 内容描述-拓展字段
        contentDescription.addElement("ext1").setText("BZ");//分类号
        String level = zyRegulationBjmoa.getLevelName();
        contentDescription.addElement("ext2").setText(
                StringUtils.isEmpty(level) ? "" : level.replace("级", "")
        );//级别
        contentDescription.addElement("ext3").setText("规章制度系统");//数据来源名称
        contentDescription.addElement("ext4").setText(DateUtils.getDate("yyyy-MM-dd HH:mm:ss").replace(" ", "T"));//同步时间，档案系统从制度系统取值的时间
        contentDescription.addElement("ext5").setText(curVersion);//版本号

        // 设置形式特征
        Element features = fileEntity.addElement("形式特征");
        features.addElement("文件组合类型").setText("组合文件");
        features.addElement("页数").setText("1");

        fileEntity.addElement("存储位置").addElement("脱机载体编号").setText("");
        fileEntity.addElement("权限管理").addElement("控制标识").setText("公司内部公开");
        fileEntity.addElement("信息系统描述");
        fileEntity.addElement("附录");

        // 文件数据
        Element fileData = fileEntity.addElement("文件数据");
        // 文件数据-正文文档
        RegulationFileMessageDTO fileMessage = fileMessageList.get(0);
        Element doc = fileData.addElement("文档");
        doc = setDoc(doc, fileMessage, 1, 1);

        // 如果有附件
        if (fileMessageList.size() > 1) {
            for (int i = 1; i < fileMessageList.size(); i++) {
                doc = fileData.addElement("文档");
                doc = setDoc(doc, fileMessageList.get(i), i + 1, 2);
            }
        }

        return fileEntity;
    }

    /**
     * 设置文档
     *
     * @param doc
     * @param fileMessage 文档属性信息
     * @param index       文档序号
     * @param fileType    文档类型
     * @return
     */
    private Element setDoc(Element doc, RegulationFileMessageDTO fileMessage, int index, int fileType) {
        doc.addElement("文档标识符").setText("修改0-文档" + index);
        doc.addElement("文档序号").setText(String.valueOf(index));
        doc.addElement("文档类型").setText(String.valueOf(fileType));
        doc.addElement("题名").setText(fileMessage.getRegulationName());//制度名称

        // 文档-文档数据
        Element docData = doc.addElement("文档数据");
        docData.addAttribute("文档数据ID", fileMessage.getCode());

        // 文档-文档数据-编码
        Element docCode = docData.addElement("编码");
        docCode.addAttribute("编码ID", fileMessage.getCode());

        // 文档-文档数据-编码-电子属性
        Element electronAttr = docCode.addElement("电子属性");
        electronAttr.addElement("格式信息");
        electronAttr.addElement("计算机文件名").setText(fileMessage.getFileName());
        electronAttr.addElement("计算机文件大小").setText(String.valueOf(fileMessage.getFileSize()));
        electronAttr.addElement("电子属性");

        // 文档-文档数据-编码-数字化属性
        Element numAttr = docCode.addElement("数字化属性");
        numAttr.addElement("数字化对象形态");
        numAttr.addElement("扫描分辨率");
        numAttr.addElement("扫描色彩模式");
        numAttr.addElement("图像压缩方案");

        docCode.addElement("编码描述").setText("仅提供SHA256摘要信息,文档数据在外部保存");
        docCode.addElement("反编码关键字").setText("sha256-base64Binary");
        docCode.addElement("编码数据").setText(fileMessage.getCodecData());

        return doc;
    }

    /**
     * 归档受理
     *
     * @param reqid    受理请求的唯一标识
     * @param filePath zip压缩包路径
     * @param httpUrl  归档信息包在业务系统中的下载地址
     * @return
     */
    private JSONObject startFiled(String reqid, String filePath, String httpUrl) {
        log.info("reqid: {}, filePath: {}, httpUrl: {}", reqid, filePath, httpUrl);
        if (StringUtils.isEmpty(reqid) || StringUtils.isEmpty(filePath) || StringUtils.isEmpty(httpUrl)) {
            return null;
        }
        //1.鉴权，获取token
        String token = token();
        if (token == null) {
            log.warn("Fail to get token!");
            return null;
        }

        String base64Encoder = null;
        byte[] digest = EncryptionUtils.calculateSHA256(filePath);
        if (digest != null) {
            base64Encoder = base64Encoder(digest);
        } else {
            log.warn("Fail to calculate SHA256!");
            return null;
        }
        if (base64Encoder == null) {
            log.warn("Fail to base64 encode!");
            return null;
        }

        // 2.受理文件新增
        JSONObject jsonObject = addFile(token, reqid, httpUrl, base64Encoder);
        return jsonObject;
    }

    /**
     * 鉴权，获取token
     *
     * @return token
     */
    private String token() {
        // 1.创建一个httpClient对象
        CloseableHttpClient client = HttpClients.createDefault();
        // 2.创建get方式请求对象
        String url = tokenUrl + "?appid=" + appid + "&secret=" + secret;
        HttpGet httpGet = new HttpGet(url);

        CloseableHttpResponse response = null;
        String token = null;
        try {
            // 3.执行get请求并返回结果
            response = client.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                // 4.获取结果实体
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(content);
                if ("0".equals(jsonObject.getString("code"))) {
                    // 5.获取token值
                    token = jsonObject.getString("data");
                    log.info("token:" + token);
                } else {
                    log.warn("Fail to get token: " + jsonObject);
                    return null;
                }
            } else {
                log.warn("Fail to get token! ");
                return null;
            }
        } catch (Exception e) {
            log.warn("Fail to get token! ");
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return null;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
                }
            }
        }
        return token;
    }

    /**
     * 受理文件归档
     *
     * @param token
     * @param reqid     受理请求的唯一标识
     * @param httpsUrl  归档信息包在业务系统中的下载地址
     * @param aipdigest 归档信息包摘要值
     * @return
     */
    private JSONObject addFile(String token, String reqid, String httpsUrl, String aipdigest) {
        log.info("token: {}, reqid: {}, httpsUrl: {}, aipdigest: {}", token, reqid, httpsUrl, aipdigest);
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(reqid) || StringUtils.isEmpty(httpsUrl) || StringUtils.isEmpty(aipdigest)) {
            return null;
        }
        //1.创建一个httpclient对象
        CloseableHttpClient client = HttpClients.createDefault();
        //2.创建post方式请求对象
        HttpPost httpPost = new HttpPost(addFileUrl);
        //3.装填参数
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("reqid", reqid);// 请求的唯一标识,使用制度的唯一标识
        jsonParam.put("appid", appid);// appid
        jsonParam.put("libcode", libcode); // 档案门类代码
        jsonParam.put("unitcode", unitcode); // 全宗标识
        jsonParam.put("url", httpsUrl);// 归档系统包在业务系统中的下载地址
        jsonParam.put("aipdigest", aipdigest);// 归档信息包摘要值
        log.info(jsonParam.toString());
        StringEntity stringEntity = new StringEntity(jsonParam.toString(), "UTF-8");
        //4.设置参数到请求对象中
        httpPost.setEntity(stringEntity);
        log.info("Authorization:Bearer " + token);
        httpPost.setHeader("Authorization", "Bearer " + token);

        CloseableHttpResponse response = null;
        JSONObject result = null;
        try {
            // 5.执行post请求并返回结果
            response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                // 6.获取结果实体
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                result = JSONObject.parseObject(content);
            } else {
                log.warn("Fail to add File!");
                return null;
            }
        } catch (IOException e) {
            log.warn("Fail to add File!");
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return null;
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
                }
            }
        }
        return result;
    }

    /**
     * 下载制度正文和附件（5级表单）
     *
     * @param zyRegulationBjmoa 制度信息
     * @param filePath          制度文件下载路径
     * @return 制度文件属性
     */
    private List<RegulationFileMessageDTO> downloadAttachment(ZyRegulationBjmoa zyRegulationBjmoa, String filePath) {
        log.info("zyRegulationBjmoa: {}, filePath: {}", zyRegulationBjmoa, filePath);
        if (zyRegulationBjmoa == null || StringUtils.isEmpty(filePath)) {
            return null;
        }
        String qiqiaoRegulationId = zyRegulationBjmoa.getQiqiaoRegulationId();
        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);
        JSONObject record = qiqiaoFormsService.queryById(recordVO);
        if (record == null) {
            log.warn("CANNOT FIND REGULATION RECORD WITH ID " + qiqiaoRegulationId);
            return null;
        }

        List<RegulationFileMessageDTO> fileMessageList = new ArrayList<>();

        // 下载制度正文
        JSONObject variables = record.getJSONObject("variables");
        String fileName = variables.getString("最终制度文件名称");
        String fileId = variables.getString("最终制度内管文件编号");
        RegulationFileMessageDTO fileMessage = downloadFile(fileId, filePath, fileName);
        if (fileMessage != null) {
            fileMessage.setFileType("1");
            fileMessageList.add(fileMessage);
        } else {
            log.warn("Fail to download regulation file!");
            return null;
        }

        // 如果制度有附件，下载制度附件
        RecordVO relatedRecordVO = new RecordVO();
        relatedRecordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        relatedRecordVO.setFormModelId(bjmoaRelatedRegulationInfoFormModelId);
        List<FieldFilter> fieldFilterList = new ArrayList<>(1);
        FieldFilter fieldFilter = new FieldFilter();
        fieldFilter.setFieldName("外键");
        fieldFilter.setLogic("eq");
        fieldFilter.setValue(qiqiaoRegulationId);
        fieldFilterList.add(fieldFilter);
        relatedRecordVO.setFilter(fieldFilterList);
        final JSONObject page = qiqiaoFormsService.page(relatedRecordVO);
        final JSONArray relatedRegulationBjmoaList = page.getJSONArray("list");
        log.info("relatedRegulationBjmoaList: " + relatedRegulationBjmoaList);

        for (int i = 0; i < relatedRegulationBjmoaList.size(); i++) {
            JSONObject relatedRegulationJson = relatedRegulationBjmoaList.getJSONObject(i);
            JSONObject relatedVariables = relatedRegulationJson.getJSONObject("variables");
            if (relatedVariables == null) {
                log.warn("Fail to find relatedVariables in relatedRegulationBjmoaList!");
                return null;
            }

            String relatedRegulationIdentifier5 = relatedVariables.getString("关联记录唯一标识");
            ZyRegulationBjmoa zyRegulationBjmoa5 = zyRegulationBjmoaMapper.queryByIdentifier(relatedRegulationIdentifier5);
            String relatedFileId = zyRegulationBjmoa5.getContentFileId();
            JSONArray relatedFileList = relatedVariables.getJSONArray("关联记录上传");
            JSONObject relatedFile = relatedFileList.getJSONObject(0);
            String relatedFileName = relatedFile.getString("name");

            fileMessage = downloadFile(relatedFileId, filePath, relatedFileName);
            if (fileMessage != null) {
                fileMessage.setFileType("2");
                fileMessageList.add(fileMessage);
            } else {
                log.warn("Fail to download regulation attachment!");
                return null;
            }
        }

        return fileMessageList;
    }

    /**
     * 下载文件到本地，并获取文件属性
     *
     * @param fileId   文件id
     * @param filePath 文件下载路径
     * @param fileName 真实文件名称
     * @return 文件属性
     */
    private RegulationFileMessageDTO downloadFile(String fileId, String filePath, String fileName) {
        log.info("fileId: {}, filePath: {}, fileName: {}", fileId, filePath, fileName);
        if (StringUtils.isEmpty(fileId) || StringUtils.isEmpty(filePath) || StringUtils.isEmpty(fileName)) {
            return null;
        }
        // 1.将附件下载到本地
        String downloadUrl = contentManagementService.getDownloadUrl(fileId);
        String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
        String outFilePath = filePath + File.separator + fileId + fileSuffix;
        HttpGet httpGet = new HttpGet(downloadUrl);
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse httpResponse = client.execute(httpGet)) {
            File file = new File(outFilePath);
            try (OutputStream out = Files.newOutputStream(file.toPath())) {
                httpResponse.getEntity().writeTo(out);
            }
        } catch (Exception e) {
            log.warn("Fail to download archive file!");
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return null;
        }

        // 2.填写文件属性
        RegulationFileMessageDTO fileMessage = new RegulationFileMessageDTO();
        String prefixSuffix = fileName.substring(0, fileName.lastIndexOf("."));
        fileMessage.setCode(fileId);
        fileMessage.setRegulationName(prefixSuffix);
        fileMessage.setFileName(fileId + fileSuffix);
        if (getFileSize(outFilePath) != -1) {
            fileMessage.setFileSize(getFileSize(outFilePath));
        } else {
            log.warn("Fail to calculate file size!");
            return null;
        }
        fileMessage.setFileSize(getFileSize(outFilePath));
        byte[] digest = EncryptionUtils.calculateSHA256(outFilePath);
        String base64Encoder = null;
        if (digest != null) {
            base64Encoder = base64Encoder(digest);
        } else {
            log.warn("Fail to calculate SHA256!");
            return null;
        }
        if (base64Encoder != null) {
            fileMessage.setCodecData(base64Encoder);
        } else {
            log.warn("Fail to base64 encode!");
            return null;
        }
        return fileMessage;
    }

    /**
     * 推送制度归档结果到制度系统七巧端
     *
     * @param jsonObject 档案系统返回的归档处理结果
     * {
     *      "reqid": "xxxxxx",          受理请求的唯一标识
     *      "appid": "bzsystem",        档案系统分配的规章制度系统标识
     *      "libcode": "M2",            档案分类代码
     *      "unitcode": "A040",         全宗标识
     *      "retcode": "xxxxxx",        归档结果代码，200表示归档成功
     *      "retdesc": "xxxxxx",        归档结果描述
     *      "detectionResult“: "xxxxxx" 四性检测未通过内容，值可能为{}
     * }
     *
     */
    @Override
    public void pushArchiveResultToQiqiao (JSONObject jsonObject) {
        log.info("jsonObject: {}", jsonObject);
        String reqid = jsonObject.getString("reqid");
        String retcode = jsonObject.getString("retcode");
        String retdesc = jsonObject.getString("retdesc ");
        String detectionResult = jsonObject.getString("detectionResult");

        String[] split = reqid.split("@");
        String qiqiaoRegulationId = split[0];

        RecordVO recordVO = new RecordVO();
        recordVO.setApplicationId(bjmoaRegulationInfoApplicationId);
        recordVO.setFormModelId(bjmoaRegulationInfoFormModelId);
        recordVO.setId(qiqiaoRegulationId);

        Map<String, Object> regulationData = new HashMap<>(4);
        regulationData.put("制度归档请求标识", reqid);
        regulationData.put("制度归档结果代码", retcode);
        regulationData.put("制度归档结果描述", retdesc);
        regulationData.put("四性检测未通过内容", detectionResult);
        recordVO.setData(regulationData);
        JSONObject jo = qiqiaoFormsService.saveOrUpdate(recordVO);
        log.info("qiqiaoFormsService.saveOrUpdate: " + jo);
    }

    /**
     *
     * @param rootPath  压缩文件夹路径
     * @param zipOut    压缩文件
     * @return
     */
    private boolean zip(String rootPath, ZipOutputStream zipOut) {
        File folder = new File(rootPath);
        File[] files = folder.listFiles();

        ZipEntry ze;
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    zip(file.toString(), zipOut);
                } else {
//                    String fileName = file.getParent() + File.separator + file.getName();// linux环境下使用
                    String fileName = file.getParent() + "/" + file.getName();// windows环境下使用

                    ze = new ZipEntry(fileName);
                    ze.setSize(file.length());
                    ze.setTime(file.lastModified());

                    zipOut.putNextEntry(ze);

                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    for (int n = 0; -1 != (n = fileInputStream.read(buffer)); ) {
                        zipOut.write(buffer, 0, n);
                    }
                    fileInputStream.close();
                }
            }
        } catch (IOException e) {
            log.warn("Fail to zip compress!");
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            return false;
        } finally {
            try {
                zipOut.flush();
                zipOut.close();
            } catch (IOException e) {
                log.warn("Fail to close zipOutputStream!");
                log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
            }
        }
        return true;
    }

    /**
     * 将字节数组base64编码
     *
     * @param digest 字节数组
     * @return
     */
    private String base64Encoder(byte[] digest) {
        if (digest == null || digest.length == 0) {
            return null;
        }
        String encoderStr = new String(Base64.getEncoder().encode(digest));
        return encoderStr;
    }

    /**
     * 获取文件大小
     *
     * @param filePath 文件路径
     * @return
     */
    private long getFileSize(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return -1;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return -1;
        }
        long length = file.length();
        return length;
    }

    /**
     * 删除文件夹
     *
     * @param file 文件夹路径
     */
    private void deleteDirectory(File file) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }
        try {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        deleteDirectory(subFile);
                    } else {
                        subFile.delete();
                    }
                }
            }
            // 删除目录中所有文件和子目录后，删除当前目录
            file.delete();
        } catch (Exception e) {
            log.error("EXCEPTION CAUGHT: " + Arrays.toString(e.getStackTrace()));
        }
    }

}
