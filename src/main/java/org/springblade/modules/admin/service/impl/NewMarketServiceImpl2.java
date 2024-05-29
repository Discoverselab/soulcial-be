package org.springblade.modules.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.cache.IUserCache;
import org.springblade.modules.admin.config.ContractProperties;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.dto.WebsocketMessageDto;
import org.springblade.modules.admin.pojo.enums.RewardMessageEnum;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.service.MarketService;
import org.springblade.modules.admin.service.MemberConnectService;
import org.springblade.modules.admin.service.UserVSoulService;
import org.springblade.modules.admin.socket.WebSocket;
import org.springblade.modules.admin.util.Market2;
import org.springblade.modules.admin.util.PickUtil;
import org.springblade.modules.system.entity.Dict;
import org.springblade.modules.system.mapper.DictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Component("newMarketService2")
@Slf4j
public class NewMarketServiceImpl2 implements MarketService {

	@Autowired
	@Qualifier("ethWeb3j")
	private Web3j web3j;
	private final static BigInteger gasLimit = Contract.GAS_LIMIT;
	// gasLimit 1,000,000
//	private final static BigInteger gasLimit = new BigInteger("1000000");
	private final static BigInteger gasPrice = Contract.GAS_PRICE;

	@Value("${contract.newadminPassword2}")
	private String coinCreatePwd;

	@Value("${contract.newkeystorePath2}")
	private String coinKeystorePath;

	@Value("${contract.newwalletFile2}")
	private String coinWalletFile;

	@Value("${contract.chainId}")
	private Long chainId;

	@Value("${contract.linkRate}")
	private BigDecimal linkRate;

	@Value("${contract.newadminAddress2}")
	private String adminAddress;

	@Value("${contract.address}")
	private String nftAddress;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	@Autowired
	PFPPickMapper pfpPickMapper;

	@Autowired
	MemberMapper memberMapper;

	@Autowired
	WallectHistoryMapper wallectHistoryMapper;

	@Autowired
	PFPTransactionMapper pfpTransactionMapper;

	@Autowired
	ChatOverviewMapper chatOverviewMapper;

	@Resource
	private ChatDetailMapper chatDetailMapper;

	@Resource
	private WebSocket webSocket;

	@Resource
	private ChatMemberMapper chatMemberMapper;

	// 添加memberConnectService
	@Autowired
	MemberConnectService memberConnectService;

	@Autowired
	DictMapper dictMapper;

	// 添加pfpHistoryMapper
	@Autowired
	PFPHistoryMapper pfpHistoryMapper;

	@Autowired
	ChatSessionHistoryMapper chatSessionHistoryMapper;

	// 添加fansMapper
	@Autowired
	FansMapper fansMapper;

	@Autowired
	ActiveMapper activeMapper;

	// 添加messageMapper
	@Autowired
	MessageMapper messageMapper;

	// 添加vSoulHistoryMapper
	@Autowired
	VSoulHistoryMapper vSoulHistoryMapper;

	// 添加userVSoulMapper
	@Autowired
	UserVSoulMapper userVSoulMapper;

	@Autowired
	UserVSoulService userVSoulService;


	// 交易所合约地址
	@Value("${contract.newmarketAddress2}")
	private String marketAddress;

	@Value("${contract.chainName}")
	private String chainName;



	@Resource
	private ContractProperties contractProperties;


	// 合约事件监听
	public static void main(String[] args) {
		System.out.println("He124124124llo World!");
	}


