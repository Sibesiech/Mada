import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

public class RSA {

    private Random secureRandom = new SecureRandom();
    private int bitLength = 512;

    private BigInteger p, q, n, phi, e, d;

    public static void main(String[] args) {
        RSA rsa = new RSA();
//        rsa.generateKeys();
        rsa.encodeFile();
        rsa.decodeFile();
    }


    // Aufgabe 1
    private void generateKeys() {

        // Aufgabe 1.a Generieren der Primzahlen
        generatePrimes();

        // Aufgabe 1.a Berechnen von phi und n für nachfolgende Berechnungen
        calculatePhiAndN();

        // Aufgabe 1.b berechnen von e und d mit erweitertem euklidischem Algorithmus
        calculateEAndD();

        // Aufgabe 1.c schreiben der Schlüssel in entsprechende Files
        writeKeysToFiles();
    }

    // Aufgabe 2
    private void encodeFile() {

        // Aufgabe 2.a Einlesen des PublicKey
        String publicKey = readPublicKeyFromFile();

        // Aufgabe 2.b Einlesen der Nachricht als bytes, was einen ASCII Wert von 0 bis 127 repräsentiert.
        byte[] message = readMessageFromFile();

        // Aufgabe 2.c Verschlüsseln der Nachricht
        ArrayList<BigInteger> encodedMessage = encodeMessageWithPublicKey(message, publicKey);

        // Aufgabe 2.d Ausgeben der verschlüsselten Nachricht als File
        writeEncodedMessageToFile(encodedMessage);
    }

    // Aufgabe 3
    private void decodeFile() {
        // Einlesen der verschlüsselten Nachricht
        String encodedMessage = readEncodedMessageFromFile();

        // Einlesen des privateKeys
        String privateKeyString = readPrivateKeyFromFile();

        // Entschlüsseln der Nachricht
        String decodedMessage = decodeMessageWithPrivateKey(encodedMessage, privateKeyString);

        // Schreibe die entschlüsselte Nachricht ins File
        writeDecodedMessageToFile(decodedMessage);
    }


    // Erzeugen von zwei unterschiedlichen Primzahlen
    private void generatePrimes() {
        p = BigInteger.probablePrime(bitLength / 2, secureRandom);

        // Um sicher zu stellen, dass nicht zwei mal die gleiche Primzahl verwendet wird, erfolgt das Erzeugen der zweiten Primzahl in dieser Schleife.
        do {
            q = BigInteger.probablePrime(bitLength / 2, secureRandom);
        } while (p.equals(q));

    }

    // Berechnen von Phi und N basierend auf den ausgewählten Primzahlen.
    private void calculatePhiAndN() {
        n = p.multiply(q);

        // (p - 1) * (q - 1) berechnen von phi ist einfach, da es sich ja um Primzahlen handeln sollte.
        phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
    }

    // Berechnen von e und d, der Vorgang kann mehrere Male wiederholt werden falls e nicht Teilerfremd zu phi ist.
    private void calculateEAndD() {
        ExtendedEuclideanAlgorithm eea;
        do {

            e = new BigInteger(bitLength, secureRandom);

            eea = new ExtendedEuclideanAlgorithm(phi, e);

            d = eea.getY0();
            //falls sich ein negatives d ergibt, wird phi hinzugerechnet.
            if (d.compareTo(BigInteger.ZERO) < 0) {
                d = d.add(phi);
            }

        } while (!eea.getA().equals(BigInteger.ONE));
    }

    // Schreiben von Pubilc- und PrivateKey in entsprechende Files
    private void writeKeysToFiles() {
        writePrivatKey();
        writePublicKey();
    }


