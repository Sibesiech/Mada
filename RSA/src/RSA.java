import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.Random;

public class RSA {
    private Logger logger;

    private Random secureRandom = new SecureRandom();
    private int bitLength = 512;

    private BigInteger p, q, n, phi, e, d;

    public static void main(String[] args) {
        RSA rsa = new RSA();
        rsa.generateKeys();

    }

    public RSA() {
        logger = new Logger(true);
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

    // Erzeugen von zwei unterschiedlichen Primzahlen
    private void generatePrimes() {
        p = BigInteger.probablePrime(bitLength / 2, secureRandom);

        // Um sicher zu stellen, dass nicht zwei mal die gleiche Primzahl verwendet wird, erfolgt das Erzeugen der zweiten Primzahl in dieser Schleife.
        do {
            q = BigInteger.probablePrime(bitLength / 2, secureRandom);
        } while (p.equals(q));
        logger.log("Pime p: " + p);
        logger.log("Pime q: " + q);
    }

    // Berechnen von Phi und N basierend auf den ausgewählten Primzahlen.
    private void calculatePhiAndN() {
        n = p.multiply(q);
        logger.log("n: " + n);

        // (p - 1) * (q - 1) berechnen von phi ist einfach, da es sich ja um Primzahlen handeln sollte.
        phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        logger.log("phi: " + phi);
    }

    // Berechnen von e und d, der Vorgang kann mehrere Male wiederholt werden falls e nicht Teilerfremd zu phi ist.
    private void calculateEAndD() {
        ExtendedEuclideanAlgorithm eea;
        do {
            if (e == null){
                logger.log("Trying to find legit e and d");
            } else {
                logger.log("Repeating calculation of e and d as GCF was not 1");
            }

            e = new BigInteger(bitLength , secureRandom);
            logger.log("Set e: " + e);

            eea = new ExtendedEuclideanAlgorithm(phi, e);

            d = eea.getY0();
            logger.log("Calculated d: " + d);
            //falls sich ein negatives d ergibt, wird phi hinzugerechnet.
            if (d.compareTo(BigInteger.ZERO)<0){
                d = d.add(phi);
                logger.log("Added phi to negative d: " + d);
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
            Files.writeString(Path.of("./output", "sk.txt"), output, StandardOpenOption.CREATE);
            logger.log("wrote private key to file");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Schreiben des PublicKey in das File pk.txt
    private void writePublicKey() {
        String output = String.format("(%d,%d)", n, e);
        try {
            Files.writeString(Path.of("./output", "pk.txt"), output, StandardOpenOption.CREATE);
            logger.log("wrote public key to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
