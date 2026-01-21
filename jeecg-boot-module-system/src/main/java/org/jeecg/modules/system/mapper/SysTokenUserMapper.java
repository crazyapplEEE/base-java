package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.common.system.vo.SysTokenUser;

import java.util.List;

@Mapper public interface SysTokenUserMapper extends BaseMapper<SysTokenUser> {
    @Select("SELECT * FROM sys_token_user WHERE token = #{token}") List<SysTokenUser> queryByToken(
        @Param("token") String token);
}
