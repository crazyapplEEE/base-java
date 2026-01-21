package org.jeecg.modules.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaHistory;

import java.util.Date;
import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Mapper public interface ZyRegulationBjmoaHistoryMapper extends BaseMapper<ZyRegulationBjmoaHistory> {
    List<ZyRegulationBjmoaHistory> queryByIdentifier(@Param("identifier") String identifier);

    List<ZyRegulationBjmoaHistory> queryByIdentifierAndVersion(@Param("identifier") String identifier, @Param("version") String version);

    List<ZyRegulationBjmoaHistory> queryByIdentifierAndVersionAndCode(@Param("identifier") String identifier,
                                                                      @Param("version") String version, @Param("code") String code);

    int inactivateByIdentifier(@Param("identifier") String identifier, @Param("abolishTime") Date abolishTime);

    List<ZyRegulationBjmoaHistory> queryNewestRegulationByIdentifier(@Param("identifier") String identifier);
}
