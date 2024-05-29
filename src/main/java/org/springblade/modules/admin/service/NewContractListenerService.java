package org.springblade.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.dao.TxnHistoryMapper;
import org.springblade.modules.admin.pojo.po.TxnHistoryPO;
import org.springblade.modules.admin.util.Market;
import org.springblade.modules.admin.util.Soultest;
import org.springblade.modules.system.entity.Dict;
import org.springblade.modules.system.mapper.DictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class NewContractListenerService {

	private static final Logger log = LoggerFactory.getLogger(NewContractListenerService.class);

	private Web3j web3j;
	private Credentials credentials;
	private String contractAddress = "";

	private int marketChainId;

	@Value("${contract.newkeystorePath}")
	private String coinKeystorePath;

	@Value("${contract.newadminPassword}")
	private String coinCreatePwd;

	@Value("${contract.newwalletFile}")
	String coinWalletFile;
//	String coinCreatePwd = "peic8888";

//	String rpcUrl = "wss://opt-goerli.g.alchemy.com/v2/0L3dnIrAWsN8pU3SOJjcjZ-q2jizxAG2";


	@Autowired
	@Qualifier("newMarketService")
	MarketService marketService;

	@Value("${contract.newmarketAddress}")
	private String marketAddress;

	@Value("${contract.marketAddress}")
	private String oldmarketAddress;

	@Value("${contract.address}")
	private String address;

	@Value("${contract.rpcUrlSocket}")
	private String rpcUrlSocket;

	@Value("${contract.listener.enable721}")
	private boolean enable721Listener;

	@Value("${contract.listener.enableMarket}")
	private boolean enableMarketListener;

	@Autowired
	DictMapper dictMapper;

	@Autowired
	TxnHistoryMapper txnHistoryMapper;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	private String CODE_721 = "721_blockNumber";
	private String CODE_MARKET = "market_blockNumber";


	private final static BigInteger gasLimit = Contract.GAS_LIMIT;


	// 721合约监听 - 转移
//	@PostConstruct
	public void initListener() throws CipherException, IOException {
		if (!enable721Listener) return;
		System.out.println("721 Contract Listener Starting!");

		Dict blockNumber = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, CODE_721)
			.eq(Dict::getIsDeleted, 0));
		// 若不存在，则新建
		if (blockNumber == null) {
			Dict newBlockNumber = new Dict();
			newBlockNumber.setCode(CODE_721);
			newBlockNumber.setDictValue("0");
			dictMapper.insert(newBlockNumber);
			blockNumber = newBlockNumber;
		}

		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		OkHttpClient httpClient = builder.build();
