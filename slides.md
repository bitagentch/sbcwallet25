---
marp: true
---

<!-- Global style -->
<style>
    html, section, div {
        background: orange;
    }
    section, span, li {
        color: black;
        clear: both;
    }
    pre {
        color: black;
        background: white;
        clear: both;
        font-family: monospace;
    }
    hr {
        background: #3468C0;
        clear: both;
    }
    h1, h2 {
        color: black;
        font-family: monospace;
    }
    a, a:hover {
        color: #3468C0;
        font-family: monospace;
        text-decoration: none;
    }
    blockquote {
        color: #3468C0;
        border-left: 0.25rem solid #3468C0;
        margin-left: 1.25rem;
    }
    img {
        float: right;
        margin: 0.25rem;
    }
</style>

[Swiss Bitcoin Conference](https://swiss-bitcoin-conference.com/) | 26. April 2025
## Nerd Academy
# Bitcoin Wallet Entwicklung
[beat@bitagent.ch](mailto:beat@bitagent.ch) | [bitagent.ch](https://bitagent.ch) | [github.com/bitagentch/sbcwallet25](https://github.com/bitagentch/sbcwallet25)

---
# Bitcoin Wallet Entwicklung
1. bitcoinjavalib
2. BIP39 - Mnemonischer Code zur Erzeugung deterministischer Schlüssel
3. BIP32 - Hierarchisch deterministische Wallets
4. Electrum Protocol
5. Wallet Transaktion
6. Bitagent Wallet Demo

---
# 1. [bitcoinjavalib](https://github.com/bitagentch/bitcoinjavalib) (1/1)
- Open Source (MIT)
- Basiert auf dem Buch [Programming Bitcoin](https://github.com/jimmysong/programmingbitcoin) von Jimmy Song, [Cover](https://github.com/jimmysong/programmingbitcoin/blob/master/images/cover.png)
- 100% Java
- Nur zwei Dependencies (Json, Test)

---
# 2. [BIP39](https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki) - Mnemonischer Code zur Erzeugung deterministischer Schlüssel (1/6)
> Dieses BIP beschreibt die Implementierung eines mnemonischen Codes oder eines mnemonischen Satzes - eine Gruppe von leicht zu merkenden Wörtern - für die Erzeugung von deterministischen Wallets.

> Es besteht aus zwei Teilen: der Generierung der Mnemonik (1) und ihrer Umwandlung in einen binären Seed (2). 
Dieser Seed kann später verwendet werden, um mit BIP32 oder ähnlichen Methoden deterministische Wallets zu erzeugen.

---
# 2.1 BIP39 - Entropie erzeugen (2/6)
> Die zulässige Grösse der Entropie beträgt 128-256 bit.

> Die Mnemonik muss die Entropie in einem Vielfachen von 32 bit kodieren.
- Mögliche Entropielängen in bit: 128, 160, 192, 224 oder 256
- Mögliche Entropielängen in byte: 16, 20, 24, 28 oder 32
> Zunächst wird eine Anfangsentropie von Entropie-Bits erzeugt.
```
Bip39Test.generateEntropy()
```

---
# 2.2 BIP39 - Entropie Checksumme anhängen (3/6)
> Eine Checksumme wird aus den ersten Entropie/32 bit des SHA256-Hashes gebildet.
- Daraus ergeben sich Checkssummen mit einer Länge von 4, 5, 6, 7 oder 8 bit
> Diese Checksumme wird an das Ende der ursprünglichen Entropie angehängt.
- Daraus ergeben sich Entropielängen von 132, 165, 198, 231 oder 266 bit
- Alle diese Entropielängen sind durch 11 teilbar

---
# 2.3 BIP39 - Mnemonischer Satz bestimmen (4/6)
> Anschliessend werden diese verketteten Bits in Gruppen von 11 Bits aufgeteilt, die jeweils eine Zahl von 0-2047 kodieren und als Index für eine Wortliste dienen.
- 2^11 = 2048
> Schliesslich wandeln wir diese Zahlen in Wörter um und verwenden die zusammengesetzten Wörter als mnemonischen Satz.
- Der mnemonische Satz besteht aus 12, 15, 18 , 21 oder 24 Wörtern
```
Bip39Test.entropyToMnemonic()
```

---
# 2.4 BIP39 - Passphrase (5/6)
> Ein Benutzer kann beschliessen, seinen mnemonischen Satz mit einer Passphrase zu schützen.
Wenn keine Passphrase vorhanden ist, wird stattdessen eine leere Zeichenkette "" verwendet.

---
# 2.5 BIP39 - Binären Seed erzeugen (6/6)
> Um einen binären Seed aus dem mnemonischen Satz zu erzeugen, verwenden wir die PBKDF2-Funktion (1) mit 
dem mnemonischen Satz als Passwort (2) und der
Zeichenfolge "mnemonic" + Passphrase als Salt (3).
Die Iterationszahl wird auf 2048 (4) gesetzt und 
HMAC-SHA512 wird als Pseudozufallsfunktion (5) verwendet.
Die Länge des abgeleiteten Schlüssels beträgt 512 bit (= 64 byte).
```
Bip39Test.mnemonicToSeed()
```
- (1) [PBKDF2](https://de.wikipedia.org/wiki/PBKDF2) (Password-Based Key Derivation Function 2)
- (5) [HMAC](https://de.wikipedia.org/wiki/HMAC) (Hash-based Message Authentication Code)

---
# 3. [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki) - Hierarchisch deterministische Wallets (1/6)
> Dieses Dokument beschreibt hierarchische deterministische Wallets (oder „HD Wallets“): Wallets, die teilweise oder ganz mit verschiedenen Systemen geteilt werden können, jedes mit oder ohne die Möglichkeit, Münzen auszugeben.

> Die Spezifikation besteht aus zwei Teilen.
Im ersten Teil wird ein System zur Ableitung eines Baums von Schlüsselpaaren aus einem einzigen Seed vorgestellt (1).
Im zweiten Teil wird gezeigt, wie eine Wallet-Struktur auf einem solchen Baum aufgebaut werden kann (2).

---
# 3.1 [BIP44](https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki) Multi-Account-Hierarchie für deterministische Wallets (2/6)
> Dieses BIP definiert eine logische Hierarchie für deterministische Walltes auf der Grundlage eines in BIP32 beschriebenen Algorithmus. 
```
m / purpose' / coin_type' / account' / change / address_index
```
- m: Extended Master Key (Private/Public)
- purpose: 84 (Native Segwit), 86 (Taproot)
- coin_type: 0 Bitcoin, 1 Bitcoin Testnet
- account: 0 .. n
- change: 0 external chain (receive), 1 internal chain (change)
- address_index: 0 .. gap_limit (20)

---
# 3.2 [BIP84](https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki) - Ableitungsschema für Native Segwit basierte Konten (3/6)
> Dieses BIP definiert das Ableitungsschema für HD Wallets, die das Serialisierungsformat P2WPKH ([BIP173](https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki)) für Transaktionen mit Segregated Witness (Native Segwit) verwenden.

- Vier Version Bytes
> Extended Public Keys verwenden 0x04b24746, um einen "zpub" Präfix zu erzeugen,
und Extended Private Keys verwenden 0x04b2430c, um einen "zprv" Präfix zu erzeugen.

---
# 3.3 BIP32 - Extended Master Key (4/6)
> Die Hauptschlüssel werden nicht direkt erzeugt, sondern aus einem potenziell kurzen Seed-Wert.

> Erzeugen Sie einen binären Seed mit einer beliebigen Länge (zwischen 128 und 512 bit; 256 bit werden empfohlen)
- Unser binärer Seed aus BIP39 hat 512 bit / 64 byte
> HMAC-SHA512(Key = "Bitcoin seed", Data = Seed)
- Wir verwenden die Version Bytes aus BIP84 für eine Native Segwit Wallet
```
Bip32Test.seedToExtendedKey()
```

---
# 3.4 BIP32 - Funktionen zur Ableitung von Child Keys (5/6)
> Ausgehend von einem Extended Parent Key und einem Index i kann der entsprechende Extended Child Key berechnet werden. Der entsprechende Algorithmus hängt davon ab, ob es sich bei dem untergeordneten Schlüssel um einen gehärteten Schlüssel handelt oder nicht (oder, äquivalent, ob i ≥ 2^31) und ob es sich um einen privaten oder öffentlichen Schlüssel handelt.

- Private Keys (gehärtet, 2^31 - 2^32-1, ≥ 0x80000000)
- Public Keys (normal, 0 - 2^31-1, < 0x80000000)
- Neutralisieren: Private parent key → Public child key

---
# 3.5 BIP84 - Account 0 (6/6)
> Account 0, root = m/84'/0'/0'

> Account 0, first receiving address = m/84'/0'/0'/0/0

> Account 0, first change address = m/84'/0'/0'/1/0
```
Bip32Test.bip84Account0()
```

---
# 4. [Electrum Protokoll](https://electrum-protocol.readthedocs.io/en/latest/) (1/7)
> Das Electrum-Protokoll ist ein Client/Server JSON-RPC-Protokoll.

> Der primäre Anwendungsfall ist eine leichte, eigenverwahrende Bitcoin-Wallet, die ihre Onchain-Transaktionshistorie verfolgt.

---
# 4.1 [Script Hashes](https://electrum-protocol.readthedocs.io/en/latest/protocol-basics.html#script-hashes) (2/7)
> Ein Skript-Hash ist der Hash der binären Bytes des Locking Scripts (ScriptPubKey), ausgedrückt als hexadezimale Zeichenfolge.
```
ElectrumTest.scriptHash()
```

---
# 4.2 [blockchain.scripthash.get_history](https://electrum-protocol.readthedocs.io/en/latest/protocol-methods.html#blockchain.scripthash.get_history) (3/7)
> Gibt die bestätigte und unbestätigte Transaktionshistorie eines Skript-Hashes zurück.
```
ElectrumTest.getHistory()
```

---
# 4.3 [blockchain.scripthash.get_balance](https://electrum-protocol.readthedocs.io/en/latest/protocol-methods.html#blockchain.scripthash.get_balance) (4/7)
> Gibt die bestätigten und unbestätigten Salden eines Script-Hashes zurück.
```
ElectrumTest.getBalance()
```

---
# 4.4 [blockchain.scripthash.listunspent](https://electrum-protocol.readthedocs.io/en/latest/protocol-methods.html#blockchain.scripthash.listunspent) (5/7)
> Gibt eine geordnete Liste von UTXOs zurück, die an einen Skript-Hash gesendet wurden.
```
ElectrumTest.listUnspent()
```

---
# 4.5 [blockchain.estimatefee](https://electrum-protocol.readthedocs.io/en/latest/protocol-methods.html#blockchain.estimatefee) (6/7)
> Gibt die geschätzte Transaktionsgebühr pro Kilobyte für eine Transaktion zurück, die innerhalb einer bestimmten Anzahl von Blöcken bestätigt werden muss.
```
ElectrumTest.estimateFee()
```

---
# 4.6 [blockchain.transaction.broadcast](https://electrum-protocol.readthedocs.io/en/latest/protocol-methods.html#blockchain.transaction.broadcast) (7/7)
> Senden einer Transaktion an das Netzwerk.

---
# 5. Wallet Transaktion (1/1)
- Transaktion erstellen
```
WalletTest.createTransaction()
```

- Transaktion senden
```
WalletTest.broadcastTransaction()
```

---
# 6. Bitagent Wallet Demo (1/1)
- [bitagent.ch](https://bitagent.ch)
