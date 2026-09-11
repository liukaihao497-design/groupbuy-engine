package com.lkh.domain.activity.service.trial.discountCalculate.factory;

import com.lkh.domain.activity.service.trial.discountCalculate.DiscountCalculateService;
import com.lkh.domain.activity.service.trial.discountCalculate.Impl.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DefaultDicountCalculateFactroy {
    private Map<String, DiscountCalculateService> map = new HashMap<>();
    public DefaultDicountCalculateFactroy(MJDiscountCalculateServiceImpl mj, ZKDiscountCalculateServiceImpl zk, NDiscountCalculateServiceImpl n, ZJDiscountCalculateServiceImpl zj, NODiscountCalculateService no) {
        map.put("MJ",mj);
        map.put("ZJ",zj);
        map.put("N",n);
        map.put("ZK",zk);
        map.put("NO",no);
    }
    public DiscountCalculateService getDiscountCalculateService(String type) {
        if(type == null || "NO".equals(type)) {
            return map.get("NO");
        }
        return map.get(type);
    }

}