//		Web3j web3j = Web3j.build(new HttpService(ETH_SEPOLIA_RPC, httpClient, false));
		// websocket形式

		Web3j web3j = Web3j.build(new HttpService(rpcUrlSocket, httpClient, false));
		Credentials credentials = WalletUtils.loadCredentials(coinCreatePwd, coinKeystorePath + "/" + coinWalletFile);  // 2. 加载钱包
		BigInteger gasPrice = Contract.GAS_PRICE;
		Soultest contract = Soultest.load(address, web3j, credentials, gasPrice, gasLimit);  // 3. 加载合约

		// 订阅transfer事件
		Dict finalBlockNumber = blockNumber;
		String height = blockNumber.getDictValue();
		BigInteger startBlock = new BigInteger(height);
		DefaultBlockParameter startParameter = startBlock.compareTo(BigInteger.ZERO) <= 0 ?
			DefaultBlockParameterName.EARLIEST : DefaultBlockParameter.valueOf(startBlock);
		contract.transferEventFlowable(startParameter, DefaultBlockParameterName.LATEST)  // 4. 订阅事件
			.subscribe(event -> {
				// 忽略mint及approve情况
				// 这里处理事件// 忽略mint操作
				if(Objects.equals(event.from, "0x0000000000000000000000000000000000000000")){
//						log.info("Mint transaction from: " + event.from + " to: "+ event.to + " ignored.");
					return;
				}
				// 忽略发送到marketAddress的approve及cancelList操作
				else if(Objects.equals(event.to, marketAddress.toLowerCase()) || Objects.equals(event.from, marketAddress.toLowerCase())){
//						log.info("Approve transaction from: " + event.from + " to: "+ event.to + " ignored.");
					return;
				}
				// 忽略发送到marketAddress的approve及cancelList操作
				else if(Objects.equals(event.to, oldmarketAddress.toLowerCase()) || Objects.equals(event.from, oldmarketAddress.toLowerCase())){
//						log.info("Approve transaction from: " + event.from + " to: "+ event.to + " ignored.");
					return;
				}

				log.info("transfer item event received: " + "from: " + event.from + " to: " + event.to + " tokenId: " + event.tokenId);
				log.info("transfer log: " + event.log.toString());
				// 输出区块高度
				log.info("transfer blockNumber: " + event.log.getBlockNumber());
				String txnHash = event.log.getTransactionHash();

				// 查询txnHistory是否存在
				long txnHistoryCount = txnHistoryMapper.selectCount(new LambdaQueryWrapper<TxnHistoryPO>()
					.eq(TxnHistoryPO::getTxnHash, txnHash)
				);
				if (txnHistoryCount > 0) {
					log.info("txnHistory already exists, txnHash: " + txnHash);
					return;
				} else {
					// 插入txnHistory
					TxnHistoryPO txnHistoryPO = new TxnHistoryPO();
					txnHistoryPO.setTokenId(event.tokenId.longValue());
					txnHistoryPO.setTxnHash(txnHash);
					txnHistoryPO.setMethod("transfer");
					txnHistoryPO.setBlock(event.log.getBlockNumber().toString());
					txnHistoryPO.setFromAddress(event.from);
					txnHistoryPO.setToAddress(event.to);
//					txnHistoryPO.setTime(event.log.getData());
					txnHistoryPO.initForUpdateNoAuth();
					txnHistoryMapper.insert(txnHistoryPO);
				}

				// 处理pfpHistory
				R result = marketService.changeOwner(
					event.tokenId.longValue(),
					event.from,
					event.to,
					txnHash
				);

				log.info("out of contract transfer result: " + result.getMsg());

//				if (result.getCode() == 200) {
				// 处理log，将区块高度 + 1存入数据库
				finalBlockNumber.setDictValue(event.log.getBlockNumber().add(BigInteger.ONE).toString());
				dictMapper.updateById(finalBlockNumber);
//				}

//				marketService.pickItem(
//					event.nftAddress,
//					event.picker,
//					event.tokenId,
//					event.pickNum
//				);
				// 处理log，将区块高度存入数据库
//				marketService.updateBlockNumber(event.log.getBlockNumber());
			}, error -> {
				// 这里处理错误
				System.out.println("12312313123123======transfer error" + error);
			});
	}

	@PostConstruct()
	public void init() throws CipherException, IOException {
		if (!enableMarketListener) return;
		System.out.println("Contract Listener Starting!");

		Dict blockNumber = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, CODE_MARKET)
			.eq(Dict::getIsDeleted, 0));
		// 若不存在，则新建
		if (blockNumber == null) {
			Dict newBlockNumber = new Dict();
			newBlockNumber.setCode(CODE_MARKET);
			newBlockNumber.setDictValue("0");
			dictMapper.insert(newBlockNumber);
			blockNumber = newBlockNumber;
		}

		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		builder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS);
		OkHttpClient httpClient = builder.build();
//		Web3j web3j = Web3j.build(new HttpService(ETH_SEPOLIA_RPC, httpClient, false));
		// websocket形式

		Web3j web3j = Web3j.build(new HttpService(rpcUrlSocket, httpClient, false));

		Credentials credentials = WalletUtils.loadCredentials(coinCreatePwd, coinKeystorePath + "/" + coinWalletFile);  // 2. 加载钱包
		BigInteger gasPrice = Contract.GAS_PRICE;
		Market contract = Market.load(marketAddress, web3j, credentials, gasPrice, gasLimit);  // 3. 加载合约

