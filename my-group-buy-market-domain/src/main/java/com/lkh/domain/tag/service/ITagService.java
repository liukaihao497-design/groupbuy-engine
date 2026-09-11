package com.lkh.domain.tag.service;

public interface ITagService {
    Boolean refreshTagsDetail2Redis(String tagId);

    Boolean executeTagJob(String tagId,String batchId);

}