	// 新增合约外转移历史记录，并修改owner
	public R changeOwner(Long tokenId, String from, String to, String txnHash) {
		// 判断from和to得规则是否均为用户的钱包合法地址，而不是合约的地址
		if (!WalletUtils.isValidAddress(from) || !WalletUtils.isValidAddress(to)) {
			return R.fail("from or to is not valid address");
		}
		PFPHistoryPO pfpHistoryPO2 = pfpHistoryMapper.selectOne(new LambdaQueryWrapper<PFPHistoryPO>()
			.eq(PFPHistoryPO::getTxnHash, txnHash)
			.eq(PFPHistoryPO::getIsDeleted, 0));
		if (pfpHistoryPO2 != null) {
			log.info("transfer txnHash: " + txnHash + " already exists, ignored.");
			return R.fail("transfer txnHash: " + txnHash + " already exists, ignored.");
		}
		PFPHistoryPO pfpHistoryPO = new PFPHistoryPO();
		pfpHistoryPO.setTokenId(tokenId);
		// 合约外转移
		pfpHistoryPO.setType(3);
//		pfpHistoryPO.setTransactionId();
		pfpHistoryPO.setAdminAddress(adminAddress);
		pfpHistoryPO.setLinkType(0);
		pfpHistoryPO.setNetwork(chainName);
		pfpHistoryPO.setContractAddress(nftAddress);
		pfpHistoryPO.setContractName(chainName);
		pfpHistoryPO.setFromAddress(from);
		// 查询tb_member中是否存在from用户
		MemberPO fromMemberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
			.eq(MemberPO::getAddress, from)
			.eq(MemberPO::getIsDeleted, 0));
		long fromId = 0L;
		if (fromMemberPO != null) {
			fromId = fromMemberPO.getId();
		}
		pfpHistoryPO.setFromUserId(fromId);
		// 查询tb_member中是否存在to用户
		MemberPO toMemberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
			.eq(MemberPO::getAddress, to)
			.eq(MemberPO::getIsDeleted, 0));
		long toId = 0L;
		if (toMemberPO != null) {
			toId = toMemberPO.getId();
		}
		pfpHistoryPO.setToAddress(to);
		pfpHistoryPO.setToUserId(toId);
		pfpHistoryPO.setTxnHash(txnHash);

		pfpHistoryPO.initForInsertNoAuth();

		pfpHistoryMapper.insert(pfpHistoryPO);

		// 更新pfpToken中owner
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		if (pfpTokenPO == null) {
			return R.fail("Token miss: " + tokenId);
		}
		pfpTokenPO.setOwnerAddress(to);
		pfpTokenPO.setOwnerUserId(toId);
		pfpTokenPO.setUpdateTime(new Date());
		pfpTokenPO.setUpdateUser(0L);
		pfpTokenMapper.updateById(pfpTokenPO);


		return R.success("success");
	}

	// 取消list
	public R cancelList(Long tokenId) {
		try {
			Credentials credentials = loadCredentials();
			// 打印签名
			String cerAddress = credentials.getAddress();
			String ecKeyPair = credentials.getEcKeyPair().toString();
			System.out.println("Credentials address:" + cerAddress);
			System.out.println("Credentials ecKeyPair:" + ecKeyPair);
			//獲取gasprice
			BigInteger gasPrice = getGasPrice();

			ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
			//加載NFT,使用op网的goerli测试链
			TransactionManager transactionManager = new RawTransactionManager(
				web3j, credentials, chainId);
			Market2 contract = Market2.load(marketAddress, web3j, transactionManager, gasProvider);
			log.info("加载交易所合约成功： " + contract);
			// 打印admin地址
			System.out.println("adminAddress: " + adminAddress);
			BigInteger productId = new BigInteger(tokenId.toString());
			// price由于eth转换为wei，所以需要乘以10的18次方
			TransactionReceipt receipt = contract.cancelListing(
				nftAddress,
				productId
			).send();
			log.info("调用cancelListing成功：" + receipt);
			//獲取交易hash
			String transactionHash = receipt.getTransactionHash();
			log.info("==============listItem交易hash： " + transactionHash);
			if (transactionHash == null) {
				log.info("list fail");
			} else {
				log.info("list success: " + transactionHash);
			}
			return R.success("cancel list success");
		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("list fail: contract call error.");
		}
	}

	@Override
	public R listNFT(Long tokenId) {
		Long userId = StpUtil.getLoginIdAsLong();
		// 获取当前用户的钱包地址，根据userId查询memberPO
		MemberPO memberPO = memberMapper.selectById(userId);
		String nftOwnerAddress = memberPO.getAddress();
		log.info("当前用户的钱包地址：" + nftOwnerAddress);

		// 查询realtokenid
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectOne(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(PFPTokenPO::getRealTokenId, tokenId)
			.eq(PFPTokenPO::getIsDeleted, 0)
		);
		if (pfpTokenPO != null && pfpTokenPO.getMintStatus() == 1 &&
			pfpTokenPO.getOwnerUserId().equals(userId) &&
			pfpTokenPO.getIsDeleted() == 0) {
			//是当前用户的资产

			//pick状态： 0-未出价(不可pick)
			if (pfpTokenPO.getPickStatus() != 0) {
				return R.fail("This PFP has list");
			}
			// 调用交易所合约
			try {
				Credentials credentials = loadCredentials();
				// 打印签名
				String address = credentials.getAddress();
				String ecKeyPair = credentials.getEcKeyPair().toString();
				System.out.println("Credentials address:" + address);
				System.out.println("Credentials ecKeyPair:" + ecKeyPair);
				//獲取gasprice
				BigInteger gasPrice = getGasPrice();
				ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
				//加載NFT,使用op网的goerli测试链
				TransactionManager transactionManager = new RawTransactionManager(
					web3j, credentials, chainId);
				Market2 contract = Market2.load(marketAddress, web3j, transactionManager, gasProvider);
				log.info("加载交易所合约成功： " + contract);
				// 打印admin地址
				System.out.println("adminAddress: " + adminAddress);
				BigInteger productId = new BigInteger(tokenId.toString());
				// price由于eth转换为wei，所以需要乘以10的18次方
				BigDecimal price = pfpTokenPO.getPrice();
//				// 判断是否已经list过
//				if (pfpTokenPO.getTransactionsCount() == 0 && pfpTokenPO.getPickId() == null) {
//					int level = pfpTokenPO.getLevel();
//					if (level == 1) {
//						price = new BigDecimal("0.01");
//					} else if (level == 2) {
//						price = new BigDecimal("0.05");
//					} else if (level == 3) {
//						price = new BigDecimal("0.25");
//					} else if (level == 4) {
//						price = new BigDecimal("0.5");
//					} else if (level == 5) {
//						price = new BigDecimal("1");
//					}
//				}
				BigInteger weiPrice = price.multiply(new BigDecimal("1000000000000000000")).toBigInteger();
				log.info("listItem 链上数据： contract add: " + pfpTokenPO.getContractAddress() + " ;tokenId=" + productId + " ;price=" + weiPrice + " ;nftOwnerAddress=" + nftOwnerAddress);
				TransactionReceipt receipt = contract.listItem(
					pfpTokenPO.getContractAddress(),
					productId,
					nftOwnerAddress,
					weiPrice
				).send();
				log.info("调用listItem成功：" + receipt);
				//獲取交易hash
				String transactionHash = receipt.getTransactionHash();
				log.info("==============listItem交易hash： " + transactionHash);
				if (transactionHash == null) {
					System.out.println("list fail");
				} else {
					System.out.println("list success: " + transactionHash);
					// list信息存储active
					ActivePO activePO = new ActivePO();
					activePO.initForInsert();
					activePO.setTokenId(tokenId);
					activePO.setType(0);
					activePO.setTokenImg(pfpTokenPO.getSquarePictureUrl());
					activePO.setUsername(memberPO.getUserName());
					activePO.setUserAddress(nftOwnerAddress);
					activePO.setUserImg(memberPO.getAvatar());
					activePO.setPrice(pfpTokenPO.getPrice());
					log.info("list信息存储active: " + activePO);
					activeMapper.insert(activePO);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return R.fail("list fail: contract call error.");
			}
			//创建pick记录
			PFPPickPO pfpPickPO = new PFPPickPO();
			pfpPickPO.setTokenId(pfpTokenPO.getRealTokenId());
			pfpPickPO.setMintUserAddress(pfpTokenPO.getMintUserAddress());
			pfpPickPO.setMintUserId(pfpTokenPO.getMintUserId());
			pfpPickPO.setLevel(pfpTokenPO.getLevel());
			pfpPickPO.setBasePrice(pfpTokenPO.getBasePrice());
			pfpPickPO.setTransactionsCount(pfpTokenPO.getTransactionsCount());
			pfpPickPO.setPrice(pfpTokenPO.getPrice());
			//交易状态：0-拼团中
			pfpPickPO.setStatus(0);
			pfpPickPO.setNowPickCount(0);

			pfpPickPO.initForInsert();
			pfpPickMapper.insert(pfpPickPO);


			pfpTokenPO.setPriceTime(new Date());
			//pick状态:1- launching (可以pick)
			pfpTokenPO.setPickStatus(1);
			//关联的pick信息
			pfpTokenPO.setPickId(pfpPickPO.getId());
			// 设置交易所合约地址
			pfpTokenPO.setContractMarketAddress(marketAddress);
//
			pfpTokenPO.initForUpdateNoAuth();
			pfpTokenMapper.updateById(pfpTokenPO);

			return R.success("list success");
		} else {
			return R.fail("please refresh and try again");
		}
	}

	// pickItem
	public R pickItem(String nftAddress, String address, BigInteger tokenId, BigInteger pickIndex, Log web3jLog) {

		try {
//			Long userId = StpUtil.getLoginIdAsLong();
//			MemberPO memberPO = memberMapper.selectById(userId);
			// 根据 address 查询 memberPO
			MemberPO memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
				.eq(MemberPO::getAddress, address)
				.eq(MemberPO::getIsDeleted, 0)
			);
			Long userId = memberPO.getId();
			PFPTokenPO pfpTokenPO = pfpTokenMapper.selectOne(new LambdaQueryWrapper<PFPTokenPO>()
				.eq(PFPTokenPO::getContractAddress, nftAddress)
				.eq(PFPTokenPO::getRealTokenId, tokenId)
				.eq(PFPTokenPO::getIsDeleted, 0)
			);

			Long pickId = pfpTokenPO.getPickId();
			PFPPickPO pfpPickPO = pfpPickMapper.selectById(pickId);

			if (pfpPickPO.getIndexUserId0() != null && pfpPickPO.getIndexUserId0().equals(userId)) {
				log.error("You have already picked the SoulCast.");
				return R.fail("You have been picked #0");
			}
			if (pfpPickPO.getIndexUserId1() != null && pfpPickPO.getIndexUserId1().equals(userId)) {
				log.error("You have already picked the SoulCast.");
				return R.fail("You have been picked #1");
			}
			if (pfpPickPO.getIndexUserId2() != null && pfpPickPO.getIndexUserId2().equals(userId)) {
				log.error("You have already picked the SoulCast.");
				return R.fail("You have been picked #2");
			}
			if (pfpPickPO.getIndexUserId3() != null && pfpPickPO.getIndexUserId3().equals(userId)) {
				log.error("You have already picked the SoulCast.");
				return R.fail("You have been picked #3");
			}

			if (pfpTokenPO.getPickStatus() != 1) {
				log.error("This PFP is not allow pick");
				return R.fail("This PFP is not allow pick");
			}


			//保存付款记录
			WallectHistoryPO wallectHistoryPO = new WallectHistoryPO();
			wallectHistoryPO.setUserId(userId);
			wallectHistoryPO.setType(3);
			wallectHistoryPO.setTxnHash(web3jLog.getTransactionHash());
			wallectHistoryPO.setPrice(pfpTokenPO.getPrice());
			// Date date = new Date();
			//		this.createTime = date;
			//		this.updateTime = date;
			//		Long userId = StpUtil.getLoginIdAsLong();
			//		this.createUser = userId;
			//		this.updateUser = userId;
			//		this.isDeleted = 0;
			Date date = new Date();
			wallectHistoryPO.setCreateTime(date);
			wallectHistoryPO.setUpdateTime(date);
			wallectHistoryPO.setCreateUser(userId);
			wallectHistoryPO.setUpdateUser(userId);
			wallectHistoryPO.setIsDeleted(0);

			wallectHistoryMapper.insert(wallectHistoryPO);

			PFPTransactionPO pfpTransactionPO = new PFPTransactionPO();
			pfpTransactionPO.setTokenId(pfpTokenPO.getRealTokenId());
			pfpTransactionPO.setAdminAddress(pfpTokenPO.getAdminAddress());
			pfpTransactionPO.setLinkType(pfpTokenPO.getLinkType());
			pfpTransactionPO.setNetwork(pfpTokenPO.getNetwork());
			pfpTransactionPO.setContractAddress(pfpTokenPO.getContractAddress());
			pfpTransactionPO.setFromAddress(memberPO.getAddress());
			pfpTransactionPO.setToAddress(pfpTokenPO.getAdminAddress());
			pfpTransactionPO.setFromUserId(memberPO.getId());
//		pfpTransactionPO.setToUserId(userId);
//		交易状态：0-未交易 1-已付款未交易PFP 2-交易完成 3-交易取消
			pfpTransactionPO.setTransactionStatus(1);
			pfpTransactionPO.setBuyerMoneyTxnHash("");

			//计算费用
			pfpTransactionPO.setListPrice(pfpTokenPO.getPrice());
			pfpTransactionPO.setCreateTime(date);
			pfpTransactionPO.setUpdateTime(date);
			pfpTransactionPO.setCreateUser(userId);
			pfpTransactionPO.setUpdateUser(userId);
			pfpTransactionPO.setIsDeleted(1);
			pfpTransactionPO.setPickId(pickId);
			pfpTransactionMapper.insert(pfpTransactionPO);

			Long pickIndexUserId = null;
			// pickIndex转为int
			int pickIndexInt = pickIndex.intValue();
			if (pickIndexInt == 0) {
				pickIndexUserId = pfpPickPO.getIndexUserId0();
			} else if (pickIndexInt == 1) {
				pickIndexUserId = pfpPickPO.getIndexUserId1();
			} else if (pickIndexInt == 2) {
				pickIndexUserId = pfpPickPO.getIndexUserId2();
			} else if (pickIndexInt == 3) {
				pickIndexUserId = pfpPickPO.getIndexUserId3();
			} else {
				log.error("pickIndex must be 0/1/2/3");
			}

			//签号已被购买
			if (pickIndexUserId != null) {
				log.error("Sorry，pick failed");
			}


			//pick逻辑
			if (pickIndexInt == 0) {
				pfpPickPO.setIndexAddress0(memberPO.getAddress());
				pfpPickPO.setIndexUserPickTime0(date);
				pfpPickPO.setIndexUserId0(userId);
				pfpPickPO.setIndexPayTxn0(web3jLog.getTransactionHash());
			} else if (pickIndexInt == 1) {
				pfpPickPO.setIndexAddress1(memberPO.getAddress());
				pfpPickPO.setIndexUserPickTime1(date);
				pfpPickPO.setIndexUserId1(userId);
				pfpPickPO.setIndexPayTxn1(web3jLog.getTransactionHash());
			} else if (pickIndexInt == 2) {
				pfpPickPO.setIndexAddress2(memberPO.getAddress());
				pfpPickPO.setIndexUserPickTime2(date);
				pfpPickPO.setIndexUserId2(userId);
				pfpPickPO.setIndexPayTxn2(web3jLog.getTransactionHash());
			} else if (pickIndexInt == 3) {
				pfpPickPO.setIndexAddress3(memberPO.getAddress());
				pfpPickPO.setIndexUserPickTime3(date);
				pfpPickPO.setIndexUserId3(userId);
				pfpPickPO.setIndexPayTxn3(web3jLog.getTransactionHash());
			}

			//当前pick人数
			pfpPickPO.setNowPickCount(pfpPickPO.getNowPickCount() + 1);
//			 Date date = new Date();
			//		this.updateTime = date;
			//		Long userId = StpUtil.getLoginIdAsLong();
			//		this.updateUser = userId;
			//		this.isDeleted = 0;
			pfpPickPO.initForUpdateNoAuth(userId);
			pfpPickMapper.updateById(pfpPickPO);

			// 查询是否已经存在该tokenId的chatOverview
			ChatOverviewPO chatOverviewPO = chatOverviewMapper.selectOne(new LambdaQueryWrapper<ChatOverviewPO>()
				.eq(ChatOverviewPO::getTokenId, tokenId)
				.eq(ChatOverviewPO::getType, 1)
				.eq(ChatOverviewPO::getIsDeleted, 0)
			);
			log.info("chatOverviewPO: " + chatOverviewPO);
			Date newDate = DateUtil.offset(new Date(), DateField.SECOND, -1);
			if (chatOverviewPO == null) {
				// 增加chatOverview
				chatOverviewPO = new ChatOverviewPO();
				chatOverviewPO.setType(1);
				chatOverviewPO.setTokenId(tokenId.longValue());
				chatOverviewPO.setStatus(1);
				chatOverviewPO.setTitle("Group #" + tokenId);
				chatOverviewPO.initForInsertNoAuth();
				chatOverviewMapper.insert(chatOverviewPO);

				// 增加mint用户的chatMember关联
				ChatMemberPO chatMemberPO = new ChatMemberPO();
				chatMemberPO.setChatId(chatOverviewPO.getId());
				chatMemberPO.setUserId(pfpTokenPO.getMintUserId());
				chatMemberPO.initForInsertNoAuth();
				log.info("insert chatMemberPO: " + chatMemberPO);
				chatMemberMapper.insert(chatMemberPO);

				// 增加用户进入记录，chatDetail增加一条
//				MemberPO mintUserPO = memberMapper.selectById(pfpTokenPO.getMintUserId());
				ChatDetailPO chatDetailPO = new ChatDetailPO();
				chatDetailPO.setType(99);
				chatDetailPO.setChatId(chatOverviewPO.getId());
				chatDetailPO.setUserId(pfpTokenPO.getMintUserId());
				chatDetailPO.setContent("joined the group chat");
				chatDetailPO.initForInsertNoAuth();
				log.info("insert chatDetailPO: " + chatDetailPO);
				chatDetailMapper.insert(chatDetailPO);

				WebsocketMessageDto messageDto = new WebsocketMessageDto();
				messageDto.setChatId(chatOverviewPO.getId());
				messageDto.setUserId(pfpTokenPO.getMintUserId());
				messageDto.setType(99);
				messageDto.setContent("joined the group chat");
				messageDto.setTime(DateUtil.now());

				// 更新聊天缓存
				IUserCache.updateUserRoomsCache();
				IUserCache.updateRoomUsersCache();
				IUserCache.updateUserCache();

				webSocket.sendMessage(chatOverviewPO.getId(), pfpTokenPO.getMintUserId(), messageDto);

				ChatSessionHistoryPO chatSessionHistoryPO = new ChatSessionHistoryPO();
				chatSessionHistoryPO.setChatId(chatOverviewPO.getId());
				chatSessionHistoryPO.setUserId(pfpTokenPO.getMintUserId());
				chatSessionHistoryPO.setEndTime(DateUtil.format(newDate, "yyyy-MM-dd HH:mm:ss"));
				chatSessionHistoryPO.initForInsertNoAuth();
				chatSessionHistoryMapper.insert(chatSessionHistoryPO);
			}
			// 查询userId是否已经存在于chatMember并且chatId为chatOverviewPO.getId()的记录
			ChatMemberPO chatMemberPO = chatMemberMapper.selectOne(new LambdaQueryWrapper<ChatMemberPO>()
				.eq(ChatMemberPO::getChatId, chatOverviewPO.getId())
				.eq(ChatMemberPO::getUserId, userId)
				.eq(ChatMemberPO::getIsDeleted, 0)
			);
			if (chatMemberPO == null) {
				// 增加chatMember关联
				chatMemberPO = new ChatMemberPO();
				chatMemberPO.setChatId(chatOverviewPO.getId());
				chatMemberPO.setUserId(userId);
				chatMemberPO.initForInsertNoAuth();
				log.info("insert chatMemberPO: " + chatMemberPO);
				chatMemberMapper.insert(chatMemberPO);

				ChatDetailPO chatDetailPO = new ChatDetailPO();
				chatDetailPO.setChatId(chatOverviewPO.getId());
				chatDetailPO.setUserId(userId);
				chatDetailPO.setType(99);
				chatDetailPO.setContent("joined the group chat");
				chatDetailPO.initForInsertNoAuth();
				log.info("insert chatDetailPO: " + chatDetailPO);
				chatDetailMapper.insert(chatDetailPO);

				WebsocketMessageDto messageDto = new WebsocketMessageDto();
				messageDto.setChatId(chatOverviewPO.getId());
				messageDto.setUserId(userId);
				messageDto.setType(99);
				messageDto.setContent("joined the group chat");
				messageDto.setTime(DateUtil.now());

				// 更新聊天缓存
				IUserCache.updateUserRoomsCache();
				IUserCache.updateRoomUsersCache();
				IUserCache.updateUserCache();

				webSocket.sendMessage(chatOverviewPO.getId(), userId, messageDto);

				ChatSessionHistoryPO chatSessionHistoryPO = new ChatSessionHistoryPO();
				chatSessionHistoryPO.setChatId(chatOverviewPO.getId());
				chatSessionHistoryPO.setUserId(userId);
				chatSessionHistoryPO.setEndTime(DateUtil.format(newDate, "yyyy-MM-dd HH:mm:ss"));
				chatSessionHistoryPO.initForInsertNoAuth();
				chatSessionHistoryMapper.insert(chatSessionHistoryPO);
			}


			// list信息存储active
			ActivePO activePO = new ActivePO();
			activePO.initForInsertNoAuth();
			activePO.setTokenId(tokenId.longValue());
			activePO.setType(1);
			activePO.setPickCount(pfpPickPO.getNowPickCount());
			activePO.setTokenImg(pfpTokenPO.getSquarePictureUrl());
			activePO.setUserAddress(memberPO.getAddress());
			activePO.setUsername(memberPO.getUserName());
			activePO.setUserImg(memberPO.getAvatar());
			activePO.setPrice(pfpTokenPO.getPrice());

			//active增加 区块高度字段 方便错误回滚
			activePO.setMarketBlockNumber(web3jLog.getBlockNumber().longValue());
			activeMapper.insert(activePO);

			// pick增加rank
			pfpTokenPO.pumpAddRank();
			pfpTokenPO.initForUpdateNoAuth();
			pfpTokenMapper.updateById(pfpTokenPO);


			//满员
			if (pfpPickPO.getNowPickCount() == 4) {

				Long now = System.currentTimeMillis();
				//开奖时间
				Long rewardTime = null;
				//开奖区块
				Long rewardBlockHeight = null;
				//开奖区块间隔数
				Long diff = null;

				//查询当前区块
//			Long blockHeight = ethService.getLastMainBlockHeight();
//
//			//获取开奖队列数量
//			Long count = bladeRedis.lLen(PickUtil.REWARD_LIST);
//			if(count == 0){
//				//没有开奖队列
//				//设置开奖区块间隔数
//				diff = rewardBlockCount;
//				//开奖区块 = 当前区块 + 开奖区块间隔数量
//				rewardBlockHeight = blockHeight + diff;
//
//				//开奖时间 = 当前时间 + 出块间隔 * 开奖区块间隔数量 (50 秒)
				rewardTime = now + 50 * 1000;
//				//缓存开奖时间
//				bladeRedis.set(PickUtil.REWARD_TIME,rewardTime);
//
//			}else {
//				//已有开奖队列，获取最后一个开奖区块
//				Long height = bladeRedis.get(PickUtil.LAST_REWARD_BLOCK_HEIGHT);
//				if(height == null){
//					//不存在则设置成最新行高 + 1
//					height = ethService.getLastMainBlockHeight() + 1;
//				}
//				//开奖区块 = 最后一个开奖区块 + 1
//				rewardBlockHeight = height + 1;
//
//				//计算开奖区块间隔数 = 开奖区块 - 当前区块
//				diff = rewardBlockHeight - blockHeight;
//
//				//开奖时间 = 当前时间 + 出块间隔 * 开奖区块间隔数量
//				rewardTime = now + blockInterval * diff * 1000;
//			}
//
//			//更新最新的开奖区块
//			bladeRedis.set(PickUtil.LAST_REWARD_BLOCK_HEIGHT,rewardBlockHeight);
//
//			//加入最右侧队列中
//			bladeRedis.rPush(PickUtil.REWARD_LIST,pfpPickPO.getId());

//			//满员时间
				pfpPickPO.setFullPickTime(new Date(now));
//			//设置开奖时间 50秒以后
				pfpPickPO.setRewardTime(new Date(rewardTime));
//			//设置开奖行高
//			pfpPickPO.setRewardBlockHeight(rewardBlockHeight);
//
				//待开奖
				pfpPickPO.setStatus(1);
				pfpPickPO.initForUpdateNoAuth(userId);
				pfpPickMapper.updateById(pfpPickPO);

				//待开奖
				pfpTokenPO.setPickStatus(2);
				pfpTokenPO.initForUpdateNoAuth(userId);
				pfpTokenMapper.updateById(pfpTokenPO);

				log.info("pick success, wait for reward");

				// 调用交易所合约 - dealList
				// 停顿40
				try {
					Thread.sleep(40 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return R.fail("pick fail: sleep error.");
				}
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
						web3j, credentials, chainId);
					Market2 contract = Market2.load(marketAddress, web3j, transactionManager, gasProvider);
					log.info("加载交易所合约成功： " + contract);
					// 打印admin地址
					System.out.println("adminAddress: " + adminAddress);
					BigInteger productId = new BigInteger(tokenId.toString());

					//费率列表
					ArrayList<BigInteger> rateList = getRate(tokenId.toString());

					// 调用
					log.info("dealList前： nftAddress= " + nftAddress + " ;tokenId=" + productId);
					TransactionReceipt receipt = contract.dealList(
						nftAddress,
						productId,
						rateList
					).send();
					log.info("调用listItem成功：" + receipt);
					//獲取交易hash
					String transactionHash = receipt.getTransactionHash();
					log.info("==============dealList交易hash： " + transactionHash);
					if (transactionHash == null) {
						log.info("deal list fail");
					} else {
						log.info("deal list success: " + transactionHash);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return R.fail("error: " + e.toString());
				}
			}
			return R.success("pick success");

		} catch (Exception e) {
			e.printStackTrace();
			return R.fail("pick fail: contract call error.");
		}


	}

	//refundPick
	@Transactional
	public R refundPick(BigInteger tokenId, BigInteger pickIndex, String buyer, Log web3jLog) {
		try {
			//取消pump
			//1.根据tokenid删除pick表中的pick记录
			LambdaQueryWrapper<PFPTokenPO> wp = new LambdaQueryWrapper<>();
			wp.eq(PFPTokenPO::getRealTokenId, tokenId);
			wp.eq(PFPTokenPO::getIsDeleted, 0);
			PFPTokenPO tokenPO = pfpTokenMapper.selectOne(wp);
			PFPPickPO pfpPickPO = pfpPickMapper.selectById(tokenPO.getPickId());

			LambdaUpdateWrapper<PFPPickPO> up = new LambdaUpdateWrapper<>();
			//pick人数-1
			up.set(PFPPickPO::getNowPickCount, pfpPickPO.getNowPickCount() - 1);
			up.eq(PFPPickPO::getId, pfpPickPO.getId());

			//2.钱包退款
			WallectHistoryPO refundWallectHistory = new WallectHistoryPO();

			switch (pickIndex.intValue()) {
				case 0:
					refundWallectHistory = new WallectHistoryPO(pfpPickPO.getIndexUserId0(), 4, null, web3jLog.getTransactionHash(), pfpPickPO.getPrice());
					up.set(PFPPickPO::getIndexUserId0, null);
					up.set(PFPPickPO::getIndexRewardTxn0, null);
					up.set(PFPPickPO::getIndexPayTxn0, null);
					up.set(PFPPickPO::getIndexUserId0, null);
					up.set(PFPPickPO::getIndexUserPickTime0, null);
					up.set(PFPPickPO::getIndexAddress0, null);
					break;
				case 1:
					refundWallectHistory = new WallectHistoryPO(pfpPickPO.getIndexUserId1(), 4, null, web3jLog.getTransactionHash(), pfpPickPO.getPrice());
					up.set(PFPPickPO::getIndexUserId1, null);
					up.set(PFPPickPO::getIndexRewardTxn1, null);
					up.set(PFPPickPO::getIndexPayTxn1, null);
					up.set(PFPPickPO::getIndexUserId1, null);
					up.set(PFPPickPO::getIndexUserPickTime1, null);
					up.set(PFPPickPO::getIndexAddress1, null);
					break;
				case 2:
					refundWallectHistory = new WallectHistoryPO(pfpPickPO.getIndexUserId2(), 4, null, web3jLog.getTransactionHash(), pfpPickPO.getPrice());
					up.set(PFPPickPO::getIndexUserId2, null);
					up.set(PFPPickPO::getIndexRewardTxn2, null);
					up.set(PFPPickPO::getIndexPayTxn2, null);
					up.set(PFPPickPO::getIndexUserId2, null);
					up.set(PFPPickPO::getIndexUserPickTime2, null);
					up.set(PFPPickPO::getIndexAddress2, null);
					break;
				case 3:
					refundWallectHistory = new WallectHistoryPO(pfpPickPO.getIndexUserId3(), 4, null, web3jLog.getTransactionHash(), pfpPickPO.getPrice());
					up.set(PFPPickPO::getIndexUserId3, null);
					up.set(PFPPickPO::getIndexRewardTxn3, null);
					up.set(PFPPickPO::getIndexPayTxn3, null);
					up.set(PFPPickPO::getIndexUserId3, null);
					up.set(PFPPickPO::getIndexUserPickTime3, null);
					up.set(PFPPickPO::getIndexAddress3, null);
					break;
				default:
					log.error("refundPick failed: pickIndex must be 0/1/2/3");
					return R.fail("pickIndex must be 0/1/2/3");
			}
			refundWallectHistory.initTime();
			int update = pfpPickMapper.update(null, up);
			int insert = wallectHistoryMapper.insert(refundWallectHistory);
			if (update == 0 || insert == 0) {
				//手动回滚事务
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				log.error("refundPick failed: tokenId: {} ; pickIndex: {} ; buyer: {}", tokenId, pickIndex, buyer);
				log.error("refundPick failed: insert status: {} ; update status: {}", insert, update);
				return R.fail("Data update or insertion failed");
			}
			return R.success("");
		} catch (Exception e) {
			log.error("refundPick failed: ", e);
			return R.fail("refundPick fail: contract call error.");
		}
	}

	// deal list
	public R dealList(
		String nftAddress,
		Long tokenId,
		String buyer,
		BigInteger buyer_index,
		BigInteger sellerAmount,
		BigInteger shareAmount,
		Log web3jLog
	) {
		try {
			// 判断tokenId是否已经处理过，如果已经处理过，则直接返回
			// 根据tokenId 获取pfpTokenPO
			PFPTokenPO pfpTokenPO = pfpTokenMapper.selectOne(new LambdaQueryWrapper<PFPTokenPO>()
				.eq(PFPTokenPO::getRealTokenId, tokenId)
				.eq(PFPTokenPO::getIsDeleted, 0)
			);
			if (pfpTokenPO.getPickStatus() != 2) {
				log.error("This PFP is not allow deal");
				return R.fail("This PFP is not allow deal");
			}
			// 根据tokenId 获取PFPPickPO
			PFPPickPO pfpPickPO = pfpPickMapper.selectById(pfpTokenPO.getPickId());


			Long pickId = pfpTokenPO.getPickId();
			//变更状态为开奖中
			//开奖中，不可交易
			pfpTokenPO.setPickStatus(3);
			pfpTokenMapper.updateById(pfpTokenPO);
			// 开奖
			String ownerAddress = pfpTokenPO.getOwnerAddress();
			Long ownerUserId = pfpTokenPO.getOwnerUserId();
			String toAddress = null;
			Long toUserId = null;
			int buyer_index_int = buyer_index.intValue();
			if (buyer_index_int == 0) {
				toAddress = pfpPickPO.getIndexAddress0();
				toUserId = pfpPickPO.getIndexUserId0();
			} else if (buyer_index_int == 1) {
				toAddress = pfpPickPO.getIndexAddress1();
				toUserId = pfpPickPO.getIndexUserId1();
			} else if (buyer_index_int == 2) {
				toAddress = pfpPickPO.getIndexAddress2();
				toUserId = pfpPickPO.getIndexUserId2();
			} else if (buyer_index_int == 3) {
				toAddress = pfpPickPO.getIndexAddress3();
				toUserId = pfpPickPO.getIndexUserId3();
			}

			//pfpPick中奖设置
			pfpPickPO.setRewardIndex(buyer_index_int);
			pfpPickPO.setRewardBlockHash(web3jLog.getTransactionHash());
			pfpPickPO.setRewardTime(new Date());
			pfpPickPO.setRewardUserId(toUserId);
			pfpPickPO.setRewardUserAddress(toAddress);

			//创建订单
			PFPTransactionPO pfpTransactionPO = new PFPTransactionPO();
			pfpTransactionPO.setTokenId(tokenId);
			pfpTransactionPO.setAdminAddress(pfpTokenPO.getAdminAddress());
			pfpTransactionPO.setLinkType(pfpTokenPO.getLinkType());
			pfpTransactionPO.setNetwork(pfpTokenPO.getNetwork());
			pfpTransactionPO.setContractAddress(pfpTokenPO.getContractAddress());
			pfpTransactionPO.setContractName(pfpTokenPO.getContractName());
			pfpTransactionPO.setFromAddress(ownerAddress);
			pfpTransactionPO.setToAddress(toAddress);
			pfpTransactionPO.setFromUserId(ownerUserId);
			pfpTransactionPO.setToUserId(toUserId);
//		交易状态：0-未交易 1-已付款未交易PFP 2-交易完成 3-交易取消
			pfpTransactionPO.setTransactionStatus(2);
			pfpTransactionPO.setPayIndex(buyer_index_int);
//		pfpTransactionPO.setPfpTxnHash();
			pfpTransactionPO.setMintUserId(pfpTokenPO.getMintUserId());
			pfpTransactionPO.setMintUserAddress(pfpTokenPO.getMintUserAddress());

			//计算费用
			pfpTransactionPO.setListPrice(pfpTokenPO.getPrice());

			pfpTransactionPO.initForInsertNoAuth();
			pfpTransactionPO.setPickId(pickId);
			pfpTransactionMapper.insert(pfpTransactionPO);
			log.info("ownerUserId: " + ownerUserId + " toUserId: " + toUserId);
			//添加双方用户为star连接
			memberConnectService.addStarConnected(ownerUserId, toUserId);

			//添加history
			PFPHistoryPO pfpHistoryPO = new PFPHistoryPO();
			pfpHistoryPO.setTokenId(pfpTokenPO.getRealTokenId());
			//被购买
			pfpHistoryPO.setType(1);
			pfpHistoryPO.setTransactionId(pfpTransactionPO.getId());
			pfpHistoryPO.setAdminAddress(pfpTokenPO.getAdminAddress());
			pfpHistoryPO.setLinkType(pfpTokenPO.getLinkType());
			pfpHistoryPO.setNetwork(pfpTokenPO.getNetwork());
			pfpHistoryPO.setContractAddress(pfpTokenPO.getContractAddress());
			pfpHistoryPO.setContractName(pfpTokenPO.getContractName());

			pfpHistoryPO.setFromAddress(pfpTransactionPO.getFromAddress());
			pfpHistoryPO.setToAddress(pfpTransactionPO.getToAddress());
			pfpHistoryPO.setFromUserId(pfpTransactionPO.getFromUserId());
			pfpHistoryPO.setToUserId(pfpTransactionPO.getToUserId());
			pfpHistoryPO.setTxnHash(web3jLog.getTransactionHash());
			pfpHistoryPO.setPrice(pfpTokenPO.getPrice());
			pfpHistoryPO.initForInsertNoAuth();

			pfpHistoryMapper.insert(pfpHistoryPO);

			//wBNB转账：卖NFT收益
//			BigDecimal loserReward = PickUtil.getLoserReward(pfpTokenPO.getLevel(), pfpTokenPO.getTransactionsCount(),linkRate);
//			BigDecimal minterReward = PickUtil.getMinterReward(pfpTokenPO.getLevel(), pfpTokenPO.getTransactionsCount(),linkRate);
//			BigDecimal sellerReward = PickUtil.getSellerReward(pfpTokenPO.getLevel(), pfpTokenPO.getTransactionsCount(),linkRate);
//			log.info("售价{}，未中奖收益{},创作者收益{}",pfpTokenPO.getPrice(),loserReward,minterReward);

			//设置未中奖收益
			// 将gas转换为eth
//			BigDecimal sellerReward = new BigDecimal(sellerAmount).divide(new BigDecimal("1000000000000000000"));
//			BigDecimal shareReward = new BigDecimal(shareAmount).divide(new BigDecimal("1000000000000000000"));
			//铸造者收益
//			BigInteger creatorRateBigInteger = contractProperties.getCreatorRate();
//			BigDecimal creatorRate = new BigDecimal(creatorRateBigInteger);
//			BigDecimal divisor = new BigDecimal(1000); // 将 1000 转换为 BigDecimal 以做精确计算
//			BigDecimal shareReward = creatorRate.divide(divisor, 10, RoundingMode.HALF_UP);
			BigDecimal shareReward = pfpPickPO.getPrice().multiply(new BigDecimal(contractProperties.getCreatorRate()).divide(BigDecimal.valueOf(1000)));
			System.out.println(shareReward.toBigInteger());
			BigDecimal sellerReward = new BigDecimal("0");

			//铸造者收益
			pfpPickPO.setMinterRewardPrice(shareReward);
			//邀请人

			//消息标题
			String title = "Launching SoulCast #" + tokenId + " Announced";
			//消息内容
			String content = "Check your rewards!";
			//中奖消息
			MessagePO message0 = new MessagePO(0, 1L, pfpPickPO.getIndexUserId0(), RewardMessageEnum.REWARD_FAILED.getName(), title, content, 0, tokenId, pickId);
			MessagePO message1 = new MessagePO(0, 1L, pfpPickPO.getIndexUserId1(), RewardMessageEnum.REWARD_FAILED.getName(), title, content, 0, tokenId, pickId);
			MessagePO message2 = new MessagePO(0, 1L, pfpPickPO.getIndexUserId2(), RewardMessageEnum.REWARD_FAILED.getName(), title, content, 0, tokenId, pickId);
			MessagePO message3 = new MessagePO(0, 1L, pfpPickPO.getIndexUserId3(), RewardMessageEnum.REWARD_FAILED.getName(), title, content, 0, tokenId, pickId);

			//钱包余额记录：退款: 4-refund
			WallectHistoryPO refundWallectHistory0 = new WallectHistoryPO(pfpPickPO.getIndexUserId0(), 4, pfpTransactionPO.getId(), null, pfpPickPO.getPrice());
			WallectHistoryPO refundWallectHistory1 = new WallectHistoryPO(pfpPickPO.getIndexUserId1(), 4, pfpTransactionPO.getId(), null, pfpPickPO.getPrice());
			WallectHistoryPO refundWallectHistory2 = new WallectHistoryPO(pfpPickPO.getIndexUserId2(), 4, pfpTransactionPO.getId(), null, pfpPickPO.getPrice());
			WallectHistoryPO refundWallectHistory3 = new WallectHistoryPO(pfpPickPO.getIndexUserId3(), 4, pfpTransactionPO.getId(), null, pfpPickPO.getPrice());

			//根据tokenid查询pump记录，返回每个位置所得收益
			BigDecimal price = pfpPickPO.getPrice();
			ArrayList<BigInteger> rate = getRate(String.valueOf(tokenId));
			BigDecimal multiply0 = price.multiply(new BigDecimal(rate.get(0)).divide(BigDecimal.valueOf(1000)));
			BigDecimal multiply1 = price.multiply(new BigDecimal(rate.get(1)).divide(BigDecimal.valueOf(1000)));
			BigDecimal multiply2 = price.multiply(new BigDecimal(rate.get(2)).divide(BigDecimal.valueOf(1000)));
			BigDecimal multiply3 = price.multiply(new BigDecimal(rate.get(3)).divide(BigDecimal.valueOf(1000)));
//			System.out.println(multiply0.toString());
//			System.out.println(multiply1.toString());
//			System.out.println(multiply2.toString());
//			System.out.println(multiply3.toString());
			//钱包余额记录：收益：2-earn
			WallectHistoryPO wallectHistory0 = new WallectHistoryPO(pfpPickPO.getIndexUserId0(), 2, pfpTransactionPO.getId(), null, multiply0);
			WallectHistoryPO wallectHistory1 = new WallectHistoryPO(pfpPickPO.getIndexUserId1(), 2, pfpTransactionPO.getId(), null, multiply1);
			WallectHistoryPO wallectHistory2 = new WallectHistoryPO(pfpPickPO.getIndexUserId2(), 2, pfpTransactionPO.getId(), null, multiply2);
			WallectHistoryPO wallectHistory3 = new WallectHistoryPO(pfpPickPO.getIndexUserId3(), 2, pfpTransactionPO.getId(), null, multiply3);

			//钱包余额记录：推荐人收益：7-recommendation
			//1.根据token查询token pump用户信息 pfpTokenPO
			Long[] pumpUserIds = {pfpPickPO.getIndexUserId0(), pfpPickPO.getIndexUserId1(), pfpPickPO.getIndexUserId2(), pfpPickPO.getIndexUserId3()};
			//2.根据pump信息查询推荐人信息
			LambdaQueryWrapper<MemberPO> wp = new LambdaQueryWrapper<MemberPO>()
				.in(MemberPO::getId, pumpUserIds);
			List<MemberPO> memberPOS = memberMapper.selectList(wp);
			//3.根据nft价格与推荐人收益费率计算出收益价格

			BigDecimal refera = price.multiply(new BigDecimal(contractProperties.getReferalRate()).divide(BigDecimal.valueOf(1000)));
			//4.添加进数据库
			for (MemberPO memberPO : memberPOS) {
				if (memberPO.getInviteUserId() == 0) {
					continue;
				}
				WallectHistoryPO referaWallectHistory = new WallectHistoryPO(memberPO.getInviteUserId(), 7, pfpTransactionPO.getId(), null, refera);
				referaWallectHistory.setTxnHash("");
				referaWallectHistory.initTime();
				wallectHistoryMapper.insert(referaWallectHistory);
			}

			//卖家收益从 nft 减去没中奖，减去推荐人收益，减去铸造者收益，平台收益
			//减去铸造者收益
			sellerReward = sellerReward.add(shareReward);
			//添加推荐人收益 X 4
			sellerReward = sellerReward.add(refera);
			sellerReward = sellerReward.add(refera);
			sellerReward = sellerReward.add(refera);
			sellerReward = sellerReward.add(refera);
			//减去平台收益
			sellerReward = sellerReward.add(price.multiply(new BigDecimal(contractProperties.getProtocolRate()).divide(BigDecimal.valueOf(1000))));
			//减去没中奖人收益
			switch (buyer_index_int) {
				case 0:
					sellerReward = sellerReward.add(multiply1);
					sellerReward = sellerReward.add(multiply2);
					sellerReward = sellerReward.add(multiply3);
					break;
				case 1:
					sellerReward = sellerReward.add(multiply0);
					sellerReward = sellerReward.add(multiply2);
					sellerReward = sellerReward.add(multiply3);
					break;
				case 2:
					sellerReward = sellerReward.add(multiply0);
					sellerReward = sellerReward.add(multiply1);
					sellerReward = sellerReward.add(multiply3);
					break;
				case 3:
					sellerReward = sellerReward.add(multiply0);
					sellerReward = sellerReward.add(multiply1);
					sellerReward = sellerReward.add(multiply2);
					break;
			}
			//nft价格减去 pump没中奖人收益，减去推荐人收益，减去铸造者收益，减去平台收益 得到卖家收益
			sellerReward = price.subtract(sellerReward);
			System.out.println(sellerReward.toString());
			pfpPickPO.setRewardPrice(sellerReward);
			//卖家收益
			pfpPickPO.setSellerRewardPrice(sellerReward);

			//中奖积分
//			BigDecimal vSoulPrice0 = null;
//			BigDecimal vSoulPrice1 = null;
//			BigDecimal vSoulPrice2 = null;
//			BigDecimal vSoulPrice3 = null;

			// 配置blade dict配置
			Dict doubleVSoulTimeDict = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
				.eq(Dict::getCode, "double_VSoul_time")
				.eq(Dict::getDictKey, "0")
				.eq(Dict::getIsDeleted, 0));
			String dictValue = doubleVSoulTimeDict.getDictValue();
			// getDictValue格式为YYYY-MM-DD HH:mm:ss 转换为Date
			Date doubleVSoulTime = DateUtil.parse(dictValue, "yyyy-MM-dd HH:mm:ss");
			// 是否双倍积分
			boolean historyType0 = false;
			boolean historyType1 = false;
			boolean historyType2 = false;
			boolean historyType3 = false;
			// 若当前时间小于doubleVSoulTime，则为双倍积分，type = 7
			if (pfpPickPO.getIndexUserPickTime0().before(doubleVSoulTime)) {
				historyType0 = true;
			}
			if (pfpPickPO.getIndexUserPickTime1().before(doubleVSoulTime)) {
				historyType1 = true;
			}
			if (pfpPickPO.getIndexUserPickTime2().before(doubleVSoulTime)) {
				historyType2 = true;
			}
			if (pfpPickPO.getIndexUserPickTime3().before(doubleVSoulTime)) {
				historyType3 = true;
			}

			//积分余额记录
			VSoulHistoryPO vSoulHistoryPO0 = new VSoulHistoryPO(pfpPickPO.getIndexUserId0(), 1, pickId, null);
			VSoulHistoryPO vSoulHistoryPO1 = new VSoulHistoryPO(pfpPickPO.getIndexUserId1(), 1, pickId, null);
			VSoulHistoryPO vSoulHistoryPO2 = new VSoulHistoryPO(pfpPickPO.getIndexUserId2(), 1, pickId, null);
			VSoulHistoryPO vSoulHistoryPO3 = new VSoulHistoryPO(pfpPickPO.getIndexUserId3(), 1, pickId, null);

			VSoulHistoryPO vSoulHistoryPO0_2 = new VSoulHistoryPO(pfpPickPO.getIndexUserId0(), 7, pickId, null);
			VSoulHistoryPO vSoulHistoryPO1_2 = new VSoulHistoryPO(pfpPickPO.getIndexUserId1(), 7, pickId, null);
			VSoulHistoryPO vSoulHistoryPO2_2 = new VSoulHistoryPO(pfpPickPO.getIndexUserId2(), 7, pickId, null);
			VSoulHistoryPO vSoulHistoryPO3_2 = new VSoulHistoryPO(pfpPickPO.getIndexUserId3(), 7, pickId, null);

			//退款金额
			BigDecimal refund = sellerReward.add(pfpPickPO.getPrice());

			// 绑定一对一聊天
			// 查询是否已经存在该tokenId的chatOverview
			ChatOverviewPO chatOverviewPO = chatOverviewMapper.selectOne(new LambdaQueryWrapper<ChatOverviewPO>()
				.eq(ChatOverviewPO::getTokenId, tokenId)
				.eq(ChatOverviewPO::getType, 0)
				.eq(ChatOverviewPO::getStatus, 1)
				.eq(ChatOverviewPO::getIsDeleted, 0)
			);
			log.info("chatOverviewPO: " + chatOverviewPO);
			if (chatOverviewPO != null) {
				// 已存在了，将原来的status设置为0
				chatOverviewPO.setStatus(0);
				chatOverviewPO.initForUpdateNoAuth();
				chatOverviewMapper.updateById(chatOverviewPO);
			}

			if (!(toUserId.equals(pfpTokenPO.getMintUserId()))) {
				// 增加chatOverview
				chatOverviewPO = new ChatOverviewPO();
				chatOverviewPO.setType(0);
				chatOverviewPO.setTokenId(tokenId);
				chatOverviewPO.setStatus(1);
				chatOverviewPO.setTitle("Chat #" + tokenId);
				chatOverviewPO.initForInsertNoAuth();
				chatOverviewMapper.insert(chatOverviewPO);

				// 增加mint用户的chatMember关联
				ChatMemberPO chatMemberPO = new ChatMemberPO();
				chatMemberPO.setChatId(chatOverviewPO.getId());
				chatMemberPO.setUserId(toUserId);
				chatMemberPO.initForInsertNoAuth();
				log.info("insert chatMemberPO: " + chatMemberPO);
				chatMemberMapper.insert(chatMemberPO);

				// 插入sessionHistory
				ChatSessionHistoryPO chatSessionHistoryPO = new ChatSessionHistoryPO();
				chatSessionHistoryPO.setChatId(chatOverviewPO.getId());
				chatSessionHistoryPO.setUserId(toUserId);
				Date newDate = DateUtil.offset(new Date(), DateField.SECOND, -1);
				chatSessionHistoryPO.setEndTime(DateUtil.format(newDate, "yyyy-MM-dd HH:mm:ss"));
				chatSessionHistoryPO.initForInsertNoAuth();
				chatSessionHistoryMapper.insert(chatSessionHistoryPO);

				ChatDetailPO chatDetailPO = new ChatDetailPO();
				chatDetailPO.setChatId(chatOverviewPO.getId());
				chatDetailPO.setUserId(toUserId);
				chatDetailPO.setType(99);
				chatDetailPO.setContent("joined the chat");
				chatDetailPO.initForInsertNoAuth();
				log.info("insert chatDetailPO: " + chatDetailPO);
				chatDetailMapper.insert(chatDetailPO);


				// 增加mint用户的chatMember关联
				ChatMemberPO chatMemberPO2 = new ChatMemberPO();
				chatMemberPO2.setChatId(chatOverviewPO.getId());
				chatMemberPO2.setUserId(pfpTokenPO.getMintUserId());
				chatMemberPO2.initForInsertNoAuth();
				log.info("insert chatMemberPO: " + chatMemberPO2);
				chatMemberMapper.insert(chatMemberPO2);

				// 插入sessionHistory
				ChatSessionHistoryPO chatSessionHistoryPO2 = new ChatSessionHistoryPO();
				chatSessionHistoryPO2.setChatId(chatOverviewPO.getId());
				chatSessionHistoryPO2.setUserId(pfpTokenPO.getMintUserId());
				chatSessionHistoryPO2.setEndTime(DateUtil.format(newDate, "yyyy-MM-dd HH:mm:ss"));
				chatSessionHistoryMapper.insert(chatSessionHistoryPO2);

				ChatDetailPO chatDetailPO2 = new ChatDetailPO();
				chatDetailPO2.setChatId(chatOverviewPO.getId());
				chatDetailPO2.setUserId(pfpTokenPO.getMintUserId());
				chatDetailPO2.setType(99);
				chatDetailPO2.setContent("joined the chat");
				chatDetailPO2.initForInsertNoAuth();
				log.info("insert chatDetailPO: " + chatDetailPO2);
				chatDetailMapper.insert(chatDetailPO2);
			}
			// 更新聊天缓存
			IUserCache.updateUserRoomsCache();
			IUserCache.updateRoomUsersCache();
			IUserCache.updateUserCache();

			//添加active表开奖记录
			ActivePO activePO = new ActivePO();
			activePO.initForInsertNoAuth();
			activePO.setTokenId(tokenId);
			activePO.setType(3);
			activePO.setTokenImg(pfpTokenPO.getSquarePictureUrl());
			//token铸造者
			activePO.setTokenUserId(pfpTokenPO.getMintUserId());
			//中奖用户
			activePO.setLotteryUserId(toUserId);


			log.info("开奖信息存储active: " + activePO);
			activeMapper.insert(activePO);
			//更新之前的记录信息为已经开奖
//			LambdaUpdateWrapper<ActivePO> updateWrapper = new LambdaUpdateWrapper<>();
//			updateWrapper.eq(ActivePO::getTokenId, tokenId);
//			updateWrapper.set(ActivePO::getCheckLotteryStatus, 1);
//			activeMapper.update(null, updateWrapper);


//			String txn0 = null;txn1 = null,txn2 = null,txn3 = null;
////			String refundTxn0 = null,refundTxn1 = null,refundTxn2 = null,refundTxn3 = null;

			if (buyer_index_int == 0) {

				//计算中奖积分
				BigDecimal vSoulPrice0 = getVSoulPriceNew(1, pfpPickPO.getIndexUserId0(), pfpTokenPO);
				vSoulHistoryPO0.setVSoulPrice(vSoulPrice0);
				vSoulHistoryPO0.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO0);
				if (historyType0) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO0_2.setVSoulPrice(vSoulPrice0);
					vSoulHistoryPO0_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO0_2);
					updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId0(), vSoulPrice0.multiply(BigDecimal.valueOf(0.1)));

				message0.setMessage(RewardMessageEnum.REWARD_SUCCESS.getName());
				message0.initForInsertNoAuth();
				messageMapper.insert(message0);

				//计算中奖积分
				BigDecimal vSoulPrice1 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId1(), pfpTokenPO);
				vSoulHistoryPO1.setVSoulPrice(vSoulPrice1);
				vSoulHistoryPO1.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO1);
				if (historyType1) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO1_2.setVSoulPrice(vSoulPrice1);
					vSoulHistoryPO1_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO1_2);
					updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId1(), vSoulPrice1.multiply(BigDecimal.valueOf(0.1)));

				message1.initForInsertNoAuth();
				messageMapper.insert(message1);

				//计算中奖积分
				BigDecimal vSoulPrice2 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId2(), pfpTokenPO);
				vSoulHistoryPO2.setVSoulPrice(vSoulPrice2);
				vSoulHistoryPO2.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO2);
				if (historyType2) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO2_2.setVSoulPrice(vSoulPrice2);
					vSoulHistoryPO2_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO2_2);
					updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId2(), vSoulPrice2.multiply(BigDecimal.valueOf(0.1)));

				message2.initForInsertNoAuth();
				messageMapper.insert(message2);

				//计算中奖积分
				BigDecimal vSoulPrice3 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId3(), pfpTokenPO);
				vSoulHistoryPO3.setVSoulPrice(vSoulPrice3);
				vSoulHistoryPO3.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO3);
				if (historyType3) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO3_2.setVSoulPrice(vSoulPrice3);
					vSoulHistoryPO3_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO3_2);
					updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId3(), vSoulPrice3.multiply(BigDecimal.valueOf(0.1)));

				message3.initForInsertNoAuth();
				messageMapper.insert(message3);

				//收益
