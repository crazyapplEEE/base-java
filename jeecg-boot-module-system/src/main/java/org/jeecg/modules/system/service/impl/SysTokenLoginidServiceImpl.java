package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.ISysTokenLoginidService;
import org.jeecg.common.system.vo.SysTokenLoginid;
import org.jeecg.modules.system.mapper.SysTokenLoginidMapper;
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
@Service @Slf4j public class SysTokenLoginidServiceImpl extends ServiceImpl<SysTokenLoginidMapper, SysTokenLoginid>
    implements ISysTokenLoginidService {
    @Autowired SysTokenLoginidMapper sysTokenLoginidMapper;

    @Override public boolean save(final String token, final String loginid) {
        SysTokenLoginid sysTokenLoginid = new SysTokenLoginid().setToken(token).setLoginid(loginid);
        return save(sysTokenLoginid);
    }

    @Override public String queryLoginidByToken(final String token) {
        return sysTokenLoginidMapper.queryLoginidByToken(token);
    }
}
