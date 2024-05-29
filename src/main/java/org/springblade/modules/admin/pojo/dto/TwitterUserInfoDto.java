package org.springblade.modules.admin.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @Auther: FengZi
 * @Date: 2024/2/18 10:13
 * @Description:
 */
@Data
@ToString
@AllArgsConstructor
public class TwitterUserInfoDto {


	private String address;
	private String twitterUserName;
	private String twitterName;
	private String twitterFollowers;
	private String twitterImageUrl;
	private String twitterId;

}
