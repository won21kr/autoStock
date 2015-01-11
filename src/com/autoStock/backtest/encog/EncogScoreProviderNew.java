package com.autoStock.backtest.encog;

import java.util.ArrayList;

import org.encog.ml.MLRegression;
import org.encog.neural.networks.training.CalculateScore;

import com.autoStock.algorithm.core.AlgorithmDefinitions.AlgorithmMode;
import com.autoStock.algorithm.core.AlgorithmRemodeler;
import com.autoStock.backtest.AlgorithmModel;
import com.autoStock.backtest.BacktestEvaluation;
import com.autoStock.backtest.BacktestEvaluationBuilder;
import com.autoStock.backtest.SingleBacktest;
import com.autoStock.signal.SignalCache;
import com.autoStock.tools.Benchmark;
import com.autoStock.trading.types.HistoricalData;

/**
 * @author Kevin Kowalewski
 *
 */
public class EncogScoreProviderNew implements CalculateScore {
	private HistoricalData historicalData;	
	public static long runCount;
	private AlgorithmModel algorithmModel;
	public static ArrayList<EncogTest> listOfEncogTest = new ArrayList<EncogTest>();
	int whichNetwork = 0;
	
	public void setDetails(AlgorithmModel algorithmModel, HistoricalData historicalData){
		this.algorithmModel = algorithmModel;
		this.historicalData = historicalData;
	}
	
	@Override
	public double calculateScore(MLRegression network) {
//		Co.print("--> Calculate score... " + algorithmModel.getUniqueIdentifier() + " ");
		//Co.println(BacktestEvaluationReader.getPrecomputedEvaluation(exchange, symbol).toString());
		
		SingleBacktest singleBacktest = new SingleBacktest(historicalData, AlgorithmMode.mode_backtest_single);
		new AlgorithmRemodeler(singleBacktest.backtestContainer.algorithm, algorithmModel).remodel(true, true, true, false);
		singleBacktest.selfPopulateBacktestData();
		singleBacktest.backtestContainer.algorithm.signalGroup.signalOfEncog.setNetwork(network, whichNetwork);
		singleBacktest.runBacktest();
		
		BacktestEvaluation backtestEvaluation = new BacktestEvaluationBuilder().buildEvaluation(singleBacktest.backtestContainer, false, false);
		
		runCount++;
		
		double score = backtestEvaluation.getScore();
		
		return score > 0 ? score : Double.MIN_VALUE;
	}

	@Override
	public boolean shouldMinimize() {
		return false;
	}
	
	public static class EncogTest {
		public MLRegression network;
		public BacktestEvaluation backtestEvaluation;
		public String table;
		
		public EncogTest(MLRegression network, BacktestEvaluation backtestEvaluation, String table) {
			this.network = network;
			this.backtestEvaluation = backtestEvaluation;
			this.table = table;
		}
	}

	public void setSignalCache(SignalCache signalCache) {
		//
	}

	public AlgorithmModel getAlgorithmModel() {
		return algorithmModel;
	}

	public void setWhichNetwork(int which) {
		this.whichNetwork = which;
	}
}
