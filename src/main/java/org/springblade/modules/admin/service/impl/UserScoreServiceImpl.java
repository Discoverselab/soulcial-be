package org.springblade.modules.admin.service.impl;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

//import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.admin.dao.MemberConnectMapper;
import org.springblade.modules.admin.dao.MemberMapper;
import org.springblade.modules.admin.dao.PFPPickMapper;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.pojo.vo.SoulVo;
import org.springblade.modules.admin.service.UserScoreService;
import org.springblade.modules.admin.util.ScoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Component
@Slf4j
public class UserScoreServiceImpl implements UserScoreService {

	// 30s超时
	private final static int timeout = 30000;
//	private final static int timeout = HttpGlobalConfig.getTimeout();

	private final static String knn3AuthKey = "a97c41edaf7bbcde96dfc31ab15226a2e09c1d15913551f85d56847d3ee10ef8";

	private final static String NFTGOAPIKEY = "18a5b214-3815-4dd8-b280-413b8844431e";

	private final static String LensFollowingCountUrl = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/lensFollowingCount";

	private final static String LensFollowersCountUrl = "https://knn3-gateway.knn3.xyz/data-api/api/lens/followers/:profileId/count";

	private final static String LensProfileIdsByAddressUrl = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/boundLens";

	private final static String PoapsCountUrl = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/poaps/count";

	private final static String SnapshotCountUrl = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/calVotes/";

	private final static String NFTCountUrlKnn3 = "https://knn3-gateway.knn3.xyz/data-api/api/addresses/holdNfts";

	private final static String NFTCountUrlNFTGO = "https://data-api.nftgo.io/eth/v2/address/metrics?address=";

//	private final static String CCMainnetUrl = "https://api.cyberconnect.dev/playground";
//	private final static String CCTestUrl = "https://api.cyberconnect.dev/testnet/playground";

	private final static String CCTestUrl = "https://api.cyberconnect.dev/testnet/";

	@Autowired
	MemberMapper memberMapper;

	@Autowired
	MemberConnectMapper memberConnectMapper;

	@Autowired
	PFPPickMapper pfpPickMapper;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	// 获取用户推特被关注数量
	public static int getTwitterFollowers(String address) {
		// 根据address查询member，返回twitterFollowersCount
		MemberMapper memberMapper = SpringUtil.getBean(MemberMapper.class);
		MemberPO memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberPO::getAddress, address.toLowerCase()));
		if (memberPO == null || memberPO.getBindTwitter() == 0) {
			return 0;
		}
		return memberPO.getTwitterFollowers();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUserScore(Long userId) {
		// 获取当前用户信息
		MemberPO memberPO = memberMapper.selectById(userId);

		Integer oldCharisma = memberPO.getCharisma();
		Integer oldExtroversion = memberPO.getExtroversion();
		Integer oldEnergy = memberPO.getEnergy();
		Integer oldWisdom = memberPO.getWisdom();
		Integer oldArt = memberPO.getArt();
		Integer oldCourage = memberPO.getCourage();

		String address = memberPO.getAddress();

		//getLensProfileIdByAddress
		String lensProfile = getLensProfileIdByAddress(address);
		if(lensProfile != null && lensProfile.length() > 0){
			//TODO 正式网的时候放开该注释
//			memberPO.setLensProfile(lensProfile);
		}

		//getLensFollowing
//		int lensFollowing = getLensFollowing(address);

		//getLensFollows
		int lensFollows = getLensFollows(lensProfile);

		//getCCFollowing
//		int ccFollowing = getCCFollowing(address);

		//getCCFollowers
//		int ccFollows = getCCFollowers(address);
		// 获取当前用户绑定的推特数量
		int ttFollows = UserScoreServiceImpl.getTwitterFollowers(address);

		//getPoapsCount
		int poapsCount = getPoapsCount(address);

		//getW3STCount
		int w3STCount = getW3STCount(address);

		//getSnapshotCount
		int snapshotCount = getSnapshotCount(address);

		//getNFTCount
		int NFTCount = getNFTCount(address);

		//getETHGasTotal(仅集成了ETH)
		BigDecimal ETHGasTotal = getETHGasTotal(address);

		//获取用户pick的次数
		LambdaQueryWrapper<PFPPickPO> pickWrapper = new LambdaQueryWrapper<>();
		pickWrapper.eq(BasePO::getIsDeleted,0);
		pickWrapper.and(tmp->{
			tmp.eq(PFPPickPO::getIndexUserId0,userId)
				.or().eq(PFPPickPO::getIndexUserId1,userId)
				.or().eq(PFPPickPO::getIndexUserId2,userId)
				.or().eq(PFPPickPO::getIndexUserId3,userId);
		});
		Long pickCount = pfpPickMapper.selectCount(pickWrapper);

		//获取用户NFT持有数量
		Long nftOwnerCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted,0)
			.eq(PFPTokenPO::getMintStatus,1)
			.eq(PFPTokenPO::getOwnerUserId,userId));

		Integer courage = getScoreCourage(ETHGasTotal,pickCount);
		Integer art = getScoreArt(NFTCount,nftOwnerCount);
		Integer wisdom = getScoreWisdom(snapshotCount);
		Integer energy = getScoreEnergy(poapsCount,w3STCount);
		// DONE 连接度 此处需要替换成一度连接数 和二度连接数
		// 1度：查询出member_connect中user_id或to_user_id为当前用户ID的数组，将数量取出
		List<MemberConnectPO> memberConnectList = memberConnectMapper.selectList(new LambdaQueryWrapper<MemberConnectPO>()
			.eq(BasePO::getIsDeleted, 0)
			.and(tmp -> tmp.eq(MemberConnectPO::getUserId, userId).or().eq(MemberConnectPO::getToUserId, userId)));
		Long x1 = (long) memberConnectList.size();
		// 2度 - v1 本id邀请的人又邀请了其他人的人数总和：查询出member中invite_user_id为当前用户ID的数组，通过该数据继续查询member中的invite_user_id，将数量取出
		List<Long> memberIdList = memberMapper.selectList(new LambdaQueryWrapper<MemberPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberPO::getInviteUserId, userId))
			.stream().map(MemberPO::getId).collect(Collectors.toList());
		//TODO 2度 - v2（暂时不用，查询效率比较低） 本id的所有connect的人的所有connect，排除自身：根据memberConnectList查询member_connect中user_id或to_user_id为当前用户ID的数组，并且user_id以及to_user_id不是本人id，将数量取出
		Long x2 = 0L;
		if (!memberIdList.isEmpty()) {
			x2 = memberMapper.selectCount(new LambdaQueryWrapper<MemberPO>()
				.eq(BasePO::getIsDeleted, 0)
				.in(MemberPO::getInviteUserId, memberIdList));
		}

		Integer extroversion = getScoreCONNECTION(x1,x2);
		//即将弃用，由上方的getScoreCONNECTION替代
