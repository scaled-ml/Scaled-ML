package io.scaledml;

import com.google.inject.Inject;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.FloatBigArrayBigList;
import it.unimi.dsi.fastutil.floats.FloatBigList;
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

    public FloatBigList n() {
        return n;
    }

    public FloatBigList z() {
        return z;
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
            currentN.add(n.getFloat(index));
            currentZ.add(z.getFloat(index));
        }
    }

    public static void saveModel(FtrlProximalModel model, Path output) throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(Files.newOutputStream(output))) {
            os.writeObject(model);
        }
    }

    public static FtrlProximalModel loadModel(Path input) throws Exception {
        try (ObjectInputStream is = new ObjectInputStream(Files.newInputStream(input))) {
            return (FtrlProximalModel) is.readObject();
        }
    }
}
