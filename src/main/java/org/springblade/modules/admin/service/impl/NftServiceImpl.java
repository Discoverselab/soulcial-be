package org.springblade.modules.admin.service.impl;

//import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.api.ResultCode;
import org.springblade.modules.admin.config.Web3jConfig;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.pojo.query.CollectCreateOrderQuery;
import org.springblade.modules.admin.pojo.query.CollectNFTQuery;
import org.springblade.modules.admin.pojo.vo.CheckPickNftVo;
import org.springblade.modules.admin.pojo.vo.MintNftVo;
import org.springblade.modules.admin.pojo.vo.PickNftVo;
import org.springblade.modules.admin.pojo.vo.SoulVo;
import org.springblade.modules.admin.service.ETHService;
import org.springblade.modules.admin.service.NftService;
import org.springblade.modules.admin.service.PfpContractService;
import org.springblade.modules.admin.util.AddressUtil;
import org.springblade.modules.admin.util.PickUtil;
import org.springblade.modules.admin.util.ScoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class NftServiceImpl implements NftService {

	@Autowired
	PfpContractService pfpContractService;

	@Autowired
	MemberMapper memberMapper;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

//	@Autowired
//	BNBService bnbService;

	@Autowired
	ETHService ethService;

	@Autowired
	PFPTransactionMapper pfpTransactionMapper;

	@Autowired
	PFPHistoryMapper pfpHistoryMapper;

	@Autowired
	PFPPickMapper pfpPickMapper;

	@Autowired
	BladeRedis bladeRedis;

	@Autowired
	private WallectHistoryMapper wallectHistoryMapper;

	@Value("${contract.linkRate}")
	private BigDecimal linkRate;

	//开奖区块间隔数
	@Value("${contract.rewardBlockCount}")
	private Long rewardBlockCount;

	//出块间隔秒数
	@Value("${contract.blockInterval}")
	private Long blockInterval;

	// admin的地址
	@Value("${contract.newadminAddress}")
	private String adminAddress;

	// 合约地址
	@Value("${contract.address}")
	private String contractAddress;

	// 合约对应rpc地址
	@Value("${contract.rpcUrl}")
	private String rpcUrl;

	// 对应链ID
	@Value("${contract.chainId}")
	private Long chainId;

	// 合约名称
	@Value("${contract.contractName}")
	private String contractName;

	// 链名称
	@Value("${contract.chainName}")
	private String chainName;

	// linkType
	@Value("${contract.linkType}")
	private Integer linkType;

	@Autowired
	WhiteListMapper whiteListMapper;

	@Override
	public synchronized R mintFreeNft(MintNftVo mintNftVo) {
		Long userId = StpUtil.getLoginIdAsLong();
		MemberPO memberPO = memberMapper.selectById(userId);
		if(memberPO == null){
			return R.fail("user not exist!");
		}
		//TODO 暂时开启永久免费铸造 freeMint
//		if(memberPO.getFreeMint() == 1){
//			return R.fail("you have used free mint chance!");
//		}

		// 当前用户pfpToken数量
		Long pfpTokenCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
				.eq(PFPTokenPO::getMintUserId, userId)
			.eq(BasePO::getIsDeleted, 0));
//		Integer mintMaxCount = Web3jConfig.MINT_MAX_COUNT;
		// TODO 用户的whiteList为1时候，无限mint
		Integer whiteList = memberPO.getWhiteList();
//		if (pfpTokenCount >= mintMaxCount && whiteList != 1) {
//			// 数量超过，不可mint
//			return R.fail(433, "over limit");
//		}
		// 使用freeMint字段判断是否已经使用过一次免费mint
//		Long mintCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
//			.eq(BasePO::getIsDeleted, 0)
//			.eq(PFPTokenPO::getMintUserId, userId));
//		if(memberPO.getFreeMint() == 1 && whiteList != 1){
//			return R.fail("mint failed: you have mint 1 NFTs。");
//		}

		if (whiteList != 1) {
			WhiteListPO whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
				.eq(WhiteListPO::getAddress, memberPO.getAddress()));
			if (whiteListPO == null && pfpTokenCount >= 1) {
				return R.fail("mint failed: you have mint 1 NFTs");
			}
			if (whiteListPO != null) {
				int canMintTwice = whiteListPO.getCanMintTwice();
				if (canMintTwice == 1 && pfpTokenCount >= 2) {
					return R.fail("mint failed: you have mint 2 NFTs");
				}
			}
		}

		String toAddress = memberPO.getAddress();


		// 获取合约 --- 废弃  使用配置文件
//		PFPContractPO contract = pfpContractService.getContract();
//		if(contract == null){
//			return R.fail("contract error");
//		}
		// 新建token
		PFPTokenPO pfpTokenPO = new PFPTokenPO();