//		Integer extroversion = getScoreExtroversion(lensFollowing,ccFollowing);

		//影响力
		//查询持有NFT数量
		Long lv1 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getMintStatus, 1)
			.eq(PFPTokenPO::getOwnerUserId, userId)
			.eq(PFPTokenPO::getLevel, 1));
		Long lv2 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getMintStatus, 1)
			.eq(PFPTokenPO::getOwnerUserId, userId)
			.eq(PFPTokenPO::getLevel, 2));
		Long lv3 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getMintStatus, 1)
			.eq(PFPTokenPO::getOwnerUserId, userId)
			.eq(PFPTokenPO::getLevel, 3));
		Long lv4 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getMintStatus, 1)
			.eq(PFPTokenPO::getOwnerUserId, userId)
			.eq(PFPTokenPO::getLevel, 4));
		Long lv5 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getMintStatus, 1)
			.eq(PFPTokenPO::getOwnerUserId, userId)
			.eq(PFPTokenPO::getLevel, 5));
		Integer charisma = getScoreINFLUENCE(lensFollows,ttFollows,lv1,lv2,lv3,lv4,lv5);

		//即将弃用，由上方的getScoreINFLUENCE替代
//		Integer charisma = getScoreCharisma(lensFollows,ccFollows);

		if(oldCourage == null || (oldCourage < courage &&
			(pickCount + ETHGasTotal.intValue()) > 0)
		){
			memberPO.setCourage(courage);
		}
		if(oldArt == null || (NFTCount + nftOwnerCount) > 0){
			memberPO.setArt(art);
		}
		if(oldWisdom == null || (oldWisdom < wisdom && poapsCount > 0)){
			memberPO.setWisdom(wisdom);
		}
		if(oldEnergy == null || (oldEnergy < energy && (poapsCount + w3STCount) > 0)){
			memberPO.setEnergy(energy);
		}
		if(oldExtroversion == null || (oldExtroversion < extroversion && (x1 + x2) > 0)){
			memberPO.setExtroversion(extroversion);
		}
		if(oldCharisma == null || (oldCharisma < charisma
			// lensFollows,ttFollows,lv1,lv2,lv3,lv4,lv5相加大于0才更新
			&& (lensFollows + ttFollows + lv1 + lv2 + lv3 + lv4 + lv5) > 0)
		){
			memberPO.setCharisma(charisma);
		}

		//更新soul字段
		if (ObjectUtil.isNotEmpty(memberPO.getCharisma())){
			SoulVo soulVo = ScoreUtil.getPersonalityCharacter(
				memberPO.getCharisma(),
				memberPO.getExtroversion(),
				memberPO.getEnergy(),
				memberPO.getWisdom(),
				memberPO.getArt(),
				memberPO.getCourage()
			);
			memberPO.setPersonality(soulVo.getPersonality());
			memberPO.setChracter(soulVo.getCharacter());
			memberPO.setSoul(soulVo.getPersonality()+" "+soulVo.getCharacter());
		}

		//计算总分
		memberPO.countLevelScore();
		//计算level
		memberPO.countLevel();

		memberPO.setUpdateTime(new Date());

		memberMapper.updateById(memberPO);
	}

	// 根据adrees获取用户id
	public static Long  getUserIdByAddress(String address) {
		MemberMapper memberMapper = SpringUtil.getBean(MemberMapper.class);
		MemberPO memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(MemberPO::getAddress, address.toLowerCase()));
		if (memberPO == null) {
			return null;
		}
		return memberPO.getId();
	}

	public static Integer getScoreINFLUENCE(int lensFollows,int ccFollows,
											Long lv1CollectCount,Long lv2CollectCount,Long lv3CollectCount,Long lv4CollectCount,Long lv5CollectCount) {
		//f1(x)
//		int count1 = lensFollows + ccFollows;
//		double f1 = 0;
//		if(count1 == 0){
//			f1 = 20;
//		}else if(count1 < 28){
//			f1 = Math.log(count1) * 18 + 40;
//			if(f1 > 100){
//				f1 = 100;
//			}
//		}else {
//			f1 = 100;
//		}
//
//		log.info("f1:"+f1);
//
//		//f2(x)
//		int count2 = (int) (lv1CollectCount + 2 * lv2CollectCount + 3 * lv3CollectCount + 4 * lv4CollectCount + 5 * lv5CollectCount);
//
//		double f2 = 0;
//		if(count2 == 0){
//			f2 = 20;
//		}else if(count2 <= 200){
//			f2 = Math.log(count2) * 13 + 30;
//			if(f2 > 100){
//				f2 = 100;
//			}
//		}else {
//			f2 = 100;
//		}
//		log.info("f2:"+f2);
//
//		double x = f1 * 0.1 + f2;
//		if(x > 100){
//			x = 100;
//		}
//
//		log.info("getScoreCharisma:"+(int)x);
		int f1 = 0;
		int f2 = 0;
		//1. 外部分f1(x)：设Lens Profile+Twitter的follower数量 x
		//  设lens和twitter followers数量分别为x1和x2，计算总数量次数为
		//  x1=常量e * x1+x2
		int x1 = (int) (Math.E * lensFollows + ccFollows);
		//  1. 当x=0或没有以上任何账号，则 f1=10 - 20 随机整数
		if (x1 == 0) {
			// f1为10-20的随机数
			Random rand = new Random();
			f1 = rand.nextInt(11) + 10;
		}
		//  2. 当1<=x<2000000， f1=πln(100 * x1)+40
		else if (x1 >= 1 && x1 < 2000000) {
			f1 = (int) (Math.PI * Math.log(100 * x1) + 40);
		}
		//  3. 当x>=2M，f1=100
		else if (x1 >= 2000000) {
			f1 = 100;
		}

		//2. 内部分f2：
		//  设应用内自己的1-5级NFT的被collect的次数分别为x1-x5，计算总collect次数为
		//  $$x_6=x_1+2x_2+3x_3+4x_4+5 x5
		int count = (int) (lv1CollectCount + 2 * lv2CollectCount + 3 * lv3CollectCount + 4 * lv4CollectCount + 5 * lv5CollectCount);
		//  1. 当x6=0，f2(x6)=0
		if (count == 0) {
			f2 = 0;
		}
		//  2. 当1<=x6<=200
		else if (count >= 1 && count <= 200) {
			//    $$f_2(x_6)=13ln(x_6)+30$$
			f2 = (int) (13 * Math.log(count) + 30);
		}
		//  3. 当x6>200，f2(x6)=100
		else if (count > 200) {
			f2 = 100;
		}
		//3. 总分f(x)=50%*f1(x)+50%*f2(x)，若总分相加超过100分，则显示100分

        return (int) (0.5 * f1 + 0.5 * f2);
	}

	//即将弃用，由上方的getScoreINFLUENCE替代
	public static Integer getScoreCharisma(int lensFollows,int ccFollows) {
		//f1(x)
		int count1 = lensFollows + ccFollows;
		double f1 = 0;
		if(count1 == 0){
			f1 = 30;
		}else if(count1 < 28){
			f1 = Math.log(count1) * 18 + 40;
			if(f1 > 100){
				f1 = 100;
			}
		}else {
			f1 = 100;
		}

		log.info("f1:"+f1);

		//f2(x)
		int count2 = 0;

		double f2 = 0;
		if(count2 == 0){
			f2 = 0;
		}else if(count2 <= 148){
			f2 = Math.log(count2) * 16 + 20;
			if(f2 > 100){
				f2 = 100;
			}
		}else {
			f2 = 100;
		}
		log.info("f2:"+f2);

		//TODO 目前仅计算f1
		double x = f1;
//		double x = f1 * 0.8 + f2 * 0.2;

		log.info("getScoreCharisma:"+(int)x);

		return (int)x;
	}

	public static Integer getScoreCONNECTION(Long x1, Long x2) {

		/*
		  设应用内自己一度连接和二度连接和分别为x1和x2，计算connect连接数量为
			$$x=x_1+0.5x_2$$
			1. 当x=0，则 f1(x)=17-23随机整数
			2. 当1<=x<1800， f1(x)=10*ln(x)+25
			3. 当x>=1800，f1(x)=100
		 */
		int count = (int) (x1 + 0.5 * x2);
		double f1 = 0;
		if(count == 0){
			// f1为17-23的随机数
			Random rand = new Random();
			f1 = rand.nextInt(7) + 17;
		}else if(count < 1800){
			f1 = Math.log(count) * 10 + 25;
		}else {
			f1 = 100;
		}

//		double f1 = 0;
//		if(count == 0){
//			f1 = 20;
//		}else if(count < 1800){
//			f1 = Math.log(count) * 10 + 25;
//			if(f1 > 100){
//				f1 = 100;
//			}
//		}else {
//			f1 = 100;
//		}

		double x = f1;

		log.info("getScoreCONNECTION:"+(int)x);

		return (int)x;
	}

	//即将弃用，由上方的getScoreCONNECTION替代
	public static Integer getScoreExtroversion(int lensFollowing,int ccFollowing) {

		//f1(x)
		int count = lensFollowing + ccFollowing;
		double f1 = 0;
		if(count == 0){
			f1 = 30;
		}else if(count < 28){
			f1 = Math.log(count) * 18 + 40;
			if(f1 > 100){
				f1 = 100;
			}
		}else {
			f1 = 100;
		}

		log.info("f1:"+f1);

		//TODO 目前仅计算f1
		double x = f1;
//		double x = f1 * 0.2;

		log.info("getScoreExtroversion:"+(int)x);

		return (int)x;
	}

	public static Integer getScoreEnergy(int poapsCount,int w3STCount) {

		/*
			假设poapsCount+w3STCount+OAT数量x
			  1. 当x=0，则 f(x)=5-10
			  2. 当1<=x<400， f(x)=10*ln(x)+40
			  3. 当x>=400，f(x)=100
		 */
		int count = poapsCount + w3STCount;
		double x = 0;
		if (count >= 1 && count < 400) {
			x = Math.log(count) * 10 + 40;
		} else if (count >= 400) {
			x = 100;
		} else {
			// x为5-10的随机数
			Random rand = new Random();
			x = rand.nextInt(6) + 5;
		}
//		double x = 0;
//		if(count == 0){
//			x = 20;
//		}else if(count < 400){
//			x = Math.log(count) * 10 + 40;
//			if(x > 100){
//				x = 100;
//			}
//		}else {
//			x = 100;
//		}

		log.info("getScoreEnergy:"+(int)x);

		return (int)x;
	}

	public static Integer getScoreWisdom(int snapshotCount) {
		/**
		 * 设Snapshot投票数量x
		 * 1. 当x=0，则 f(x)=5-10
		 * 2. 当1<=x<20， f(x)=20*ln(x)+40
		 * 3. 当x>=20，f(x)=100
		 */
		int x = snapshotCount;
		double result = 0;
		if (x == 0) {
			// result为30±3的随机数
			Random rand = new Random();
			// 5~10 分
			result = rand.nextInt(6) + 5;
		} else if (x >= 1 && x < 20) {
			result = Math.log(x) * 20 + 40;
		} else if (x >= 20) {
			result = 100;
		}
//		if(count == 0){
//			x = 20;
//		}else if(count < 20){
//			x = Math.log(count) * 20 + 40;
//			if(x > 100){
//				x = 100;
//			}
//		}else {
//			x = 100;
//		}

		log.info("getScoreWisdom:"+(int)result);

		return (int)result;
	}

	public static Integer getScoreArt(Integer nftCount,Long nftOwnerCount) {
		/**
		 *  外部分f1(x)：持有NFT数量（剔除游戏道具/poap类/sbt类）（根据NFTGO的数据返回）
		 *   1. 当x=0，则 f1(x)=20整数
		 *   2. 当1<=x<100， f1(x)=13*ln(x)+40
		 *   3. 当x>=100，f1(x)=100
		 */
		int count = nftCount;
		double f1 = 0;
		if(count == 0){
			f1 = 20;
		} else if (count >= 1 && count < 100) {
			f1 = Math.log(count) * 13 + 40;
		} else if (count >= 100) {
			f1 = 100;
		}

		/**
		 * 内部分f2(x)：设应用内持有SoulCast的数量（包括自己mint的或者购买到别人的，如果卖掉，会被扣分）
		 *   1. 当x=0，f2(x)=0
		 *   2. 当1<=x<=20，f2(x)=20*ln(x)+40
		 *   3. 当x>20，f2(x)=100
		 */

		double f2 = 0;
		if(nftOwnerCount == 0){
			f2 = 0;
		}else if(nftOwnerCount <= 20){
			f2 = Math.log(nftOwnerCount) * 20 + 40;
		}else {
			f2 = 100;
		}

		double x = 0.5 * f1 + 0.5 * f2;

		log.info("getScoreArt:"+(int)x);

		return (int)x;
	}

	public static Integer getScoreCourage(BigDecimal ethGasTotal,Long pickCount) {
		/**
		 *  外部分f1(x)：ETH+Polygon+BSC 累计消耗的gas金额（U），若后面更新时发现得到的分数小于之前的得分，则不减分，若分数变大，则更新
		 *   1. 当x=0，则 f(x)=10-19随机整数
		 *   2. 当1<=x<1440， f(x)=11*ln(x)+20
		 *   3. 当x>=1440，f(x)=100
		 */
		BigDecimal sum = ethGasTotal.setScale(0,BigDecimal.ROUND_HALF_UP);
		int count = sum.intValue();

		double f1 = 0;
		if(count == 0){
			// f1为10-19的随机数
			Random rand = new Random();
			f1 = rand.nextInt(10) + 10;
		}else if(count < 1440){
			f1 = Math.log(count) * 11 + 20;
		}else {
			f1 = 100;
		}
		/**
		 *  内部分f2(x)：应用内参与Pick到次数（只要参与pick就记入，不论是否在开奖队列中）
		 *   1. 当x=0，则 f(x)=0
		 *   2. 当1<=x<300， f(x)=13*ln(x)+25
		 *   3. 当x>=300，f(x)=100
		 */
		double f2 = 0;
		if(pickCount == 0){
			f2 = 0;
		} else if (pickCount < 300) {
			f2 = Math.log(pickCount) * 13 + 25;
		} else {
			f2 = 100;
		}

		double x = 0.5 * f1 + 0.5 * f2;

		log.info("getScoreCourage:"+(int)x);

		return (int)x;
	}


	public static int getLensFollowing(String address) {
		int count = 0;
		try {
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("address",address);

			//获取lens following
			HttpRequest httpRequest = HttpRequest.get(LensFollowingCountUrl).header("auth-key", knn3AuthKey).form(paramMap).timeout(timeout);
			String body = httpRequest.execute().body();
			log.info("getLensFollowing:body:"+body);
			count = Integer.parseInt(body);
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getLensFollowing:count:"+count);
		return count;
	}

	public static String getLensProfileIdByAddress(String address) {
		String lensProfileIds = "";
		try {
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("address",address);

			HttpRequest httpRequest = HttpRequest.get(LensProfileIdsByAddressUrl).form(paramMap).header("auth-key", knn3AuthKey).timeout(timeout);
			String body = httpRequest.execute().body();

			System.out.println("getLensProfileIdByAddress:body:"+body);

			JSONObject jsonObject = JSONObject.parseObject(body);
			JSONArray list = new JSONArray();
			if (jsonObject != null) {
				list = jsonObject.getJSONArray("list");
			}
			for (int i=0;i<list.size();i++){
				JSONObject profileObj = list.getJSONObject(i);
				Integer profileId = profileObj.getInteger("profileId");
				System.out.println("getLensProfileIdByAddress:profileId:"+profileId);

				lensProfileIds = lensProfileIds + profileId + ",";
			}
			if(lensProfileIds.endsWith(",")){
				lensProfileIds.substring(0,lensProfileIds.length()-1);
			}
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
		log.info("getLensProfileIdByAddress:lensProfileIds:"+lensProfileIds);
		return lensProfileIds;
	}

	/**
	 * 根据地址查询lens的用户名
	 * @param address
	 * @return
	 */
	@Override
	public String getLensNameByAddress(String address) {
		String lensName = null;
		try {
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("address",address);

			HttpRequest httpRequest = HttpRequest.get(LensProfileIdsByAddressUrl).form(paramMap).header("auth-key", knn3AuthKey).timeout(timeout);
			String body = httpRequest.execute().body();

			System.out.println("getLensNameByAddress:body:"+body);

			JSONObject jsonObject = JSONObject.parseObject(body);
			JSONArray list = jsonObject.getJSONArray("list");

			List<JSONObject> profiles = new ArrayList<>();
			for (int i=0;i<list.size();i++){
				JSONObject profileObj = list.getJSONObject(i);
				profiles.add(profileObj);
			}

			List<String> collect = profiles.stream().sorted(Comparator.comparing(x -> {
					return x.getInteger("profileId");
				}))
				.map(x -> {
					return x.getString("handle");
				}).collect(Collectors.toList());

			lensName = collect.get(0);

			log.info("getLensNameByAddress:lensProfileIds:" + lensName);
			return lensName;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void batchCalcUserSoul() {
		List<MemberPO> userList = memberMapper.selectList(null);
		for (MemberPO memberPO : userList) {
			//更新soul字段
			if (ObjectUtil.isNotEmpty(memberPO.getCharisma())){
				SoulVo soulVo = ScoreUtil.getPersonalityCharacter(memberPO.getCharisma(), memberPO.getExtroversion(), memberPO.getEnergy(), memberPO.getWisdom(), memberPO.getArt(), memberPO.getCourage());
				memberPO.setPersonality(soulVo.getPersonality());
				memberPO.setChracter(soulVo.getCharacter());
				memberPO.setSoul(soulVo.getPersonality()+" "+soulVo.getCharacter());
				memberMapper.updateById(memberPO);
			}
		}

	}

	public static int getLensFollows(String lensProfileIds) {
		int count = 0;
		try {

			if(lensProfileIds == null || lensProfileIds.length() == 0){
				return 0;
			}

			String[] array = lensProfileIds.split(",");
			for (int i=0;i<array.length;i++){
				String profileId = array[i];
				System.out.println("getLensFollows:profileId:"+profileId);
				int lensFollows = getLensFollowsByProfileId(profileId);

				count = count + lensFollows;
			}
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getLensFollows:count:"+count);
		return count;
	}

	public static int getLensFollowsByProfileId(String profileId) {
		int count = 0;
		try {
			//获取lens following
			HttpRequest httpRequest = HttpRequest.get(LensFollowersCountUrl.replace(":profileId",profileId)).header("auth-key", knn3AuthKey).timeout(timeout);
			String body = httpRequest.execute().body();

			System.out.println("getLensFollowsByProfileId:body:"+body);
			count = Integer.parseInt(body);
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getLensFollowsByProfileId:LensFollows:"+count);
		return count;
	}

	public static int getPoapsCount(String address) {
		int count = 0;
		try {

			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("address",address);

			HttpRequest httpRequest = HttpRequest.get(PoapsCountUrl).form(paramMap).header("auth-key", knn3AuthKey).timeout(timeout);
			String body = httpRequest.execute().body();
			System.out.println("getPoapsCount:body:"+body);
			count = Integer.parseInt(body);
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getPoapsCount:count:"+count);
		return count;
	}

	public static int getSnapshotCount(String address) {
		int count = 0;
		try {

			HttpRequest httpRequest = HttpRequest.get(SnapshotCountUrl + address).header("auth-key", knn3AuthKey).timeout(timeout);
			String body = httpRequest.execute().body();
			System.out.println("getSnapshotCount:body:"+body);
			count = JSONObject.parseObject(body).getInteger("total");
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getSnapshotCount:count:"+count);
		return count;
	}

	public static int getNFTCount(String address) {
		int count = 0;
		try {
			HttpRequest httpRequest = HttpRequest.get(NFTCountUrlNFTGO + address).header("X-API-KEY", NFTGOAPIKEY).timeout(timeout);
			String body = httpRequest.execute().body();
			System.out.println("getNFTCount:body:"+body);
			JSONObject jsonObject = JSONObject.parseObject(body);
			count = jsonObject.getInteger("nft_num");

		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getNFTCount:count:"+count);
		return count;
	}

	public static BigDecimal getETHGasTotal(String address) {
		//TODO 替换apiKeyToken
		String ethApiKeyToken = "CQ1VFBM1UC8U56YUPFRQ2VJUT115D4DTF1";

		BigDecimal ethUsedSum = BigDecimal.ZERO;
		try {

			String ethUrl = "https://api.etherscan.io/api" +
				"?module=account" +
				"&action=txlist" +
				"&address=" + address +
				"&startblock=0" +
				"&endblock=99999999" +
				"&page=1" +
				"&offset=10" +
				"&sort=asc" +
				"&apikey=" + ethApiKeyToken;

			//ethereum 网络
			HttpRequest httpRequest = HttpRequest.get(ethUrl).header("auth-key", knn3AuthKey).timeout(timeout);
			String body = httpRequest.execute().body();
			System.out.println("body:"+body);

			JSONObject jsonObject = JSONObject.parseObject(body);
			JSONArray list = jsonObject.getJSONArray("result");
			for (int i=0;i<list.size();i++){
				JSONObject transactionJsonObj = list.getJSONObject(i);
				String gasUsed = transactionJsonObj.getString("gasUsed");
				String gasPrice = transactionJsonObj.getString("gasPrice");

				BigDecimal ethUsed = new BigDecimal(gasUsed).multiply(new BigDecimal(gasPrice)).divide(new BigDecimal(Math.pow(10,18)),18,BigDecimal.ROUND_HALF_UP);
				System.out.println("getETHGasPrice:ethUsed:"+ethUsed);

				ethUsedSum = ethUsedSum.add(ethUsed);
			}
		}catch (Exception e){
			e.printStackTrace();
			return BigDecimal.ZERO;
		}
		log.info("getETHGasPrice:ethUsedSum:"+ethUsedSum.toString());

		String ETHpriceUrl = "https://api.etherscan.io/api?module=stats&action=ethprice&apikey=" + ethApiKeyToken;
		String ethPriceBody = HttpUtil.get(ETHpriceUrl);
		System.out.println("getETHGasPrice:ethPriceBody:"+ethPriceBody);
		JSONObject jsonObject = JSONObject.parseObject(ethPriceBody);
		String ethPriceStr = jsonObject.getJSONObject("result").getString("ethusd");
		System.out.println("getETHGasPrice:ethPrice:"+ethPriceStr);

		BigDecimal ethPrice = new BigDecimal(ethPriceStr);
		BigDecimal usdtPrice = ethUsedSum.multiply(ethPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
		System.out.println("getETHGasPrice:usdtPrice:"+usdtPrice);
		return usdtPrice;
	}


	public static int getW3STCount(String address) {
		int count = 0;
		try {
			JSONObject paramObj = new JSONObject();
			paramObj.put("query","query getCollectedEssencesByAddressEVM($address: AddressEVM!){\n" +
				"      address(address: $address) {\n" +
				"        wallet {\n" +
				"          collectedEssences(first: 4){\n" +
				"            edges{\n" +
				"              node{\n" +
				"                tokenID\n" +
				"                wallet{\n" +
				"                  address\n" +
				"                  \n" +
				"                }\n" +
				"                essence{\n" +
				"                  essenceID\n" +
				"                  name\n" +
				"                  tokenURI\n" +
				"                  createdBy{\n" +
				"                    profileID\n" +
				"                    handle\n" +
				"                  }\n" +
				"                }\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        }\n" +
				"      }\n" +
				"    }");
			JSONObject variables = new JSONObject();
			variables.put("address",address);
			paramObj.put("variables",variables);
			paramObj.put("operationName","getCollectedEssencesByAddressEVM");

			String param = paramObj.toJSONString();
			System.out.println("param:" + param);

			HttpRequest httpRequest = HttpRequest.post(CCTestUrl).body(param).timeout(timeout);
			String body = httpRequest.execute().body();

			System.out.println("getW3STCount:body:"+body);
			JSONObject jsonObject = JSONObject.parseObject(body);
			JSONArray edges = jsonObject.getJSONObject("data").getJSONObject("address")
				.getJSONObject("wallet").getJSONObject("collectedEssences").getJSONArray("edges");

			for (int i=0;i<edges.size();i++){
				JSONObject edgeObj = edges.getJSONObject(i);
				String name = edgeObj.getJSONObject("node").getJSONObject("essence").getString("name");
				System.out.println("getW3STCount:name:"+name);

				if("Web3 Status Token".equalsIgnoreCase(name)){
					count = count + 1;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getW3STCount:count:"+count);
		return count;
	}

	public static int getCCFollowers(String address) {
		int count = 0;
		try {
			//根据地址查询CC handle
			JSONObject paramObj = new JSONObject();
			paramObj.put("query","query getProfileByAddress($address: AddressEVM!) {\n" +
				"      address(address: $address) {\n" +
				"        wallet {\n" +
				"          profiles {\n" +
				"            edges {\n" +
				"              node {\n" +
				"                profileID\n" +
				"                handle\n" +
				"                avatar\n" +
				"                isPrimary\n" +
				"                metadataInfo {\n" +
				"                  avatar\n" +
				"                  displayName\n" +
				"                }\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"        }\n" +
				"      }\n" +
				"    }");
			JSONObject variables = new JSONObject();
			variables.put("address",address);
			paramObj.put("variables",variables);
			paramObj.put("operationName","getProfileByAddress");

			String param = paramObj.toJSONString();
			System.out.println("param:" + param);

			HttpRequest httpRequest = HttpRequest.post(CCTestUrl).body(param).timeout(timeout);
			String body = httpRequest.execute().body();

			System.out.println("getCCFollowers:body:"+body);
			JSONObject jsonObject = JSONObject.parseObject(body);
			JSONArray edges = jsonObject.getJSONObject("data").getJSONObject("address")
				.getJSONObject("wallet").getJSONObject("profiles").getJSONArray("edges");

			for (int i=0;i<edges.size();i++){
				JSONObject edgeObj = edges.getJSONObject(i);
				String handle = edgeObj.getJSONObject("node").getString("handle");
				System.out.println("getCCFollowers:handle:"+handle);
				count = count + getCCFollowersByHandle(handle,address);
			}
		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getCCFollowers:count:"+count);
		return count;
	}

	public static int getCCFollowersByHandle(String handle,String address) {
		int count = 0;
		try {
			//根据handle 查询cc follow relation
			JSONObject paramObj = new JSONObject();
			paramObj.put("query","query getFollowersByHandle($handle: String!, $me: AddressEVM!) {\n" +
				"      profileByHandle(handle: $handle) {\n" +
				"        followerCount\n" +
				"        isFollowedByMe(me: $me)\n" +
				"        followers {\n" +
				"          totalCount\n" +
				"          pageInfo {\n" +
				"            hasPreviousPage\n" +
				"            startCursor\n" +
				"            hasNextPage\n" +
				"          }\n" +
				"        }\n" +
				"      }\n" +
				"    }");
			JSONObject variables = new JSONObject();
			variables.put("handle",handle);
			variables.put("me",address);
			paramObj.put("variables",variables);
			paramObj.put("operationName","getFollowersByHandle");

			String param = paramObj.toJSONString();
			System.out.println("param:" + param);

			HttpRequest httpRequest = HttpRequest.post(CCTestUrl).body(param).timeout(timeout);
			String body = httpRequest.execute().body();

			System.out.println("getCCFollowersByHandle:body:"+body);
			JSONObject jsonObject = JSONObject.parseObject(body);
			count = jsonObject.getJSONObject("data").getJSONObject("profileByHandle")
				.getInteger("followerCount");

		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getCCFollowersByHandle:count:"+count);
		return count;
	}

	public static int getCCFollowing(String address) {
		int count = 0;
		try {
			//查询cc following
			JSONObject paramObj = new JSONObject();
			paramObj.put("query","query getFollowingsByAddressEVM($address: AddressEVM!, ) {\n" +
				"      address(address: $address) {\n" +
				"        followingCount\n" +
				"        followings {\n" +
				"          totalCount\n" +
				"          edges {\n" +
				"            node {\n" +
				"              address {\n" +
				"                address\n" +
				"              }\n" +
				"            }\n" +
				"          }\n" +
				"          pageInfo {\n" +
				"            hasPreviousPage\n" +
				"            startCursor\n" +
				"            hasNextPage\n" +
				"          }\n" +
				"        }\n" +
				"      }\n" +
				"    }");
			JSONObject variables = new JSONObject();
			variables.put("address",address);
			paramObj.put("variables",variables);
			paramObj.put("operationName","getFollowingsByAddressEVM");

			String param = paramObj.toJSONString();
			System.out.println("param:" + param);

			HttpRequest httpRequest = HttpRequest.post(CCTestUrl).body(param).timeout(timeout);
			String body = httpRequest.execute().body();

			System.out.println("getCCFollowing:body:"+body);
			JSONObject jsonObject = JSONObject.parseObject(body);
			count = jsonObject.getJSONObject("data").getJSONObject("address").getInteger("followingCount");

		}catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		log.info("getCCFollowing:count:"+count);
		return count;
	}
}
