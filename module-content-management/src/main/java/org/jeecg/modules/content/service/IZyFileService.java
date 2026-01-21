package org.jeecg.modules.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.constant.ZyFileCategoryEnum;
import org.jeecg.modules.content.dto.EcmFileDTO;
import org.jeecg.modules.content.dto.ZyFileDTO;
import org.jeecg.modules.content.entity.ZyFile;

import java.util.List;

public interface IZyFileService extends IService<ZyFile> {
    Page<ZyFile> queryFilePageList(final Page<ZyFile> page, final Integer parentId, final ZyFileCategoryEnum category);

    Page<ZyFile> queryFilePageList(final Page<ZyFile> page, final Integer parentId, final ZyFileCategoryEnum category,
        final String keyword);

    List<ZyFile> queryFileList(final Integer parentId, final ZyFileCategoryEnum category);

    List<ZyFile> queryFileList(final Integer parentId, final ZyFileCategoryEnum category, final String keyword);

    boolean addEcmFileDTOList(final ZyFileDTO zyFileDTO, final List<EcmFileDTO> ecmFileDTOList);

    boolean addEcmFileDTOList(final Integer parentId, final List<EcmFileDTO> ecmFileDTOList,
        final ZyFileCategoryEnum category);

    boolean addEcmFileDTOList(final Integer parentId, final List<EcmFileDTO> ecmFileDTOList,
        final ZyFileCategoryEnum category, final String keyword);

    int softDeleteFiles(final Integer parentId, final ZyFileCategoryEnum category);

    int softDeleteFiles(final Integer parentId, final ZyFileCategoryEnum category, final String keyword);
}
