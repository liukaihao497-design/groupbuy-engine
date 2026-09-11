package com.lkh.infrastructure.dao;

import com.lkh.infrastructure.dao.po.NotifyTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author DELL
* @description 针对表【notify_task】的数据库操作Mapper
* @createDate 2026-06-16 16:42:42
* @Entity com.lkh.infrastructure.dao.po.NotifyTask
*/
@Mapper
public interface NotifyTaskMapper {

    int insert(NotifyTask req);

    NotifyTask queryByTeamId(@Param("teamId") String teamId);

    List<NotifyTask> queryNoCompleteORRetryTask(@Param("count") Integer count
            ,@Param("createStatus") Integer createStatus,@Param("retryStatus") Integer retryStatus);

    int setRetryAndIncryCount(@Param("teamId") String teamId);
    int setSuccessAndIncryCount(@Param("teamId") String teamId);
    int setFailAndIncryCount(@Param("teamId") String teamId);

}




