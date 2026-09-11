package com.lkh.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public enum TagScopeEnum {
    VISIBLE("1","不可见"),
    ENABLE("2","不可参与");
    private String tagType;
    private String tagInfo;

    /**
     * 判断tagScope字符串中是否包含指定tagType 例 “12”中是否包含"1"
     * tagScope为空时代表无条件限制，此方法返回false 取反为true 不用额外处理
     * @param tagScope
     * @param tagType
     * @return
     */
    public static boolean contains(String tagScope, TagScopeEnum tagType) {
        String[] tokens = tagScope.split("[,;\\s]");
        for (String token : tokens) {
            if(tagType.getTagType().equals(token)) {
                return true;
            }
        }
        return false;
    }
    public static boolean noContain(String tagScope, TagScopeEnum tagType) {
        return !contains(tagScope, tagType);
    }
    public String getTagType() {
        return tagType;
    }

}
