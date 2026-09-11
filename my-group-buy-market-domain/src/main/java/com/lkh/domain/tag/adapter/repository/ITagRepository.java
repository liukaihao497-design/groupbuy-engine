package com.lkh.domain.tag.adapter.repository;

import com.lkh.domain.activity.model.valobj.TagIdScopeVO;
import com.lkh.domain.tag.model.entity.CrowdTagsJobEntity;
import com.lkh.domain.tag.model.entity.TagDetailEntity;

import java.util.List;
import java.util.Map;

public interface ITagRepository {
    List<TagDetailEntity> queryTagUsersByTagId(String tagId);
    boolean getBit(String key,int index);
    Boolean setRedisTagUserBits(String tagId,Map<String, Integer> map);

    CrowdTagsJobEntity queryCrowdTagsJob(String tagId, String batchId);

    void saveCrowdTagsDetails(String tagId, List<String> userIdList);

    void updateCrowdTagsStatistics(String tagId, int size);

    List<String> queryTagIdsByUserId(String userId);

    TagIdScopeVO queryTagIdAndScopeByActivityId(Long activityId);
}
