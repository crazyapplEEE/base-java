package org.jeecg.modules.regulation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.regulation.entity.ZyBiiRegulationAdmin;
import org.jeecg.modules.regulation.entity.ZyRead;
import org.jeecg.modules.regulation.mapper.ZyBiiRegulationAdminMapper;
import org.jeecg.modules.regulation.mapper.ZyReadMapper;
import org.jeecg.modules.regulation.service.IZyBiiRegulationAdminService;
import org.jeecg.modules.regulation.service.IZyReadService;
import org.springframework.stereotype.Service;

/**
 * @author Tong Ling
 * @date 2023-11-17
 */
@Service public class ZyBiiRegulationAdminServiceImpl
    extends ServiceImpl<ZyBiiRegulationAdminMapper, ZyBiiRegulationAdmin> implements IZyBiiRegulationAdminService {
}
