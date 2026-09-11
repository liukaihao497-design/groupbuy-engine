package com.lkh.infrastructure.dao;

import com.lkh.infrastructure.dao.po.CrowdTagsJob;
import org.apache.ibatis.annotations.Mapper;

/**
* @author DELL
* @description 针对表【crowd_tags_job(人群标签任务)】的数据库操作Mapper
* @createDate 2026-06-07 11:35:50
* @Entity com.lkh.infrastructure.dao.po.CrowdTagsJob
*/
@Mapper
public interface CrowdTagsJobMapper {

    CrowdTagsJob queryCrowdTagsJob(String tagId, String batchId);
}




