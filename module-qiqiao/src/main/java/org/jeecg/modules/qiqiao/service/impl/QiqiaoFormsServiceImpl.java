package org.jeecg.modules.qiqiao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jeecg.modules.common.constant.HttpMethods;
import org.jeecg.modules.common.utils.HttpPostMultipart;
import org.jeecg.modules.common.utils.StringUtils;
import org.jeecg.modules.qiqiao.constants.RecordVO;
import org.jeecg.modules.qiqiao.service.IQiqiaoFormsService;
import org.jeecg.modules.qiqiao.service.IQiqiaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;

@Slf4j @Service public class QiqiaoFormsServiceImpl implements IQiqiaoFormsService {
    @Autowired private IQiqiaoService qiqiaoService;

    @Value("${biisaas.baseUrl}") private String baseUrl;
    @Value("${biisaas.loginUserId}") private String loginUserId;
    @Value("${biisaas.table.saveOrUpdate}") private String saveOrUpdateUrl;
    @Value("${biisaas.table.queryById}") private String queryByIdUrl;
    @Value("${biisaas.table.query}") private String queryUrl;
    @Value("${biisaas.table.page}") private String pageUrl;
    @Value("${biisaas.table.delete}") private String deleteUrl;
    @Value("${biisaas.table.batchSave}") private String batchSaveUrl;
    @Value("${biisaas.table.batchUpdate}") private String batchUpdateUrl;
    @Value("${biisaas.table.upload}") private String uploadUrl;
    @Value("${biisaas.table.download}") private String downloadUrl;

    @Override public JSONObject queryById(final RecordVO recordVO) {
        String prefix = "queryById: ";
        String result = qiqiaoService.requestWith(
            baseUrl + queryByIdUrl.replace("{applicationId}", recordVO.getApplicationId())
                .replace("{formModelId}", recordVO.getFormModelId()).replace("{id}", recordVO.getId()), HttpMethods.GET,
            null, qiqiaoService.getAccessToken(false), 1);
        if (StringUtils.isNotEmpty(result)) {
            JSONObject resultJson = JSON.parseObject(result);
            if ("0".equals(resultJson.getString("code"))) {
                // 如果version=0就是不存在
                Integer version = ((JSONObject)resultJson.get("data")).getInteger("version");
                if (version == 0) {
                    return null;
                } else {
                    return (JSONObject)resultJson.get("data");
                }
            }
            return null;
        } else {
            log.error(prefix + result);
            return null;
        }
    }

    @Override public JSONObject page(final RecordVO recordVO) {
        String prefix = "page: ";
        String result;
        recordVO.setDefaultRecordVO();
        if (CollectionUtil.isNotEmpty(recordVO.getFilter())) {
            result = qiqiaoService.requestWith(
                baseUrl + queryUrl.replace("{applicationId}", recordVO.getApplicationId())
                    .replace("{formModelId}", recordVO.getFormModelId()) + recordVO.getRequestParam(), HttpMethods.POST,
                recordVO.getFilter(), qiqiaoService.getAccessToken(false), 1);
        } else {
            result = qiqiaoService.requestWith(baseUrl + pageUrl.replace("{applicationId}", recordVO.getApplicationId())
                    .replace("{formModelId}", recordVO.getFormModelId()) + recordVO.getRequestParam(), HttpMethods.GET,
                null, qiqiaoService.getAccessToken(false), 1);
        }
        if (StringUtils.isNotEmpty(result)) {
            JSONObject resultJson = JSON.parseObject(result);
            if ("0".equals(resultJson.getString("code"))) {
                return (JSONObject)resultJson.get("data");
            } else {
                log.error(prefix + result);
                return null;
            }
        } else {
            log.error(prefix + result);
            return null;
        }
    }

    @Override public boolean deleteById(final RecordVO recordVO) {
        String prefix = "deleteById: ";
        String result = qiqiaoService.requestWith(
            baseUrl + deleteUrl.replace("{applicationId}", recordVO.getApplicationId())
                .replace("{formModelId}", recordVO.getFormModelId()).replace("{id}", recordVO.getId()),
            HttpMethods.DELETE, null, qiqiaoService.getAccessToken(false), 0);
        return true;
    }