//		// 新增占位
		pfpTokenPO.setAdminAddress(adminAddress);
		pfpTokenMapper.insert(pfpTokenPO);

		//校验钱包地址合法性
		if(!AddressUtil.isETHAddress(toAddress)){
			// 删除token
			pfpTokenPO.initForUpdate();
			return R.fail("address is not illegal");
		}

		pfpTokenPO.setLinkType(linkType);
		pfpTokenPO.setNetwork(chainName);
		pfpTokenPO.setContractAddress(contractAddress);
		pfpTokenPO.setContractName(contractName);
		pfpTokenPO.setOwnerAddress(toAddress);
		pfpTokenPO.setOwnerUserId(userId);
		//铸造中
		pfpTokenPO.setMintStatus(2);
		pfpTokenPO.setMintUserAddress(toAddress);
		pfpTokenPO.setMintUserId(userId);
		pfpTokenPO.setPictureUrl(mintNftVo.getPictureUrl());
		pfpTokenPO.setSquarePictureUrl(mintNftVo.getSquarePictureUrl());
		pfpTokenPO.setColorAttribute(mintNftVo.getColorAttribute());
		pfpTokenPO.setMood(mintNftVo.getMood());
		pfpTokenPO.setColor(mintNftVo.getColor());
		pfpTokenPO.setWeather(mintNftVo.getWeather());
		pfpTokenPO.setPersonality(mintNftVo.getPersonality());
		pfpTokenPO.setLikes(0);
		pfpTokenPO.initForInsert();

		//算分
		pfpTokenPO.setCharisma(memberPO.getCharisma());
		pfpTokenPO.setExtroversion(memberPO.getExtroversion());
		pfpTokenPO.setEnergy(memberPO.getEnergy());
		pfpTokenPO.setWisdom(memberPO.getWisdom());
		pfpTokenPO.setArt(memberPO.getArt());
		pfpTokenPO.setCourage(memberPO.getCourage());
		pfpTokenPO.setMintUserTags(memberPO.getUserTags());

		//计算soul(Personality+Character)
		if (ObjectUtil.isNotEmpty(pfpTokenPO.getCharisma())) {
			SoulVo soulVo = ScoreUtil.getPersonalityCharacter(pfpTokenPO.getCharisma(), pfpTokenPO.getExtroversion(), pfpTokenPO.getEnergy(), pfpTokenPO.getWisdom(), pfpTokenPO.getArt(), pfpTokenPO.getCourage());
			pfpTokenPO.setSoul(soulVo.getPersonality()+" "+soulVo.getCharacter());
		}
		//计算总分，设置用户的levelScore
		pfpTokenPO.setLevelScore(memberPO.getLevelScore());
//		pfpTokenPO.countLevelScore();

		//设置level，存入用户的level
		pfpTokenPO.setLevel(memberPO.getLevel());
//		pfpTokenPO.countLevel();

		PickUtil instance = PickUtil.getInstance(pfpTokenPO.getLevel(),linkRate);
		//设置底价
		pfpTokenPO.setBasePrice(instance.getBasePrice());
		//设置当前价格
		pfpTokenPO.setPrice(instance.getBasePrice());
		//设置交易次数
		pfpTokenPO.setTransactionsCount(0);
		//设置pickStatus:不可pick
		pfpTokenPO.setPickStatus(0);

//		pfpTokenMapper.insert(pfpTokenPO);
		pfpTokenMapper.updateById(pfpTokenPO);

		//用户免费mint变更为已使用
		memberPO.setFreeMint(1);
		memberPO.initForUpdate();

		memberMapper.updateById(memberPO);

		// 获取当前所有pfpToken数量，+1后放入realTokenId中，上方已经新增占位，所以这里不需要+1
		Long realTokenId = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)) ;
		pfpTokenPO.setRealTokenId(realTokenId);
		R<String> result = ethService.mintNFT(
			adminAddress,
			contractAddress,
			toAddress,
			chainId,
            realTokenId);
