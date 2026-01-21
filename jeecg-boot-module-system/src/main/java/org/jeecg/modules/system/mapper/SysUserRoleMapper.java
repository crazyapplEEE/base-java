package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.system.entity.SysUserRole;

import java.util.List;

/**
 * <p>
 * 用户角色表 Mapper 接口
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Mapper public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    List<String> getRoleByUserName(@Param("username") String username);

    List<String> getRoleIdByUserName(@Param("username") String username);

    @Delete("DELETE FROM sys_user_role where user_id = #{userId}") void deleteUser(@Param("userId") String userId);
}
