package com.lkh.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),
    NOT_NULL("0003","参数不能为空"),
    HTTP_EXCEPTION("0101","HTTP请求出现异常"),
    INT_FORMAT_ERROR("0110","整形格式化异常"),
    E0001("E0001", "不存在对应的折扣计算服务"),
    E0002("E0002", "无拼团营销配置"),

    E0003("E0003","降级处理"),
    E0004("E0004","切量处理"),
    E0005("E0005","拼团失败"),
    INDEX_EXCEPTION("E0006","唯一索引冲突"),
    E0006("E0006", "拼团组队完结，锁单量已达成"),
    E0007("E0007", "拼团活动已结束"),
    E0008("E0008","当前活动不对此用户开放"),
    E0103("E0103","当前用户参与已达上限"),
    E0101("E0101", "拼团活动未生效"),
    E0102("E0102", "不在拼团活动有效时间内"),
    E0110("E0110","交易单号信息不存在"),
    E0111("E0111","用户不匹配"),
    E0112("E0112","更新订单状态异常"),
    E0113("E0113","更新拼团数量异常"),
    E0114("E0114","拼团成功回调任务添加失败"),
    E0115("E0115","当前用户已完成支付"),
    E0116("E0116","拼团已结束"),
    E0117("E0117","当前SC下商品不可参与"),
    E0118("E0118","当前任务不存在"),
    E0119("E0119","拼团名额已满"),
    RATE_LIMITER("E0200", "限流处理");

    private String code;
    private String info;

}
