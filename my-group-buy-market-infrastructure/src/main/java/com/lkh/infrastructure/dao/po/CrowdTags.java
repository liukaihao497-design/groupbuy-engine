package com.lkh.infrastructure.dao.po;

import java.util.Date;
import lombok.Data;

/**
 * 人群标签
 * @TableName crowd_tags
 */
@Data
public class CrowdTags {
    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 人群ID
     */
    private String tagId;

    /**
     * 人群名称
     */
    private String tagName;

    /**
     * 人群描述
     */
    private String tagDesc;

    /**
     * 人群标签统计量
     */
    private Integer statistics;

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
        CrowdTags other = (CrowdTags) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTagId() == null ? other.getTagId() == null : this.getTagId().equals(other.getTagId()))
            && (this.getTagName() == null ? other.getTagName() == null : this.getTagName().equals(other.getTagName()))
            && (this.getTagDesc() == null ? other.getTagDesc() == null : this.getTagDesc().equals(other.getTagDesc()))
            && (this.getStatistics() == null ? other.getStatistics() == null : this.getStatistics().equals(other.getStatistics()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTagId() == null) ? 0 : getTagId().hashCode());
        result = prime * result + ((getTagName() == null) ? 0 : getTagName().hashCode());
        result = prime * result + ((getTagDesc() == null) ? 0 : getTagDesc().hashCode());
        result = prime * result + ((getStatistics() == null) ? 0 : getStatistics().hashCode());
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
        sb.append(", tagName=").append(tagName);
        sb.append(", tagDesc=").append(tagDesc);
        sb.append(", statistics=").append(statistics);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}