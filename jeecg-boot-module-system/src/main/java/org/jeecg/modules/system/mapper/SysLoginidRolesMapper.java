package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.common.system.vo.SysLoginidRoles;

@Mapper public interface SysLoginidRolesMapper extends BaseMapper<SysLoginidRoles> {
    @Select("SELECT * FROM sys_loginid_roles WHERE loginid = #{loginid}")
    SysLoginidRoles getByLoginid(@Param("loginid") String loginid);

    @Select("SELECT roles FROM sys_loginid_roles WHERE loginid = #{loginid}")
    String queryRolesByLoginid(@Param("loginid") String loginid);
}