//		R<String> result = ethService.mintNFT(
//			contract.getAdminAddress(),
//			contract.getContractAddress(),
//			contract.getAdminJsonFile(),
//			toAddress,
//			contract.getChainId(),
////		pfpTokenPO.getId());
//			realTokenId);
		log.info("mintResult {}", result.toString());
		Long pfpTokenPOId = pfpTokenPO.getId();
		log.info("pfpTokenPOId {}", pfpTokenPOId);

		if(result.getCode() == ResultCode.SUCCESS.getCode()){
			String txnHash = result.getData();

			//已铸造
			pfpTokenPO.setMintStatus(1);
			pfpTokenPO.setMintTime(new Date());
			pfpTokenPO.setMintTxnHash(txnHash);
			pfpTokenPO.initForUpdate();

			pfpTokenMapper.updateById(pfpTokenPO);

			// 添加history
			PFPHistoryPO pfpHistoryPO = new PFPHistoryPO();
			pfpHistoryPO.setTokenId(pfpTokenPO.getRealTokenId());
			//铸造
			pfpHistoryPO.setType(0);
//			pfpHistoryPO.setTransactionId();
			pfpHistoryPO.setAdminAddress(pfpTokenPO.getAdminAddress());
			pfpHistoryPO.setLinkType(pfpTokenPO.getLinkType());
			pfpHistoryPO.setNetwork(pfpTokenPO.getNetwork());
			pfpHistoryPO.setContractAddress(pfpTokenPO.getContractAddress());
			pfpHistoryPO.setContractName(pfpTokenPO.getContractName());
			pfpHistoryPO.setToAddress(pfpTokenPO.getMintUserAddress());
//			pfpHistoryPO.setToAddress();
			pfpHistoryPO.setToUserId(pfpTokenPO.getMintUserId());
//			pfpHistoryPO.setToUserId();
			pfpHistoryPO.setTxnHash(txnHash);
//			pfpHistoryPO.setPrice();

			pfpHistoryPO.initForInsert();

			pfpHistoryMapper.insert(pfpHistoryPO);

			return R.data(pfpTokenPO.getRealTokenId(),"mint success");

		}else {
			// 铸造失败，退还免费次数
			// 用户免费mint变更为未使用
			memberPO.setFreeMint(0);
			memberPO.initForUpdate();

			memberMapper.updateById(memberPO);

			// 删除token
//			pfpTokenPO.initForUpdate();
//			pfpTokenPO.setIsDeleted(1);
			// 根据pfpTokenPOId查询新的pfpTokenPO
			log.info("mint出错，打印相关信息，pfpTokenPO {}", pfpTokenPO);
//			pfpTokenPO = pfpTokenMapper.selectById(pfpTokenPOId);
//			pfpTokenPO.initForUpdate();
//			pfpTokenPO.setIsDeleted(1);
			log.info("获取新的，pfpTokenPO {}", pfpTokenPO);

			pfpTokenMapper.deleteById(pfpTokenPO);

			return result;
		}
	}

	@Override
	public synchronized R frontMintFreeNft(MintNftVo mintNftVo) {
		Long userId = StpUtil.getLoginIdAsLong();
		MemberPO memberPO = memberMapper.selectById(userId);
		if(memberPO == null){
			return R.fail("user not exist!");
		}

		if(memberPO.getFreeMint() == 1){
			return R.fail("you have used free mint chance!");
		}

		String toAddress = memberPO.getAddress();

		//校验钱包地址合法性
		if(!AddressUtil.isETHAddress(toAddress)){
			return R.fail("address is not illegal");
		}

		//获取合约
		PFPContractPO contract = pfpContractService.getContract();

		if(contract == null){
			return R.fail("contract error");
		}
		//新建token
		PFPTokenPO pfpTokenPO = new PFPTokenPO();

		pfpTokenPO.setAdminAddress(contract.getAdminAddress());
		pfpTokenPO.setLinkType(contract.getLinkType());
		pfpTokenPO.setNetwork(contract.getNetwork());
		pfpTokenPO.setContractAddress(contract.getContractAddress());
		pfpTokenPO.setContractName(contract.getContractName());
		pfpTokenPO.setOwnerAddress(toAddress);
		pfpTokenPO.setOwnerUserId(userId);
		//铸造中
		pfpTokenPO.setMintStatus(2);
		pfpTokenPO.setMintUserAddress(toAddress);
		pfpTokenPO.setMintUserId(userId);
		pfpTokenPO.setPictureUrl(mintNftVo.getPictureUrl());
		pfpTokenPO.setSquarePictureUrl(mintNftVo.getSquarePictureUrl());
		pfpTokenPO.setColorAttribute(mintNftVo.getColorAttribute());
		pfpTokenPO.setMood(mintNftVo.getMood());
		pfpTokenPO.setColor(mintNftVo.getColor());
		pfpTokenPO.setWeather(mintNftVo.getWeather());
		pfpTokenPO.setPersonality(mintNftVo.getPersonality());
		pfpTokenPO.setLikes(0);
		pfpTokenPO.initForInsert();

		//算分
		pfpTokenPO.setCharisma(memberPO.getCharisma());
		pfpTokenPO.setExtroversion(memberPO.getExtroversion());
		pfpTokenPO.setEnergy(memberPO.getEnergy());
		pfpTokenPO.setWisdom(memberPO.getWisdom());
		pfpTokenPO.setArt(memberPO.getArt());
		pfpTokenPO.setCourage(memberPO.getCourage());
		pfpTokenPO.setMintUserTags(memberPO.getUserTags());

		//计算soul(Personality+Character)
		if (ObjectUtil.isNotEmpty(pfpTokenPO.getCharisma())) {
			SoulVo soulVo = ScoreUtil.getPersonalityCharacter(pfpTokenPO.getCharisma(), pfpTokenPO.getExtroversion(), pfpTokenPO.getEnergy(), pfpTokenPO.getWisdom(), pfpTokenPO.getArt(), pfpTokenPO.getCourage());
			pfpTokenPO.setSoul(soulVo.getPersonality()+" "+soulVo.getCharacter());
		}

		//计算总分
		pfpTokenPO.countLevelScore();

		//设置level
		pfpTokenPO.countLevel();

		pfpTokenMapper.insert(pfpTokenPO);

		//用户免费mint变更为已使用
//		memberPO.setFreeMint(1);
//		memberPO.initForUpdate();

//		memberMapper.updateById(memberPO);

		return R.data(pfpTokenPO.getId());
	}

