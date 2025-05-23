package ch.bitagent.sbc.wallet;

import ch.bitagent.bitcoin.lib.ecc.Int;
import ch.bitagent.bitcoin.lib.helper.Properties;
import ch.bitagent.bitcoin.lib.network.Electrum;
import ch.bitagent.bitcoin.lib.script.Script;
import ch.bitagent.bitcoin.lib.tx.Tx;
import ch.bitagent.bitcoin.lib.tx.TxIn;
import ch.bitagent.bitcoin.lib.tx.TxOut;
import ch.bitagent.bitcoin.lib.wallet.Wallet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WalletTest {

    private final Logger log = Logger.getLogger(WalletTest.class.getSimpleName());

    @Test
    void createTransaction() {
        var mnemonic = Properties.getWalletMnemonic("wallet.properties", 0);
        var wallet = Wallet.parse(mnemonic, "", Wallet.PURPOSE_NATIVE_SEGWIT, Wallet.COIN_TYPE_BITCOIN, 0, 3);
        wallet.history(0);
        wallet.history(1);
        log.info(wallet.toString());
        assertFalse(wallet.getAddressList0().isEmpty());
        assertFalse(wallet.getAddressList1().isEmpty());
        if (wallet.getUtxoList().isEmpty()) {
            log.severe("no utxo's!");
            return;
        }
        assertFalse(wallet.getUtxoList().isEmpty());

        long txInAmount = wallet.getUtxoAmount();
        log.info(String.format("1. tx in amount %s", txInAmount));
        if (txInAmount == 0) {
            log.severe("no tx in amount!");
            return;
        }

        var receiveAddress = wallet.nextReceiveAddress();
        log.info(String.format("2. receive address %s", receiveAddress));
        var receiveAmount = txInAmount;
        var txOutReceive = new TxOut(Int.parse(receiveAmount), receiveAddress.scriptPubkey());
        // https://mempool.space/tx/cb194d430fc89c750888e1611a28e201d04c56347736c5aa75f05ff66a6ba854
        var txOutReturn = new TxOut(Int.parse(0), Script.opReturn("sbc25 nerd workshop"));

        var version = 2;
        var electrum = new Electrum();
        var locktime = electrum.height();
        List<TxIn> txInList = wallet.getTxInList();
        var tx = new Tx(Int.parse(version), txInList, List.of(txOutReceive, txOutReturn), Int.parse(locktime), false, true);

        Map<String, String> cache = new HashMap<>();
        wallet.txSignInput(tx, txInList, cache);
        log.info(String.format("3. tx size %svB", tx.sizeVirtualBytes()));

        // https://mempool.space
        var satsVB = electrum.estimateFee(1);
        log.info(String.format("4. network fee %ssats/vB", satsVB));
        var feeAmount = tx.sizeVirtualBytes() * satsVB;
        log.info(String.format("5. tx fee %s", feeAmount));
        receiveAmount = txInAmount - feeAmount;
        log.info(String.format("6. receive amount %s", receiveAmount));

        txOutReceive = new TxOut(Int.parse(receiveAmount), receiveAddress.scriptPubkey());
        tx = new Tx(Int.parse(version), txInList, List.of(txOutReceive, txOutReturn), Int.parse(locktime), false, true);
        wallet.txSignInput(tx, txInList, cache);
        log.info(tx.toString());

        log.info(String.format("7. broadcast transaction%n%s", tx.hexString()));
    }

    @Disabled(value = "manual")
    @Test
    void broadcastTransaction() {
        var electrum = new Electrum();
        var txId = electrum.broadcastTransaction("");
        log.info(txId);
        assertNotNull(txId);
    }
}
