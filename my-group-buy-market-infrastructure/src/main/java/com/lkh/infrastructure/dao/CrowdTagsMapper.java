package com.lkh.infrastructure.dao;

import com.lkh.infrastructure.dao.po.CrowdTags;
import org.apache.ibatis.annotations.Mapper;

/**
* @author DELL
* @description 针对表【crowd_tags(人群标签)】的数据库操作Mapper
* @createDate 2026-06-07 11:35:50
* @Entity com.lkh.infrastructure.dao.po.CrowdTags
*/
@Mapper
public interface CrowdTagsMapper {

    int update(CrowdTags req);
}




