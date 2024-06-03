package com.lizhuhao.fundingmanagement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * @since 2024-04-13
 */
@Getter
@Setter
  @TableName("tbl_project")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * ID
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      /**
     * 项目名称
     */
      private String projectName;

      /**
     * 项目号
     */
      private Integer projectNumber;

      /**
     * 项目类型id
     */
      @JsonProperty
      private Integer pId;

      /**
       * 负责人id
       */
      @JsonProperty
      private Integer uId;
      /**
     * 项目经费总额
     */
      private BigDecimal totalBudget;

      /**
     * 项目经费余额
     */
      private BigDecimal balance;

      /**
     * 项目负责人
     */
      private String responsiblePerson;

      /**
     * 项目开始时间
     */
      private Date startTime;

      /**
     * 项目结束时间
     */
      private Date endTime;

      /**
     * 删除标志
     */
      private Boolean delFlag;

      /**
     * 项目创建时间
     */
      private Date createTime;

      /**
     * 项目修改时间
     */
      private Date modifyTime;

    /**
     * 上传结题报告标志
     */
    private Boolean uploadFlag;
}
