package com.lkh.trigger.http;

import cn.bugstack.wrench.dynamic.config.center.domain.model.valobj.AttributeVO;
import com.lkh.api.IDCCService;
import com.lkh.api.response.Response;
import com.lkh.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;

import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/gbm/dcc/")
@Slf4j
public class DCCController implements IDCCService {
    @Resource(name = "dynamicConfigCenterRedisTopic")
    private RTopic dccTopic;


    @RequestMapping(value = "update_config", method = RequestMethod.GET)
    @Override
    public Response<Boolean> updateDCCValue(@RequestParam String key, @RequestParam String value) {
        try {
            log.info("DCC 动态配置值变更 key:{} value:{}", key, value);
//            dccTopic.publish(key + "," + value);
            dccTopic.publish(new AttributeVO(key,value));
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("DCC 动态配置值变更失败 key:{} value:{}", key, value, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostConstruct
    public void init() {
        System.out.println("dccController load");
    }

}
