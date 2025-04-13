package ch.bitagent.sbc.wallet;

import ch.bitagent.bitcoin.lib.helper.Bytes;
import ch.bitagent.bitcoin.lib.wallet.MnemonicSentence;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Bip39Test {

    private final Logger log = Logger.getLogger(Bip39Test.class.getSimpleName());

    @Test
    void generateEntropy() {
        byte[] entropy = MnemonicSentence.generateEntropy(128);
        assertEquals(16, entropy.length); // 128 / 8
        String entropyHex = Bytes.byteArrayToHexString(entropy);
        log.info(entropyHex);
        assertEquals(32, entropyHex.length()); // 16 * 2
    }

    @Test
    void entropyToMnemonic() {
        byte[] entropy = MnemonicSentence.generateEntropy(128);
        String mnemonicSentence = MnemonicSentence.entropyToMnemonic(entropy);
        log.info(mnemonicSentence);
        assertEquals(12, mnemonicSentence.split(" ").length);
    }

    @Test
    void  mnemonicToSeed() {
        byte[] entropy = MnemonicSentence.generateEntropy(128);
        String mnemonicSentence = MnemonicSentence.entropyToMnemonic(entropy);
        String passphrase = "bitagent";
        byte[] seed = MnemonicSentence.mnemonicToSeed(mnemonicSentence, passphrase);
        assertEquals(64, seed.length);
        String seedHex = Bytes.byteArrayToHexString(seed);
        log.info(seedHex);
        assertEquals(128, seedHex.length()); // 64 * 2
    }
}