    // 插入数据
    @Override public JSONObject saveOrUpdate(final RecordVO recordVO) {
        String prefix = "saveOrUpdate: ";
        Map<String, Object> requestObj = new HashMap<>();
        requestObj.put("variables", recordVO.getData());
        if (StringUtils.isEmpty(recordVO.getLoginUserId())) {
            requestObj.put("loginUserId", loginUserId);
        } else {
            requestObj.put("loginUserId", recordVO.getLoginUserId());
        }
        // 如果有id，则先查询id是否存在
        if (StringUtils.isNotEmpty(recordVO.getId())) {
            requestObj.put("id", recordVO.getId());
            JSONObject existRecord = queryById(recordVO);
            if (existRecord != null) {
                // 判断JSON数据是否一致
                // Map转为JSON
                String existRecordJson = JSON.toJSONString(recordVO.getData());
                if (existRecord.getJSONObject("variables").toJSONString().equals(existRecordJson)) {
                    log.info(prefix + "数据一致，不需要更新");
                    return existRecord;
                }
                // 执行更新
                Integer version = existRecord.getInteger("version");
                requestObj.put("version", version);
                log.info(prefix + "执行更新 " + requestObj);
            } else {
                // 执行新增
                log.info(prefix + "执行新增 " + requestObj);
            }
        }
        String result = qiqiaoService.requestWith(
            baseUrl + saveOrUpdateUrl.replace("{applicationId}", recordVO.getApplicationId())
                .replace("{formModelId}", recordVO.getFormModelId()), HttpMethods.POST, requestObj,
            qiqiaoService.getAccessToken(false), 1);
        if (StringUtils.isNotEmpty(result)) {
            log.info(prefix + result);
            return JSON.parseObject(result).getJSONObject("data");
        } else {
            log.error(prefix + result);
            return null;
        }
    }

    @Override public boolean batchSave(final List<RecordVO> recordVOList) {
        if (CollectionUtil.isEmpty(recordVOList)) {
            return true;
        }
        String prefix = "batchSave: ";
        List<Map> requestObj = new ArrayList<>();
        for (RecordVO recordVO : recordVOList) {
            Map<String, Object> item = new HashMap<>();
            item.put("variables", recordVO.getData());
            if (StringUtils.isEmpty(recordVO.getLoginUserId())) {
                item.put("loginUserId", loginUserId);
            } else {
                item.put("loginUserId", recordVO.getLoginUserId());
            }
            item.put("id", recordVO.getId());
            requestObj.add(item);
        }
        String result = null;
        result = qiqiaoService.requestWith(
            baseUrl + batchSaveUrl.replace("{applicationId}", recordVOList.get(0).getApplicationId())
                .replace("{formModelId}", recordVOList.get(0).getFormModelId()), HttpMethods.POST, requestObj,
            qiqiaoService.getAccessToken(false), 0);
        return true;
    }

    @Override public boolean batchUpdate(final List<RecordVO> recordVOList) {
        if (CollectionUtil.isEmpty(recordVOList)) {
            return true;
        }
        String prefix = "batchUpdate: ";
        List<Map> requestObj = new ArrayList<>();
        for (RecordVO recordVO : recordVOList) {
            Map<String, Object> item = new HashMap<>();
            item.put("variables", recordVO.getData());
            if (StringUtils.isEmpty(recordVO.getLoginUserId())) {
                item.put("loginUserId", loginUserId);
            } else {
                item.put("loginUserId", recordVO.getLoginUserId());
            }
            item.put("id", recordVO.getId());
            requestObj.add(item);
        }
        String result = null;
        result = qiqiaoService.requestWith(
            baseUrl + batchUpdateUrl.replace("{applicationId}", recordVOList.get(0).getApplicationId())
                .replace("{formModelId}", recordVOList.get(0).getFormModelId()), HttpMethods.POST, requestObj,
            qiqiaoService.getAccessToken(false), 0);
        return true;
    }

