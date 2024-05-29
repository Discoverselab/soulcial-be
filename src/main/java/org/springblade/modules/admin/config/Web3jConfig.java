package org.springblade.modules.admin.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import jnr.ffi.annotations.In;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

/**
 * web3j配置类
 */
@Configuration
public class Web3jConfig  {
//	@Value("${contract.password}")
//	public String coinCreatePwd;
//
	@Value("${contract.rpcUrl}")
	public String rpcUrl;

	/**
	 * BSC测试网RPC
	 */
	public static final String BSC_RPC_TEST = "https://data-seed-prebsc-1-s1.binance.org:8545/";

	/**
	 * BSC测试网网链ID
	 */
	public static final Long BSC_CHAIN_ID_TEST = 97L;

	/**
	 * opBSC测试网RPC
	 */
	public static final String OPBSC_RPC_TEST = "https://opbnb-testnet-rpc.bnbchain.org";

	/**
	 * opBNBRPC
	 */
	public static final String OPBSC_RPC = "https://goerli.optimism.io";
//	public static final String OPBSC_RPC = "https://mainnet.optimism.io";

	/**
	 * opBSC测试网网链ID
	 */
	public static final Long OPBSC_CHAIN_ID_TEST = 5611L;

	/**
	 * BSC正式网RPC
	 */
	public static final String BSC_RPC = "https://bsc-dataseed.binance.org/";

	/**
	 * BSC正式网链ID
	 */
	public static final Long BSC_CHAIN_ID = 56L;

	/**
	 * ETH_SEPOLIA测试网RPC
	 */
//	public static final String ETH_SEPOLIA_RPC = "https://eth-sepolia.g.alchemy.com/v2/VAaFGT2RIlmQ-BLDfaWS9YXwDEZE-aCr";
	public static final String ETH_SEPOLIA_RPC = "https://eth-sepolia.g.alchemy.com/v2/G0t3sPN8quwUxWAPpuQA8p2lDBR3olhM";
//	public static final String ETH_SEPOLIA_RPC = "https://testnet.era.zksync.dev";

	/**
	 * ETH_SEPOLIA测试网链ID
	 */
	public static final Long ETH_SEPOLIA_CHAIN_ID = 11155111L;
//	public static final Long ETH_SEPOLIA_CHAIN_ID = 280L;

	/**
	 * ETH主网
	 */
	public static final String ETH_MAIN_RPC = "https://eth-mainnet.g.alchemy.com/v2/HwqN3VZzCQyePSQkoBaR6zJs3stL1XOD";
	//ETH主网链ID
	public static final Long ETH_MAIN_CHAIN_ID = 1L;

	/**
	 * 最大mint数量
	 */
	public static final Integer MINT_MAX_COUNT = 4000;

	/**
	 * 交易、部署合约用的链
	 */
	@Bean(name = "ethWeb3j")
	public Web3j ethWeb3j(){
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		OkHttpClient httpClient = builder.build();
//		Web3j web3j = Web3j.build(new HttpService(ETH_SEPOLIA_RPC, httpClient, false));
		Web3j web3j = Web3j.build(new HttpService(rpcUrl, httpClient, false));
		return web3j;
	}

	/**
	 * 开奖用的链
	 */
	@Value("${contract.rewardRpcUrl}")
	private String rewardRpcUrl;

	/**
	 * 开奖用的链
	 */
	@Bean(name = "ethMainWeb3j")
	public Web3j ethMainWeb3j(){
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		OkHttpClient httpClient = builder.build();
//		Web3j web3j = Web3j.build(new HttpService(ETH_MAIN_RPC, httpClient, false));
		Web3j web3j = Web3j.build(new HttpService(rewardRpcUrl, httpClient, false));
		return web3j;
	}

}
