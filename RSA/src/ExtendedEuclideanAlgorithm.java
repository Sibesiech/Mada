import java.math.BigInteger;

public class ExtendedEuclideanAlgorithm {
    private int a = 0;
    private int b = 1;
    private int x0 = 2;
    private int y0 = 3;
    private int x1 = 4;
    private int y1 = 5;
    private int q = 6;
    private int r = 7;

    private final BigInteger[] row;

    public ExtendedEuclideanAlgorithm(BigInteger initialA, BigInteger initialB) {
        row = new BigInteger[]{initialA, initialB, BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE, null, null};
        calculate();
    }

    private void calculate() {
        while (!row[b].equals(BigInteger.ZERO)) {
            BigInteger[] divideAndRemainder = row[a].divideAndRemainder(row[b]);
            row[q] = divideAndRemainder[0];
            row[r] = divideAndRemainder[1];
            row[a] = row[b];
            row[b] = row[r];
            BigInteger newX1 = row[x0].subtract(row[q].multiply(row[x1]));
            BigInteger newY1 = row[y0].subtract(row[q].multiply(row[y1]));
            row[x0] = row[x1];
            row[y0] = row[y1];
            row[x1] = newX1;
            row[y1] = newY1;
        }
    }

    public BigInteger getA() {
        return row[a];
    }

    public BigInteger getY0() {
        return row[y0];
    }
}