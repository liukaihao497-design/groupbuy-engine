package com.lkh.infrastructure.adapter.repository;

import com.lkh.domain.activity.model.valobj.TagIdScopeVO;
import com.lkh.domain.tag.adapter.repository.ITagRepository;
import com.lkh.domain.tag.model.entity.CrowdTagsJobEntity;
import com.lkh.domain.tag.model.entity.TagDetailEntity;
import com.lkh.infrastructure.dao.*;
import com.lkh.infrastructure.dao.po.CrowdTags;
import com.lkh.infrastructure.dao.po.CrowdTagsDetail;
import com.lkh.infrastructure.dao.po.CrowdTagsJob;
import com.lkh.infrastructure.dao.po.ScGoodsActivity;
import com.lkh.infrastructure.redis.IRedisService;
import com.lkh.infrastructure.utils.mapper.ObjMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class TagRepository implements ITagRepository {
    @Autowired
    private ObjMapper objMapper;
    @Autowired
    private CrowdTagsDetailMapper crowdTagsDetailMapper;
    @Autowired
    private CrowdTagsJobMapper crowdTagsJobMapper;
    @Autowired
    private IRedisService redisService;
    @Autowired
    private CrowdTagsMapper crowdTagsMapper;
    @Autowired
    private ScGoodsActivityMapper scGoodsActivityMapper;
    @Override
    public List<TagDetailEntity> queryTagUsersByTagId(String tagId) {
        List<CrowdTagsDetail> list = crowdTagsDetailMapper.queryTagUsersByTagId(tagId);
        // po转entity
        List<TagDetailEntity> entityList = objMapper.toList(list);
        return entityList;
    }

    @Override
    public Boolean setRedisTagUserBits(String tagId,Map<String, Integer> map) {
        redisService.remove(tagId);
        String key = tagId;
        map.forEach((k,v)->{
            redisService.setBit(tagId,v,true);
        });
        return true;
    }

    @Override
    public CrowdTagsJobEntity queryCrowdTagsJob(String tagId, String batchId) {
        CrowdTagsJob tagsJob = crowdTagsJobMapper.queryCrowdTagsJob(tagId,batchId);
        CrowdTagsJobEntity entity = objMapper.toEntity(tagsJob);
        return entity;
    }

    @Override
    public void saveCrowdTagsDetails(String tagId, List<String> userIdList) {

            for (String userId : userIdList) {
                CrowdTagsDetail req = new CrowdTagsDetail();
                req.setTagId(tagId);
                req.setUserId(userId);
                req.setCreateTime(new Date());
                req.setUpdateTime(new Date());
                try{
                    crowdTagsDetailMapper.saveCrowdTagsDetailMapper(req);
                }  catch (Exception e){
            System.err.println("tag_details表重复插入"+e.getMessage());
        }

            }


    }

    @Override
    public void updateCrowdTagsStatistics(String tagId, int size) {
        CrowdTags req = new CrowdTags();
        req.setTagId(tagId);
        req.setUpdateTime(new Date());
        req.setStatistics(size);
        crowdTagsMapper.update(req);
    }

    @Override
    public List<String> queryTagIdsByUserId(String userId) {
        List<String> userTagIds = crowdTagsDetailMapper.queryTagIdByUserId(userId);
        return userTagIds;
    }

    @Override
    public TagIdScopeVO queryTagIdAndScopeByActivityId(Long activityId) {
        ScGoodsActivity req = new ScGoodsActivity();
        req.setActivityId(activityId);

        TagIdScopeVO vo = new TagIdScopeVO();
        return null;
    }

    public boolean getBit(String key,int index) {
        return redisService.getBit(key,index);
    }
}
