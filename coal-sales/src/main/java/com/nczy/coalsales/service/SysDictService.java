package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.SysDictDTO;
import com.nczy.coalsales.entity.SysDict;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据字典服务
 * <p>
 * 除提供字典项的 CRUD 外，还承担「业务参数读取」职责：
 * 业务规则参数（超发阈值、磅单净重区间、密码锁定策略等）统一存于 sys_dict，
 * 服务启动时加载进内存缓存，业务代码通过 getParam / getParamAsXxx 读取，避免高频查库与硬编码。
 */
public interface SysDictService extends IService<SysDict> {

    /* ==================== 管理端 CRUD ==================== */

    /** 分页查询，可按 dictType 筛选（管理员维护字典用） */
    PageResult<SysDict> pageDict(Long current, Long size, String dictType);

    /** 按类型取全部键值（前端下拉、列表展示用） */
    List<SysDict> listByType(String type);

    /** 新增字典项 */
    void addDict(SysDictDTO dto);

    /** 修改字典项 */
    void updateDict(Long id, SysDictDTO dto);

    /** 逻辑删除字典项 */
    void deleteDict(Long id);

    /* ==================== 业务参数读取（走内存缓存） ==================== */

    /**
     * 取 biz_param 类型下的字符串值，缺失时返回 defaultValue。
     * 例如：getBizParam("net_weight_min", "20")
     */
    String getBizParam(String key, String defaultValue);

    /** 取任意类型下的字符串值，缺失时返回 defaultValue */
    String getParam(String type, String key, String defaultValue);

    /** 取 biz_param 的整型值，缺失/格式错误时返回 defaultValue（不抛异常） */
    Integer getParamAsInt(String key, Integer defaultValue);

    /** 取 biz_param 的长整型值，缺失/格式错误时返回 defaultValue */
    Long getParamAsLong(String key, Long defaultValue);

    /** 取 biz_param 的浮点值，缺失/格式错误时返回 defaultValue */
    Double getParamAsDouble(String key, Double defaultValue);

    /** 取 biz_param 的 BigDecimal 值，缺失/格式错误时返回 defaultValue（金额/比例/吨数用） */
    BigDecimal getParamAsDecimal(String key, BigDecimal defaultValue);
}
