package com.wust.endpoint.chain.persist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 配置端点表
 * </p>
 *
 * @author xujiao
 * @since 2022-02-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("endpoint_info")
public class EndpointInfoEntity extends Model<EndpointInfoEntity> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 机构名称
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 端点名称
     */
    @TableField("endpoint_name")
    private String endpointName;

    /**
     * 端点地址
     */
    @TableField("endpoint_url")
    private String endpointUrl;

    /**
     * 接入状态：1.正常、-1.停止
     */
    @TableField("open_status")
    private Integer openStatus;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
