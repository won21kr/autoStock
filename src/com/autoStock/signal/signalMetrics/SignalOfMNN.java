package com.autoStock.signal.signalMetrics;

import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import com.autoStock.backtest.encog.TrainEncogSignal.EncogNetworkType;
import com.autoStock.position.PositionDefinitions.PositionType;
import com.autoStock.signal.SignalBase;
import com.autoStock.signal.SignalDefinitions.SignalMetricType;
import com.autoStock.signal.SignalDefinitions.SignalParameters;
import com.autoStock.signal.SignalDefinitions.SignalPointType;
import com.autoStock.signal.SignalPoint;
import com.autoStock.signal.extras.EncogInputWindow;
import com.autoStock.signal.extras.EncogNetworkProvider;
import com.autoStock.tools.MathTools;

/**
 * @author Kevin Kowalewski
 * 
 */
public class SignalOfMNN extends SignalBase {
	public static EncogNetworkType encogNetworkType = EncogNetworkType.basic;
	private static final double NEURON_THRESHOLD = 0.95;
	public static final int INPUT_WINDOW_EXTRAS = 0;
	public static final int INPUT_WINDOW_PS = 20;
	public static final int INPUTS = 6;
	private String networkName;
	private MLRegression basicNetwork;
	private EncogInputWindow encogInputWindow;

	public SignalOfMNN(SignalParameters signalParameters) {
		super(SignalMetricType.metric_encog, signalParameters);
	}

	public void setNetwork(MLRegression network) {
		this.basicNetwork = network;
	}
	
	public void setNetworkName(String networkName) {
		this.networkName = networkName;
		
		if (encogNetworkType == EncogNetworkType.basic){
			basicNetwork = new EncogNetworkProvider(networkName).getBasicNetwork();
		}else if (encogNetworkType == EncogNetworkType.neat){
			basicNetwork = new EncogNetworkProvider(networkName).getNeatNetwork();
		}
	}

	@Override
	public SignalPoint getSignalPoint(boolean havePosition, PositionType positionType) {
		SignalPoint signalPoint = new SignalPoint();

		if (encogInputWindow == null || basicNetwork == null){
//			Co.println("--> No network! " + (encogInputWindow == null) + ", " + (basicNetwork == null));
			return signalPoint;
		}

		NormalizedField normalizedFieldForSignals = new NormalizedField(NormalizationAction.Normalize, "Signal Delta Normalizer", 10000, -10000, 1, -1);

		MLData input = new BasicMLData(basicNetwork.getInputCount());

		double[] inputWindow = encogInputWindow.getAsWindow();

		if (inputWindow.length != basicNetwork.getInputCount()) {
			throw new IllegalArgumentException("Input sizes don't match, supplied, needed: " + inputWindow.length + ", " + basicNetwork.getInputCount());
		}

		for (int i = 0; i < inputWindow.length; i++) {
			//Co.print(" " + inputWindow[i]);
			input.add(i, normalizedFieldForSignals.normalize(inputWindow[i]));
		}

		MLData output = basicNetwork.compute(input);

		double valueForLongEntry = output.getData(0);
		double valueForShortEntry = output.getData(1);
		double valueForAnyExit = output.getData(2);
		double valueForReeentry = output.getData(3);
		
//		Co.println("--> Values: " + valueForLongEntry + ", " + valueForShortEntry + ", " + valueForAnyExit);

		if (valueForLongEntry >= NEURON_THRESHOLD) {
			// Co.println("--> Long entry?");
			signalPoint.signalPointType = SignalPointType.long_entry;
			signalPoint.signalMetricType = SignalMetricType.metric_encog;
			// }
		} else if (valueForShortEntry >= NEURON_THRESHOLD) {
			signalPoint.signalPointType = SignalPointType.short_entry;
			signalPoint.signalMetricType = SignalMetricType.metric_encog;
		} else if (valueForReeentry >= NEURON_THRESHOLD) {
			signalPoint.signalPointType = SignalPointType.reentry;
			signalPoint.signalMetricType = SignalMetricType.metric_encog;
		} else if (valueForAnyExit >= NEURON_THRESHOLD && havePosition) {
			if (positionType == PositionType.position_long) {
				signalPoint.signalPointType = SignalPointType.long_exit;
			} else if (positionType == PositionType.position_short) {
				signalPoint.signalPointType = SignalPointType.short_exit;
			} else {
				throw new IllegalStateException("Have position of type: " + positionType.name());
			}
			signalPoint.signalMetricType = SignalMetricType.metric_encog;
		}

		return signalPoint;
	}

	public void setInput(EncogInputWindow encogInputWindow) {
		if (encogInputWindow == null){
			throw new IllegalArgumentException("Can't set a null EncogInputWindow");
		}
		this.encogInputWindow = encogInputWindow;
	}
	
	public double[] getInputWindowRounded(){
		if (encogInputWindow == null){return null;}
		double[] array = new double[encogInputWindow.getAsWindow().length];
		
		for (int i=0; i<array.length; i++){
			array[i] = MathTools.round(encogInputWindow.getAsWindow()[i]);
		}
		
		return array;
	}
	
	public EncogInputWindow getInputWindow() {
		return encogInputWindow;
	}
	
	public static int getInputWindowLength(){
		return (INPUT_WINDOW_PS * INPUTS) + INPUT_WINDOW_EXTRAS;
	}

	@Override
	public void setInput(double value) {
		throw new NoSuchMethodError("Don't call this");
	}
	
	@Override
	public void reset() {
		super.reset();
		encogInputWindow = null;
	}

	public boolean isLongEnough(SignalBase... arrayOfSignalBase) {
		for (SignalBase signalBase : arrayOfSignalBase){
			if (signalBase.listOfNormalizedValuePersist.size() <= INPUT_WINDOW_PS){
				return false;
			}
		}
		
		return true;
	}
}