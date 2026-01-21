package org.jeecg.modules.content.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.content.dto.EcmFileDTO;
import org.jeecg.modules.content.dto.FileModel;
import org.jeecg.modules.content.dto.PageModel;
import org.jeecg.modules.content.dto.WpsFormatDTO;
import org.jeecg.modules.content.service.IContentManagementService;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.jeecg.modules.common.utils.StringUtils.encodeURIComponent;

@Service @Slf4j public class ContentManagementServiceImpl implements IContentManagementService {
    private String appId;
    private String token;
    @Autowired private IQiqiaoFormsService qiqiaoFormsService;
    @Value("${content-management.docTypeId}") private String docTypeId;
    @Value("${content-management.uploadUrl}") private String uploadUrl;
    @Value("${content-management.downloadUrl}") private String downloadUrl;
    @Value("${content-management.batchDownloadUrl}") private String batchDownloadUrl;
    @Value("${content-management.deleteUrl}") private String deleteUrl;
    @Value("${content-management.previewUrl}") private String previewUrl;
    @Value("${content-management.renameUrl}") private String renameUrl;
    @Value("${content-management.searchNewUrl}") private String searchNewUrl;
    @Value("${content-management.officeOperateUrl}") private String officeOperateUrl;
    @Value("${content-management.officeConvertUrl}") private String officeConvertUrl;
    @Value("${content-management.officeWrapheaderUrl}") private String officeWrapheaderUrl;
    @Value("${content-management.queryTaskUrl}") private String queryTaskUrl;
    @Value("${content-management.downloadConvertedUrl}") private String downloadConvertedUrl;

    public ContentManagementServiceImpl() {
    }

    public ContentManagementServiceImpl(String appId, String token) {
        this.appId = appId;
        this.token = token;
    }

    private File convertMultipartfile2File(final MultipartFile multipartFile) throws Exception {
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return null;
        }

