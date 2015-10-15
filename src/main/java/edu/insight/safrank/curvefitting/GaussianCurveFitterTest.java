package edu.insight.safrank.curvefitting;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import edu.insight.safrank.topics.MyCallable;

public class GaussianCurveFitterTest {

	public static Double getVal(WeightedObservedPoints obs, double targetYear){
		MyCallable callable1 = new MyCallable(obs, targetYear);
		FutureTask<Double> futureTask1 = new FutureTask<Double>(callable1);
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(futureTask1);
		Double predVal = null;
		try {
			predVal = futureTask1.get(4, TimeUnit.SECONDS);
			futureTask1 = null;
			callable1 = null;
			executor.shutdownNow();
			executor = null;
		}
		catch (TimeoutException e) {
			WeightedObservedPoints obs1 = new WeightedObservedPoints();
			List<WeightedObservedPoint> list = obs.toList();
			for(WeightedObservedPoint p : list){
				if(p.getY()!=0.0){
					obs1.add(p);
				}
			}
			futureTask1 = null;
			callable1 = null;
			executor.shutdownNow();
			executor = null;
			if(obs1.toList().size() == obs.toList().size()){
				return getLinVal(obs, targetYear);
			} else {
				if(obs1.toList().size()>2){
					return getVal(obs1, targetYear);	
				} else {		
					return getLinVal(obs, targetYear);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return predVal;
	}


	public static Double getLinVal(WeightedObservedPoints obs, double targetYear){
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
		//	System.out.println("fitting");
		double[] bestFit = fitter.fit(obs.toList());
		PolynomialFunction f = new PolynomialFunction(bestFit);
		//	System.out.println("done");
		double predictSafVal = f.value(targetYear);
		return predictSafVal;
	}

	public static void main(String[] args) {
		WeightedObservedPoints obs = new WeightedObservedPoints();
		obs.add(2000, 1.38629436493);
		obs.add(2002, 4.62323951721);
		obs.add(2004, 0.0);
		obs.add(2006, 0.0);
		obs.add(2008, 4.62323951721);
		obs.add(2010, 0.0);
		obs.add(2012, 7.06401872635);
		Double predVal = getVal(obs, 2014);
		System.out.println("FinalVal" + " " + predVal);
	}


}



