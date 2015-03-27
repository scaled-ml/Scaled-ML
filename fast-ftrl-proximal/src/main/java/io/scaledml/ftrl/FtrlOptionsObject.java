package io.scaledml.ftrl;


public class FtrlOptionsObject implements FtrlOptions {
    private int hashcodeBits = 18;
    private double alfa = 0.005;
    private double beta = 0.1;
    private double l1 = 0.;
    private double l2 = 0.;
    private String predictions;
    private String data;
    private String finalRegressor;
    private String initialRegressor;
    private boolean testOnly = false;

    @Override
    public int hashcodeBits() {
        return hashcodeBits;
    }

    @Override
    public double alfa() {
        return alfa;
    }

    @Override
    public double beta() {
        return beta;
    }

    @Override
    public double l1() {
        return l1;
    }

    @Override
    public double l2() {
        return l2;
    }

    @Override
    public String finalRegressor() {
        return finalRegressor;
    }

    @Override
    public String initialRegressor() {
        return initialRegressor;
    }

    @Override
    public boolean testOnly() {
        return testOnly;
    }

    @Override
    public String data() {
        return data;
    }

    @Override
    public String predictions() {
        return predictions;
    }

    @Override
    public boolean help() {
        return false;
    }

    public FtrlOptionsObject hashcodeBits(int hashcodeBits) {
        this.hashcodeBits = hashcodeBits;
        return this;
    }

    public FtrlOptionsObject alfa(double alfa) {
        this.alfa = alfa;
        return this;
    }

    public FtrlOptionsObject beta(double beta) {
        this.beta = beta;
        return this;
    }

    public FtrlOptionsObject l1(double l1) {
        this.l1 = l1;
        return this;
    }

    public FtrlOptionsObject l2(double l2) {
        this.l2 = l2;
        return this;
    }

    public FtrlOptionsObject finalRegressor(String finalRegressor) {
        this.finalRegressor = finalRegressor;
        return this;
    }

    public FtrlOptionsObject initialRegressor(String initialRegressor) {
        this.initialRegressor = initialRegressor;
        return this;
    }

    public FtrlOptionsObject testOnly(boolean testOnly) {
        this.testOnly = testOnly;
        return this;
    }

    public FtrlOptionsObject data(String data) {
        this.data = data;
        return this;
    }

    public FtrlOptionsObject predictions(String predictions) {
        this.predictions = predictions;
        return this;
    }
}
