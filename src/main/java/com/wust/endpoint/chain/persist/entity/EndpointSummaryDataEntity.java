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
 * 端点摘要主数据表
 * @author xujiao
 * @since 2022-02-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("endpoint_summary_data")
public class EndpointSummaryDataEntity extends Model<EndpointSummaryDataEntity> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 端点id
     */
    @TableField("endpoint_id")
    private Integer endpointId;

    /**
     * 存证数据
     */
    @TableField("data_json")
    private String dataJson;

    /**
     * 摘要文件编号
     */
    @TableField("file_no")
    private String fileNo;

    /**
     * 存证时间
     */
    @TableField("save_time")
    private LocalDateTime saveTime;

    /**
     * 上链状态
     */
    @TableField("chain_status")
    private Integer chainStatus;

    /**
     * 上链地址
     */
    @TableField("chain_address")
    private String chainAddress;

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
