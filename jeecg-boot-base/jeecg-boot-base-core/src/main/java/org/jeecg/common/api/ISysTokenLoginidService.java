package org.jeecg.common.api;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface ISysTokenLoginidService {
    boolean save(final String token, final String loginid);

    String queryLoginidByToken(final String token);
}
