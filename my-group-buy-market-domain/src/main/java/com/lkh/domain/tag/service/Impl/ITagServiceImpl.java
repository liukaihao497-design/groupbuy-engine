package com.lkh.domain.tag.service.Impl;

import com.lkh.domain.tag.adapter.repository.ITagRepository;
import com.lkh.domain.tag.model.entity.CrowdTagsJobEntity;
import com.lkh.domain.tag.model.entity.TagDetailEntity;
import com.lkh.domain.tag.service.ITagService;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class ITagServiceImpl implements ITagService {

    @Autowired
    private ITagRepository tagRepository;

    @Override
    public Boolean refreshTagsDetail2Redis(String tagId) {

        //读数据
        List<TagDetailEntity> tagDetailEntityList = tagRepository.queryTagUsersByTagId(tagId);
        if (Collections.isEmpty(tagDetailEntityList)) {
            return false;
        }

        //写入redis
        Map<String, Integer> map = new HashMap<>(tagDetailEntityList.size());
        for (TagDetailEntity tagDetailEntity : tagDetailEntityList) {
            map.put(tagDetailEntity.getUserId(), userId2Int(tagDetailEntity.getUserId()));
        }
        // 执行bit设置，但如果已经存在tagid的key，则删除key后执行
        Boolean b = tagRepository.setRedisTagUserBits(tagId, map);
        return b;
    }

    /**
     * 执行任务
     * @param tagId
     * @param batchId
     * @return
     */
    @Override
    public Boolean executeTagJob(String tagId, String batchId) {
        // 查询任务详情
        CrowdTagsJobEntity entity = tagRepository.queryCrowdTagsJob(tagId,batchId);
        // TODO 根据任务详细，筛选出标签对应人群
        List<String> userIdList = new ArrayList<String>() {{
            add("xiaofuge");
            add("liergou");
            add("xfg01");
            add("xfg02");
            add("xfg03");
            add("liukaihao");
        }};
        // 将符合标签的用户存入详细表
        tagRepository.saveCrowdTagsDetails(tagId,userIdList);
        // 更新人群标签统计人数信息
        tagRepository.updateCrowdTagsStatistics(tagId,userIdList.size());
        // 刷新标签到redis供其他模块进行判断
        refreshTagsDetail2Redis(tagId);
        return true;
    }


    private Integer userId2Int(String userId) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(userId.getBytes(StandardCharsets.UTF_8));
            // 将哈希字节数组转换为正整数
            BigInteger bigInt = new BigInteger(1, hashBytes);
            // 取模以确保索引在合理范围内
            return bigInt.mod(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}

