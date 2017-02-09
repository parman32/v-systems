package scorex.transaction.state.database.state.extension

import scorex.settings.ChainParameters
import scorex.transaction.assets.exchange.ExchangeTransaction
import scorex.transaction.assets.{BurnTransaction, IssueTransaction, ReissueTransaction, TransferTransaction}
import scorex.transaction.lease.{LeaseCancelTransaction, LeaseTransaction}
import scorex.transaction.state.database.blockchain.StoredState
import scorex.transaction.{GenesisTransaction, PaymentTransaction, Transaction}

class ActivatedValidator(settings: ChainParameters) extends StateExtension {


  override def isValid(storedState: StoredState, tx: Transaction, height: Int): Boolean = tx match {
    case tx: PaymentTransaction => true
    case gtx: GenesisTransaction => true
    case tx: TransferTransaction => true
    case tx: IssueTransaction => true
    case tx: ReissueTransaction => true
    case tx: BurnTransaction => tx.timestamp > settings.allowBurnTransactionAfterTimestamp
    case tx: ExchangeTransaction => true
    case tx: LeaseTransaction => true
    case tx: LeaseCancelTransaction => true
    case _ => false
  }


  override def process(storedState: StoredState, tx: Transaction, blockTs: Long, height: Int): Unit = {}
}
