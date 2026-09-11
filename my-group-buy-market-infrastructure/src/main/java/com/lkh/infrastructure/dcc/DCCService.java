package com.lkh.infrastructure.dcc;


import cn.bugstack.wrench.dynamic.config.center.types.annotations.DCCValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service("dccService")
public class DCCService {


    /**
     * 降级开关 0关闭、1开启
     */
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    @DCCValue("cutRange:100")
    private String cutRange;

    @DCCValue("scBlacklist:s02c02")
    private String scBlacklist;
    @DCCValue("cacheOpenSwitch:0")
    private String cacheOpenSwitch;

    public boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId) {
        // 计算哈希码的绝对值
        int hashCode = Math.abs(userId.hashCode());

        // 获取最后两位
        int lastTwoDigits = hashCode % 100;

        // 判断是否在切量范围内
        if (lastTwoDigits <= Integer.parseInt(cutRange)) {
            return true;
        }

        return false;
    }

    /**
     * 缓存开启开关，true为开启，1为关闭
     */
    public boolean isCacheOpenSwitch(){
        return "0".equals(cacheOpenSwitch);
    }

    public boolean isScBlacklist(String source, String channel) {
        if(StringUtils.isBlank(source) || StringUtils.isBlank(channel)) {
            return false;
        }
        return scBlacklist.contains(source + channel);
    }
}
