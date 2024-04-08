package com.lizhuhao.fundingmanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 
 * </p>
 *
 * @author lizhuhao
 * @since 2024-03-28
 */
@Getter
@Setter
@TableName("tbl_user")
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * ID
     */
        private String id;

      /**
     * 姓名
     */
      private String name;

      /**
     * 账号（手机号）
     */
      private String account;

      /**
     * 密码
     */
      private String password;

      /**
     * 权限
     */
      private String permissions;

      /**
     * 删除标志
     */
      private Integer delFlag;

      /**
     * 创建日期
     */
      private LocalDateTime createTime;

      /**
     * 修改日期
     */
      private LocalDateTime modifyTime;


}