//			ThreadUtil.execAsync(new Runnable() {
//				@Override
//				public void run() {
				//给中签者转NFT
//						R<String> transferNFTResult = approveTransferNFT(ownerAddress,pfpPickPO.getIndexAddress0(),tokenId);
//						if(transferNFTResult.getCode() != 200){
//							log.error("=========transferNFTerror===========");
//						}
				//NFT交易hash
//						String transferNFTTxn = transferNFTResult.getData();
				pfpTransactionPO.setPfpTxnHash("");
				pfpPickPO.setNftTxn("");
				pfpHistoryPO.setTxnHash("");

				pfpHistoryMapper.updateById(pfpHistoryPO);

//						String txn1 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress1(),refund).getData();
				wallectHistory1.setTxnHash("");
				wallectHistory1.initTime();
				wallectHistoryMapper.insert(wallectHistory1);

				refundWallectHistory1.setTxnHash("");
				refundWallectHistory1.initTime();
				wallectHistoryMapper.insert(refundWallectHistory1);

				pfpPickPO.setIndexRewardTxn1("");
				pfpPickMapper.updateById(pfpPickPO);

//						String txn2 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress2(),refund).getData();
				wallectHistory2.setTxnHash("");
				wallectHistory2.initTime();
				wallectHistoryMapper.insert(wallectHistory2);

				refundWallectHistory2.setTxnHash("");
				refundWallectHistory2.initTime();
				wallectHistoryMapper.insert(refundWallectHistory2);

				pfpPickPO.setIndexRewardTxn2("");
				pfpPickMapper.updateById(pfpPickPO);


