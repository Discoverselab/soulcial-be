package org.springblade.modules.admin.pojo.enums;

/**
 * 开奖消息枚举
 */
public enum RewardMessageEnum {

	/**
	 * 中奖
	 */
	REWARD_SUCCESS(1,"REWARD_SUCCESS"),

	/**
	 * 未中奖
	 */
	REWARD_FAILED(2,"REWARD_FAILED"),

	/**
	 * 卖出
	 */
	SELL_SUCCESS(3,"SELL_SUCCESS"),

	/**
	 * 铸造
	 */
	MINTER_REWARD(4,"MINTER_REWARD"),

	/**
	 * 卖家和铸造者是同一人
	 */
	SELLER_IS_MINTER(5,"SELLER_IS_MINTER"),

	;

	private int code;
	private String name;

	RewardMessageEnum(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	};
	public String getName() {
		return name;
	};


	public static String getNameByCode(Integer code){
		for (RewardMessageEnum value : RewardMessageEnum.values()) {
			if(value.code == code){
				return value.name;
			}
		}
		return null;
	}

}