//	public static void main(String[] args) throws Exception{
//		OkHttpClient.Builder builder = new OkHttpClient.Builder();
//		builder.connectTimeout(30 * 1000, TimeUnit.MILLISECONDS);
//		builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
//		builder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS);
//		OkHttpClient httpClient = builder.build();
//		Web3j web3j = Web3j.build(new HttpService(ETH_SEPOLIA_RPC, httpClient, false));
//
//		EthBlock block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(3872972L), true).send();
//		String hash = block.getBlock().getHash();
//		System.out.println("hash:"+hash);
//	}

	@Override
	public R prePickNFT(CheckPickNftVo checkPickNftVo){
		Long userId = StpUtil.getLoginIdAsLong();
		Long tokenId = checkPickNftVo.getTokenId();
		Integer pickIndex = checkPickNftVo.getPickIndex();

//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		if(pfpTokenPO.getPickStatus() != 1){
			return R.fail(666,"This PFP is not allow pick");
		}

		Long pickId = pfpTokenPO.getPickId();
		PFPPickPO pfpPickPO = pfpPickMapper.selectById(pickId);

		Long pickIndexUserId = null;
		if(pickIndex == 0){
			pickIndexUserId = pfpPickPO.getIndexUserId0();
		} else if (pickIndex == 1) {
			pickIndexUserId = pfpPickPO.getIndexUserId1();
		} else if (pickIndex == 2) {
			pickIndexUserId = pfpPickPO.getIndexUserId2();
		} else if (pickIndex == 3) {
			pickIndexUserId = pfpPickPO.getIndexUserId3();
		}else {
			return R.fail("pickIndex must be 0/1/2/3");
		}

		//签号已被购买
		if(pickIndexUserId != null){
			return R.fail(666,"Sorry，pick failed");
		}

		if(pfpPickPO.getIndexUserId0() != null && pfpPickPO.getIndexUserId0().equals(userId)){
//			return R.fail("You have been picked #0");
			return R.fail("You have already picked the SoulCast.");
		}
		if(pfpPickPO.getIndexUserId1() != null && pfpPickPO.getIndexUserId1().equals(userId)){
//			return R.fail("You have been picked #1");
			return R.fail("You have already picked the SoulCast.");
		}
		if(pfpPickPO.getIndexUserId2() != null && pfpPickPO.getIndexUserId2().equals(userId)){
//			return R.fail("You have been picked #2");
			return R.fail("You have already picked the SoulCast.");
		}
		if(pfpPickPO.getIndexUserId3() != null && pfpPickPO.getIndexUserId3().equals(userId)){
//			return R.fail("You have been picked #3");
			return R.fail("You have already picked the SoulCast.");
		}

		return R.success("check success");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public synchronized R pickNFT(PickNftVo pickNftVo) throws Exception{
		Long userId = StpUtil.getLoginIdAsLong();
		MemberPO memberPO = memberMapper.selectById(userId);

		Long tokenId = pickNftVo.getTokenId();
		Integer pickIndex = pickNftVo.getPickIndex();
		String payTxn = pickNftVo.getTxn();

//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		if(pfpTokenPO.getPickStatus() != 1){
			return R.fail("This PFP is not allow pick");
		}

		Long pickId = pfpTokenPO.getPickId();

		//TODO 校验交易哈希
		//校验转账交易是否被使用
		PFPTransactionPO pfpTransactionPO = pfpTransactionMapper.selectOne(new LambdaQueryWrapper<PFPTransactionPO>()
			.eq(PFPTransactionPO::getBuyerMoneyTxnHash, payTxn));

		if(pfpTransactionPO != null){
			return R.fail("transaction number :" +  payTxn + " has been used");
		}

		//链上校验交易哈希是否成功、金额是否正确
		int reTryCount = 0;
		Boolean checkFlag = false;
		while (reTryCount < 100){
			R<Boolean> checkBNBTransResult = ethService.checkBNBTransacation(payTxn, pfpTokenPO.getPrice(),memberPO.getAddress(),pfpTokenPO.getAdminAddress());
//			R<Boolean> checkBNBTransResult = ethService.checkWBNBTransacation(payTxn, pfpTokenPO.getPrice(),memberPO.getAddress(),pfpTokenPO.getAdminAddress());
			if(checkBNBTransResult.getCode() == 200){
				//终止循环
				reTryCount = 100;
				checkFlag = checkBNBTransResult.getData();
			}else {
				// 休眠3秒再进行重试
				Thread.sleep(10 * 1000);
				reTryCount++;
			}
		}

		if(!checkFlag){
			//未校验通过
			return R.fail("transacation check failed: transfer failed!");
		}

		//保存付款记录
		WallectHistoryPO wallectHistoryPO = new WallectHistoryPO();
		wallectHistoryPO.setUserId(userId);
		wallectHistoryPO.setType(3);
		wallectHistoryPO.setTxnHash(payTxn);
		wallectHistoryPO.setPrice(pfpTokenPO.getPrice());
		wallectHistoryPO.initForInsert();

		wallectHistoryMapper.insert(wallectHistoryPO);

		pfpTransactionPO = new PFPTransactionPO();
		pfpTransactionPO.setTokenId(pfpTokenPO.getId());
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
		pfpTransactionPO.setBuyerMoneyTxnHash(payTxn);

		//计算费用
		pfpTransactionPO.setListPrice(pfpTokenPO.getPrice());
		Date date = new Date();
		pfpTransactionPO.setCreateTime(date);
		pfpTransactionPO.setUpdateTime(date);
		pfpTransactionPO.setCreateUser(userId);
		pfpTransactionPO.setUpdateUser(userId);
		pfpTransactionPO.setIsDeleted(1);
		pfpTransactionPO.setPickId(pickId);
		pfpTransactionMapper.insert(pfpTransactionPO);

		PFPPickPO pfpPickPO = pfpPickMapper.selectById(pickId);

		Long pickIndexUserId = null;
		if(pickIndex == 0){
			pickIndexUserId = pfpPickPO.getIndexUserId0();
		} else if (pickIndex == 1) {
			pickIndexUserId = pfpPickPO.getIndexUserId1();
		} else if (pickIndex == 2) {
			pickIndexUserId = pfpPickPO.getIndexUserId2();
		} else if (pickIndex == 3) {
			pickIndexUserId = pfpPickPO.getIndexUserId3();
		}else {
			return R.fail("pickIndex must be 0/1/2/3");
		}

		//签号已被购买
		if(pickIndexUserId != null){
			return R.fail("Sorry，pick failed");
		}

		if(pfpPickPO.getIndexUserId0() != null && pfpPickPO.getIndexUserId0().equals(userId)){
//			return R.fail("You have been picked #0");
			return R.fail("You have already picked the SoulCast.");
		}
		if(pfpPickPO.getIndexUserId1() != null && pfpPickPO.getIndexUserId1().equals(userId)){
//			return R.fail("You have been picked #1");
			return R.fail("You have already picked the SoulCast.");
		}
		if(pfpPickPO.getIndexUserId2() != null && pfpPickPO.getIndexUserId2().equals(userId)){
//			return R.fail("You have been picked #2");
			return R.fail("You have already picked the SoulCast.");
		}
		if(pfpPickPO.getIndexUserId3() != null && pfpPickPO.getIndexUserId3().equals(userId)){
//			return R.fail("You have been picked #3");
			return R.fail("You have already picked the SoulCast.");
		}

		//pick逻辑
		if(pickIndex == 0){
			pfpPickPO.setIndexAddress0(memberPO.getAddress());
			pfpPickPO.setIndexUserId0(userId);
			pfpPickPO.setIndexPayTxn0(payTxn);
		} else if (pickIndex == 1) {
			pfpPickPO.setIndexAddress1(memberPO.getAddress());
			pfpPickPO.setIndexUserId1(userId);
			pfpPickPO.setIndexPayTxn1(payTxn);
		} else if (pickIndex == 2) {
			pfpPickPO.setIndexAddress2(memberPO.getAddress());
			pfpPickPO.setIndexUserId2(userId);
			pfpPickPO.setIndexPayTxn2(payTxn);
		} else if (pickIndex == 3) {
			pfpPickPO.setIndexAddress3(memberPO.getAddress());
			pfpPickPO.setIndexUserId3(userId);
			pfpPickPO.setIndexPayTxn3(payTxn);
		}

		//当前pick人数
		pfpPickPO.setNowPickCount(pfpPickPO.getNowPickCount() + 1);

		pfpPickPO.initForUpdate();
		pfpPickMapper.updateById(pfpPickPO);

		//满员
		if(pfpPickPO.getNowPickCount() == 4){

			Long now = System.currentTimeMillis();
			//开奖时间
			Long rewardTime = null;
			//开奖区块
			Long rewardBlockHeight = null;
			//开奖区块间隔数
			Long diff = null;

			//查询当前区块
			Long blockHeight = ethService.getLastMainBlockHeight();

			//获取开奖队列数量
			Long count = bladeRedis.lLen(PickUtil.REWARD_LIST);
			if(count == 0){
				//没有开奖队列
				//设置开奖区块间隔数
				diff = rewardBlockCount;
				//开奖区块 = 当前区块 + 开奖区块间隔数量
				rewardBlockHeight = blockHeight + diff;

				//开奖时间 = 当前时间 + 出块间隔 * 开奖区块间隔数量
				rewardTime = now + blockInterval * diff * 1000;
				//缓存开奖时间
				bladeRedis.set(PickUtil.REWARD_TIME,rewardTime);

			}else {
				//已有开奖队列，获取最后一个开奖区块
				Long height = bladeRedis.get(PickUtil.LAST_REWARD_BLOCK_HEIGHT);
				if(height == null){
					//不存在则设置成最新行高 + 1
					height = ethService.getLastMainBlockHeight() + 1;
				}
				//开奖区块 = 最后一个开奖区块 + 1
				rewardBlockHeight = height + 1;

				//计算开奖区块间隔数 = 开奖区块 - 当前区块
				diff = rewardBlockHeight - blockHeight;

				//开奖时间 = 当前时间 + 出块间隔 * 开奖区块间隔数量
				rewardTime = now + blockInterval * diff * 1000;
			}

			//更新最新的开奖区块
			bladeRedis.set(PickUtil.LAST_REWARD_BLOCK_HEIGHT,rewardBlockHeight);

			//加入最右侧队列中
			bladeRedis.rPush(PickUtil.REWARD_LIST,pfpPickPO.getId());

			//满员时间
			pfpPickPO.setFullPickTime(new Date(now));
			//设置开奖时间
			pfpPickPO.setRewardTime(new Date(rewardTime));
			//设置开奖行高
			pfpPickPO.setRewardBlockHeight(rewardBlockHeight);

			//待开奖
			pfpPickPO.setStatus(1);
			pfpPickPO.initForUpdate();
			pfpPickMapper.updateById(pfpPickPO);

			//待开奖
			pfpTokenPO.setPickStatus(2);
			pfpTokenPO.initForUpdate();
			pfpTokenMapper.updateById(pfpTokenPO);
		}

		return R.success("pick success");
	}

	@Override
	public void batchCalcNftSoul() {
		List<PFPTokenPO> pfpTokenPOS = pfpTokenMapper.selectList(null);
		for (PFPTokenPO pfpTokenPO : pfpTokenPOS) {
			//计算soul(Personality+Character)
			if (ObjectUtil.isNotEmpty(pfpTokenPO.getCharisma())) {
				SoulVo soulVo = ScoreUtil.getPersonalityCharacter(pfpTokenPO.getCharisma(), pfpTokenPO.getExtroversion(), pfpTokenPO.getEnergy(), pfpTokenPO.getWisdom(), pfpTokenPO.getArt(), pfpTokenPO.getCourage());
				pfpTokenPO.setSoul(soulVo.getPersonality()+" "+soulVo.getCharacter());
				pfpTokenMapper.updateById(pfpTokenPO);
			}
		}
	}

	@Override
	public R collectNFT(CollectNFTQuery collectNFTQuery) {
		String txn = collectNFTQuery.getTxn();
		Long tokenId = collectNFTQuery.getTokenId();

		Long userId = StpUtil.getLoginIdAsLong();
		MemberPO memberPO = memberMapper.selectById(userId);
		String toAddress = memberPO.getAddress();

//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		String ownerAddress = pfpTokenPO.getOwnerAddress();
		Long ownerUserId = pfpTokenPO.getOwnerUserId();

		if(pfpTokenPO.getPrice() == null){
			return R.fail("collect is not support");
		}

		//校验转账交易是否被使用
		PFPTransactionPO pfpTransactionPO = pfpTransactionMapper.selectOne(new LambdaQueryWrapper<PFPTransactionPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTransactionPO::getBuyerMoneyTxnHash, txn));

		if(pfpTransactionPO != null){
			//翻译
			return R.fail("交易hash已被使用");
		}

		//创建订单
		pfpTransactionPO = new PFPTransactionPO();
		pfpTransactionPO.setTokenId(pfpTokenPO.getId());
		pfpTransactionPO.setAdminAddress(pfpTokenPO.getAdminAddress());
		pfpTransactionPO.setLinkType(pfpTokenPO.getLinkType());
		pfpTransactionPO.setNetwork(pfpTokenPO.getNetwork());
		pfpTransactionPO.setContractAddress(pfpTokenPO.getContractAddress());
		pfpTransactionPO.setContractName(pfpTokenPO.getContractName());
		pfpTransactionPO.setFromAddress(ownerAddress);
		pfpTransactionPO.setToAddress(toAddress);
		pfpTransactionPO.setFromUserId(ownerUserId);
		pfpTransactionPO.setToUserId(userId);