    // Schreiben des PrivateKey in das File sk.txt
    private void writePrivatKey() {
        String output = String.format("(%d,%d)", n, d);
        try {
            Files.writeString(Path.of("./output", "sk.txt"), output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Schreiben des PublicKey in das File pk.txt
    private void writePublicKey() {
        String output = String.format("(%d,%d)", n, e);
        try {
            Files.writeString(Path.of("./output", "pk.txt"), output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Verschlüsseln der Nachricht basierend auf dem PublicKey
    private ArrayList<BigInteger> encodeMessageWithPublicKey(byte[] message, String publicKeyString) {
        // Sicherheitshalber Leerschläge entfernen.
        String strippedPublicKey = publicKeyString.stripLeading().stripTrailing();
        // Entfernen der Klammern sowie splitten von n und e
        String[] splitStrings = strippedPublicKey.substring(1, strippedPublicKey.length() - 1).split(",");

        BigInteger n = new BigInteger(splitStrings[0]);
        BigInteger e = new BigInteger(splitStrings[1]);

        ArrayList<BigInteger> encodedMessage = new ArrayList<>();
        for (byte b : message) {
            //leider war ich Zeitlich nicht mehr in der Lage die schnellen Exponentation selber zu implementieren.
            encodedMessage.add(fastExponentiation(BigInteger.valueOf(b), e, n));
        }
        return encodedMessage;
    }


    // Einlesen der Nachricht als Byte Array ein Byte repräsentiert automatisch einen Wert zwischen 0 und 127 und bildet ASCII somit ab.
    private byte[] readMessageFromFile() {
        byte[] messageAsByteArray = null;
        try {
            messageAsByteArray = Files.readAllBytes(Path.of("./input/text.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messageAsByteArray;
    }

    // Einlesen des PublicKeys vorerst als String. Dieser wird später umgewandelt und n und e extrahiert.
    private String readPublicKeyFromFile() {
        String publicKeyString = null;
        try {
            publicKeyString = Files.readString(Path.of("./input/pk.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return publicKeyString;
    }

    // Speichern der verschlüsselten Nachricht als File
    private void writeEncodedMessageToFile(ArrayList<BigInteger> encodedMessage) {
        // Zusammenbauen der verschlüsselten Nachricht auf Basis der BigInts
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < encodedMessage.size(); i++) {
            output.append(encodedMessage.get(i).toString());
            if (i != encodedMessage.size() - 1) {
                output.append(",");
            }
        }
        try {
            Files.writeString(Path.of("./output", "chiffre.txt"), output.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Einlesen der verschlüsselten Nachricht
    private String readEncodedMessageFromFile() {
        String encodedMessage = "";
        try {
            encodedMessage = Files.readString(Path.of("./input", "chiffre.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedMessage;
    }

    // Einlesen des privaten Schlüssels
    private String readPrivateKeyFromFile() {
        String publicKeyString = null;
        try {
            publicKeyString = Files.readString(Path.of("./input", "sk.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return publicKeyString;
    }

    private String decodeMessageWithPrivateKey(String encodedMessage, String privateKeyString) {
        // Sicherheitshalber Leerschläge entfernen.
        String strippedPublicKey = privateKeyString.stripLeading().stripTrailing();
        // Entfernen der Klammern sowie splitten von n und e
        String[] splitStrings = strippedPublicKey.substring(1, strippedPublicKey.length() - 1).split(",");

        BigInteger n = new BigInteger(splitStrings[0]);
        BigInteger d = new BigInteger(splitStrings[1]);

        String[] encodedBytes = encodedMessage.split(",");
        StringBuilder decodedMessage = new StringBuilder();
        for (String encodedByteString : encodedBytes) {
            // Entschlüsseln der einzelnen bytes und zusammenfügen in der entschlüsselten Nachricht.
            decodedMessage.append(Character.toString(fastExponentiation(new BigInteger(encodedByteString), d, n).byteValue()));
        }
        return decodedMessage.toString();
    }

    // Speichern der entschlüsselten Nachricht
    private void writeDecodedMessageToFile(String decodedMessage) {
        try {
            Files.writeString(Path.of("./output", "text-d.txt"), decodedMessage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // x^e mod m Methode für die schnelle Exponentation
    private BigInteger fastExponentiation(BigInteger base, BigInteger exponent, BigInteger modulo) {
        BigInteger h = BigInteger.ONE;
        BigInteger k = base;

        // Um denn Exponenten durchlaufen zu können wird er hier als String abgebildet.
        String exponentAsBinary = exponent.toString(2);
        int i = exponentAsBinary.length() - 1;

        while (i >= 0) {
            // Gemäss dem Algorithmus wird bei Bits die 1 sind das Multiplizieren sowie Modulo durchgeführt
            if (exponentAsBinary.charAt(i) == '1') {
                h = h.multiply(k).mod(modulo);
            }
            k = k.modPow(BigInteger.TWO, modulo);
            i--;
        }
        return h;
    }
}
