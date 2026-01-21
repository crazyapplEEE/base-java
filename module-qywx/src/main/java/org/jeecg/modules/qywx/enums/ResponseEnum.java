package org.jeecg.modules.qywx.enums;

import lombok.Getter;

@Getter public enum ResponseEnum {

    //流程的响应
    RESPONSE_YES("000000", "SUCCESS"), RESPONSE_NO("999999", "FAIL"), RESPONSE_MESS("000001", "未做审批"),

    //oa批量插入
    OA_INSERTBATH_SUCCESS("10000", "OA批量插入成功"), OA_INSERTBATH_FAIL("10001", "OA批量插入失败"),

    //前端的响应
    SUCCESS("200", "SUCCESS"), FAIL("500", "FAIL"), DATA_NULL("500", "数据为空");

    private final String respCode;

    private final String respDesc;

    ResponseEnum(String respCode, String respDesc) {
        this.respCode = respCode;
        this.respDesc = respDesc;
    }

}
