package org.jeecg.modules.content.constant;

public enum ZyFileCategoryEnum {
    OTHERS(0), // 其他
    SUPPLIER(10), // 供应商附件
    SUPPLIER_PERSON(11), // 供应商人员附件
    PURCHASE(20), // 采购需求附件
    PURCHASE_PROJECT(30); // 采购项目附件

    private final Integer categoryId;

    ZyFileCategoryEnum(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer value() {
        return categoryId;
    }
}
