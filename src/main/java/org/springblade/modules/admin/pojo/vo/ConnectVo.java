package org.springblade.modules.admin.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("好友连接VO")
public class ConnectVo {

	@ApiModelProperty("连接id")
	private Long id;

	@ApiModelProperty("发起连接的用户id")
	private Long userId;

	@ApiModelProperty("被连接的用户id")
	private Long toUserId;

	@ApiModelProperty("关联的用户id（userId或toUserId其中一个）")
	private Long linkUserId;

	@ApiModelProperty("0-待确认，1-已连接，2-star连接")
	private Integer status;

	@ApiModelProperty("好友名称")
	private String userName;

	@ApiModelProperty("好友头像")
	private String userHeadImgUrl;

	@ApiModelProperty("好友钱包地址")
	private String userBnbAddress;

	@ApiModelProperty("创建时间")
	private Date createTime;
}
