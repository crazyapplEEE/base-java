package org.jeecg.common.api;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface ISysLoginidRolesService {
    boolean saveByLoginid(final String loginid, final String roles);

    String queryRolesByLoginid(final String loginid);
}
