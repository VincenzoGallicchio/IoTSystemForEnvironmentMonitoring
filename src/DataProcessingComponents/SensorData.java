package DataProcessingComponents;

public class SensorData {
	private String SenderMacID;
	private String location;
	private float temperature;
	private float gas;
	private	String light;
	private float humidity;
	private String infrared;
	
	public SensorData(float temperature, float gas, String light, float humidity,
			String infrared) {
		super();
		this.temperature = temperature;
		this.gas = gas;
		this.light = light;
		this.humidity = humidity;
		this.infrared = infrared;
	}
	
	public SensorData(float temperature, float humidity, float gas) {
		super();
		this.temperature = temperature;
		this.gas = gas;
		this.humidity = humidity;
	}

	public SensorData() {
		// TODO Auto-generated constructor stub
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSenderMacID() {
		return SenderMacID;
	}

	public void setSenderMacID(String senderMacID) {
		SenderMacID = senderMacID;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getGas() {
		return gas;
	}

	public void setGas(float gas) {
		this.gas = gas;
	}

	public String getLight() {
		return light;
	}

	public void setLight(String light) {
		this.light = light;
	}

	public float getHumidity() {
		return humidity;
	}

	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}

	public String getInfrared() {
		return this.infrared;
	}

	public void setInfrared(String infrared) {
		this.infrared = infrared;
	}

	@Override
	public String toString() {
		return "temperature=" + temperature + ", gas=" + gas + ", humidity=" + humidity;
	}

	
	
}
