package org.springblade.modules.admin.service;

import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.pojo.po.PFPTransactionPO;
import org.springblade.modules.admin.pojo.query.CollectCreateOrderQuery;
import org.springblade.modules.admin.pojo.query.CollectNFTQuery;
import org.springblade.modules.admin.pojo.vo.CheckPickNftVo;
import org.springblade.modules.admin.pojo.vo.MintNftVo;
import org.springblade.modules.admin.pojo.vo.PickNftVo;

public interface NftService {

	R mintFreeNft(MintNftVo mintNftVo);

    R collectNFT(CollectNFTQuery collectNFTQuery);

    R checkApprove(Long tokenId, Long userId);

	PFPTransactionPO getLastTransaction(Long tokenId);

	R collectNFTOnline(CollectNFTQuery collectNFTQuery) throws Exception;

    R collectCreateOrder(CollectCreateOrderQuery collectCreateOrderQuery);

	R transferNFT(PFPTransactionPO x);

	R frontMintFreeNft(MintNftVo mintNftVo);

	R prePickNFT(CheckPickNftVo checkPickNftVo);

	R pickNFT(PickNftVo pickNftVo) throws Exception;

	void batchCalcNftSoul();
}
