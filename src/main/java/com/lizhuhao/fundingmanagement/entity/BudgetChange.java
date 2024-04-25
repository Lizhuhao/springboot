package com.lizhuhao.fundingmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author lizhuhao
 * @since 2024-04-16
 */
@Getter
@Setter
  @TableName("tbl_budget_change")
public class BudgetChange implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * ID
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      /**
     * 所属项目id
     */
      private Integer projectId;

      /**
     * 用户id
     */
      private Integer userId;

      /**
     * 经费类型id
     */
      private Integer fundingTypeId;

      /**
     * 花费金额
     */
      private BigDecimal costAmount;

      /**
     * 删除标志
     */
      private Boolean delFlag;

      /**
     * 创建时间
     */
      private Date createTime;

      /**
     * 修改时间
     */
      private Date modifyTime;

      /**
     * 摘要
     */
      private String digest;

      /**
     * 凭证日期
     */
      private Date evidenceDate;

      /**
     * 凭证编号
     */
      private String evidenceNumber;

      /**
     * 凭证名
     */
      private String evidenceName;

      /**
     * 凭证url
     */
      private String evidenceUrl;

      /**
     * 凭证图片的md5
     */
      private String md5;


}