//						String txn3 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress3(),refund).getData();
				wallectHistory3.setTxnHash("");
				wallectHistory3.initTime();
				wallectHistoryMapper.insert(wallectHistory3);

				refundWallectHistory3.setTxnHash("");
				refundWallectHistory3.initTime();
				wallectHistoryMapper.insert(refundWallectHistory3);

				pfpPickPO.setIndexRewardTxn3("");
				pfpPickMapper.updateById(pfpPickPO);

				//wBNB转账：铸造者收益
//						String minterRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpTokenPO.getMintUserAddress(),minterReward).getData();
				//铸造者收益流水号
				pfpTransactionPO.setMinterMoneyTxnHash("");
				pfpPickPO.setMinterRewardTxn("");

				//铸造者收益记录
				WallectHistoryPO minterWallectHistory = new WallectHistoryPO(pfpTokenPO.getMintUserId(), 6, pfpTransactionPO.getId(), "minterRewardTxn", shareReward);
				minterWallectHistory.initForInsertNoAuth();
				wallectHistoryMapper.insert(minterWallectHistory);

				//wBNB转账：卖出者收益
//						String sellerRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), ownerAddress,sellerReward).getData();
				//卖出者收益流水号
				pfpTransactionPO.setSellerMoneyTxnHash("");
				pfpPickPO.setSellerRewardTxn("");

				pfpTransactionMapper.updateById(pfpTransactionPO);
				pfpPickMapper.updateById(pfpPickPO);

				//卖家收益记录
				WallectHistoryPO sellerWallectHistory = new WallectHistoryPO(ownerUserId, 5, pfpTransactionPO.getId(), "sellerRewardTxn", sellerReward);
				sellerWallectHistory.initForInsertNoAuth();
				wallectHistoryMapper.insert(sellerWallectHistory);

				//collect粉丝
				FansPO fansPO = new FansPO(1, pfpPickPO.getIndexUserId0(), pfpTokenPO.getMintUserId(), tokenId, pickId);
				fansPO.initForInsertNoAuth();
				fansMapper.insert(fansPO);
