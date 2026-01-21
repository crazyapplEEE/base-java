package org.jeecg.modules.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regulation.dto.RegulationQueryDTO;
import org.jeecg.modules.regulation.entity.ZyRegulationBii;
import org.jeecg.modules.regulation.vo.ZyRegulationBiiVO;

import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Mapper public interface ZyRegulationBiiMapper extends BaseMapper<ZyRegulationBii> {
    int removeByCode(@Param("code") String code);

    ZyRegulationBii queryByIdentifier(@Param("identifier") String identifier);

    List<ZyRegulationBiiVO> queryNewestVersionPageList(Page<ZyRegulationBiiVO> page,
                                                       @Param("queryDTO") RegulationQueryDTO queryDTO);

    List<ZyRegulationBii> queryList(@Param("queryDTO") RegulationQueryDTO queryDTO);

    int inactivateById(@Param("id") Integer id);

    int inactivateByIdentifier(@Param("identifier") String identifier);

    ZyRegulationBii queryByContentFileId(@Param("contentFileId") String contentFileId);
}
