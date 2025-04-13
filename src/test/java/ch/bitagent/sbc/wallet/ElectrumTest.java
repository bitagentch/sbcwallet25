package ch.bitagent.sbc.wallet;

import ch.bitagent.bitcoin.lib.helper.Bech32;
import ch.bitagent.bitcoin.lib.helper.Bytes;
import ch.bitagent.bitcoin.lib.helper.Hash;
import ch.bitagent.bitcoin.lib.network.Electrum;
import ch.bitagent.bitcoin.lib.wallet.Address;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ElectrumTest {

    private final Logger log = Logger.getLogger(ElectrumTest.class.getSimpleName());

    @Test
    void scriptHash() {
        var scriptPubkey = Bech32.decodeSegwit(Bip32Test.RECEIVE_ADDRESS);
        var hash = Hash.sha256(Bytes.hexStringToByteArray(scriptPubkey));
        var scriptHash = Bytes.byteArrayToHexString(Bytes.changeOrder(hash));
        assertEquals(scriptHash, Address.parse(Bip32Test.RECEIVE_ADDRESS).electrumScripthash());
        log.info(scriptHash);
    }

    @Test
    void getHistory() {
        var scriptHash = Address.parse(Bip32Test.RECEIVE_ADDRESS).electrumScripthash();
        var electrum = new Electrum();
        var history = electrum.getHistory(scriptHash);
        assertFalse(history.isEmpty());
        for (Object tx : history) {
            log.info(tx.toString());
        }
    }

    @Test
    void getBalance() {
        var scriptHash = Address.parse(Bip32Test.RECEIVE_ADDRESS).electrumScripthash();
        var electrum = new Electrum();
        var balance = electrum.getBalance(scriptHash);
        assertNotNull(balance);
        log.info(balance.toString());
    }

    @Test
    void listUnspent() {
        var scriptHash = Address.parse(Bip32Test.RECEIVE_ADDRESS).electrumScripthash();
        var electrum = new Electrum();
        var unspent = electrum.listUnspent(scriptHash);
        assertTrue(unspent.isEmpty());
        log.info(unspent.toString());
    }

    @Test
    void estimateFee() {
        var electrum = new Electrum();
        var fee = electrum.estimateFee(1);
        assertTrue(fee > 0);
        log.info(fee.toString());
    }
}
