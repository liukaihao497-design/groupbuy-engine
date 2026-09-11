package com.lkh.infrastructure.dao.po;

import java.util.Date;
import lombok.Data;

/**
 * 人群标签任务
 * @TableName crowd_tags_job
 */
@Data
public class CrowdTagsJob {
    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 标签ID
     */
    private String tagId;

    /**
     * 批次ID
     */
    private String batchId;

    /**
     * 标签类型（参与量、消费金额）
     */
    private Integer tagType;

    /**
     * 标签规则（限定类型 N次）
     */
    private String tagRule;

    /**
     * 统计数据，开始时间
     */
    private Date statStartTime;

    /**
     * 统计数据，结束时间
     */
    private Date statEndTime;

    /**
     * 状态；0初始、1计划（进入执行阶段）、2重置、3完成
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        CrowdTagsJob other = (CrowdTagsJob) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTagId() == null ? other.getTagId() == null : this.getTagId().equals(other.getTagId()))
            && (this.getBatchId() == null ? other.getBatchId() == null : this.getBatchId().equals(other.getBatchId()))
            && (this.getTagType() == null ? other.getTagType() == null : this.getTagType().equals(other.getTagType()))
            && (this.getTagRule() == null ? other.getTagRule() == null : this.getTagRule().equals(other.getTagRule()))
            && (this.getStatStartTime() == null ? other.getStatStartTime() == null : this.getStatStartTime().equals(other.getStatStartTime()))
            && (this.getStatEndTime() == null ? other.getStatEndTime() == null : this.getStatEndTime().equals(other.getStatEndTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTagId() == null) ? 0 : getTagId().hashCode());
        result = prime * result + ((getBatchId() == null) ? 0 : getBatchId().hashCode());
        result = prime * result + ((getTagType() == null) ? 0 : getTagType().hashCode());
        result = prime * result + ((getTagRule() == null) ? 0 : getTagRule().hashCode());
        result = prime * result + ((getStatStartTime() == null) ? 0 : getStatStartTime().hashCode());
        result = prime * result + ((getStatEndTime() == null) ? 0 : getStatEndTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", tagId=").append(tagId);
        sb.append(", batchId=").append(batchId);
        sb.append(", tagType=").append(tagType);
        sb.append(", tagRule=").append(tagRule);
        sb.append(", statStartTime=").append(statStartTime);
        sb.append(", statEndTime=").append(statEndTime);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}