        File file = null;
        try (InputStream ins = multipartFile.getInputStream()) {
            file = convertInputStream2File(ins, UUIDGenerator.generate() + "_" + multipartFile.getOriginalFilename());
        }
        return file;
    }

    private File convertInputStream2File(final InputStream ins, final String outputName) throws Exception {
        File file = new File(outputName);

        try (OutputStream os = Files.newOutputStream(file.toPath())) {
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        return file;
    }

    private List<EcmFileDTO> convertJSON2EcmFileDTOs(final JSONObject jsonObject) {
        final String prefix = "[ContentManagementImpl::convertJSON2EcmFileDTOs] ";
        log.info(prefix + "start conversion!");
        if (jsonObject == null || !"200".equals(jsonObject.getString("code"))) {
            return null;
        }

        final JSONObject dataObject = jsonObject.getJSONObject("data");
        if (dataObject == null) {
            log.error(prefix + "dataObject null!");
            return null;
        }

        final String author = dataObject.getString("author");
        final String objectId = dataObject.getString("objectId");
        final String docId = dataObject.getString("docId");
        final JSONArray ecmFiles = dataObject.getJSONArray("ecmFiles");
        if (ecmFiles == null) {
            log.error(prefix + "ecmFiles null!");
            return null;
        }

        final int size = ecmFiles.size();
        List<EcmFileDTO> ecmFileDTOs = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            final EcmFileDTO ecmFileDTO = new EcmFileDTO();
            final JSONObject ecmFileJSON = ecmFiles.getJSONObject(i);

            final String fileId = ecmFileJSON.getString("fileId");
            final String fileName = ecmFileJSON.getString("fileName");
            final String folderFilePath = ecmFileJSON.getString("folderFilePath");
            final String wpsId = ecmFileJSON.getString("wpsId");

            ecmFileDTO.setAuthor(author).setObjectId(objectId).setDocId(docId).setFileId(fileId).setFileName(fileName)
                .setFolderFilePath(folderFilePath).setWpsId(wpsId).setDownloadUrl(getDownloadUrl(fileId))
                .setPreviewUrl(getPreviewUrl(fileId));

            if (ecmFileDTO.getDownloadUrl() == null) {
                log.error(prefix + fileName + "'s download url is null!");
                return null;
            }

            ecmFileDTOs.add(ecmFileDTO);
        }

        log.info(prefix + "finish conversion!");
        return ecmFileDTOs;
    }

    private JSONObject renameUUIDFiles(final JSONObject ecmFileListJson) {
        final String prefix = "[ContentManagementImpl::renameUUIDFiles] ";
        log.info(prefix + "start renaming!");
        if (ecmFileListJson == null || !"200".equals(ecmFileListJson.getString("code"))) {
            return null;
        }

        final JSONObject dataObject = ecmFileListJson.getJSONObject("data");
        if (dataObject == null) {
            log.error(prefix + "dataObject null!");
            return null;
        }

        final String docId = dataObject.getString("docId");
        final JSONArray ecmFiles = dataObject.getJSONArray("ecmFiles");
        if (ecmFiles == null) {
            log.error(prefix + "ecmFiles null!");
            return null;
        }

        final int size = ecmFiles.size();
        for (int i = 0; i < size; ++i) {
            final JSONObject ecmFileJSON = ecmFiles.getJSONObject(i);
            final String fileId = ecmFileJSON.getString("fileId");

            // 因为之前添加了"{UUID}_"作为文件名的前缀，这里为了前端以及下载的更友好的展示，需要进行重命名
            final String fileName = ecmFileJSON.getString("fileName");
            final int idxUnderline = fileName.indexOf("_");
            if (idxUnderline == -1) {
                log.warn(prefix + "cannot find underline");
            } else if (idxUnderline == fileName.length() - 1) {
                log.warn(prefix + "underline lies at the end");
            } else {
                String newFileName = fileName.substring(idxUnderline + 1);
                renameFile(docId, fileId, newFileName);
                ecmFileJSON.put("fileName", newFileName);
            }
        }

        log.info(prefix + "finish renaming!");
        return ecmFileListJson;
    }

    @Override public JSONObject uploadMultipartFileList(MultipartFile[] multipartFiles) {
        final String prefix = "[ContentManagementServiceImpl::uploadMultipartFileList] ";
        if (ArrayUtils.isEmpty(multipartFiles)) {
            log.error(prefix + "empty input");
            return null;
        }
        final String filename = multipartFiles[0].getOriginalFilename();
        log.info(prefix + "start " + filename);

        // Note: we need to maintain a list of files to be deleted later, otherwise files would be created in our project folder.
        List<File> files = new ArrayList<>(multipartFiles.length);
        JSONObject result = null;
        try {
            log.info(prefix + "start convertMultipartfile2File " + filename);
            for (int i = 0; i < multipartFiles.length; ++i) {
                final MultipartFile multipartFile = multipartFiles[i];
                final File file = convertMultipartfile2File(multipartFile);
                files.add(file);
            }
            log.info(prefix + "finish convertMultipartfile2File " + filename);
            result = upload(files, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // [IMPORTANT] Otherwise we would get a bunch of files in our project folder!
            log.info(prefix + "files start to be deleted!");
            files.forEach(file -> {
                if (file != null) {
                    file.delete();
                }
            });
            log.info(prefix + "files finished being deleted!");
        }
        return result;
    }

    @Override public JSONObject upload(final List<File> files, String docId) {
        final String prefix = "[ContentManagementImpl::upload] ";

        if (CollectionUtils.isEmpty(files)) {
            return null;
        }
        final String filename = files.get(0).getName();
        log.info(prefix + "upload List<File> files " + filename);

        HttpPost httpPost = new HttpPost(uploadUrl);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

        if (StringUtils.isEmpty(docId)) {
            docId = UUIDGenerator.generate();
        }

        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE).setCharset(Consts.UTF_8)
            .addTextBody("ecmApp.token", token).addTextBody("ecmApp.appId", appId).addTextBody("ecmDoc.docId", docId)
            .addTextBody("ecmDoc.docTypeId", docTypeId);

        // Note: we need to maintain a list of files to be deleted later, otherwise files would be created in our project folder.
        log.info(prefix + "create client " + filename);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (int i = 0; i < files.size(); ++i) {
                multipartEntityBuilder.addBinaryBody("files", files.get(i));
            }
            httpPost.setEntity(multipartEntityBuilder.build());

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                log.info(prefix + "execute response " + filename);
                HttpEntity entity = response.getEntity();
                String result = null;
                if (entity != null) {
                    result = EntityUtils.toString(entity, Consts.UTF_8);
                }
                EntityUtils.consume(entity);
                JSONObject resObj = JSONObject.parseObject(result);
                log.info(prefix + "return response " + filename);
                return resObj;
            }
        } catch (Exception e) {
            log.error(prefix + "exception " + filename + " " + e.getMessage());
            e.printStackTrace();
        } finally {
            log.info(prefix + "finish " + filename);
        }

        return null;
    }

    @Override public List<EcmFileDTO> uploadFiles(MultipartFile[] multipartFiles) {
        JSONObject resObj = uploadMultipartFileList(multipartFiles);
        resObj = renameUUIDFiles(resObj);
        return convertJSON2EcmFileDTOs(resObj);
    }

    @Override public List<EcmFileDTO> uploadFiles(final List<File> files) {
        return uploadFiles(files, null);
    }

    @Override public List<EcmFileDTO> uploadFiles(final List<File> files, String docId) {
        JSONObject resObj = upload(files, docId);
        resObj = renameUUIDFiles(resObj);
        return convertJSON2EcmFileDTOs(resObj);
    }

    @Override public JSONObject deleteFiles(final String objectId, final String fileIds) {
        HttpPost httpPost = new HttpPost(deleteUrl);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntityBuilder.setCharset(Consts.UTF_8);
        multipartEntityBuilder.addTextBody("token", token);
        multipartEntityBuilder.addTextBody("appId", appId);
        // multipartEntityBuilder.addTextBody("docId", docId);
        multipartEntityBuilder.addTextBody("docTypeId", docTypeId);
        multipartEntityBuilder.addTextBody("objectId", objectId);
        multipartEntityBuilder.addTextBody("fileIds", fileIds);
        httpPost.setEntity(multipartEntityBuilder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
            }
            EntityUtils.consume(entity);
            JSONObject resObj = JSONObject.parseObject(result);
            return resObj;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override public String getDownloadUrl(final String fileId) {
        // e.g. http://10.99.150.40:20001/service-router/router/download?token=28813b6ab52e4c06842b87730fc74460&appId=80000030000&doctypeId=H3-1&fileId=619edf564a8c114f41ffb08f
        final String fileDownloadUrl =
            UriComponentsBuilder.fromUriString(downloadUrl).queryParam("token", token).queryParam("appId", appId)
                .queryParam("doctypeId", docTypeId).queryParam("fileId", fileId).build().toString();
        return fileDownloadUrl;
    }

    @Override public String getDownloadNewestUrl(final String docId) {
        // e.g. http://10.99.150.40:20001/service-router/router/download?token=28813b6ab52e4c06842b87730fc74460&appId=80000030000&doctypeId=H3-1&fileId=619edf564a8c114f41ffb08f
        final String fileDownloadUrl =
            UriComponentsBuilder.fromUriString(downloadUrl).queryParam("token", token).queryParam("appId", appId)
                .queryParam("doctypeId", docTypeId).queryParam("docId", docId).build().toString();
        return fileDownloadUrl;
    }

    @Override public String getBatchDownloadUrl(final List<String> fileIds) {
        // e.g. http://10.99.150.40:20001/service-router/router/batchDownload?token=28813b6ab52e4c06842b87730fc74460&appId=80000030000&fileIds=619edf564a8c114f41ffb08f_619edee84a8c114f41ffb08c
        if (fileIds == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fileIds.size(); ++i) {
            sb.append(fileIds.get(i));
            if (i != fileIds.size() - 1) {
                sb.append("_");
            }
        }
        return getBatchDownloadUrl(sb.toString());
    }

    @Override public String getBatchDownloadUrl(final String fileIds) {
        final String zipDownloadUrl =
            UriComponentsBuilder.fromUriString(batchDownloadUrl).queryParam("token", token).queryParam("appId", appId)
                .queryParam("fileIds", fileIds).build().toString();
        return zipDownloadUrl;
    }

    @Override public String getPreviewUrl(String fileId) {
        return getPreviewUrl(fileId, "北京市基础设施投资有限公司");
    }

    @Override public String getPreviewUrl(final String fileId, final String mark) {
        // e.g. http://10.99.150.40:20001/service-frond/preview?token=28813b6ab52e4c06842b87730fc74460&appId=80000030000&fileId=619edf564a8c114f41ffb08f&mark=京投
        final String filePreviewUrl =
            UriComponentsBuilder.fromUriString(previewUrl).queryParam("token", token).queryParam("appId", appId)
                .queryParam("fileId", fileId).queryParam("mark", encodeURIComponent(mark)).build().toString();
        return filePreviewUrl;
    }

    @Override public JSONObject renameFile(final String docId, final String newFileName) {
        return renameFile(docId, null, newFileName);
    }

    @Override public JSONObject renameFile(final String docId, final String fileId, final String newFileName) {
        final String prefix = "[ContentManagementServiceImpl::renameFile] ";
        log.info(prefix + "start to process docId: " + docId + ", fileId: " + fileId + ", newFileName: " + newFileName);

        if (StringUtils.isEmpty(docId) || StringUtils.isEmpty(newFileName)) {
            log.warn("EMPTY INPUT");
            return null;
        }

        String renameFileUrl = null;
        if (StringUtils.isEmpty(fileId)) {
            renameFileUrl =
                UriComponentsBuilder.fromUriString(renameUrl).queryParam("token", token).queryParam("appId", appId)
                    .queryParam("doctypeId", docTypeId).queryParam("docId", docId)
                    .queryParam("newFileName", encodeURIComponent(newFileName)).build().toString();
        } else {
            renameFileUrl =
                UriComponentsBuilder.fromUriString(renameUrl).queryParam("token", token).queryParam("appId", appId)
                    .queryParam("doctypeId", docTypeId).queryParam("docId", docId).queryParam("fileId", fileId)
                    .queryParam("newFileName", encodeURIComponent(newFileName)).build().toString();
        }

        HttpGet httpGet = new HttpGet(renameFileUrl);
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
            }
            EntityUtils.consume(entity);
            JSONObject resObj = JSONObject.parseObject(result);

            log.info(prefix + "finished processing docId: " + docId + ", fileId: " + fileId);
            return resObj;
        } catch (Exception e) {
            log.error(prefix + "catch exception: " + e.getMessage());
        }
        return null;
    }

    @Override public PageModel<FileModel> searchNew(long pageNo, long pageSize, final String searchMessage) {
        final String prefix = "[ContentManagementImpl::searchNew] ";
        log.info(prefix + "pageNo: " + pageNo + ", pageSize: " + pageSize + ", searchMessage: " + searchMessage);

        if (pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        PageModel<FileModel> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        if (StringUtils.isEmpty(searchMessage)) {
            log.warn(prefix + "searchMessage IS EMPTY");
            return null;
        }

        HttpPost httpPost = new HttpPost(searchNewUrl);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE).setCharset(Consts.UTF_8);
        ContentType contentType = ContentType.create("multipart/form-data", StandardCharsets.UTF_8);
        multipartEntityBuilder.addTextBody("token", token);
        multipartEntityBuilder.addTextBody("appId", appId);
        multipartEntityBuilder.addTextBody("searchMessage", searchMessage, contentType);
        multipartEntityBuilder.addTextBody("pageNumber", String.valueOf(pageNo));
        multipartEntityBuilder.addTextBody("pageSize", String.valueOf(pageSize));
        httpPost.setEntity(multipartEntityBuilder.build());
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
            }
            EntityUtils.consume(entity);
            JSONObject resObj = JSONObject.parseObject(result);
            log.info(prefix + "resObj: " + resObj);
            if ("200".equals(resObj.getString("code"))) {

                return new ObjectMapper().readValue(resObj.getString("data"),
                    new TypeReference<PageModel<FileModel>>() {
                    });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public boolean officeOperate(final WpsFormatDTO wpsFormatDTO) {
        return callWps(wpsFormatDTO, officeOperateUrl);
    }

    @Override public boolean officeConvert(final WpsFormatDTO wpsFormatDTO) {
        return callWps(wpsFormatDTO, officeConvertUrl);
    }

    @Override public boolean officeWrapheader(final WpsFormatDTO wpsFormatDTO) {
        final String prefix = "[ContentManagementServiceImpl::officeWrapheader] ";
        log.info(prefix + "wpsFormatDTO: " + wpsFormatDTO);
        if (wpsFormatDTO == null) {
            return false;
        }

        String entityJson = JSONObject.toJSONString(wpsFormatDTO);
        HttpPost httpPost = new HttpPost(officeWrapheaderUrl);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setEntity(new StringEntity(entityJson, "UTF-8"));
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
            }
            log.info(prefix + "result: " + result);
            // {"success":true,"message":"操作成功！","code":200,"result":false,"timestamp":1698043814687}
            EntityUtils.consume(entity);
            JSONObject resObj = JSONObject.parseObject(result);
            final Boolean ret = resObj.getBoolean("result");
            return ret != null && ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override public JSONObject queryTask(final String taskId) {
        final String prefix = "[ContentManagementServiceImpl::downloadConvertedFile] ";
        log.info(prefix + "taskId: " + taskId);

        if (StringUtils.isEmpty(taskId)) {
            return null;
        }

        final String url =
            UriComponentsBuilder.fromUriString(queryTaskUrl).queryParam("taskId", taskId).build().toString();
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = client.execute(httpGet)) {
            HttpEntity entity = httpResponse.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
            }
            log.info(prefix + "result: " + result);
            EntityUtils.consume(entity);
            return JSONObject.parseObject(result);
        } catch (Exception e) {
            log.error(prefix + "catch exception: " + e.getMessage());
        }

        return null;
    }

    @Override public boolean downloadConvertedFile(final String downloadId, final String outFilePath,
        final HttpServletResponse response) {
        final String prefix = "[ContentManagementServiceImpl::downloadConvertedFile] ";
        log.info(prefix + "downloadId: " + downloadId + ", outFilePath: " + outFilePath + ", response: " + response);

        if (StringUtils.isEmpty(downloadId)) {
            return false;
        }

        if (StringUtils.isEmpty(outFilePath) && response == null) {
            return false;
        }

        final String url =
            UriComponentsBuilder.fromUriString(downloadConvertedUrl).queryParam("downloadId", downloadId).build()
                .toString();
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = client.execute(httpGet)) {
            if (StringUtils.isEmpty(outFilePath)) {
                httpResponse.getEntity().writeTo(response.getOutputStream());
            } else {
                File file = new File(outFilePath);
                try (OutputStream out = Files.newOutputStream(file.toPath())) {
                    httpResponse.getEntity().writeTo(out);
                }
            }

            log.info("download file " + downloadId + " success");
            return true;
        } catch (Exception e) {
            log.error(prefix + "catch exception: " + e.getMessage());
        }

        return false;
    }

    @Override
    public JSONArray upload2Qiqiao(final List<File> files, final String formFieldType, final String applicationId,
        final String formModelId) {
        if (CollectionUtils.isEmpty(files) || StringUtils.isEmpty(formFieldType)) {
            return null;
        }

        final RecordVO recordVO = new RecordVO();
        recordVO.setFormFieldType(formFieldType);
        recordVO.setFiles(files);
        recordVO.setApplicationId(applicationId);
        recordVO.setFormModelId(formModelId);
        log.info("recordVO: " + recordVO);
        return qiqiaoFormsService.upload(recordVO);
    }

    private boolean callWps(final WpsFormatDTO wpsFormatDTO, final String url) {
        final String prefix = "[ContentManagementServiceImpl::callWps] ";
        log.info(prefix + "wpsFormatDTO: " + wpsFormatDTO + ", url: " + url);
        if (wpsFormatDTO == null) {
            return false;
        }

        String entityJson = JSONObject.toJSONString(wpsFormatDTO);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setEntity(new StringEntity(entityJson, "UTF-8"));
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Consts.UTF_8);
            }
            log.info(prefix + "result: " + result);
            // {"success":true,"message":"操作成功！","code":200,"result":true,"timestamp":1684994495353}
            EntityUtils.consume(entity);
            JSONObject resObj = JSONObject.parseObject(result);
            final Boolean ret = resObj.getBoolean("result");
            return ret != null && ret;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
