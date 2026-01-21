package org.jeecg.common.constant;

/**
 * @author: huangxutao
 * @date: 2019-06-14
 * @description: 缓存常量
 */
public interface CacheConstant {

    /**
     * 字典信息缓存
     */
    String SYS_DICT_CACHE = "sys:cache:dict";
    /**
     * 表字典信息缓存
     */
    String SYS_DICT_TABLE_CACHE = "sys:cache:dictTable";
    String SYS_DICT_TABLE_BY_KEYS_CACHE = SYS_DICT_TABLE_CACHE + "ByKeys";

    /**
     * 数据权限配置缓存
     */
    String SYS_DATA_PERMISSIONS_CACHE = "sys:cache:permission:datarules";

    /**
     * 缓存用户信息
     */
    String SYS_USERS_CACHE = "sys:cache:user";

    /**
     * 全部部门信息缓存
     */
    String SYS_DEPARTS_CACHE = "sys:cache:depart:alldata";

    /**
     * 全部部门ids缓存
     */
    String SYS_DEPART_IDS_CACHE = "sys:cache:depart:allids";

    /**
     * 测试缓存key
     */
    String TEST_DEMO_CACHE = "test:demo";

    /**
     * 字典信息缓存
     */
    String SYS_DYNAMICDB_CACHE = "sys:cache:dbconnect:dynamic:";

    /**
     * gateway路由缓存
     */
    String GATEWAY_ROUTES = "sys:cache:cloud:gateway_routes";

    /**
     * gateway路由 reload key
     */
    String ROUTE_JVM_RELOAD_TOPIC = "gateway_jvm_route_reload_topic";

    /**
     * TODO 冗余代码 待删除
     * 插件商城排行榜
     */
    String PLUGIN_MALL_RANKING = "pluginMall::rankingList";
    /**
     * TODO 冗余代码 待删除
     * 插件商城排行榜
     */
    String PLUGIN_MALL_PAGE_LIST = "pluginMall::queryPageList";

    /**
     * online列表页配置信息缓存key
     */
    String ONLINE_LIST = "sys:cache:online:list";

    /**
     * online表单页配置信息缓存key
     */
    String ONLINE_FORM = "sys:cache:online:form";

    /**
     * online报表
     */
    String ONLINE_RP = "sys:cache:online:rp";

    /**
     * online图表
     */
    String ONLINE_GRAPH = "sys:cache:online:graph";
}
