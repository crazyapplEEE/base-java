package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.ISysTokenUserService;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.SysTokenUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.system.mapper.SysTokenUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service @Slf4j public class SysTokenUserServiceImpl extends ServiceImpl<SysTokenUserMapper, SysTokenUser>
    implements ISysTokenUserService {
    @Autowired SysTokenUserMapper sysTokenUserMapper;
    @Autowired RedisUtil redisUtil;

    @Override public SysTokenUser queryByToken(final String token) {
        // 从redis里查询
        return (SysTokenUser)redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + token);
    }
}