//				}
//			});
			} else if (buyer_index_int == 1) {

				//计算中奖积分
				BigDecimal vSoulPrice0 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId0(), pfpTokenPO);
				vSoulHistoryPO0.setVSoulPrice(vSoulPrice0);
				vSoulHistoryPO0.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO0);
				if (historyType0) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO0_2.setVSoulPrice(vSoulPrice0);
					vSoulHistoryPO0_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO0_2);
					updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId0(), vSoulPrice0.multiply(BigDecimal.valueOf(0.1)));

				message0.initForInsertNoAuth();
				messageMapper.insert(message0);

				//计算中奖积分
				BigDecimal vSoulPrice1 = getVSoulPriceNew(1, pfpPickPO.getIndexUserId1(), pfpTokenPO);
				vSoulHistoryPO1.setVSoulPrice(vSoulPrice1);
				vSoulHistoryPO1.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO1);
				if (historyType1) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO1_2.setVSoulPrice(vSoulPrice1);
					vSoulHistoryPO1_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO1_2);
					updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId1(), vSoulPrice1.multiply(BigDecimal.valueOf(0.1)));

				message1.setMessage(RewardMessageEnum.REWARD_SUCCESS.getName());
				message1.initForInsertNoAuth();
				messageMapper.insert(message1);

				//计算中奖积分
				BigDecimal vSoulPrice2 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId2(), pfpTokenPO);
				vSoulHistoryPO2.setVSoulPrice(vSoulPrice2);
				vSoulHistoryPO2.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO2);
				if (historyType2) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO2_2.setVSoulPrice(vSoulPrice2);
					vSoulHistoryPO2_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO2_2);
					updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId2(), vSoulPrice2.multiply(BigDecimal.valueOf(0.1)));

				message2.initForInsertNoAuth();
				messageMapper.insert(message2);

				//计算中奖积分
				BigDecimal vSoulPrice3 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId3(), pfpTokenPO);
				vSoulHistoryPO3.setVSoulPrice(vSoulPrice3);
				vSoulHistoryPO3.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO3);
				if (historyType3) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO3_2.setVSoulPrice(vSoulPrice3);
					vSoulHistoryPO3_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO3_2);
					updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId3(), vSoulPrice3.multiply(BigDecimal.valueOf(0.1)));

				message3.initForInsertNoAuth();
				messageMapper.insert(message3);

