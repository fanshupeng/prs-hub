package com.prs.hub.constant;

public enum ResultCodeEnum {
    SUCCESS("成功","0"),
    FAIL("失败","1"),
    EMPTY("空参","2"),
    EXCEPTION("异常","3"),
    EXISTING("已存在相同账户","4");
    private String name;
    private String code;

    private ResultCodeEnum(String name,String code){
        this.name=name;
        this.code=code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