    @Override public JSONArray upload(final RecordVO recordVO) {
        final String prefix = "[QiqiaoFormsServiceImpl::upload] ";
        if (recordVO == null) {
            log.warn(prefix + "recordVO IS NULL!");
            return null;
        }

        JSONArray resultJson = null;
        try {
            // 请求头
            final Map<String, String> headers = new HashMap<>();
            final String token = qiqiaoService.getAccessToken(false);
            headers.put("X-Auth0-Token", token);

            final String requestUrl = baseUrl + uploadUrl.replace("{applicationId}", recordVO.getApplicationId())
                .replace("{formModelId}", recordVO.getFormModelId());
            log.info(prefix + "requestUrl=" + requestUrl);
            HttpPostMultipart multipart = new HttpPostMultipart(requestUrl, "utf-8", headers);
            // post参数
            multipart.addFormField("fieldType", recordVO.getFormFieldType());
            // 上传文件
            final List<File> files = recordVO.getFiles();
            if (CollectionUtils.isEmpty(files)) {
                log.error(prefix + "FILES ARE EMPTY!");
                return null;
            }
            for (final File file : files) {
                multipart.addFilePart("files", file);
            }

            // 返回信息
            final String response = multipart.finish();
            log.info(prefix + "response: " + response);
            if (StringUtils.isNotEmpty(response)) {
                final JSONObject responseObj = JSON.parseObject(response);
                if ("0".equals(responseObj.getString("code"))) {
                    resultJson = responseObj.getJSONArray("data");
                }
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT! " + Arrays.toString(e.getStackTrace()));
        }

        return resultJson;
    }

    @Override public void download(final RecordVO recordVO, final String outFilePath) {
        final String prefix = "[QiqiaoFormsServiceImpl::download] ";
        if (recordVO == null || StringUtils.isEmpty(outFilePath)) {
            log.warn(prefix + "recordVO IS NULL or outputPath IS NULL!");
            return;
        }
        final String applicationId = recordVO.getApplicationId();
        final String fileId = recordVO.getFileId();
        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(fileId)) {
            log.warn("APPLICATION ID: " + applicationId + ", FILE ID: " + fileId);
            return;
        }

        try {
            HttpGet httpGet = new HttpGet(getDownloadUrl(recordVO));
            try (CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse httpResponse = client.execute(httpGet)) {
                File file = new File(outFilePath);
                try (OutputStream out = Files.newOutputStream(file.toPath())) {
                    httpResponse.getEntity().writeTo(out);
                }

                log.info("download file " + fileId + " for application " + applicationId + " success");
            } catch (Exception e) {
                log.error(prefix + "catch exception: " + Arrays.toString(e.getStackTrace()));
            }
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT! " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override public String getDownloadUrl(final RecordVO recordVO) {
        final String prefix = "[QiqiaoFormsServiceImpl::getDownloadUrl] ";
        if (recordVO == null) {
            log.warn(prefix + "recordVO IS NULL!");
            return null;
        }
        final String applicationId = recordVO.getApplicationId();
        final String fileId = recordVO.getFileId();
        if (StringUtils.isEmpty(applicationId) || StringUtils.isEmpty(fileId)) {
            log.warn("APPLICATION ID: " + applicationId + ", FILE ID: " + fileId);
            return null;
        }

        try {
            final String token = qiqiaoService.getAccessToken(false);
            final String requestUrl =
                baseUrl + downloadUrl.replace("{applicationId}", applicationId).replace("{fileId}", fileId)
                    .replace("{token}", token);
            log.info(prefix + "requestUrl=" + requestUrl);
            return requestUrl;
        } catch (Exception e) {
            log.error(prefix + "EXCEPTION CAUGHT! " + Arrays.toString(e.getStackTrace()));
        }
        return null;
    }
}