//			ThreadUtil.execAsync(new Runnable() {
//				@Override
//				public void run() {
				//给中签者转NFT
//						R<String> transferNFTResult = approveTransferNFT(ownerAddress,pfpPickPO.getIndexAddress1(),tokenId);
//						if(transferNFTResult.getCode() != 200){
//							log.error("=========transferNFTerror===========");
//						}
				//NFT交易hash
//						String transferNFTTxn = transferNFTResult.getData();
				pfpTransactionPO.setPfpTxnHash("transferNFTTxn");
				pfpPickPO.setNftTxn("transferNFTTxn");
				pfpHistoryPO.setTxnHash("transferNFTTxn");

				pfpHistoryMapper.updateById(pfpHistoryPO);

//						String txn0 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress0(),refund).getData();
				wallectHistory0.setTxnHash("txn0");
				wallectHistory0.initTime();
				wallectHistoryMapper.insert(wallectHistory0);

				refundWallectHistory0.setTxnHash("txn0");
				refundWallectHistory0.initTime();
				wallectHistoryMapper.insert(refundWallectHistory0);

				pfpPickPO.setIndexRewardTxn0("txn0");
				pfpPickMapper.updateById(pfpPickPO);

//						String txn2 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress2(),refund).getData();
				wallectHistory2.setTxnHash("txn2");
				wallectHistory2.initTime();
				wallectHistoryMapper.insert(wallectHistory2);

				refundWallectHistory2.setTxnHash("txn2");
				refundWallectHistory2.initTime();
				wallectHistoryMapper.insert(refundWallectHistory2);

				pfpPickPO.setIndexRewardTxn2("txn2");
				pfpPickMapper.updateById(pfpPickPO);

//						String txn3 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress3(),refund).getData();
				wallectHistory3.setTxnHash("txn3");
				wallectHistory3.initTime();
				wallectHistoryMapper.insert(wallectHistory3);

				refundWallectHistory3.setTxnHash("txn3");
				refundWallectHistory3.initTime();
				wallectHistoryMapper.insert(refundWallectHistory3);

				pfpPickPO.setIndexRewardTxn3("txn3");
				pfpPickMapper.updateById(pfpPickPO);

				//wBNB转账：铸造者收益
//						String minterRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpTokenPO.getMintUserAddress(),minterReward).getData();
				//铸造者收益流水号
				pfpTransactionPO.setMinterMoneyTxnHash("minterRewardTxn");
				pfpPickPO.setMinterRewardTxn("minterRewardTxn");

				//铸造者收益记录
				WallectHistoryPO minterWallectHistory = new WallectHistoryPO(pfpTokenPO.getMintUserId(), 6, pfpTransactionPO.getId(), "minterRewardTxn", shareReward);
				minterWallectHistory.initForInsertNoAuth();
				wallectHistoryMapper.insert(minterWallectHistory);

				//wBNB转账：卖出者收益
//						String sellerRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), ownerAddress,sellerReward).getData();
				//卖出者收益流水号
				pfpTransactionPO.setSellerMoneyTxnHash("sellerRewardTxn");
				pfpPickPO.setSellerRewardTxn("sellerRewardTxn");

				pfpTransactionMapper.updateById(pfpTransactionPO);
				pfpPickMapper.updateById(pfpPickPO);

				//卖家收益记录
				WallectHistoryPO sellerWallectHistory = new WallectHistoryPO(ownerUserId, 5, pfpTransactionPO.getId(), "sellerRewardTxn", sellerReward);
				sellerWallectHistory.initForInsertNoAuth();
				wallectHistoryMapper.insert(sellerWallectHistory);

				//collect粉丝
				FansPO fansPO = new FansPO(1, pfpPickPO.getIndexUserId1(), pfpTokenPO.getMintUserId(), tokenId, pickId);
				fansPO.initForInsertNoAuth();
				fansMapper.insert(fansPO);
