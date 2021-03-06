package scorex.transaction

import com.google.common.base.Charsets
import com.wavesplatform.state2.ByteStr
import scorex.account._
import scorex.api.http.alias.CreateAliasRequest
import scorex.api.http.assets._
import vsys.api.http.contract.{ChangeContractStatusRequest, CreateContractRequest, SignedChangeContractStatusRequest}
import vsys.api.http.database.DbPutRequest
import scorex.api.http.leasing.{LeaseCancelRequest, LeaseRequest}
import vsys.api.http.spos.{ContendSlotsRequest, ReleaseSlotsRequest}
import vsys.contract.Contract
import scorex.crypto.encode.Base58
import scorex.transaction.assets._
import vsys.transaction.spos.{ContendSlotsTransaction, ReleaseSlotsTransaction}
import vsys.transaction.contract.{ChangeContractStatusAction, ChangeContractStatusTransaction, CreateContractTransaction}
import vsys.transaction.database.DbPutTransaction
import scorex.transaction.lease.{LeaseCancelTransaction, LeaseTransaction}
import scorex.utils.Time
import vsys.wallet.Wallet

object TransactionFactory {

  def createPayment(request: PaymentRequest, wallet: Wallet, time: Time): Either[ValidationError, PaymentTransaction] = for {
    publicKey <- wallet.findPrivateKey(request.sender)
    recipient <- Address.fromString(request.recipient)
    tx <- PaymentTransaction
      .create(
        publicKey,
        recipient,
        request.amount,
        request.fee,
        request.feeScale,
        time.getTimestamp(),
        request.attachment.filter(_.nonEmpty).map(Base58.decode(_).get).getOrElse(Array.emptyByteArray))
  } yield tx


  def transferAsset(request: TransferRequest, wallet: Wallet, time: Time): Either[ValidationError, TransferTransaction] =
    for {
      senderPrivateKey <- wallet.findPrivateKey(request.sender)
      recipientAcc <- AddressOrAlias.fromString(request.recipient)
      tx <- TransferTransaction
        .create(request.assetId.map(s => ByteStr.decodeBase58(s).get),
          senderPrivateKey,
          recipientAcc,
          request.amount,
          time.getTimestamp(),
          request.feeAssetId.map(s => ByteStr.decodeBase58(s).get),
          request.fee,
          request.attachment.filter(_.nonEmpty).map(Base58.decode(_).get).getOrElse(Array.emptyByteArray))
    } yield tx

  def issueAsset(request: IssueRequest, wallet: Wallet, time: Time): Either[ValidationError, IssueTransaction] =
    for {
      senderPrivateKey <- wallet.findPrivateKey(request.sender)
      tx <- IssueTransaction.create(senderPrivateKey,
        request.name.getBytes(Charsets.UTF_8),
        request.description.getBytes(Charsets.UTF_8),
        request.quantity, request.decimals, request.reissuable, request.fee, time.getTimestamp())
    } yield tx

  def lease(request: LeaseRequest, wallet: Wallet, time: Time): Either[ValidationError, LeaseTransaction] = for {
    senderPrivateKey <- wallet.findPrivateKey(request.sender)
    recipientAcc <- AddressOrAlias.fromString(request.recipient)
    tx <- LeaseTransaction.create(senderPrivateKey, request.amount, request.fee, request.feeScale, time.getTimestamp(), recipientAcc)
  } yield tx

  def leaseCancel(request: LeaseCancelRequest, wallet: Wallet, time: Time): Either[ValidationError, LeaseCancelTransaction] =
    for {
      pk <- wallet.findPrivateKey(request.sender)
      tx <- LeaseCancelTransaction.create(pk, ByteStr.decodeBase58(request.txId).get, request.fee, request.feeScale, time.getTimestamp())
    } yield tx

  def alias(request: CreateAliasRequest, wallet: Wallet, time: Time): Either[ValidationError, CreateAliasTransaction] = for {
    senderPrivateKey <- wallet.findPrivateKey(request.sender)
    alias <- Alias.buildWithCurrentNetworkByte(request.alias)
    tx <- CreateAliasTransaction.create(senderPrivateKey, alias, request.fee, time.getTimestamp())
  } yield tx

  def contendSlots(request: ContendSlotsRequest, wallet:Wallet, time: Time): Either[ValidationError, ContendSlotsTransaction] = for {
    senderPrivateKey <- wallet.findPrivateKey(request.sender)
    tx <- ContendSlotsTransaction.create(senderPrivateKey, request.slotId, request.fee, request.feeScale, time.getTimestamp())
  } yield tx

  def releaseSlots(request: ReleaseSlotsRequest, wallet:Wallet, time: Time): Either[ValidationError, ReleaseSlotsTransaction] = for {
    senderPrivateKey <- wallet.findPrivateKey(request.sender)
    tx <- ReleaseSlotsTransaction.create(senderPrivateKey, request.slotId, request.fee, request.feeScale, time.getTimestamp())
  } yield tx
    
  def createContract(request: CreateContractRequest, wallet: Wallet, time: Time): Either[ValidationError, CreateContractTransaction] = for {
    senderPrivateKey <- wallet.findPrivateKey(request.sender)
    contract <- Contract.buildContract(request.content, request.name, true)
    tx <- CreateContractTransaction.create(senderPrivateKey, contract, request.fee, request.feeScale, time.getTimestamp())
  } yield tx

  def changeContractStatus(request: ChangeContractStatusRequest, action: ChangeContractStatusAction.Value, wallet: Wallet, time: Time): Either[ValidationError, ChangeContractStatusTransaction] = for {
    senderPrivateKey <- wallet.findPrivateKey(request.sender)
    tx <- ChangeContractStatusTransaction.create(senderPrivateKey, request.contractName, action, request.fee, request.feeScale, time.getTimestamp())
  } yield tx

  def broadcastChangeContractStatus(request: SignedChangeContractStatusRequest, action: ChangeContractStatusAction.Value): Either[ValidationError, ChangeContractStatusTransaction] = for {
    _signature <- ByteStr.decodeBase58(request.signature).toOption.toRight(ValidationError.InvalidRequestSignature)
    _sender <- PublicKeyAccount.fromBase58String(request.senderPublicKey)
    tx <- ChangeContractStatusTransaction.create(_sender, request.contractName, action, request.fee, request.feeScale, request.timestamp, _signature)
  } yield tx

  def dbPut(request: DbPutRequest, wallet: Wallet, time: Time): Either[ValidationError, DbPutTransaction] = for {
    senderPrivateKey <- wallet.findPrivateKey(request.sender)
    tx <- DbPutTransaction.create(senderPrivateKey, request.dbKey, request.dataType, request.data, request.fee, request.feeScale, time.getTimestamp())
  } yield tx

  def reissueAsset(request: ReissueRequest, wallet: Wallet, time: Time): Either[ValidationError, ReissueTransaction] = for {
    pk <- wallet.findPrivateKey(request.sender)
    tx <- ReissueTransaction.create(pk, ByteStr.decodeBase58(request.assetId).get, request.quantity, request.reissuable, request.fee, time.getTimestamp())
  } yield tx

  def burnAsset(request: BurnRequest, wallet: Wallet, time: Time): Either[ValidationError, BurnTransaction] = for {
    pk <- wallet.findPrivateKey(request.sender)
    tx <- BurnTransaction.create(pk, ByteStr.decodeBase58(request.assetId).get, request.quantity, request.fee, time.getTimestamp())
  } yield tx

}
