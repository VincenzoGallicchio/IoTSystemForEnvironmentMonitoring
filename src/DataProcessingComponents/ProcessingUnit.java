package DataProcessingComponents;

import java.util.ArrayList;
import java.util.Date;

public class ProcessingUnit {
	private ArrayList<Object[]> measurementListToMine;
	
	public ProcessingUnit() {
		measurementListToMine = new ArrayList<Object[]>();
	}
	
	public void endMiningPhase() {
		measurementListToMine = new ArrayList<Object[]>();
	}
	
	public void addMeasurement(SensorData measurement) {
		Object[] timeInstance = new Object[2];
		timeInstance[0] = measurement;
		timeInstance[1] = new Date();
		this.measurementListToMine.add(timeInstance);
	}
	
	public SensorData getDataMean() {
		SensorData toProcess;
		SensorData minedData;
		float totalTemp = 0.0f;
		float totalUmid = 0.0f;
		float totalGas = 0.0f;
		int NoI = measurementListToMine.size();
		for(Object[] measurement : measurementListToMine) {
			//get data only
			toProcess = (SensorData)measurement[0];
			totalTemp += toProcess.getTemperature();
			totalUmid += toProcess.getHumidity();
			totalGas += toProcess.getGas();
		}
		if(NoI == 0)
			return new SensorData(0, 0, 0);
		minedData = new SensorData((totalTemp/NoI), (totalUmid/NoI), (totalGas/NoI));
		return minedData;
	}
	
	public SensorData getDataMax() {
		SensorData toProcess;
		SensorData minedData;
		float maxTemp = 0.0f;
		float maxUmid = 0.0f;
		float maxGas = 0.0f;
		
		for(Object[] measurement : measurementListToMine) {
			//get data only
			toProcess = (SensorData)measurement[0];
			
			if(toProcess.getTemperature() > maxTemp)
				maxTemp = toProcess.getTemperature();
			
			if(toProcess.getHumidity() > maxUmid)
				maxUmid = toProcess.getHumidity();
			
			if(toProcess.getGas() > maxGas)
				maxGas = toProcess.getGas();

		}
		
		minedData = new SensorData(maxTemp, maxUmid, maxGas);
		return minedData;
	}
	
	public SensorData getDataMin() {
		SensorData toProcess;
		SensorData minedData;
		if(measurementListToMine.size() == 0)
			return  new SensorData(0.0f, 0.0f, "Nessun dato", 0.0f, "Nessun Dato");
		toProcess = (SensorData)measurementListToMine.get(0)[0];
		float minTemp = toProcess.getTemperature();
		float minUmid = toProcess.getHumidity();
		float minGas = toProcess.getGas();
		
		for(Object[] measurement : measurementListToMine) {
			//get data only
			toProcess = (SensorData)measurement[0];
			
			if(toProcess.getTemperature() < minTemp)
				minTemp = toProcess.getTemperature();
			
			if(toProcess.getHumidity() < minUmid)
				minUmid = toProcess.getHumidity();
			
			if(toProcess.getGas() < minGas)
				minGas = toProcess.getGas();

		}
		
		minedData = new SensorData(minTemp, minUmid, minGas);
		return minedData;
	}
	
}
