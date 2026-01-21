package org.jeecg.modules.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.regulation.entity.ZyRegulationArchive;

@Mapper
public interface ZyRegulationArchiveMapper extends BaseMapper<ZyRegulationArchive> {

    @Select("SELECT * FROM zy_regulation_archive WHERE qiqiao_regulation_id = #{qiqiaoRegulationId}")
    ZyRegulationArchive selectByQiqiaoRegulationId(@Param("qiqiaoRegulationId") String qiqiaoRegulationId);
}