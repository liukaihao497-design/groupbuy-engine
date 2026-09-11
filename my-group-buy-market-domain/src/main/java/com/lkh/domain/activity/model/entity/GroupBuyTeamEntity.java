package com.lkh.domain.activity.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @TableName group_buy_order
 */
@Data
public class GroupBuyTeamEntity {
    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 拼单组队ID
     */
    private String teamId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 渠道
     */
    private String source;

    /**
     * 来源
     */
    private String channel;

    /**
     * 原始价格
     */
    private BigDecimal originalPrice;

    /**
     * 折扣金额
     */
    private BigDecimal deductionPrice;

    /**
     * 支付价格
     */
    private BigDecimal payPrice;

    /**
     * 目标数量
     */
    private Integer targetCount;

    /**
     * 完成数量
     */
    private Integer completeCount;

    /**
     * 锁单数量
     */
    private Integer lockCount;

    /**
     * 状态（0-拼单中、1-完成、2-失败）
     */
    private Integer status;

    /**
     * 有效开始时间
     */
    private Date validStartTime;
    /**
     * 拼团结束时间
     */
    private Date validEndTime;

    /**
     * 拼团成功回调地址
     */
    private String notifyUrl;

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
        GroupBuyTeamEntity other = (GroupBuyTeamEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTeamId() == null ? other.getTeamId() == null : this.getTeamId().equals(other.getTeamId()))
            && (this.getActivityId() == null ? other.getActivityId() == null : this.getActivityId().equals(other.getActivityId()))
            && (this.getSource() == null ? other.getSource() == null : this.getSource().equals(other.getSource()))
            && (this.getChannel() == null ? other.getChannel() == null : this.getChannel().equals(other.getChannel()))
            && (this.getOriginalPrice() == null ? other.getOriginalPrice() == null : this.getOriginalPrice().equals(other.getOriginalPrice()))
            && (this.getDeductionPrice() == null ? other.getDeductionPrice() == null : this.getDeductionPrice().equals(other.getDeductionPrice()))
            && (this.getPayPrice() == null ? other.getPayPrice() == null : this.getPayPrice().equals(other.getPayPrice()))
            && (this.getTargetCount() == null ? other.getTargetCount() == null : this.getTargetCount().equals(other.getTargetCount()))
            && (this.getCompleteCount() == null ? other.getCompleteCount() == null : this.getCompleteCount().equals(other.getCompleteCount()))
            && (this.getLockCount() == null ? other.getLockCount() == null : this.getLockCount().equals(other.getLockCount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTeamId() == null) ? 0 : getTeamId().hashCode());
        result = prime * result + ((getActivityId() == null) ? 0 : getActivityId().hashCode());
        result = prime * result + ((getSource() == null) ? 0 : getSource().hashCode());
        result = prime * result + ((getChannel() == null) ? 0 : getChannel().hashCode());
        result = prime * result + ((getOriginalPrice() == null) ? 0 : getOriginalPrice().hashCode());
        result = prime * result + ((getDeductionPrice() == null) ? 0 : getDeductionPrice().hashCode());
        result = prime * result + ((getPayPrice() == null) ? 0 : getPayPrice().hashCode());
        result = prime * result + ((getTargetCount() == null) ? 0 : getTargetCount().hashCode());
        result = prime * result + ((getCompleteCount() == null) ? 0 : getCompleteCount().hashCode());
        result = prime * result + ((getLockCount() == null) ? 0 : getLockCount().hashCode());
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
        sb.append(", teamId=").append(teamId);
        sb.append(", activityId=").append(activityId);
        sb.append(", source=").append(source);
        sb.append(", channel=").append(channel);
        sb.append(", originalPrice=").append(originalPrice);
        sb.append(", deductionPrice=").append(deductionPrice);
        sb.append(", payPrice=").append(payPrice);
        sb.append(", targetCount=").append(targetCount);
        sb.append(", completeCount=").append(completeCount);
        sb.append(", lockCount=").append(lockCount);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}