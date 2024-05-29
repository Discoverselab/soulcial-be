package org.springblade.modules.admin.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * tb_member实体类
 *
 * @author yuanxx
 *
 */
@Data
@ApiModel("用户信息PO")
@TableName("tb_member")
public class MemberPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 登录类型：0-钱包 1-particle
	 */
	Integer loginType;

	/**
	 * particleType类型：传数字每个数字分别代表一种类型
	 */
	Integer particleType;

	/**
	*钱包地址
	*/
	private String address;
	/**
	 *个人简介
	 */
	private String bio;
	/**
	 * 签名秘钥
	 */
	private String signToken;

	/**
	 * 是否绑定推特
	 */
	private Integer bindTwitter;
	/**
	 * 推特粉丝数
	 */
	private Integer twitterFollowers;
	/**
	 * 推特昵称
	 */
	private String twitterName;
	/**
	 * 推特用户名
	 */
	private String twitterUsername;
	/**
	 * 推特头像
	 */
	private String twitterImageUrl;
	/**
	 * 推特ID
	 */
	private String twitterId;
	/**
	*年龄
	*/
//	private Integer age;
	/**
	*性别：0-女 1-男
	*/
	private Integer sex;
	/**
	*联系方式
	*/
//	private String phone;
	/**
	*邮箱
	*/
//	private String email;
	/**
	*微信/QQ
	*/
//	private String contact;
	/**
	*免费铸造：0-未使用 1-已使用
	*/
	private Integer freeMint;
	/**
	 * 永久邀请码
	 */
	private String superInviteCode;
	/**
	 *用户标签（多个用逗号隔开）
	 */
	private String userTags;
	/**
	 * 昵称
	 */
	private String userName;
	/**
	 * 头像
	 */
	private String avatar;

	/**
	 * 等级分数：整数0-600
	 */
	private Integer levelScore;

	/**
	 * lens账号，多个用逗号隔开
	 */
	private String lensProfile;

	/**
	 * 用户等级（level）
	 */
	private Integer level;

	/**
	 * 临时：是否为用户白名单，0-否 1-是，若是，则无限mint
	 */
	private Integer whiteList;

	@ApiModelProperty("charisma(6边型算分):已替换成INFLUENCE")
	private Integer charisma;

	@ApiModelProperty("extroversion(6边型算分):已替换成CONNECTION")
	private Integer extroversion;

	@ApiModelProperty("energy(6边型算分)")
	private Integer energy;

	@ApiModelProperty("wisdom(6边型算分)")
	private Integer wisdom;

	@ApiModelProperty("art(6边型算分)")
	private Integer art;

	@ApiModelProperty("courage(6边型算分)")
	private Integer courage;

	@ApiModelProperty("dataverse：stream_id")
	private String streamId;

	@ApiModelProperty("被邀请用户id")
	private Long inviteUserId;

	@ApiModelProperty("Personality 特征形容")
	private String personality;

	@ApiModelProperty("Character 身份角色")
	private String chracter;

	@ApiModelProperty("身份：Personality+Character")
	private String soul;

	public void countLevel(){
		if(this.levelScore == null || this.levelScore < 100){
			this.level = 1;
		} else if(this.levelScore < 200){
			this.level = 2;
		} else if(this.levelScore < 350){
			this.level = 3;
		} else if(this.levelScore < 500){
			this.level = 4;
		} else {
			this.level = 5;
		}
	}

	public void countLevelScore(){
		int courage = this.courage == null ? 0 : this.courage;
		int charisma = this.charisma == null ? 0 : this.charisma;
		int extroversion = this.extroversion == null ? 0 : this.extroversion;
		int energy = this.energy == null ? 0 : this.energy;
		int wisdom = this.wisdom == null ? 0 : this.wisdom;
		int art = this.art == null ? 0 : this.art;

		this.levelScore = courage + charisma + extroversion + energy + wisdom + art;
	}
}
