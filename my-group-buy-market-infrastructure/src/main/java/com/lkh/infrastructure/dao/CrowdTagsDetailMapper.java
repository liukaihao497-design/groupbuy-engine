package com.lkh.infrastructure.dao;

import com.lkh.infrastructure.dao.po.CrowdTagsDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author DELL
* @description 针对表【crowd_tags_detail(人群标签明细)】的数据库操作Mapper
* @createDate 2026-06-07 11:35:50
* @Entity com.lkh.infrastructure.dao.po.CrowdTagsDetail
*/
@Mapper
public interface CrowdTagsDetailMapper {

    List<CrowdTagsDetail> queryTagUsersByTagId(String tagId);

    int saveCrowdTagsDetailMapper(CrowdTagsDetail crowdTagsDetail);

    List<String> queryTagIdByUserId(String userId);

    List<String> queryUserIdByTagId(String tagId);
}




