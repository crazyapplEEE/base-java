package org.jeecg.modules.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regulation.entity.ZyRegulationBjmoaDept;

import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Mapper public interface ZyRegulationBjmoaDeptMapper extends BaseMapper<ZyRegulationBjmoaDept> {
    List<ZyRegulationBjmoaDept> getByRegulationCodeAndVersion(@Param("code") String code, @Param("version") String version);

    List<ZyRegulationBjmoaDept> getByIdentifierAndVersion(@Param("identifier") String identifier, @Param("version") String version);

    List<ZyRegulationBjmoaDept> getByIdentifier(@Param("identifier") String identifier);

    List<ZyRegulationBjmoaDept> getByQiqiaoDeptIdList(@Param("deptIdList") List<String> deptIdList);
}
