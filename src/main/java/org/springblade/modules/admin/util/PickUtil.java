package org.springblade.modules.admin.util;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PickUtil {

	/**
	 * 无开奖队列
	 */
	public static final Integer QUEUE_STATUS_NULL = 0;

	/**
	 * 等待开奖
	 */
	public static final Integer QUEUE_STATUS_WAITING = 1;

	/**
	 * 开奖中
	 */
	public static final Integer QUEUE_STATUS_REWARDING = 2;

	/**
	 * 开奖时间
	 */
	public static final String REWARD_TIME = "reward:time";

	/**
	 * 队列状态：0-无队列 1-待开奖 2-开奖中
	 */
	public static final String REWARD_QUEUE_STATUS = "reward:queue:status";

	/**
	 * 开奖队列
	 */
	public static final String REWARD_LIST = "reward:list";

	/**
	 * 最后一个开奖的区块高度
	 */
	public static final String LAST_REWARD_BLOCK_HEIGHT = "last:reward:block:height";

	/**
	 * 等级：1,2,3,4,5
	 */
	@ApiModelProperty("等级：1,2,3,4,5")
	private Integer level;

	/**
	 * 未中奖返现比例
	 */
	@ApiModelProperty("未中奖返现比例:单位：%")
	private BigDecimal loseLotteryReword;

	/**
	 * 平台抽佣比例
	 */
	@ApiModelProperty("平台抽佣比例:单位：%")
	private BigDecimal plantformReword;

	/**
	 * 创作者版税比例
	 */
	@ApiModelProperty("创作者版税比例:单位：%")
	private BigDecimal minterReword;

	/**
	 * 卖家收益比例
	 */
	@ApiModelProperty("卖家收益比例:单位：%")
	private BigDecimal sellerReword;


	/**
	 * 底价
	 */
	@ApiModelProperty("底价:单位：wETH")
	private BigDecimal basePrice;

	/**
	 * 底价增长率
	 */
	@ApiModelProperty("底价底价增长率:单位：%")
	private BigDecimal priceRiseRate;

	public static PickUtil getInstance(Integer level,BigDecimal linkRate){
		if(level == null || level < 1 || level > 5){
			return null;
		}
		PickUtil pickUtil = new PickUtil();
		pickUtil.setLevel(level);
		pickUtil.setLoseLotteryReword(new BigDecimal("4"));
		pickUtil.setPlantformReword(new BigDecimal("4"));
		pickUtil.setMinterReword(new BigDecimal("4"));
		pickUtil.setSellerReword(new BigDecimal("80"));
		pickUtil.setPriceRiseRate(new BigDecimal("10"));
		if(level == 1) {
			pickUtil.setBasePrice(new BigDecimal("0.01"));
		}else if (level == 2){
			pickUtil.setBasePrice(new BigDecimal("0.05"));
		}else if (level == 3){
			pickUtil.setBasePrice(new BigDecimal("0.25"));
		}else if (level == 4){
			pickUtil.setBasePrice(new BigDecimal("0.5"));
		}else if (level == 5){
			pickUtil.setBasePrice(new BigDecimal("0.1"));
		}

		//链的倍率
		pickUtil.setBasePrice(pickUtil.getBasePrice().multiply(linkRate));

		return pickUtil;
	}

	public static List<PickUtil> getList(BigDecimal linkRate){
		List<PickUtil> list = new ArrayList<>();
		list.add(getInstance(1,linkRate));
		list.add(getInstance(2,linkRate));
		list.add(getInstance(3,linkRate));
		list.add(getInstance(4,linkRate));
		list.add(getInstance(5,linkRate));
		return list;
	}

	public static BigDecimal getSalePrice(Integer level,Integer transactionCount, BigDecimal linkRate){
		PickUtil instance = getInstance(level,linkRate);
		BigDecimal basePrice = instance.getBasePrice();
		//增长率 除以100 + 1
		BigDecimal priceRiseRate = instance.getPriceRiseRate().divide(new BigDecimal("100")).add(BigDecimal.ONE);

		double pow = Math.pow(priceRiseRate.doubleValue(), transactionCount);
		BigDecimal salePrice = basePrice.multiply(new BigDecimal(pow)).setScale(8,BigDecimal.ROUND_HALF_UP);
		return salePrice;
	}

	public static BigDecimal getSalePriceByGroWithrate(BigDecimal price, BigDecimal groWithrate){
//		//增长率 除以100 + 1
		BigDecimal priceRiseRate = groWithrate.add(BigDecimal.ONE);
		BigDecimal salePrice = price.multiply(priceRiseRate);
		return salePrice;
	}


	public static BigDecimal getMinterReward(Integer level,Integer transactionCount, BigDecimal linkRate){
		PickUtil instance = getInstance(level,linkRate);
		BigDecimal basePrice = instance.getBasePrice();
		//增长率 除以100 + 1
		BigDecimal priceRiseRate = instance.getPriceRiseRate().divide(new BigDecimal("100")).add(BigDecimal.ONE);

		double pow = Math.pow(priceRiseRate.doubleValue(), transactionCount);
		BigDecimal salePrice = basePrice.multiply(new BigDecimal(pow)).setScale(8,BigDecimal.ROUND_HALF_UP);

		BigDecimal minterReward = salePrice.multiply(instance.getMinterReword()).divide(new BigDecimal("100")).setScale(8, BigDecimal.ROUND_HALF_UP);
		return minterReward;
	}

	public static BigDecimal getLoserReward(Integer level,Integer transactionCount, BigDecimal linkRate){
		PickUtil instance = getInstance(level,linkRate);
		BigDecimal basePrice = instance.getBasePrice();
		//增长率 除以100 + 1
		BigDecimal priceRiseRate = instance.getPriceRiseRate().divide(new BigDecimal("100")).add(BigDecimal.ONE);

		double pow = Math.pow(priceRiseRate.doubleValue(), transactionCount);
		BigDecimal salePrice = basePrice.multiply(new BigDecimal(pow)).setScale(8,BigDecimal.ROUND_HALF_UP);

		BigDecimal loserReward = salePrice.multiply(instance.getLoseLotteryReword()).divide(new BigDecimal("100")).setScale(8, BigDecimal.ROUND_HALF_UP);
		return loserReward;
	}

	public static BigDecimal getSellerReward(Integer level, Integer transactionCount, BigDecimal linkRate) {
		PickUtil instance = getInstance(level,linkRate);
		BigDecimal basePrice = instance.getBasePrice();
		//增长率 除以100 + 1
		BigDecimal priceRiseRate = instance.getPriceRiseRate().divide(new BigDecimal("100")).add(BigDecimal.ONE);

		double pow = Math.pow(priceRiseRate.doubleValue(), transactionCount);
		BigDecimal salePrice = basePrice.multiply(new BigDecimal(pow)).setScale(8,BigDecimal.ROUND_HALF_UP);
		BigDecimal sellerReward = salePrice.multiply(instance.getSellerReword()).divide(new BigDecimal("100")).setScale(8, BigDecimal.ROUND_HALF_UP);
		return sellerReward;
	}
}
