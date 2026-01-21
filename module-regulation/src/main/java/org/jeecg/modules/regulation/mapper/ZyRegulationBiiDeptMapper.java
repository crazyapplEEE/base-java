package org.jeecg.modules.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiDept;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiDept;

import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
@Mapper public interface ZyRegulationBiiDeptMapper extends BaseMapper<ZyRegulationBiiDept> {
    List<ZyRegulationBiiDept> getByRegulationCodeAndVersion(@Param("code") String code, @Param("version") String version);

    List<ZyRegulationBiiDept> getByQiqiaoDeptIdList(@Param("deptIdList") List<String> deptIdList);
}
