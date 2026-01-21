package org.jeecg.modules.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regulation.dto.RegulationQueryDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoa;
import org.jeecg.modules.regulation.vo.ZyRegulationBjmoaStatisticsVO;
import org.jeecg.modules.regulation.vo.ZyRegulationBjmoaVO;

import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Mapper public interface ZyRegulationBjmoaMapper extends BaseMapper<ZyRegulationBjmoa> {

    ZyRegulationBjmoa queryByContentFileId(@Param("contentFileId") String contentFileId);

    ZyRegulationBjmoa queryByIdentifier(@Param("identifier") String identifier);

    ZyRegulationBjmoa queryByQiqiaoRegulationId(@Param("qiqiaoRegulationId") String qiqiaoRegulationId);

    List<ZyRegulationBjmoaVO> queryNewestVersionPageList(Page<ZyRegulationBjmoaVO> page,
        @Param("queryDTO") RegulationQueryDTO queryDTO);

    int activateByQiqiaoRegulationId(@Param("qiqiaoRegulationId") String qiqiaoRegulationId);

    int inactivateByIdentifier(@Param("identifier") String identifier);

    List<ZyRegulationBjmoaStatisticsVO> queryRegulationStatistics(@Param("year") Integer year);

    String queryRelatedRegulationLevelId(@Param("identifier") String identifier);
}
