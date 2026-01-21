package org.jeecg.modules.content.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.constant.ZyFileCategoryEnum;
import org.jeecg.modules.content.dto.EcmFileDTO;
import org.jeecg.modules.content.dto.ZyFileDTO;
import org.jeecg.modules.content.entity.ZyFile;
import org.jeecg.modules.content.mapper.ZyFileMapper;
import org.jeecg.modules.content.service.IZyFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;

@Service public class ZyFileServiceImpl extends ServiceImpl<ZyFileMapper, ZyFile> implements IZyFileService {
    @Autowired ZyFileMapper zyFileMapper;

    private Page<ZyFile> queryPageList(final Page<ZyFile> page, final ZyFileDTO zyFileDTO) {
        final List<ZyFile> zyFileList = zyFileMapper.queryPageList(page, zyFileDTO);
        page.setRecords(zyFileList);
        return page;
    }

    private ZyFile convertEcmFileDTO2ZyFile(final ZyFileDTO zyFileDTO, final EcmFileDTO ecmFileDTO) {
        if (zyFileDTO == null || ecmFileDTO == null) {
            return null;
        }

        ZyFile zyFile = new ZyFile().setCategory(zyFileDTO.getCategory()).setParentId(zyFileDTO.getParentId())
            .setKeyword(zyFileDTO.getKeyword()).setAuthor(ecmFileDTO.getAuthor()).setObjectId(ecmFileDTO.getObjectId())
            .setDocId(ecmFileDTO.getDocId()).setFileId(ecmFileDTO.getFileId()).setFileName(ecmFileDTO.getFileName())
            .setFolderFilePath(ecmFileDTO.getFolderFilePath()).setWpsId(ecmFileDTO.getWpsId())
            .setPreviewUrl(ecmFileDTO.getPreviewUrl()).setDownloadUrl(ecmFileDTO.getDownloadUrl());
        zyFile.setDelFlag(0);
        return zyFile;
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public boolean addEcmFileDTOList(final ZyFileDTO zyFileDTO, final List<EcmFileDTO> ecmFileDTOList) {
        if (zyFileDTO == null) {
            return false;
        }

        if (ecmFileDTOList == null || ecmFileDTOList.isEmpty()) {
            return true;
        }

        for (final EcmFileDTO ecmFileDTO : ecmFileDTOList) {
            final ZyFile zyFile = convertEcmFileDTO2ZyFile(zyFileDTO, ecmFileDTO);
            if (zyFile == null || !save(zyFile)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return false;
            }
        }

        return true;
    }

    @Override public Page<ZyFile> queryFilePageList(final Page<ZyFile> page, final Integer parentId,
        final ZyFileCategoryEnum category) {
        return queryFilePageList(page, parentId, category, null);
    }

    @Override public Page<ZyFile> queryFilePageList(final Page<ZyFile> page, final Integer parentId,
        final ZyFileCategoryEnum category, final String keyword) {
        if (category == null) {
            return page;
        }
        ZyFileDTO queryDTO = new ZyFileDTO().setParentId(parentId).setCategory(category.value()).setKeyword(keyword);
        return queryPageList(page, queryDTO);
    }

    @Override public List<ZyFile> queryFileList(final Integer parentId, final ZyFileCategoryEnum category) {
        return queryFileList(parentId, category, null);
    }

    @Override
    public List<ZyFile> queryFileList(final Integer parentId, final ZyFileCategoryEnum category, final String keyword) {
        if (category == null) {
            return new ArrayList<>();
        }
        ZyFileDTO zyFileDTO = new ZyFileDTO().setParentId(parentId).setCategory(category.value()).setKeyword(keyword);
        return zyFileMapper.queryList(zyFileDTO);
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public boolean addEcmFileDTOList(final Integer parentId, final List<EcmFileDTO> ecmFileDTOList,
        final ZyFileCategoryEnum category) {
        return addEcmFileDTOList(parentId, ecmFileDTOList, category, null);
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public boolean addEcmFileDTOList(final Integer parentId, final List<EcmFileDTO> ecmFileDTOList,
        final ZyFileCategoryEnum category, final String keyword) {
        if (category == null) {
            return false;
        }
        ZyFileDTO zyFileDTO = new ZyFileDTO().setParentId(parentId).setCategory(category.value()).setKeyword(keyword);
        return addEcmFileDTOList(zyFileDTO, ecmFileDTOList);
    }

    @Override public int softDeleteFiles(final Integer parentId, final ZyFileCategoryEnum category) {
        return softDeleteFiles(parentId, category, null);
    }

    @Override
    public int softDeleteFiles(final Integer parentId, final ZyFileCategoryEnum category, final String keyword) {
        ZyFileDTO zyFileDTO = new ZyFileDTO().setParentId(parentId).setCategory(category.value()).setKeyword(keyword);
        return zyFileMapper.softDelete(zyFileDTO);
    }
}
