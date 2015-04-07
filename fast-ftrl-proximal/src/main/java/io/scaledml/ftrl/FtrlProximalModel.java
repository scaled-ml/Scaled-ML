package io.scaledml.ftrl;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatBigArrayBigList;
import it.unimi.dsi.fastutil.floats.FloatBigList;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class FtrlProximalModel implements Serializable {
    private FloatBigList n;
    private FloatBigList z;
    private double lambda1;
    private double lambda2;
    private double alfa;
    private double beta;

    public double lambda1() {
        return lambda1;
    }

    public FtrlProximalModel lambda1(double lambda1) {
        this.lambda1 = lambda1;
        return this;
    }

    public double lambda2() {
        return lambda2;
    }

    public FtrlProximalModel lambda2(double lambda2) {
        this.lambda2 = lambda2;
        return this;
    }

    public double alfa() {
        return alfa;
    }

    public FtrlProximalModel alfa(double alfa) {
        this.alfa = alfa;
        return this;
    }

    public double beta() {
        return beta;
    }

    public FtrlProximalModel beta(double beta) {
        this.beta = beta;
        return this;
    }

    public FtrlProximalModel featuresNumber(long featuresNumber) {
        n = new FloatBigArrayBigList(featuresNumber);
        z = new FloatBigArrayBigList(featuresNumber);
        n.size(featuresNumber);
        z.size(featuresNumber);
        return this;
    }

    public void readVectors(LongList indexes, DoubleList currentN, DoubleList currentZ) {
        currentN.clear();
        currentZ.clear();
        for (long index : indexes) {
            currentN.add(n.get(index));
            currentZ.add(z.get(index));
        }
    }

    public static void saveModel(FtrlProximalModel model, Path output) throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(new FastBufferedOutputStream(Files.newOutputStream(output)))) {
            os.writeObject(model);
        }
    }

    public static FtrlProximalModel loadModel(Path input) throws Exception {
        try (ObjectInputStream is = new ObjectInputStream(new FastBufferedInputStream(Files.newInputStream(input)))) {
            return (FtrlProximalModel) is.readObject();
        }
    }

    public long featuresNumber() {
        return n.size64();
    }

    public void writeToModel(Increment increment) {
        for (int i = 0; i < increment.indexes().size(); i++) {
            long index = increment.indexes().getLong(i);
            double nDelta = increment.incrementOfN().getDouble(i);
            double zDelta = increment.incrementOfZ().getDouble(i);
            n.set(index, (float) (n.get(index) + nDelta));
            z.set(index, (float) (z.get(index) + zDelta));
        }
    }
}
