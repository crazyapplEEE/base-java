package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.ISysLoginidRolesService;
import org.jeecg.common.system.vo.SysLoginidRoles;
import org.jeecg.modules.system.mapper.SysLoginidRolesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @Author: scott
 * @Date: 2018-12-20
 */
@Service @Slf4j public class SysLoginidRolesServiceImpl extends ServiceImpl<SysLoginidRolesMapper, SysLoginidRoles>
    implements ISysLoginidRolesService {
    @Autowired SysLoginidRolesMapper sysLoginidRolesMapper;

    @Override public boolean saveByLoginid(String loginid, String roles) {
        SysLoginidRoles sysLoginidRoles = sysLoginidRolesMapper.getByLoginid(loginid);
        if (sysLoginidRoles == null) {
            sysLoginidRoles = new SysLoginidRoles().setLoginid(loginid).setRoles(roles);
            return save(sysLoginidRoles);
        }
        sysLoginidRoles.setLoginid(loginid).setRoles(roles);
        return updateById(sysLoginidRoles);
    }

    @Override public String queryRolesByLoginid(final String loginid) {
        return sysLoginidRolesMapper.queryRolesByLoginid(loginid);
    }
}
