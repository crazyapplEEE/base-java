package org.jeecg.modules.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.content.dto.ZyFileDTO;
import org.jeecg.modules.content.entity.ZyFile;

import java.util.List;

@Mapper public interface ZyFileMapper extends BaseMapper<ZyFile> {
    List<ZyFile> queryPageList(final Page<ZyFile> page, @Param("zyFileDTO") final ZyFileDTO zyFileDTO);

    int softDelete(@Param("zyFileDTO") final ZyFileDTO zyFileDTO);

    List<ZyFile> queryList(@Param("zyFileDTO") final ZyFileDTO zyFileDTO);
}
