package org.jeecg.modules.regulation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.regulation.entity.ZyRegulationBiiDept;

import java.util.List;

/**
 * @author Tong Ling
 * @date 2023-05-19
 */
public interface IZyRegulationBiiDeptService extends IService<ZyRegulationBiiDept> {
    List<ZyRegulationBiiDept> getByRegulationCodeAndVersion(String regulationCode, String version);

    List<ZyRegulationBiiDept> getByQiqiaoDeptIdList(List<String> deptIdList);
}
