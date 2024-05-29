package org.springblade.modules.admin.service;

import org.springblade.core.tool.api.R;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;

public interface MarketService {
	// listNFT
	R listNFT(Long tokenId);

	R cancelList(Long tokenId);

	R changeOwner(Long tokenId, String from, String to, String txHash);

	// pickItem
	R pickItem(String nftAddress, String address, BigInteger tokenId, BigInteger pickIndex, Log log);

	// dealList

	R dealList(String nftAddress, Long tokenId, String buyer, BigInteger buyer_index, BigInteger sellerAmount, BigInteger shareAmount, Log log);

	default R refundPick(BigInteger tokenId, BigInteger index, String buyr, Log log) {
		return R.success("refundPick");
	}
}