//				}
//			});
			} else if (buyer_index_int == 2) {

				//计算中奖积分
				BigDecimal vSoulPrice0 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId0(), pfpTokenPO);
				vSoulHistoryPO0.setVSoulPrice(vSoulPrice0);
				vSoulHistoryPO0.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO0);
				if (historyType0) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO0_2.setVSoulPrice(vSoulPrice0);
					vSoulHistoryPO0_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO0_2);
					updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId0(), vSoulPrice0.multiply(BigDecimal.valueOf(0.1)));


				message0.initForInsertNoAuth();
				messageMapper.insert(message0);

				//计算中奖积分
				BigDecimal vSoulPrice1 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId1(), pfpTokenPO);
				vSoulHistoryPO1.setVSoulPrice(vSoulPrice1);
				vSoulHistoryPO1.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO1);
				if (historyType1) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO1_2.setVSoulPrice(vSoulPrice1);
					vSoulHistoryPO1_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO1_2);
					updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId1(), vSoulPrice1.multiply(BigDecimal.valueOf(0.1)));

				message1.initForInsertNoAuth();
				messageMapper.insert(message1);

				//计算中奖积分
				BigDecimal vSoulPrice2 = getVSoulPriceNew(1, pfpPickPO.getIndexUserId2(), pfpTokenPO);
				vSoulHistoryPO2.setVSoulPrice(vSoulPrice2);
				vSoulHistoryPO2.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO2);
				if (historyType2) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO2_2.setVSoulPrice(vSoulPrice2);
					vSoulHistoryPO2_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO2_2);
					updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId2(), vSoulPrice2.multiply(BigDecimal.valueOf(0.1)));

				message2.setMessage(RewardMessageEnum.REWARD_SUCCESS.getName());
				message2.initForInsertNoAuth();
				messageMapper.insert(message2);

				//计算中奖积分
				BigDecimal vSoulPrice3 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId3(), pfpTokenPO);
				vSoulHistoryPO3.setVSoulPrice(vSoulPrice3);
				vSoulHistoryPO3.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO3);
				if (historyType3) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO3_2.setVSoulPrice(vSoulPrice3);
					vSoulHistoryPO3_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO3_2);
					updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId3(), vSoulPrice3.multiply(BigDecimal.valueOf(0.1)));

				message3.initForInsertNoAuth();
				messageMapper.insert(message3);

//			ThreadUtil.execAsync(new Runnable() {
//				@Override
//				public void run() {
				//给中签者转NFT
//						R<String> transferNFTResult = approveTransferNFT(ownerAddress,pfpPickPO.getIndexAddress2(),tokenId);
//						if(transferNFTResult.getCode() != 200){
//							log.error("=========transferNFTerror===========");
//						}
				//NFT交易hash
//						String transferNFTTxn = transferNFTResult.getData();
				pfpTransactionPO.setPfpTxnHash("transferNFTTxn");
				pfpPickPO.setNftTxn("transferNFTTxn");
				pfpHistoryPO.setTxnHash("transferNFTTxn");

				pfpHistoryMapper.updateById(pfpHistoryPO);

//						String txn0 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress0(),refund).getData();
				wallectHistory0.setTxnHash("txn0");
				wallectHistory0.initTime();
				wallectHistoryMapper.insert(wallectHistory0);

				refundWallectHistory0.setTxnHash("txn0");
				refundWallectHistory0.initTime();
				wallectHistoryMapper.insert(refundWallectHistory0);

				pfpPickPO.setIndexRewardTxn0("txn0");
				pfpPickMapper.updateById(pfpPickPO);

//						String txn1 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress1(),refund).getData();
				wallectHistory1.setTxnHash("txn1");
				wallectHistory1.initTime();
				wallectHistoryMapper.insert(wallectHistory1);

				refundWallectHistory1.setTxnHash("txn1");
				refundWallectHistory1.initTime();
				wallectHistoryMapper.insert(refundWallectHistory1);

				pfpPickPO.setIndexRewardTxn1("txn1");
				pfpPickMapper.updateById(pfpPickPO);

//						String txn3 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress3(),refund).getData();
				wallectHistory3.setTxnHash("txn3");
				wallectHistory3.initTime();
				wallectHistoryMapper.insert(wallectHistory3);

				refundWallectHistory3.setTxnHash("txn3");
				refundWallectHistory3.initTime();
				wallectHistoryMapper.insert(refundWallectHistory3);

				pfpPickPO.setIndexRewardTxn3("txn3");
				pfpPickMapper.updateById(pfpPickPO);

				//wBNB转账：铸造者收益
//						String minterRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpTokenPO.getMintUserAddress(),minterReward).getData();
				//铸造者收益流水号
				pfpTransactionPO.setMinterMoneyTxnHash("minterRewardTxn");
				pfpPickPO.setMinterRewardTxn("minterRewardTxn");

				//铸造者收益记录
				WallectHistoryPO minterWallectHistory = new WallectHistoryPO(pfpTokenPO.getMintUserId(), 6, pfpTransactionPO.getId(), "minterRewardTxn", shareReward);
				minterWallectHistory.initForInsertNoAuth();
				wallectHistoryMapper.insert(minterWallectHistory);

				//wBNB转账：卖出者收益
//						String sellerRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), ownerAddress,sellerReward).getData();
				//卖出者收益流水号
				pfpTransactionPO.setSellerMoneyTxnHash("sellerRewardTxn");
				pfpPickPO.setSellerRewardTxn("sellerRewardTxn");

				pfpTransactionMapper.updateById(pfpTransactionPO);
				pfpPickMapper.updateById(pfpPickPO);

				//卖家收益记录
				WallectHistoryPO sellerWallectHistory = new WallectHistoryPO(ownerUserId, 5, pfpTransactionPO.getId(), "sellerRewardTxn", sellerReward);
				sellerWallectHistory.initForInsertNoAuth();
				wallectHistoryMapper.insert(sellerWallectHistory);

				//collect粉丝
				FansPO fansPO = new FansPO(1, pfpPickPO.getIndexUserId2(), pfpTokenPO.getMintUserId(), tokenId, pickId);
				fansPO.initForInsertNoAuth();
				fansMapper.insert(fansPO);
//				}
//			});
			} else if (buyer_index_int == 3) {

				//计算中奖积分
				BigDecimal vSoulPrice0 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId0(), pfpTokenPO);
				vSoulHistoryPO0.setVSoulPrice(vSoulPrice0);
				vSoulHistoryPO0.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO0);
				if (historyType0) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO0_2.setVSoulPrice(vSoulPrice0);
					vSoulHistoryPO0_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO0_2);
					updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId0(), vSoulPrice0);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId0(), vSoulPrice0.multiply(BigDecimal.valueOf(0.1)));

				message0.initForInsertNoAuth();
				messageMapper.insert(message0);

				//计算中奖积分
				BigDecimal vSoulPrice1 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId1(), pfpTokenPO);

				vSoulHistoryPO1.setVSoulPrice(vSoulPrice1);
				vSoulHistoryPO1.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO1);
				if (historyType1) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO1_2.setVSoulPrice(vSoulPrice1);
					vSoulHistoryPO1_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO1_2);
					updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId1(), vSoulPrice1);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId1(), vSoulPrice1.multiply(BigDecimal.valueOf(0.1)));

				message1.initForInsertNoAuth();
				messageMapper.insert(message1);

				//计算中奖积分
				BigDecimal vSoulPrice2 = getVSoulPriceNew(0, pfpPickPO.getIndexUserId2(), pfpTokenPO);
				vSoulHistoryPO2.setVSoulPrice(vSoulPrice2);
				vSoulHistoryPO2.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO2);
				if (historyType2) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO2_2.setVSoulPrice(vSoulPrice2);
					vSoulHistoryPO2_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO2_2);
					updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId2(), vSoulPrice2);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId2(), vSoulPrice2.multiply(BigDecimal.valueOf(0.1)));

				message2.initForInsertNoAuth();
				messageMapper.insert(message2);

				//计算中奖积分
				BigDecimal vSoulPrice3 = getVSoulPriceNew(1, pfpPickPO.getIndexUserId3(), pfpTokenPO);
				vSoulHistoryPO3.setVSoulPrice(vSoulPrice3);
				vSoulHistoryPO3.initForInsertNoAuth();
				vSoulHistoryMapper.insertNoZero(vSoulHistoryPO3);
				if (historyType3) {
					// vSoulPrice0 为双倍积分
					vSoulHistoryPO3_2.setVSoulPrice(vSoulPrice3);
					vSoulHistoryPO3_2.initForInsertNoAuth();
					vSoulHistoryMapper.insertNoZero(vSoulHistoryPO3_2);
					updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);
				}
				//更新积分余额
				updateUserVSoul(pfpPickPO.getIndexUserId3(), vSoulPrice3);
				//pump，邀请人 10%的vSOUL返佣
				userVSoulService.setxInviteUserVSoulPriceByUserId(pfpPickPO.getIndexUserId3(), vSoulPrice3.multiply(BigDecimal.valueOf(0.1)));

				message3.setMessage(RewardMessageEnum.REWARD_SUCCESS.getName());
				message3.initForInsertNoAuth();
				messageMapper.insert(message3);

//			ThreadUtil.execAsync(new Runnable() {
//				@Override
//				public void run() {
				//给中签者转NFT
//						R<String> transferNFTResult = approveTransferNFT(ownerAddress,pfpPickPO.getIndexAddress3(),tokenId);
//						if(transferNFTResult.getCode() != 200){
//							log.error("=========transferNFTerror===========");
//						}
				//NFT交易hash
//						String transferNFTTxn = transferNFTResult.getData();
				pfpTransactionPO.setPfpTxnHash("transferNFTTxn");
				pfpPickPO.setNftTxn("transferNFTTxn");
				pfpHistoryPO.setTxnHash("transferNFTTxn");

				pfpHistoryMapper.updateById(pfpHistoryPO);

//						String txn0 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress0(),refund).getData();
				wallectHistory0.setTxnHash("txn0");
				wallectHistory0.initTime();
				wallectHistoryMapper.insert(wallectHistory0);

				refundWallectHistory0.setTxnHash("txn0");
				refundWallectHistory0.initTime();
				wallectHistoryMapper.insert(refundWallectHistory0);

				pfpPickPO.setIndexRewardTxn0("txn0");
				pfpPickMapper.updateById(pfpPickPO);

//						String txn1 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress1(),refund).getData();
				wallectHistory1.setTxnHash("txn1");
				wallectHistory1.initTime();
				wallectHistoryMapper.insert(wallectHistory1);

				refundWallectHistory1.setTxnHash("txn1");
				refundWallectHistory1.initTime();
				wallectHistoryMapper.insert(refundWallectHistory1);

				pfpPickPO.setIndexRewardTxn1("txn1");
				pfpPickMapper.updateById(pfpPickPO);

//						String txn2 = wEthTransferFrom(pfpTokenPO.getAdminAddress(),pfpPickPO.getIndexAddress2(),refund).getData();
				wallectHistory2.setTxnHash("txn2");
				wallectHistory2.initTime();
				wallectHistoryMapper.insert(wallectHistory2);

				refundWallectHistory2.setTxnHash("txn2");
				refundWallectHistory2.initTime();
				wallectHistoryMapper.insert(refundWallectHistory2);

				pfpPickPO.setIndexRewardTxn2("txn2");
				pfpPickMapper.updateById(pfpPickPO);

				//wBNB转账：铸造者收益
//						String minterRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), pfpTokenPO.getMintUserAddress(),minterReward).getData();
				//铸造者收益流水号
				pfpTransactionPO.setMinterMoneyTxnHash("minterRewardTxn");
				pfpPickPO.setMinterRewardTxn("minterRewardTxn");

				//铸造者收益记录
				WallectHistoryPO minterWallectHistory = new WallectHistoryPO(pfpTokenPO.getMintUserId(), 6, pfpTransactionPO.getId(), "minterRewardTxn", shareReward);
				minterWallectHistory.initForInsertNoAuth();
				wallectHistoryMapper.insert(minterWallectHistory);

				//wBNB转账：卖出者收益
//						String sellerRewardTxn = wEthTransferFrom(pfpTokenPO.getAdminAddress(), ownerAddress,sellerReward).getData();
				//卖出者收益流水号
				pfpTransactionPO.setSellerMoneyTxnHash("sellerRewardTxn");
				pfpPickPO.setSellerRewardTxn("sellerRewardTxn");

				pfpTransactionMapper.updateById(pfpTransactionPO);
				pfpPickMapper.updateById(pfpPickPO);

				//卖家收益记录
				WallectHistoryPO sellerWallectHistory = new WallectHistoryPO(ownerUserId, 5, pfpTransactionPO.getId(), "sellerRewardTxn", sellerReward);
				sellerWallectHistory.initForInsertNoAuth();
				wallectHistoryMapper.insert(sellerWallectHistory);

				//collect粉丝
				FansPO fansPO = new FansPO(1, pfpPickPO.getIndexUserId3(), pfpTokenPO.getMintUserId(), tokenId, pickId);
				fansPO.initForInsertNoAuth();
				fansMapper.insert(fansPO);
