package org.jeecg.config.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * mybatis拦截器，自动注入创建人、创建时间、修改人、修改时间
 *
 * @Author scott
 * @Date 2019-01-19
 */
@Slf4j @Component
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class MybatisInterceptor implements Interceptor {

    @Override public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement)invocation.getArgs()[0];
        String sqlId = mappedStatement.getId();
        log.debug("------sqlId------" + sqlId);
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];
        log.debug("------sqlCommandType------" + sqlCommandType);

        if (parameter == null) {
            return invocation.proceed();
        }
        if (SqlCommandType.INSERT == sqlCommandType) {
            LoginUser sysUser = this.getLoginUser(); // 登录人账号
            Field[] fields = oConvertUtils.getAllFields(parameter);
            for (Field field : fields) {
                log.debug("------field.name------" + field.getName());
                field.setAccessible(true);
                try {
                    Object localVar = field.get(parameter);
                    switch (field.getName()) {
                        case "createBy": {
                            if (localVar == null && sysUser != null) {
                                field.set(parameter, sysUser.getRealname());
                            }
                            break;
                        }
                        case "creatorId": {
                            if (localVar == null && sysUser != null) {
                                field.set(parameter, sysUser.getOaId());
                            }
                            break;
                        }
                        case "createDept": {
                            if (localVar == null && sysUser != null) {
                                field.set(parameter, sysUser.getDept());
                            }
                            break;
                        }
                        case "createDeptId": {
                            if (localVar == null && sysUser != null) {
                                field.set(parameter, sysUser.getDeptOaId());
                            }
                            break;
                        }
                        case "createSubCompany": {
                            if (localVar == null && sysUser != null) {
                                field.set(parameter, sysUser.getCompany());
                            }
                            break;
                        }
                        case "createSubCompanyId": {
                            if (localVar == null && sysUser != null) {
                                field.set(parameter, sysUser.getCompanyOaId());
                            }
                            break;
                        }
                        case "createTime": {
                            if (localVar == null) {
                                field.set(parameter, DateUtils.getCurrentDateTimeInBeijing());
                            }
                            break;
                        }
                        case "createMpUserId": {
                            if (localVar == null && sysUser != null) {
                                field.set(parameter, sysUser.getMpUserId() == null ? "" : sysUser.getMpUserId());
                            }
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                field.setAccessible(false);
            }
        }
        if (SqlCommandType.UPDATE == sqlCommandType) {
            LoginUser sysUser = this.getLoginUser();
            Field[] fields = null;
            if (parameter instanceof ParamMap) {
                ParamMap<?> p = (ParamMap<?>)parameter;
                //update-begin-author:scott date:20190729 for:批量更新报错issues/IZA3Q--
                if (p.containsKey("et")) {
                    parameter = p.get("et");
                } else {
                    parameter = p.get("param1");
                }
                //update-end-author:scott date:20190729 for:批量更新报错issues/IZA3Q-

                //update-begin-author:scott date:20190729 for:更新指定字段时报错 issues/#516-
                if (parameter == null) {
                    return invocation.proceed();
                }
                //update-end-author:scott date:20190729 for:更新指定字段时报错 issues/#516-

                fields = oConvertUtils.getAllFields(parameter);
            } else {
                fields = oConvertUtils.getAllFields(parameter);
            }

            for (Field field : fields) {
                log.debug("------field.name------" + field.getName());
                field.setAccessible(true);
                try {
                    Object localVar = field.get(parameter);
                    switch (field.getName()) {
                        case "updateBy": {
                            if (sysUser == null) {
                                // should never happen
                                field.set(parameter, null);
                            } else {
                                field.set(parameter, sysUser.getRealname());
                            }
                            break;
                        }
                        case "updaterId": {
                            if (sysUser == null) {
                                // should never happen
                                field.set(parameter, null);
                            } else {
                                field.set(parameter, sysUser.getOaId());
                            }
                            break;
                        }
                        case "updateDept": {
                            if (sysUser == null) {
                                // should never happen
                                field.set(parameter, null);
                            } else {
                                field.set(parameter, sysUser.getDept());
                            }
                            break;
                        }
                        case "updateDeptId": {
                            if (sysUser == null) {
                                // should never happen
                                field.set(parameter, null);
                            } else {
                                field.set(parameter, sysUser.getDeptOaId());
                            }
                            break;
                        }
                        case "updateSubCompany": {
                            if (sysUser == null) {
                                // should never happen
                                field.set(parameter, null);
                            } else {
                                field.set(parameter, sysUser.getCompany());
                            }
                            break;
                        }
                        case "updateSubCompanyId": {
                            if (sysUser == null) {
                                // should never happen
                                field.set(parameter, null);
                            } else {
                                field.set(parameter, sysUser.getCompanyOaId());
                            }
                            break;
                        }
                        case "updateTime": {
                            field.set(parameter, DateUtils.getCurrentDateTimeInBeijing());
                            break;
                        }
                        case "updateMpUserId": {
                            if (sysUser == null) {
                                // should never happen
                                field.set(parameter, null);
                            } else {
                                field.set(parameter, sysUser.getMpUserId() == null ? "" : sysUser.getMpUserId());
                            }
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                field.setAccessible(false);
            }
        }
        return invocation.proceed();
    }

    @Override public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override public void setProperties(Properties properties) {
        // TODO Auto-generated method stub
    }

    //update-begin--Author:scott  Date:20191213 for：关于使用Quartz 开启线程任务， #465
    private LoginUser getLoginUser() {
        LoginUser sysUser = null;
        try {
            sysUser =
                SecurityUtils.getSubject().getPrincipal() != null ? (LoginUser)SecurityUtils.getSubject().getPrincipal()
                    : null;
        } catch (Exception e) {
            //e.printStackTrace();
            sysUser = null;
        }
        return sysUser;
    }
    //update-end--Author:scott  Date:20191213 for：关于使用Quzrtz 开启线程任务， #465

}
