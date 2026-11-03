package com.nczy.coalsales.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解：标注在需要记录操作日志的方法上。
 * <p>
 * value 为操作内容描述，如「审核合同」「磅单作废」。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /** 操作内容 */
    String value();
}