//				}
//			});
			}

			pfpPickPO.setSellerUserId(pfpTokenPO.getOwnerUserId());
			pfpPickPO.setSellerUserAddress(ownerAddress);

			//交易记录：交易完成
			pfpTransactionPO.setTransactionStatus(2);
			pfpTransactionMapper.updateById(pfpTransactionPO);

			//pfpPick修改状态:已开奖
			pfpPickPO.setStatus(2);
			pfpPickMapper.updateById(pfpPickPO);

			//修改持有人
			pfpTokenPO.setOwnerAddress(toAddress);
			pfpTokenPO.setOwnerUserId(toUserId);
			pfpTokenPO.setPrice(null);
			pfpTokenPO.setPriceTime(null);

			pfpTokenPO.initForUpdateNoAuth();
			//设置最新成交价
			pfpTokenPO.setLastSale(pfpTransactionPO.getListPrice());
			pfpTokenPO.setLastSaleTime(new Date());

			//已成交一次，修改价格
			pfpTokenPO.setTransactionsCount(pfpTokenPO.getTransactionsCount() + 1);
			//涨价率
			BigDecimal groWithrate = new BigDecimal(contractProperties.getGroWithrate()).divide(BigDecimal.valueOf(1000));
			System.out.println(groWithrate);
			pfpTokenPO.setPrice(PickUtil.getSalePriceByGroWithrate(price, groWithrate));
			//未上架
			pfpTokenPO.setPickStatus(0);
			pfpTokenPO.setPickId(null);

			// 交易完成，修改rank
			pfpTokenPO.dealItemRank();
			pfpTokenMapper.updateById(pfpTokenPO);

			//pick粉丝
			FansPO fans0 = new FansPO(0, pfpPickPO.getIndexUserId0(), pfpTokenPO.getMintUserId(), tokenId, pickId);
			FansPO fans1 = new FansPO(0, pfpPickPO.getIndexUserId1(), pfpTokenPO.getMintUserId(), tokenId, pickId);
			FansPO fans2 = new FansPO(0, pfpPickPO.getIndexUserId2(), pfpTokenPO.getMintUserId(), tokenId, pickId);
			FansPO fans3 = new FansPO(0, pfpPickPO.getIndexUserId3(), pfpTokenPO.getMintUserId(), tokenId, pickId);

			fans0.initForInsertNoAuth();
			fans1.initForInsertNoAuth();
			fans2.initForInsertNoAuth();
			fans3.initForInsertNoAuth();

			fansMapper.insert(fans0);
			fansMapper.insert(fans1);
			fansMapper.insert(fans2);
			fansMapper.insert(fans3);

			//铸造者 = 卖出者
			if (ownerUserId.equals(pfpTokenPO.getMintUserId())) {
				//只发送一条消息
				MessagePO messagePO = new MessagePO(0, 1L, ownerUserId, RewardMessageEnum.SELLER_IS_MINTER.getName(), title, content, 0, tokenId, pickId);
				messagePO.initForInsertNoAuth();
				messageMapper.insert(messagePO);
			} else {
				//卖家消息
				MessagePO sellMessage = new MessagePO(0, 1L, ownerUserId, RewardMessageEnum.SELL_SUCCESS.getName(), title, content, 0, tokenId, pickId);
				sellMessage.initForInsertNoAuth();
				messageMapper.insert(sellMessage);

				//铸造者消息
				MessagePO minterMessage = new MessagePO(0, 1L, pfpTokenPO.getMintUserId(), RewardMessageEnum.MINTER_REWARD.getName(), title, content, 0, tokenId, pickId);
				minterMessage.initForInsertNoAuth();
				messageMapper.insert(minterMessage);
			}
			log.info("reward success");
			return R.success("reward success");
		} catch (Exception e) {
			log.error("reward error", e);
			return R.fail("reward error");
		}
	}

	/**
	 * 积分计算
	 *
	 * @param rewardType 0-未中奖 1-中奖
	 * @param memberId   用户id
	 * @param x          NFT信息
	 * @return
	 */
	public BigDecimal getVSoulPrice(Integer rewardType, Long memberId, PFPTokenPO x) {
		//NFT等级
		Integer level = x.getLevel();

		//基础津贴
		BigDecimal base = null;
		if (level == 1) {
			base = new BigDecimal("100");
		} else if (level == 2) {
			base = new BigDecimal("500");
		} else if (level == 3) {
			base = new BigDecimal("2500");
		} else if (level == 4) {
			base = new BigDecimal("5000");
		} else if (level == 5) {
			base = new BigDecimal("10000");
		}

		//如果中奖：基础津贴 * 4
		if (rewardType == 1) {
			base = base.multiply(new BigDecimal("4"));
		}

//		MemberPO memberPO = memberMapper.selectById(memberId);

		//计算匹配度
//		int match = ScoreUtil.getMatch(memberPO.getUserTags(),
//			memberPO.getCharisma(), memberPO.getExtroversion(), memberPO.getEnergy(),
//			memberPO.getWisdom(), memberPO.getArt(), memberPO.getCourage(),
//			x.getMintUserTags(),
//			x.getCharisma(), x.getExtroversion(), x.getEnergy(),
//			x.getWisdom(), x.getArt(), x.getCourage());

		// 获取当前持有NFT
		List<PFPTokenPO> pfpTokenPOS = pfpTokenMapper.selectList(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			// 排除当前NFT
			.ne(PFPTokenPO::getId, x.getId())
			.eq(PFPTokenPO::getOwnerUserId, memberId)
			// pickStatus == 0
//			.eq(PFPTokenPO::getPickStatus, 0)
			.eq(PFPTokenPO::getMintStatus, 1));
		// 查询memberId持有的nft数量，若为0，则积分为0
		if (pfpTokenPOS.isEmpty()) {
			return BigDecimal.ZERO;
		}

		//NFT持有加成
		BigDecimal addition = BigDecimal.ZERO;
		for (PFPTokenPO pfpTokenPO : pfpTokenPOS) {
			Integer pfpLevel = pfpTokenPO.getLevel();
			if (pfpLevel == 1) {
				addition = addition.add(new BigDecimal("0.05"));
			} else if (pfpLevel == 2) {
				addition = addition.add(new BigDecimal("0.1"));
			} else if (pfpLevel == 3) {
				addition = addition.add(new BigDecimal("0.15"));
			} else if (pfpLevel == 4) {
				addition = addition.add(new BigDecimal("0.20"));
			} else if (pfpLevel == 5) {
				addition = addition.add(new BigDecimal("0.25"));
			}
		}

		//加成 = 1 + 加成
		addition = BigDecimal.ONE.add(addition);

		//匹配度 = 匹配度 / 100
//		BigDecimal matchPercent = new BigDecimal(match).divide(new BigDecimal(100),2,BigDecimal.ROUND_HALF_UP);

		//计算积分
		BigDecimal vSoul = base.multiply(addition).setScale(0, BigDecimal.ROUND_UP);

		return vSoul;
	}


	/**
	 * 积分计算
	 *
	 * @param rewardType 0-未中奖 1-中奖
	 * @param memberId   用户id
	 * @param x          NFT信息
	 * @return
	 */
	public BigDecimal getVSoulPriceNew(Integer rewardType, Long memberId, PFPTokenPO x) {
		//NFT等级
		Integer level = x.getLevel();

		//基础津贴(nft没中的情况下基础积分)
		BigDecimal base = null;
		if (level == 1) {
			base = new BigDecimal("10");
		} else if (level == 2) {
			base = new BigDecimal("50");
		} else if (level == 3) {
			base = new BigDecimal("250");
		} else if (level == 4) {
			base = new BigDecimal("500");
		} else if (level == 5) {
			base = new BigDecimal("1000");
		}

		//如果中奖：基础津贴 * 4
		if (rewardType == 1) {
			base = base.multiply(new BigDecimal("4"));
		}

		// 获取当前持有NFT
		List<PFPTokenPO> pfpTokenPOS = pfpTokenMapper.selectList(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			// 排除当前NFT
			.ne(PFPTokenPO::getId, x.getId())
			.eq(PFPTokenPO::getOwnerUserId, memberId)
			.eq(PFPTokenPO::getMintStatus, 1));
		// 如果没有nft 默认 1 * 基础津贴
		if (pfpTokenPOS.isEmpty()) {
			return base.multiply(new BigDecimal("1")).setScale(0, BigDecimal.ROUND_UP);
		}

		//NFT持有加成
		BigDecimal booster = BigDecimal.ZERO;

		for (PFPTokenPO pfpTokenPO : pfpTokenPOS) {
			Integer pfpLevel = pfpTokenPO.getLevel();
			if (pfpLevel == 1) {
				booster = booster.add(new BigDecimal("0.5"));
			} else if (pfpLevel == 2) {
				booster = booster.add(new BigDecimal("1"));
			} else if (pfpLevel == 3) {
				booster = booster.add(new BigDecimal("1.5"));
			} else if (pfpLevel == 4) {
				booster = booster.add(new BigDecimal("2"));
			} else if (pfpLevel == 5) {
				booster = booster.add(new BigDecimal("2.5"));
			}
		}
		//加成 = 最高等级nft boost+加成
		List<Integer> nftLevels = pfpTokenPOS.stream().map(s -> s.getLevel()).collect(Collectors.toList());
		Integer max = Collections.max(nftLevels);
		switch (max) {
			case 1:
				booster = booster.add(new BigDecimal("10"));
				booster = booster.subtract(new BigDecimal("0.5"));
				break;
			case 2:
				booster = booster.add(new BigDecimal("11"));
				booster = booster.subtract(new BigDecimal("1"));
				break;
			case 3:
				booster = booster.add(new BigDecimal("12"));
				booster = booster.subtract(new BigDecimal("1.5"));
				break;
			case 4:
				booster = booster.add(new BigDecimal("13"));
				booster = booster.subtract(new BigDecimal("2"));
				break;
			case 5:
				booster = booster.add(new BigDecimal("15"));
				booster = booster.subtract(new BigDecimal("2.5"));
				break;
			default:
				break;
		}


		//计算积分
		BigDecimal vSoul = base.multiply(booster).setScale(0, BigDecimal.ROUND_UP);

		return vSoul;
	}


	private void updateUserVSoul(Long userId, BigDecimal vSoulPrice) {


		UserVSoulPO userVSoulPO = userVSoulMapper.selectOne(new LambdaQueryWrapper<UserVSoulPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(UserVSoulPO::getUserId, userId));

		if (userVSoulPO == null) {
			userVSoulPO = new UserVSoulPO();
			userVSoulPO.setUserId(userId);
			userVSoulPO.setVSoulPrice(BigDecimal.ZERO);
			userVSoulPO.initForInsertNoAuth();

			userVSoulMapper.insert(userVSoulPO);
		}

		//更新余额
		userVSoulPO.setVSoulPrice(userVSoulPO.getVSoulPrice().add(vSoulPrice));

		userVSoulMapper.updateById(userVSoulPO);
	}


	private Credentials loadCredentials() throws Exception {
		String walletFile = coinKeystorePath + "/" + coinWalletFile;
		Credentials credentials = WalletUtils.loadCredentials(coinCreatePwd, walletFile);
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
	private ArrayList<BigInteger> getRate(String tokenId) {
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
