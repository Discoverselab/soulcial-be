package org.springblade.modules.admin.tasks;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.admin.config.ContractProperties;
import org.springblade.modules.admin.dao.PFPPickMapper;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.pojo.po.PFPPickPO;
import org.springblade.modules.admin.pojo.po.PFPTokenPO;
import org.springblade.modules.admin.util.Market2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * @Auther: FengZi
 * @Date: 2023/12/8 16:15
 * @Description:
 */
@Component
@Slf4j
public class LotteryFailureTask {

	@Autowired
	@Qualifier("ethWeb3j")
	private Web3j web3j;
	private final static BigInteger gasLimit = Contract.GAS_LIMIT;
	@Resource
	private ContractProperties contractProperties;
	@Resource
	private PFPTokenMapper pfpTokenMapper;


	@Autowired
	private PFPPickMapper pfpPickMapper;


	/**
	 * 十分钟检查一下，如果有超过五分钟未开奖的token，调用交易所合约的dealList方法，手动进行开奖
	 * @author FengZi
	 * @date 16:56 2023/12/8
	 **/
//	@Scheduled(fixedDelay = 10 * 60 * 1000)
	public void sayWord() {
		log.info("=====定时任务开始执行=====");
		log.info("定时监听开奖失败nft。。。");
		log.info("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓");
		//获取开奖时间大于5分钟的token信息
		List<PFPTokenPO> tokens = pfpTokenMapper.findDelayedLotteryTokens();
		log.info("开奖时间大于5分钟的token记录数： " + tokens.size());
		tokens.forEach(
			s->{
				log.info("token信息： " + s);
				//根据nft合约地址判断调取哪个dealList方法
				String contractMarketAddress = s.getContractMarketAddress();
				String marketAddress = "";
				String adminAddress = "";
				if (contractProperties.getMarketAddress().equals(contractMarketAddress)) {
					marketAddress = contractProperties.getMarketAddress();
					adminAddress = contractProperties.getAdminAddress();
				}else if (contractProperties.getNewmarketAddress().equals(contractMarketAddress)) {
					marketAddress = contractProperties.getNewmarketAddress();
					adminAddress = contractProperties.getNewadminAddress();
				}else {
					marketAddress = contractProperties.getNewmarketAddress2();
					adminAddress = contractProperties.getNewmarketAddress2();
				}

				//调用交易所合约的dealList方法
				Boolean b = SendDealList(s.getRealTokenId().toString(),marketAddress,adminAddress);
				log.info("token: " + s.getRealTokenId() + "调用交易所合约的dealList方法结果： " + b);
			}
		);
		log.info("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
		log.info("=====定时任务执行结束=====");
	}

	/**
	 * 调用该token在链上的deaillist方法
	 * @author FengZi
	 * @date 16:41 2023/12/8
	 * @param tokenId
	 **/
	public Boolean SendDealList(String tokenId,String marketAddress,String adminAddress){
		try {
			Credentials credentials = loadCredentials();
			// 打印签名
			String creAddress = credentials.getAddress();
			String ecKeyPair = credentials.getEcKeyPair().toString();
			System.out.println("Credentials address:" + creAddress);
			System.out.println("Credentials ecKeyPair:" + ecKeyPair);

			// 获取gas price
			BigInteger gasPrice = getGasPrice();
			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

			TransactionManager transactionManager = new RawTransactionManager(
				web3j, credentials, contractProperties.getChainId()); // Add the nonce here

			Market2 contract = Market2.load(marketAddress, web3j, transactionManager, gasProvider);

			//费率列表
			ArrayList<BigInteger> rateList = getRate(tokenId.toString());

			log.info("加载交易所合约成功： " + contract);
			// 打印admin地址
			System.out.println("adminAddress: " + adminAddress);
			BigInteger productId = new BigInteger(tokenId);
			// 调用
			log.info("dealList前： nftAddress= " + contractProperties.getAddress() + " ;tokenId=" + productId);
			TransactionReceipt receipt = contract.dealList(
				contractProperties.getAddress(),
				productId,
				rateList
			).send();
			log.info("调用listItem成功：" + receipt);
			//獲取交易hash
			String transactionHash = receipt.getTransactionHash();
			log.info("==============dealList交易hash： " + transactionHash);
			if (transactionHash == null) {
				log.info("deal list fail");
				return false;
			} else {
				log.info("deal list success: " + transactionHash);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private Credentials loadCredentials() throws Exception {
		String walletFile = contractProperties.getNewkeystorePath() + "/" + contractProperties.getNewwalletFile();
		Credentials credentials = WalletUtils.loadCredentials(contractProperties.getNewadminPassword(), walletFile);
		return credentials;
	}

	private BigInteger getGasPrice() throws IOException {
		EthGasPrice gasPrice = web3j.ethGasPrice().send();
		BigInteger baseGasPrice = gasPrice.getGasPrice();
		return baseGasPrice;
	}


	/**
	 * 根据tokenid返回对应费率列表
	 *
	 * @param tokenId
	 * @return java.util.ArrayList<java.lang.String>
	 * @author FengZi
	 * @date 16:46 2023/12/13
	 **/
	public ArrayList<BigInteger> getRate(String tokenId) {
		ArrayList<BigInteger> list = new ArrayList<>();
		//查询pump位置，补充费用率
		//1.根据tokenid查询pick位置
		LambdaQueryWrapper<PFPPickPO> po = new LambdaQueryWrapper<>();
		po.eq(PFPPickPO::getTokenId, tokenId);
		po.orderByDesc(PFPPickPO::getCreateTime);
		List<PFPPickPO> pickPOS = pfpPickMapper.selectList(po);
		if (pickPOS != null && pickPOS.size() > 0) {
			PFPPickPO pickPO = pickPOS.get(0);
			//2.根据pick位置查询pump位置
			Date indexUserPickTime0 = pickPO.getIndexUserPickTime0();
			Date indexUserPickTime1 = pickPO.getIndexUserPickTime1();
			Date indexUserPickTime2 = pickPO.getIndexUserPickTime2();
			Date indexUserPickTime3 = pickPO.getIndexUserPickTime3();
			Date[] dates = {indexUserPickTime0, indexUserPickTime1, indexUserPickTime2, indexUserPickTime3};
			ArrayList<Integer> sortedDateIndices = getSortedDateIndices(dates);
			sortedDateIndices.forEach(s -> {
				Integer index = s;
				if (index == 0) {
					list.add(contractProperties.getPump1Rate());
				} else if (index == 1) {
					list.add(contractProperties.getPump2Rate());
				} else if (index == 2) {
					list.add(contractProperties.getPump3Rate());
				} else if (index == 3) {
					list.add(contractProperties.getPump4Rate());
				}
			});
			list.add(contractProperties.getCreatorRate());
			list.add(contractProperties.getProtocolRate());
			list.add(contractProperties.getReferalRate());
			list.add(contractProperties.getGroWithrate());
		}
		return list;
	}

	/**
	 * 获取排序后的索引
	 *
	 * @param dates 日期数组
	 * @return int[]    排序后的索引
	 * @author FengZi
	 * @date 16:41 2023/12/13
	 **/
	public static ArrayList<Integer> getSortedDateIndices(Date[] dates) {
		ArrayList<Integer> objects = new ArrayList<>();
		// 构建包含日期和原始索引的pair的列表
		List<Map.Entry<Date, Integer>> indexedDates = new ArrayList<>();

		for (int i = 0; i < dates.length; i++) {
			indexedDates.add(new AbstractMap.SimpleImmutableEntry<>(dates[i], i));
		}

		// 按日期对pair列表进行排序
		indexedDates.sort(Map.Entry.comparingByKey());

		// 构建一个反映原始数组在排序后数组中位置的索引数组
		int[] indices = new int[dates.length];
		for (int i = 0; i < indexedDates.size(); i++) {
			indices[indexedDates.get(i).getValue()] = i;
		}
		for (int i = 0; i < indices.length; i++) {
			objects.add(indices[i]);
		}
		// 将Integer[] 转换回基本类型 int[]
		return objects;
	}


}