//		contract.itemListedEventFlowable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)  // 4. 订阅事件
//			.subscribe(event -> {
//				// 这里处理事件
//				System.out.println("receive listener ====>  nftOwner: " + event.nftOwner + " tokenId: " + event.tokenId + " price: " + event.price);
//				marketService.listNFT(
//					// 转为Long
//					Long.parseLong(event.tokenId.toString())
//				);
//			}, error -> {
//				// 这里处理错误
//				System.out.println("12312313123123======" + error);
//			});

		// 订阅pickItem事件
		Dict finalBlockNumber = blockNumber;
		// 获取高度
		String height = blockNumber.getDictValue();
		BigInteger startBlock = new BigInteger(height);
		DefaultBlockParameter startParameter = startBlock.compareTo(BigInteger.ZERO) <= 0 ?
			DefaultBlockParameterName.EARLIEST : DefaultBlockParameter.valueOf(startBlock);
		contract.itemPickedEventFlowable(startParameter, DefaultBlockParameterName.LATEST)  // 4. 订阅事件
			.subscribe(event -> {

				// 这里处理事件
				log.info("pick item event received: " + "address: " + event.picker + " tokenId: " + event.tokenId + " pickNum: " + event.pickNum);
				String txnHash = event.log.getTransactionHash();
				// 查询txnHistory是否存在
				long txnHistoryCount = txnHistoryMapper.selectCount(new LambdaQueryWrapper<TxnHistoryPO>()
					.eq(TxnHistoryPO::getTxnHash, txnHash)
				);
				if (txnHistoryCount > 0) {
					log.info("txnHistory already exists, txnHash: " + txnHash);
					return;
				} else {
					// 插入txnHistory
					TxnHistoryPO txnHistoryPO = new TxnHistoryPO();
					txnHistoryPO.setTokenId(event.tokenId.longValue());
					txnHistoryPO.setTxnHash(txnHash);
					txnHistoryPO.setMethod("pump");
					txnHistoryPO.setBlock(event.log.getBlockNumber().toString());
					txnHistoryPO.setFromAddress(event.picker);
					txnHistoryPO.setToAddress(marketAddress);
//					txnHistoryPO.setTime(event.log.getData());
					txnHistoryPO.initForUpdateNoAuth();
					txnHistoryMapper.insert(txnHistoryPO);
				}

				R result = marketService.pickItem(
					event.nftAddress,
					event.picker,
					event.tokenId,
					event.pickNum,
					event.log
				);
				log.info("block number: " + event.log.getBlockNumber());
				log.info("finalBlockNumber: " + finalBlockNumber);
				// 处理log，将区块高度 + 1存入数据库
				finalBlockNumber.setDictValue(event.log.getBlockNumber().add(BigInteger.ONE).toString());
				dictMapper.updateById(finalBlockNumber);

			}, error -> {
				// 这里处理错误
				log.error("12312313123123======pick error" + error);
			});
		// 订阅itemDeal事件
		contract.itemDealEventFlowable(startParameter, DefaultBlockParameterName.LATEST)  // 4. 订阅事件
			.subscribe(event -> {
				// 这里处理事件
				// typedResponse.nftAddress = (String) eventValues.getNonIndexedValues().get(0).getValue();
		//        typedResponse.tokenId = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
		//        typedResponse.buyer = (String) eventValues.getNonIndexedValues().get(2).getValue();
		//        typedResponse.buyer_index = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
		//        typedResponse.sellerAmount = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
		//        typedResponse.shareAmount = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
				String nftAddress = event.nftAddress;
				// tokenId 转换为Long
				Long tokenId = Long.parseLong(event.tokenId.toString());
				String buyer = event.buyer;
				BigInteger buyer_index = event.buyer_index;
				BigInteger sellerAmount = event.sellerAmount;
				BigInteger shareAmount = event.shareAmount;
				log.info("item deal event received: " + "nftAddress: " + nftAddress + " tokenId: " + tokenId + " buyer: " + buyer + " buyer_index: " + buyer_index + " sellerAmount: " + sellerAmount + " shareAmount: " + shareAmount);
				String txnHash = event.log.getTransactionHash();
				// 查询txnHistory是否存在
				long txnHistoryCount = txnHistoryMapper.selectCount(new LambdaQueryWrapper<TxnHistoryPO>()
					.eq(TxnHistoryPO::getTxnHash, txnHash)
				);
				if (txnHistoryCount > 0) {
					log.info("txnHistory already exists, txnHash: " + txnHash);
					return;
				} else {
					// 插入txnHistory
					TxnHistoryPO txnHistoryPO = new TxnHistoryPO();
					txnHistoryPO.setTokenId(event.tokenId.longValue());
					txnHistoryPO.setTxnHash(txnHash);
					txnHistoryPO.setMethod("deal list");
					txnHistoryPO.setBlock(event.log.getBlockNumber().toString());
					txnHistoryPO.setFromAddress(marketAddress);
					txnHistoryPO.setToAddress(event.buyer);
//					txnHistoryPO.setTime(event.log.getData());
					txnHistoryPO.initForUpdateNoAuth();
					txnHistoryMapper.insert(txnHistoryPO);
				}
				R result = marketService.dealList(nftAddress, tokenId, buyer, buyer_index, sellerAmount, shareAmount, event.log);
//				if (result.getCode() == 200) {
					// 处理log，将区块高度 + 1存入数据库
					finalBlockNumber.setDictValue(event.log.getBlockNumber().add(BigInteger.ONE).toString());
					dictMapper.updateById(finalBlockNumber);
//				}
			}, error -> {
				// 这里处理错误
				log.error("12312313123123======deal error" + error);
			});
	}


}
