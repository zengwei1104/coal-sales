package com.nczy.coalsales.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 数据字典新增 / 修改入参
 * <p>
 * 对应表 sys_dict。dict_type + dict_key 由唯一索引 uk_dict 约束，重复会由数据库兜底拦截。
 */
@Data
public class SysDictDTO {

    /** 字典类型（如 biz_param），必填 */
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    /** 字典键（如 net_weight_min），必填 */
    @NotBlank(message = "字典键不能为空")
    private String dictKey;

    /** 字典值（如 "20"），必填；统一按字符串存储 */
    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    /** 说明（可空） */
    private String remark;

    /** 排序号（可空，默认 0） */
    private Integer sortOrder;
}