//		交易状态：0-未交易 1-已付款未交易PFP 2-交易完成 3-交易取消
		pfpTransactionPO.setTransactionStatus(1);
		pfpTransactionPO.setBuyerMoneyTxnHash(txn);
//		pfpTransactionPO.setPfpTxnHash();
		pfpTransactionPO.setMintUserId(pfpTokenPO.getMintUserId());
		pfpTransactionPO.setMintUserAddress(pfpTokenPO.getMintUserAddress());

		//计算费用
		pfpTransactionPO.setListPrice(pfpTokenPO.getPrice());

		pfpTransactionPO.initForInsert();
		pfpTransactionMapper.insert(pfpTransactionPO);

		//TODO 链上校验交易哈希是否成功、金额是否正确

		//TODO NFT是否授权校验

		//TODO NFT转账

		//TODO NFT转账校验

		//TODO BNB手续费计算

		//TODO BNB转账

		//TODO BNB转账校验

		pfpTransactionPO.setTransactionStatus(2);
		pfpTransactionPO.setPfpTxnHash("pfpTxnHash");
		pfpTransactionMapper.updateById(pfpTransactionPO);


		//修改持有人
		pfpTokenPO.setOwnerAddress(toAddress);
		pfpTokenPO.setOwnerUserId(userId);
		pfpTokenPO.setPrice(null);
		pfpTokenPO.setPriceTime(null);

		pfpTokenPO.initForUpdate();

		pfpTokenMapper.updateById(pfpTokenPO);

		return R.success("success");
	}

	@Override
	public R checkApprove(Long tokenId, Long userId) {
		MemberPO memberPO = memberMapper.selectById(userId);
		String address = memberPO.getAddress();
		R result = ethService.checkApprove(tokenId,address);
		return result;
	}

	@Override
	public PFPTransactionPO getLastTransaction(Long tokenId) {

		//降序获取交易记录
		List<PFPTransactionPO> pfpTransactionPOS = pfpTransactionMapper.selectList(new LambdaQueryWrapper<PFPTransactionPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTransactionPO::getTokenId, tokenId)
			//已完成
			.eq(PFPTransactionPO::getTransactionStatus, 2)
			.orderByDesc(BasePO::getUpdateTime).last("limit 1"));

		if(pfpTransactionPOS != null && pfpTransactionPOS.size() > 0){
			return pfpTransactionPOS.get(0);
		}else {
			return new PFPTransactionPO();
		}

	}

	//TODO 事务优化
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R collectNFTOnline(CollectNFTQuery collectNFTQuery) throws Exception{
		String txn = collectNFTQuery.getTxn();
		Long tokenId = collectNFTQuery.getTokenId();
		String payAddress = collectNFTQuery.getPayAddress();

		Long userId = StpUtil.getLoginIdAsLong();
		MemberPO memberPO = memberMapper.selectById(userId);
		String toAddress = memberPO.getAddress();

//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		String ownerAddress = pfpTokenPO.getOwnerAddress();
		String minterAddress = pfpTokenPO.getMintUserAddress();
		Long ownerUserId = pfpTokenPO.getOwnerUserId();

		//校验转账交易是否被使用
		PFPTransactionPO pfpTransactionPO = pfpTransactionMapper.selectOne(new LambdaQueryWrapper<PFPTransactionPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTransactionPO::getBuyerMoneyTxnHash, txn));

		if(pfpTransactionPO != null){
			return R.fail("transaction number :" +  txn + " has been used");
		}

		if(pfpTokenPO.getPrice() == null){
			return R.fail("collect is not support");
		}

		//链上校验交易哈希是否成功、金额是否正确
		int reTryCount = 0;
		Boolean checkFlag = false;
		while (reTryCount < 100){
			R<Boolean> checkBNBTransResult = ethService.checkBNBTransacation(txn, pfpTokenPO.getPrice(),payAddress,pfpTokenPO.getAdminAddress());
			if(checkBNBTransResult.getCode() == 200){
				//终止循环
				reTryCount = 100;
				checkFlag = checkBNBTransResult.getData();
			}else {
				// 休眠3秒再进行重试
				Thread.sleep(10 * 1000);
				reTryCount++;
			}
		}

		if(!checkFlag){
			//未校验通过
			return R.fail("bnb transacation check failed: bnb transfer failed!");
		}

		//创建订单
		pfpTransactionPO = new PFPTransactionPO();
		pfpTransactionPO.setTokenId(pfpTokenPO.getId());
		pfpTransactionPO.setAdminAddress(pfpTokenPO.getAdminAddress());
		pfpTransactionPO.setLinkType(pfpTokenPO.getLinkType());
		pfpTransactionPO.setNetwork(pfpTokenPO.getNetwork());
		pfpTransactionPO.setContractAddress(pfpTokenPO.getContractAddress());
		pfpTransactionPO.setContractName(pfpTokenPO.getContractName());
		pfpTransactionPO.setFromAddress(ownerAddress);
		pfpTransactionPO.setToAddress(toAddress);
		pfpTransactionPO.setFromUserId(ownerUserId);
		pfpTransactionPO.setToUserId(userId);
