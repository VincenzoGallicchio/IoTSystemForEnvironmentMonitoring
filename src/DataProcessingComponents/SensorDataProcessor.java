package DataProcessingComponents;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.paho.client.mqttv3.MqttException;

import MQTT.Subscriber;

/**
 * Servlet implementation class SensorDataProcessor
 */

@WebServlet("/SensorDataProcessor")
public class SensorDataProcessor extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(SensorDataProcessor.class.getName());
	
	private float temperature;
	private float gas;
	private float humid;
	private float lightValue;
	private String isLightOn;
	private String infrared;
	private ProcessingUnit pU;
	private SensorData sD;
	private Subscriber subscriber;
	
    /**
     * Default constructor. 
     */
    public SensorDataProcessor() {
    	
    }
    
    public void init(ServletConfig config) throws ServletException{
    	super.init(config );
    	try {
			this.subscriber = new Subscriber();
		} catch (MqttException e) {
			e.printStackTrace();
		}
    	
    	//Initialization
    	SensorData sharedData =  new SensorData(0.0f, 0.0f, "Nessun dato", 0.0f, "Nessun Dato");
    	this.sD =  new SensorData(0.0f, 0.0f, "Nessun dato", 0.0f, "Nessun Dato");
    	
    	//add a SensorData instance as a variable available through the entire web app
    	getServletContext().setAttribute( "realTimeMeasurement", sharedData);
    	
    	pU = new ProcessingUnit();
    	TimedUpload timerTask = new TimedUpload();
        //running timer task as daemon thread
        Timer timer = new Timer(true);
        //Every minute sends relevant data to Cloud
        timer.scheduleAtFixedRate(timerTask, 0, 1*60000);
    }
  
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//it continues only if all http parameters are != null
		boolean proceed = this.parseParameters(request);
		if(proceed) {
			this.sD = new SensorData(temperature, gas, isLightOn, humid, infrared);
			synchronized(this) {
				this.pU.addMeasurement(sD);
			}
			LOGGER.info(this.sD.toString() + " - " + new Date());
			getServletContext().setAttribute( "realTimeMeasurement", new SensorData(temperature, gas, isLightOn, humid, infrared));
		}
	}

	private boolean parseParameters(HttpServletRequest request) {
		boolean proceed = true;
		if(request.getParameter("temp") != null){
			temperature = Float.parseFloat(request.getParameter("temp"));		
		}else 
			proceed = false;
		
		if(request.getParameter("humid") != null){
			humid = Float.parseFloat(request.getParameter("humid"));	
		}else 
			proceed = false;
		
		if(request.getParameter("gas") != null){
			gas = Float.parseFloat(request.getParameter("gas"));	
		}else 
			proceed = false;
		
		if(request.getParameter("light")!=null) {
			this.lightValue = Float.parseFloat(request.getParameter("light"));
			this.isLightOn = discretizeLightAnalogicValue(this.lightValue);
		}else 
			proceed = false;
		 
		if(request.getParameter("infrared")!=null) {
			if(Integer.parseInt(request.getParameter("infrared")) == 1)
				this.infrared = "Allerta animale";
			else
				this.infrared = "Nessuna allerta";
		}else 
			proceed = false;
		
		return proceed;
	}
	
	private String discretizeLightAnalogicValue(float lightValue) {
		String isLightOn = "";
		if(lightValue < 256)
			isLightOn = "Luce molto alta";
		else if(lightValue >= 256 && lightValue < 512)
			isLightOn = "Luce alta";
		else if(lightValue >= 512 && lightValue < 768)
			isLightOn = "Luce bassa";
		else
			isLightOn = "Luce molta bassa o spenta";
			
		return isLightOn;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	private class TimedUpload extends TimerTask {
	    @Override
	    public void run() {
	        try {
				completeUpload();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	
	    private void completeUpload() throws MqttException {
	    	synchronized(this) {
	    		//Send data to cloud for other applications' needs
	    		subscriber.sendMessage("sensor/measurements", "MAX: " + pU.getDataMax().toString() + " - " +
		    			"MEAN: " + pU.getDataMean().toString() + " - " + "MIN: " + pU.getDataMin().toString());
	    		
	    		System.out.println("MAX: " + pU.getDataMax().toString() + " - " +
		    			"MEAN: " + pU.getDataMean().toString() + " - " + "MIN: " + pU.getDataMin().toString());
	    		pU.endMiningPhase();
	    	}
	    }
	}
}
