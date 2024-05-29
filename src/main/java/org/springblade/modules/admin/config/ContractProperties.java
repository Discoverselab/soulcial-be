package org.springblade.modules.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * 合约配置类
 *
 * @Auther: FengZi
 * @Date: 2023/11/27 15:28
 * @Description:
 */
@Data
@Component
@ConfigurationProperties(value = "contract")
public class ContractProperties {
	// 监听器相关配置
	private Listener listener;
	// 合约地址
	private String address;
	// RPC URL
	private String rpcUrl;
	// RPC WebSocket URL
	private String rpcUrlSocket;
	// 市场合约地址
	private String marketAddress;
	// 管理员地址
	private String adminAddress;
	// 链ID
	private int chainId;
	// 链名称
	private String chainName;
	// 合约名称
	private String contractName;
	// 关联类型
	private int linkType;
	// 管理员密码
	private String adminPassword;
	// keystore文件路径
	private String keystorePath;
	// 钱包文件名
	private String walletFile;
	// Wrapped合约地址
	private String wrappedAddress;
	// 开奖RPC URL
	private String rewardRpcUrl;
	// 价格倍率
	private double linkRate;
	// BSC链开奖区块间隔数
	private int rewardBlockCount;
	// BSC链出块间隔
	private int blockInterval;
	// 新市场合约地址
	private String newmarketAddress;
	// 新管理员地址
	private String newadminAddress;
	// 新keystore路径
	private String newkeystorePath;
	// 新钱包文件名
	private String newwalletFile;
	// 新管理员密码
	private String newadminPassword;

	// 新市场合约地址
	private String newmarketAddress2;
	// 新管理员地址
	private String newadminAddress2;
	// 新keystore路径
	private String newkeystorePath2;
	// 新钱包文件名
	private String newwalletFile2;
	// 新管理员密码
	private String newadminPassword2;

	//第一个pump 手续费比例比例
	private BigInteger pump1Rate;
	//第二个pump 手续费比例比例
	private BigInteger pump2Rate;
	//第三个pump 手续费比例比例
	private BigInteger pump3Rate;
	//第四个pump 手续费比例比例
	private BigInteger pump4Rate;
	//nft铸造者手续费比例
	private BigInteger creatorRate;
	//平台抽成手续费比例
	private BigInteger protocolRate;
	//推荐人（该nft本轮pump用户推荐人）共四位 每人的手续费比例
	private BigInteger referalRate;
	//涨价幅度
	private BigInteger groWithrate;

	//合约版本
	private String contractVersion;

	// Listener内部类用于封装监听器配置
	@Data
	public static class Listener {
		// 是否启用721监听
		private boolean enable721;
		// 是否启用市场监听
		private boolean enableMarket;
	}
}
