package com.supermap.learning.minIO.common.enumeration;

/**
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5位数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：10001。10：通用 001：系统未知异常
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * <p>
 * 错误码列表：
 * 10：通用
 * &nbsp;     001：参数格式校验异常
 * &nbsp;     002：请求短信验证码频率过高
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 * 15：用户
 * 21：库存
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "请求参数校验失败"),
    VALID_SMS_CODE_EXCEPTION(10002, "请求短信验证码频率过高"),
    TO_MANY_REQUEST(10003, "请求流量过大"),

    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    PRODUCT_SEARCH_EXCEPTION(11001, "商品搜索异常"),

    USER_EXIST_EXCEPTION(15000, "用户已存在"),
    PHONE_EXIST_EXCEPTION(15001, "手机号已存在"),
    LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION(15002, "用户名或密码错误"),
    LOGIN_ACCOUNT_OAUTH_INVALID_EXCEPTION(15003, "第三方登录错误"),

    NO_STOCK_EXCEPTION(21000, "商品库存不足"),
    DEDUCTION_STOCK_EXCEPTION(21001, "扣减库存异常");

    private final int code;
    private final String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
