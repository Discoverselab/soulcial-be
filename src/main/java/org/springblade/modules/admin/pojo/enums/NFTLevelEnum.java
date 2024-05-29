package org.springblade.modules.admin.pojo.enums;


public enum NFTLevelEnum {

    /**
     * Origin
	 */
	ORIGIN(1,"Origin"),

	/**
	 * Vitality
	 */
	VITALITY(2,"Vitality"),

	/**
	 * Eureka
	 */
	EUREKA(3,"Eureka"),

	/**
	 * Spark
	 */
	SPARK(4,"Spark"),

	/**
	 * Flow
	 */
	FLOW(5,"Flow");


	private int code;
	private String name;

	NFTLevelEnum(int code, String name) {
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
		for (NFTLevelEnum value : NFTLevelEnum.values()) {
			if(value.code == code){
				return value.name;
			}
		}
		return null;
	}

}
