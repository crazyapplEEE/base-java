package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.common.system.vo.SysTokenLoginid;

@Mapper public interface SysTokenLoginidMapper extends BaseMapper<SysTokenLoginid> {
    @Select("SELECT loginid FROM sys_token_loginid WHERE token = #{token}") String queryLoginidByToken(
        @Param("token") String token);
}