//		交易状态：0-未交易 1-已付款未交易PFP 2-交易完成 3-交易取消
		pfpTransactionPO.setTransactionStatus(1);
		pfpTransactionPO.setBuyerMoneyTxnHash(txn);
//		pfpTransactionPO.setPfpTxnHash();
		pfpTransactionPO.setMintUserId(pfpTokenPO.getMintUserId());
		pfpTransactionPO.setMintUserAddress(pfpTokenPO.getMintUserAddress());

		//计算费用
		pfpTransactionPO.setListPrice(pfpTokenPO.getPrice());

		pfpTransactionPO.initForInsert();
		pfpTransactionMapper.insert(pfpTransactionPO);

		//NFT是否授权校验
//		R approveCheckResult = checkApprove(pfpTokenPO.getId(), pfpTokenPO.getOwnerUserId());
//		if(approveCheckResult.getCode() != 200){
//			return approveCheckResult;
//		}

		//NFT转账
//		R<String> transferNFTResult = ethService.approveTransferNFT(ownerAddress,toAddress,tokenId);
//		if(transferNFTResult.getCode() != 200){
//			return transferNFTResult;
//		}
//		String transferNFTTxn = transferNFTResult.getData();
		String transferNFTTxn = "0x156464er64wq61sdf32ds4g64a65e1wqerqwe1fasdfsdf";
