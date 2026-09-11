package com.lkh.infrastructure.dao.po;

import java.util.Date;

/**
 * 
 * @TableName group_buy_discount
 */
public class GroupBuyDiscount {
    /**
     * 自增ID
     */
    private Long id;

    /**
     * 折扣ID
     */
    private Integer discountId;

    /**
     * 折扣标题
     */
    private String discountName;

    /**
     * 折扣描述
     */
    private String discountDesc;

    /**
     * 折扣类型（0:base、1:tag）
     */
    private Integer discountType;

    /**
     * 营销优惠计划（ZJ:直减、MJ:满减、N元购）
     */
    private String marketPlan;

    /**
     * 营销优惠表达式
     */
    private String marketExpr;

    /**
     * 人群标签，特定优惠限定
     */
    private String tagId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 自增ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 自增ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 折扣ID
     */
    public Integer getDiscountId() {
        return discountId;
    }

    /**
     * 折扣ID
     */
    public void setDiscountId(Integer discountId) {
        this.discountId = discountId;
    }

    /**
     * 折扣标题
     */
    public String getDiscountName() {
        return discountName;
    }

    /**
     * 折扣标题
     */
    public void setDiscountName(String discountName) {
        this.discountName = discountName;
    }

    /**
     * 折扣描述
     */
    public String getDiscountDesc() {
        return discountDesc;
    }

    /**
     * 折扣描述
     */
    public void setDiscountDesc(String discountDesc) {
        this.discountDesc = discountDesc;
    }

    /**
     * 折扣类型（0:base、1:tag）
     */
    public Integer getDiscountType() {
        return discountType;
    }

    /**
     * 折扣类型（0:base、1:tag）
     */
    public void setDiscountType(Integer discountType) {
        this.discountType = discountType;
    }

    /**
     * 营销优惠计划（ZJ:直减、MJ:满减、N元购）
     */
    public String getMarketPlan() {
        return marketPlan;
    }

    /**
     * 营销优惠计划（ZJ:直减、MJ:满减、N元购）
     */
    public void setMarketPlan(String marketPlan) {
        this.marketPlan = marketPlan;
    }

    /**
     * 营销优惠表达式
     */
    public String getMarketExpr() {
        return marketExpr;
    }

    /**
     * 营销优惠表达式
     */
    public void setMarketExpr(String marketExpr) {
        this.marketExpr = marketExpr;
    }

    /**
     * 人群标签，特定优惠限定
     */
    public String getTagId() {
        return tagId;
    }

    /**
     * 人群标签，特定优惠限定
     */
    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

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
        GroupBuyDiscount other = (GroupBuyDiscount) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getDiscountId() == null ? other.getDiscountId() == null : this.getDiscountId().equals(other.getDiscountId()))
            && (this.getDiscountName() == null ? other.getDiscountName() == null : this.getDiscountName().equals(other.getDiscountName()))
            && (this.getDiscountDesc() == null ? other.getDiscountDesc() == null : this.getDiscountDesc().equals(other.getDiscountDesc()))
            && (this.getDiscountType() == null ? other.getDiscountType() == null : this.getDiscountType().equals(other.getDiscountType()))
            && (this.getMarketPlan() == null ? other.getMarketPlan() == null : this.getMarketPlan().equals(other.getMarketPlan()))
            && (this.getMarketExpr() == null ? other.getMarketExpr() == null : this.getMarketExpr().equals(other.getMarketExpr()))
            && (this.getTagId() == null ? other.getTagId() == null : this.getTagId().equals(other.getTagId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDiscountId() == null) ? 0 : getDiscountId().hashCode());
        result = prime * result + ((getDiscountName() == null) ? 0 : getDiscountName().hashCode());
        result = prime * result + ((getDiscountDesc() == null) ? 0 : getDiscountDesc().hashCode());
        result = prime * result + ((getDiscountType() == null) ? 0 : getDiscountType().hashCode());
        result = prime * result + ((getMarketPlan() == null) ? 0 : getMarketPlan().hashCode());
        result = prime * result + ((getMarketExpr() == null) ? 0 : getMarketExpr().hashCode());
        result = prime * result + ((getTagId() == null) ? 0 : getTagId().hashCode());
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
        sb.append(", discountId=").append(discountId);
        sb.append(", discountName=").append(discountName);
        sb.append(", discountDesc=").append(discountDesc);
        sb.append(", discountType=").append(discountType);
        sb.append(", marketPlan=").append(marketPlan);
        sb.append(", marketExpr=").append(marketExpr);
        sb.append(", tagId=").append(tagId);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }

    public static String cacheRedisKey(String discountId) {
        return "group_buy_market_cn.bugstack.infrastructure.dao.po.GroupBuyDiscount_" + discountId;
    }
}