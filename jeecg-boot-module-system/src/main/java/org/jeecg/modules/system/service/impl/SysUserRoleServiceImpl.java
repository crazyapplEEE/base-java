package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.mapper.SysUserRoleMapper;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户角色表 服务实现类
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Service public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
    implements ISysUserRoleService {
    @Override public boolean removeSysUserRole(final String userId, final String roleId) {
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("role_id", roleId);
        return remove(queryWrapper);
    }

    @Override public boolean addSysUserRole(final String userId, final String roleId) {
        SysUserRole sysUserRole = new SysUserRole(userId, roleId);
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("role_id", roleId);
        try {
            SysUserRole one = getOne(queryWrapper);
            if (one == null) {
                return save(sysUserRole);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
