package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.SysDictDTO;
import com.nczy.coalsales.entity.SysDict;
import com.nczy.coalsales.mapper.SysDictMapper;
import com.nczy.coalsales.service.SysDictService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据字典服务实现
 * <p>
 * 设计要点：
 * <ul>
 *   <li><b>读多写少 → 内存缓存</b>：业务参数（净重区间、超发阈值、锁定策略等）每次过磅/下单都要读，
 *       若每次都查库会对 DB 造成无谓压力。故启动时全量加载进内存，业务代码走内存取值。</li>
 *   <li><b>写后刷新</b>：管理员增删改字典后主动 refreshCache()，保证「改配置立即生效」。</li>
 *   <li><b>安全类型转换</b>：字典值统一存字符串，getParamAsXxx 做带默认值的兜底转换，脏数据不崩。</li>
 * </ul>
 */
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict>
        implements SysDictService {

    /**
     * 内存缓存：dictType -> (dictKey -> dictValue)
     * 使用 ConcurrentHashMap 保证并发读写安全（过磅线程读、管理端刷新线程写）。
     */
    private final Map<String, Map<String, String>> dictCache = new ConcurrentHashMap<>();

    /** Bean 初始化完成后全量加载一次缓存，保证首个请求就能拿到参数 */
    @PostConstruct
    public void initCache() {
        refreshCache();
    }

    /**
     * 全量刷新缓存：从 DB 读取所有未删除的字典，重建内存映射。
     * 加 synchronized 防止并发刷新时出现「边清边写」的脏数据。
     */
    public synchronized void refreshCache() {
        List<SysDict> all = list();
        Map<String, Map<String, String>> tmp = new HashMap<>();
        for (SysDict d : all) {
            // 某 type 第一次出现时，自动创建内层 map
            tmp.computeIfAbsent(d.getDictType(), k -> new HashMap<>())
               .put(d.getDictKey(), d.getDictValue());
        }
        dictCache.clear();
        dictCache.putAll(tmp);
    }

    /* ==================== 管理端 CRUD ==================== */

    @Override
    public PageResult<SysDict> pageDict(Long current, Long size, String dictType) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        // 有 dictType 才追加条件，为空则查全部
        wrapper.eq(StringUtils.hasText(dictType), SysDict::getDictType, dictType)
               .orderByAsc(SysDict::getSortOrder)
               .orderByDesc(SysDict::getCreateTime);

        Page<SysDict> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<SysDict> listByType(String type) {
        return list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, type)
                .orderByAsc(SysDict::getSortOrder));
    }

    @Override
    public void addDict(SysDictDTO dto) {
        SysDict dict = new SysDict();
        dict.setDictType(dto.getDictType());
        dict.setDictKey(dto.getDictKey());
        dict.setDictValue(dto.getDictValue());
        dict.setRemark(dto.getRemark());
        dict.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        save(dict);
        refreshCache(); // 新增后刷新缓存，立即生效
    }

    @Override
    public void updateDict(Long id, SysDictDTO dto) {
        SysDict dict = new SysDict();
        dict.setId(id);
        dict.setDictType(dto.getDictType());
        dict.setDictKey(dto.getDictKey());
        dict.setDictValue(dto.getDictValue());
        dict.setRemark(dto.getRemark());
        dict.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        updateById(dict);
        refreshCache(); // 修改后刷新缓存，立即生效
    }

    @Override
    public void deleteDict(Long id) {
        removeById(id); // @TableLogic 自动逻辑删除
        refreshCache(); // 删除后刷新缓存，立即生效
    }

    /* ==================== 业务参数读取（走内存缓存） ==================== */

    @Override
    public String getBizParam(String key, String defaultValue) {
        return getParam("biz_param", key, defaultValue);
    }

    @Override
    public String getParam(String type, String key, String defaultValue) {
        Map<String, String> map = dictCache.get(type);
        if (map == null) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public Integer getParamAsInt(String key, Integer defaultValue) {
        String v = getBizParam(key, null);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue; // 脏数据兜底，不抛异常
        }
    }

    @Override
    public Long getParamAsLong(String key, Long defaultValue) {
        String v = getBizParam(key, null);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Double getParamAsDouble(String key, Double defaultValue) {
        String v = getBizParam(key, null);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public BigDecimal getParamAsDecimal(String key, BigDecimal defaultValue) {
        String v = getBizParam(key, null);
        if (v == null || v.isBlank()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
