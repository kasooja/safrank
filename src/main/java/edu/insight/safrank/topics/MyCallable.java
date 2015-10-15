package edu.insight.safrank.topics;

import java.util.concurrent.Callable;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class MyCallable implements Callable<Double> {

	private WeightedObservedPoints obs = null;
	private double predictionYear = 2000;
	
	public MyCallable(WeightedObservedPoints obs, double predictionYear) {
		this.obs = obs;
		this.predictionYear = predictionYear;
	}
	
	@Override
	public Double call() throws Exception {
		GaussianCurveFitter fitter = GaussianCurveFitter.create();
		System.out.println("fitting");
		double[] bestFit = fitter.fit(obs.toList());
		Gaussian f = new Gaussian(bestFit[0], bestFit[1], bestFit[2]);
		System.out.println("done");
		double predictSafVal = f.value(predictionYear);
		return predictSafVal;
	}

}