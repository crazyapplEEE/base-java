package org.jeecg.modules.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regulation.entity.ZyRead;
import org.jeecg.modules.regulation.entity.ZyRelatedRegulation;

import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Mapper public interface ZyRelatedRegulationMapper extends BaseMapper<ZyRelatedRegulation> {
    ZyRelatedRegulation queryByRegulationIdentifiers(@Param("regulationIdentifierA") String regulationIdentifierA,
                                                     @Param("regulationIdentifierB") String regulationIdentifierB);

    List<ZyRelatedRegulation> queryByRegulationIdentifier(@Param("regulationIdentifier") String regulationIdentifier);

    int deleteByRegulationIdentifier(@Param("regulationIdentifier") String regulationIdentifier);
}
