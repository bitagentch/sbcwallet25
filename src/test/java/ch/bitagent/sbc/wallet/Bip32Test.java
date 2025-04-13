package ch.bitagent.sbc.wallet;

import ch.bitagent.bitcoin.lib.ecc.S256Point;
import ch.bitagent.bitcoin.lib.wallet.ExtendedKey;
import ch.bitagent.bitcoin.lib.wallet.MnemonicSentence;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Bip32Test {

    private final Logger log = Logger.getLogger(Bip32Test.class.getSimpleName());

    // https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki#test-vectors
    private static final String MNEMONIC_SENTENCE = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
    public static final String RECEIVE_ADDRESS = "bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu";

    @Test
    void seedToExtendedKey() {
        byte[] seed = MnemonicSentence.mnemonicToSeed(MNEMONIC_SENTENCE, "");

        String extendedPrivateKey = MnemonicSentence.seedToExtendedKey(seed, ExtendedKey.PREFIX_ZPRV); // BIP84
        log.info(extendedPrivateKey);
        assertEquals("zprvAWgYBBk7JR8Gjrh4UJQ2uJdG1r3WNRRfURiABBE3RvMXYSrRJL62XuezvGdPvG6GFBZduosCc1YP5wixPox7zhZLfiUm8aunE96BBa4Kei5", extendedPrivateKey);

        String extendedPublicKey = MnemonicSentence.seedToExtendedKey(seed, ExtendedKey.PREFIX_ZPUB); // BIP84
        log.info(extendedPublicKey);
        assertEquals("zpub6jftahH18ngZxLmXaKw3GSZzZsszmt9WqedkyZdezFtWRFBZqsQH5hyUmb4pCEeZGmVfQuP5bedXTB8is6fTv19U1GQRyQUKQGUTzyHACMF", extendedPublicKey);
    }

    @Test
    void bip84Account0() {
        byte[] seed = MnemonicSentence.mnemonicToSeed(MNEMONIC_SENTENCE, "");
        var extendedPrivateKey = ExtendedKey.parse(MnemonicSentence.seedToExtendedKey(seed, ExtendedKey.PREFIX_ZPRV));

        // Account 0, root = m/84'/0'/0'
        var zprv = extendedPrivateKey
                .derive(84, true, false)
                .derive(0, true, false)
                .derive(0, true, false);
        var zprvString = zprv.serialize(false);
        log.info(zprvString);
        assertEquals("zprvAdG4iTXWBoARxkkzNpNh8r6Qag3irQB8PzEMkAFeTRXxHpbF9z4QgEvBRmfvqWvGp42t42nvgGpNgYSJA9iefm1yYNZKEm7z6qUWCroSQnE", zprvString);

        var zpub = extendedPrivateKey
                .derive(84, true, false)
                .derive(0, true, false)
                .derive(0, true, true);
        var zpubString = zpub.serialize(false);
        log.info(zpubString);
        assertEquals("zpub6rFR7y4Q2AijBEqTUquhVz398htDFrtymD9xYYfG1m4wAcvPhXNfE3EfH1r1ADqtfSdVCToUG868RvUUkgDKf31mGDtKsAYz2oz2AGutZYs", zpubString);

        assertEquals(zpubString, zprv.serialize(true));

        // Account 0, first receiving address = m/84'/0'/0'/0/0
        var receivePubkey = zpub.derive(0).derive(0);
        var receiveAddress = S256Point.parse(receivePubkey.getKey()).addressBech32P2wpkh(false);
        log.info(receiveAddress);
        assertEquals(RECEIVE_ADDRESS, receiveAddress);

        // Account 0, first change address = m/84'/0'/0'/1/0
        var changePubkey = zpub.derive(1).derive(0);
        var changeAddress = S256Point.parse(changePubkey.getKey()).addressBech32P2wpkh(false);
        log.info(changeAddress);
        assertEquals("bc1q8c6fshw2dlwun7ekn9qwf37cu2rn755upcp6el", changeAddress);
    }
}
