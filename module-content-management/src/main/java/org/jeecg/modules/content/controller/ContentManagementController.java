package org.jeecg.modules.content.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.content.dto.EcmFileDTO;
import org.jeecg.modules.content.dto.FileModel;
import org.jeecg.modules.content.dto.PageModel;
import org.jeecg.modules.content.service.IContentManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Tong Ling
 * @date 2021-11-25
 */
@Api(tags = "支撑-内容管理平台") @RestController @RequestMapping("/contentManagement")
public class ContentManagementController {
    @Autowired @Qualifier("biiContentManagementService") private IContentManagementService contentManagementService;

    // https://stackoverflow.com/questions/14799966/detect-an-executable-file-in-java
    // private boolean isExecutable(File file) {
    //     byte[] firstBytes = new byte[4];
    //     try {
    //         FileInputStream input = new FileInputStream(file);
    //         input.read(firstBytes);

    //         // Check for Windows executable
    //         return firstBytes[0] == 0x4d && firstBytes[1] == 0x5a;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return false;
    // }

    private boolean isExecutable(MultipartFile file) {
        if (file == null) {
            return false;
        }

        boolean executable = false;

        // naively check extension
        {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            Set<String> executableExtensionSet = new HashSet<>();
            executableExtensionSet.add("");
            executableExtensionSet.add("sh");
            executableExtensionSet.add("exe");
            executableExtensionSet.add("bin");
            executableExtensionSet.add("jar");

            executable = executableExtensionSet.contains(extension);
        }

        // check magic number to see if executable on Windows
        // if (!executable) {
        //     Path filepath = Paths.get("/tmp/", file.getOriginalFilename());
        //     try (OutputStream os = Files.newOutputStream(filepath)) {
        //         os.write(file.getBytes());
        //         File tmpFile = new File(String.valueOf(filepath));
        //         executable = isExecutable(tmpFile);
        //         tmpFile.delete();
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // }

        return executable;
    }

    @ApiOperation("上传文件") @PostMapping("upload") public Result<?> uploadFiles(MultipartFile[] files) {
        for (MultipartFile file : files) {
            if (file == null) {
                return Result.error("文件为空！");
            }
            if (isExecutable(file)) {
                return Result.error("不允许上传可执行文件：" + file.getOriginalFilename());
            }
        }

        final List<EcmFileDTO> ecmFileDTOs = contentManagementService.uploadFiles(files);
        if (ecmFileDTOs == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("上传【");
            for (final MultipartFile file : files) {
                sb.append(file.getOriginalFilename());
            }
            sb.append("】失败!");
            return Result.error(sb.toString());
        }

        return Result.OK(ecmFileDTOs);
    }

    @ApiOperation("物理删除文件") @GetMapping("deleteFileReal")
    public JSONObject deleteRealFiles(@RequestParam("objectId") String objectId,
        @RequestParam("fileIds") String fileIds) {
        final JSONObject jsonObject = contentManagementService.deleteFiles(objectId, fileIds);
        return jsonObject;
    }

    @AutoLog("支撑-内容管理平台-获取单个文件下载链接") @ApiOperation("获取单个文件下载链接") @GetMapping("download")
    public String getDownloadUrl(@RequestParam("fileId") String fileId) {
        final String downloadUrl = contentManagementService.getDownloadUrl(fileId);
        return downloadUrl;
    }

    @AutoLog("支撑-内容管理平台-获取文件预览链接") @ApiOperation("获取文件预览链接") @GetMapping("preview")
    public String getPreviewUrl(@RequestParam("fileId") String fileId,
        @RequestParam(value = "mark", required = false, defaultValue = "北京基础设施投资有限公司") String mark) {
        final String previewUrl = contentManagementService.getPreviewUrl(fileId, mark);
        return previewUrl;
    }

    @AutoLog("支撑-内容管理平台-全文检索") @ApiOperation("全文检索") @GetMapping("searchNew")
    public PageModel<FileModel> deleteRealFiles(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam("searchMessage") String searchMessage) {
        final PageModel<FileModel> result = contentManagementService.searchNew(pageNo, pageSize, searchMessage);
        return result;
    }
}
