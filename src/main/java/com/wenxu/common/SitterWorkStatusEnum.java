package com.wenxu.common;

public enum SitterWorkStatusEnum {

    RESTING(0, "休息中"),
    ACCEPTING(1, "接单中"),
    SERVING(2, "服务中");

    private final Integer status;
    private final String desc;

    SitterWorkStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDescByStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (SitterWorkStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum.getDesc();
            }
        }
        return null;
    }
}
