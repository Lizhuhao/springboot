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
 * @since 2024-04-17
 */
@Getter
@Setter
  @TableName("tbl_funding")
public class Funding implements Serializable {

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
     * 数额
     */
      private BigDecimal amount;

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
     * 经费类型id
     */
      private Integer fundingTypeId;


}
