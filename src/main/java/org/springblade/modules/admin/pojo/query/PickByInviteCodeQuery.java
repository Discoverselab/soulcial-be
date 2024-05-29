package org.springblade.modules.admin.pojo.query;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("通过邀请码查询用户")
@AllArgsConstructor
@NoArgsConstructor
public class PickByInviteCodeQuery {

	private String inviteCode;
}
