# Java FTRL-Proximal implementation


Here is java FTRL-Proximal implementation. 

This is a machine learning algorithm that predicts the probability of some kind of events such as clicks on ad.

Main advantage of this implementation that it gets benifits from multi-core hardware and can _scale up to 32 cores_.

Build
-----

To build a project [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and [maven3](http://maven.apache.org/download.cgi) must be installed.

Build command:
```
$ cd fast-ftrl-proximal
$ mvn clean install
```

Runnable jar ftrl-proximal.jar will be found in fast-ftrl-proximal/target/ directory.

Input format
------------
Input format is similar to [vowpal wabbit's one](http://github.com/JohnLangford/vowpal_wabbit/wiki/Input-format) except that example's weight nor feature's weight are not supported.

Only ascii symbols are supported yet.
Run
---
Train:
```
$ java -Xmx2G -jar ftrl-proximal.jar -d train.slit.logit.vw -b 22 -f model1
```
Apply:
```
$ java -Xmx2G -jar ftrl-proximal.jar -d test.slit.logit.vw -i model1 -p predictions
```

Run from java
-------------
You can train or apply the algorithm from java by specifing options via FtrlOptionsObject:
```
 Main.runFtrlProximal(new FtrlOptionsObject()
                .finalRegressor("model")
                .data("train-small.vw")
                .alfa(0.01)
                .lambda1(1.)
                .lambda2(2.)));
```
Options
-------

```
The options available are:
	[--ftrl_alpha value] : ftrl alpha parameter (option in ftrl)
	[--ftrl_beta value] : ftrl beta patameter (option in ftrl)
	[--data -d value] : Example Set
	[--final_regressor -f value] : Final regressor to save (arg inputStream filename)
	[--format value] : Input file format.'vw' or 'csv' are currently supported
	[--bit_precision -b value] : number of bits in the feature table
	[--help -h] : Show this help
	[--initial_regressor -i value] : Initial regressor(s) to load into memory (arg inputStream filename)
	[--l1 value] : l_1 lambda (L1 regularization)
	[--l2 value] : l_2 lambda (L2 regularization)
	[--predictions -p value] : File to output predictions to
	[--quadratic -q] : Add quadratic features
	[--testonly -t] : Ignore label information and just test
	[--threads value] : Parallelization level
```

References
----------
For more information see ["Ad Click Prediction: a View from the Trenches"](http://research.google.com/pubs/pub41159.html) paper.

[ ![Codeship Status for scaled-ml/Scaled-ML](https://codeship.com/projects/55b7e1c0-bfe9-0132-f5cd-7eb09717a41c/status?branch=master)](https://codeship.com/projects/73069)
