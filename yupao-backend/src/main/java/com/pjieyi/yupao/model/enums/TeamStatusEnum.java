package com.pjieyi.yupao.model.enums;

/**
 * @author pjieyi
 * @desc 队伍状态枚举
 */
public enum TeamStatusEnum {

    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密")
    ;

    private final int value;
    private final String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    //通过value判断是否包含枚举值
    public static TeamStatusEnum getEnumByValue(Integer value){
        if (value==null){
            return null;
        }
        TeamStatusEnum[] enums = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum:enums){
            if (teamStatusEnum.getValue() == value){
                return teamStatusEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
