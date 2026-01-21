package org.jeecg.modules.content.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.content.dto.EcmFileDTO;
import org.jeecg.modules.content.dto.FileModel;
import org.jeecg.modules.content.dto.PageModel;
import org.jeecg.modules.content.dto.WpsFormatDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

public interface IContentManagementService {
    /**
     * 上传多个文件
     *
     * @param multipartFiles
     * @return
     *  {
     *      "code": "200",
     *      "msg": null,
     *      "data": {
     *          "objectId": "XXX",
     *          "docId": "000",
     *          "docTypeId": "1",
     *          "sourceAppId": "XXX",
     *          "author": null,
     *          "versionLabel": "2",
     *          "ecmFiles": [
     *              {
     *                  "fileId": "XXX",
     *                  "fileName": "XXX.doc",
     *                  "folderFilePath": "",
     *                  "wpsId": "XXX"
     *              }
     *          ]
     *      }
     *  }
     */
    JSONObject uploadMultipartFileList(MultipartFile[] multipartFiles);

    JSONObject upload(List<File> files, String docId);

    List<EcmFileDTO> uploadFiles(MultipartFile[] multipartFiles);

    List<EcmFileDTO> uploadFiles(List<File> files);

    List<EcmFileDTO> uploadFiles(List<File> files, String docId);

    /**
     * 物理删除文件
     *
     * @param objectId
     * @param fileIds
     * @return
     */
    JSONObject deleteFiles(final String objectId, final String fileIds);

    String getDownloadUrl(final String fileId);

    String getDownloadNewestUrl(final String docId);

    String getBatchDownloadUrl(final List<String> fileIds);

    String getBatchDownloadUrl(final String fileIds);

    String getPreviewUrl(final String fileId);

    /**
     * 获取文件预览链接
     *
     * @param fileId 文件ID
     * @param mark   水印
     * @return 文件预览链接
     */
    String getPreviewUrl(final String fileId, final String mark);

    JSONObject renameFile(String docId, String newFileName);

    JSONObject renameFile(String docId, String fileId, String newFileName);

    PageModel<FileModel> searchNew(long pageNo, long pageSize, String searchMessage);

    boolean officeOperate(WpsFormatDTO wpsFormatDTO);

    boolean officeConvert(WpsFormatDTO wpsFormatDTO);

    boolean officeWrapheader(WpsFormatDTO wpsFormatDTO);

    JSONObject queryTask(String taskId);

    boolean downloadConvertedFile(String downloadId, String outFilePath, HttpServletResponse response);

    JSONArray upload2Qiqiao(List<File> files, String formFieldType, String applicationId, String formModelId);
}
