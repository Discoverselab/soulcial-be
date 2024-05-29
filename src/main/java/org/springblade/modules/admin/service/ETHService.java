package org.springblade.modules.admin.service;

import org.springblade.core.tool.api.R;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface ETHService {

	String createAdminWallet();

	R<String> mintNFT(String adminAddress, String contractAddress, String toAddress, Long chainId, Long tokenId);

//	Boolean testApprove(String txid) throws Exception;

    R checkApprove(Long tokenId, String address);

	R<Boolean> checkBNBTransacation(String txn, BigDecimal price,String fromAddress,String toAddress);

	R<String> approveTransferNFT(String fromAddress,String toAddress, Long tokenId);

	Boolean checkNFTOwner(String toAddress, Long tokenId);

	R<String> transferBNB(String ownerAddress, BigDecimal sellerEarnPrice);

	R<String> wEthTransferFrom(String fromAddress, String toAddress, BigDecimal amount);

	BigInteger getWEthAllowance(String fromAddress);

	R<Boolean> checkWBNBTransacation(String payTxn, BigDecimal price, String address, String adminAddress);

	Long getLastMainBlockHeight();

	R rewardNFT(Long pickId);

	Long getLastBlockHeight();
}
