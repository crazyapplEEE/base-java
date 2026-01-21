package org.jeecg.modules.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiHistory;

import java.util.Date;
import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Mapper public interface ZyRegulationBiiHistoryMapper extends BaseMapper<ZyRegulationBiiHistory> {
    List<ZyRegulationBiiHistory> queryByIdentifier(@Param("identifier") String identifier);

    int updateAbolishTime(@Param("identifier") String identifier, @Param("abolishTime") Date abolishTime);
}