//		pfpTransactionPO.setPfpTxnHash(transferNFTTxn);

		//NFT转账校验
//		reTryCount = 0;
//		Boolean checkNFTOwner = false;
//		while (reTryCount < 10){
//			checkNFTOwner = ethService.checkNFTOwner(toAddress,tokenId);
//			if(checkNFTOwner){
//				//终止循环
//				reTryCount = 10;
//			}else {
//				// 休眠3秒再进行重试
//				Thread.sleep(10 * 1000);
//				reTryCount++;
//			}
//		}
//
//		if(!checkNFTOwner){
//			return R.fail("NFT transfer check failed:NFT owner is not correct");
//		}

		//添加history
		PFPHistoryPO pfpHistoryPO = new PFPHistoryPO();
		pfpHistoryPO.setTokenId(pfpTokenPO.getId());
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
		pfpHistoryPO.setTxnHash(transferNFTTxn);
		pfpHistoryPO.setPrice(pfpTransactionPO.getListPrice());
		pfpHistoryPO.initForInsert();

		pfpHistoryMapper.insert(pfpHistoryPO);

		//BNB转账：卖NFT收益
		R<String> transferBNBResult = ethService.transferBNB(ownerAddress,pfpTransactionPO.getSellerEarnPrice());
		if(transferBNBResult.getCode() != 200){
			//TODO 稍后再次尝试
		}
		//售卖者收款流水号
		String sellerMoneyTxnHash = transferBNBResult.getData();
		pfpTransactionPO.setSellerMoneyTxnHash(sellerMoneyTxnHash);

		//BNB转账：铸造者收益
		R<String> mintTransferBNBResult = ethService.transferBNB(minterAddress,pfpTransactionPO.getMinterEarnPrice());
		if(mintTransferBNBResult.getCode() != 200){
			//TODO 稍后再次尝试
		}
		//铸造者收益流水号
		String minterMoneyTxnHash = mintTransferBNBResult.getData();
		pfpTransactionPO.setMinterMoneyTxnHash(minterMoneyTxnHash);


		pfpTransactionPO.setTransactionStatus(2);
		pfpTransactionMapper.updateById(pfpTransactionPO);

		//修改持有人
		pfpTokenPO.setOwnerAddress(toAddress);
		pfpTokenPO.setOwnerUserId(userId);
		pfpTokenPO.setPrice(null);
		pfpTokenPO.setPriceTime(null);

		pfpTokenPO.initForUpdate();
		//设置最新成交价
		pfpTokenPO.setLastSale(pfpTransactionPO.getListPrice());
		pfpTokenPO.setLastSaleTime(new Date());

		pfpTokenMapper.updateById(pfpTokenPO);

		return R.success("success");
	}

	/**
	 * 创建订单
	 * @param collectCreateOrderQuery
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R collectCreateOrder(CollectCreateOrderQuery collectCreateOrderQuery) {

		Long userId = StpUtil.getLoginIdAsLong();
		Long tokenId = collectCreateOrderQuery.getTokenId();

		//购买方用户信息
		MemberPO memberPO = memberMapper.selectById(tokenId);

		//获取NFT信息
//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		//交易中
		if(pfpTokenPO.getStatus() == 1){
			return R.fail("This PFP is currently being traded");
		}

		BigDecimal price = pfpTokenPO.getPrice();
		//如果价格小于0.01
//		if(price == null || price.compareTo(new BigDecimal("0.01")) < 0){
//			return R.fail("Price must be greater then or equal to 0.01");
//		}

		//校验该用户是否有待付款的订单
		PFPTransactionPO temp = pfpTransactionMapper.selectOne(new LambdaQueryWrapper<PFPTransactionPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTransactionPO::getToUserId, userId)
			.eq(PFPTransactionPO::getTransactionStatus, 0));

		if(temp != null){
			return R.fail("You have an unpaid order, please make payment or cancel before placing the order");
		}


		//交易中
		pfpTokenPO.setStatus(1);
		pfpTokenMapper.updateById(pfpTokenPO);

		PFPTransactionPO pfpTransactionPO = new PFPTransactionPO();
		//设置价格、收益价格
		pfpTransactionPO.setListPrice(price);
		pfpTransactionPO.setTokenId(tokenId);
		pfpTransactionPO.setAdminAddress(pfpTokenPO.getAdminAddress());
		pfpTransactionPO.setLinkType(pfpTokenPO.getLinkType());
		pfpTransactionPO.setNetwork(pfpTokenPO.getNetwork());
		pfpTransactionPO.setContractAddress(pfpTokenPO.getContractAddress());
		pfpTransactionPO.setContractName(pfpTokenPO.getContractName());

		pfpTransactionPO.setFromAddress(pfpTokenPO.getOwnerAddress());
		pfpTransactionPO.setToAddress(memberPO.getAddress());

		pfpTransactionPO.setFromUserId(pfpTokenPO.getOwnerUserId());
		pfpTransactionPO.setToUserId(userId);
		//未交易，已下单
		pfpTransactionPO.setTransactionStatus(0);

		pfpTransactionPO.setMintUserId(pfpTokenPO.getMintUserId());
		pfpTransactionPO.setMintUserAddress(pfpTokenPO.getMintUserAddress());

		pfpTransactionPO.initForInsert();

		pfpTransactionMapper.insert(pfpTransactionPO);

		return R.success("Create order success");
	}

	/**
	 * 收款验证成功，转NFT以及用户收益
	 */
	@Override
	public R transferNFT(PFPTransactionPO pfpTransactionPO) {
		Long tokenId = pfpTransactionPO.getTokenId();
//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);

		String ownerAddress = pfpTransactionPO.getFromAddress();
		String toAddress = pfpTransactionPO.getToAddress();

		//NFT是否授权校验
		R approveCheckResult = ethService.checkApprove(tokenId,ownerAddress);
		if(approveCheckResult.getCode() != 200){
			return approveCheckResult;
		}

		//NFT转账
		R<String> transferNFTResult = ethService.approveTransferNFT(ownerAddress,toAddress,tokenId);
		if(transferNFTResult.getCode() != 200){
			return transferNFTResult;
		}
		String transferNFTTxn = transferNFTResult.getData();
		pfpTransactionPO.setPfpTxnHash(transferNFTTxn);

		//NFT转账校验
		int reTryCount = 0;
		Boolean checkNFTOwner = false;
		while (reTryCount < 10){
			checkNFTOwner = ethService.checkNFTOwner(toAddress,tokenId);
			if(checkNFTOwner){
				//终止循环
				reTryCount = 10;
			}else {
				try {
					// 休眠10秒再进行重试
					Thread.sleep(10*000);
				}catch (Exception e){}
				reTryCount++;
			}
		}

		if(!checkNFTOwner){
			return R.fail("NFT transfer check failed:NFT owner is not correct");
		}

		//BNB转账：卖NFT收益
		R<String> transferBNBResult = ethService.transferBNB(ownerAddress,pfpTransactionPO.getSellerEarnPrice());
		if(transferBNBResult.getCode() != 200){
			//TODO 稍后再次尝试
		}
		//售卖者收款流水号
		String sellerMoneyTxnHash = transferBNBResult.getData();
		pfpTransactionPO.setSellerMoneyTxnHash(sellerMoneyTxnHash);

		//BNB转账：铸造者收益
		R<String> mintTransferBNBResult = ethService.transferBNB(pfpTokenPO.getMintUserAddress(),pfpTransactionPO.getMinterEarnPrice());
		if(mintTransferBNBResult.getCode() != 200){
			//TODO 稍后再次尝试
		}
		//铸造者收益流水号
		String minterMoneyTxnHash = mintTransferBNBResult.getData();
		pfpTransactionPO.setMinterMoneyTxnHash(minterMoneyTxnHash);

		pfpTransactionPO.setTransactionStatus(2);
		pfpTransactionMapper.updateById(pfpTransactionPO);

		//修改持有人
		pfpTokenPO.setOwnerAddress(toAddress);
		pfpTokenPO.setOwnerUserId(pfpTransactionPO.getToUserId());
		pfpTokenPO.setPrice(null);
		pfpTokenPO.setPriceTime(null);

		pfpTokenPO.initForUpdate();
		//设置最新成交价
		pfpTokenPO.setLastSale(pfpTransactionPO.getListPrice());
		pfpTokenPO.setLastSaleTime(new Date());

		//交易状态：0-可交易
		pfpTokenPO.setStatus(0);
		pfpTokenMapper.updateById(pfpTokenPO);

		return R.success("transfer success");
	}

	/**
	 * 获得随机标签
	 * @return
	 */
//	private static String getRandomTags() {
//		List<String> tags = new ArrayList<>();
//		for (UserTagsEnum value : UserTagsEnum.values()) {
//			int code = value.getCode();
//			tags.add(code + "");
//		}
//
//		List<String> integers = RandomUtil.randomEleList(tags, 3);
//
//		String userTags = integers.stream().collect(Collectors.joining(","));
//		System.out.println("userTags:"+userTags);
//
//		return userTags;
//	}

}
