package com.lizhuhao.fundingmanagement.utils;

import cn.hutool.core.util.StrUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    public static String timeProcess(String time) {
        if(StrUtil.isBlank(time)){
            return "";
        }
        //解析时间戳字符串
        Instant instant = Instant.parse(time);
        //转换为本地时间（UTC+8时区）
        LocalDateTime localDateTime = instant.atZone(ZoneId.of("UTC+8")).toLocalDateTime();
        //规范格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }
}
