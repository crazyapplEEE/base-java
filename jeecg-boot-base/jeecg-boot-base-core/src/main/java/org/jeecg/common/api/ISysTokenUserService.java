package org.jeecg.common.api;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.system.vo.SysTokenUser;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface ISysTokenUserService extends IService<SysTokenUser> {
    SysTokenUser queryByToken(final String token);
}
