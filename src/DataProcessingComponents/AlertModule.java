package DataProcessingComponents;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.Logger;

/**
 * Servlet implementation class AlertModule
 */
@WebServlet("/AlertModule")
public class AlertModule extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(AlertModule.class.getName());
	
	private String value;
	private String type;
	private String eventType;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AlertModule() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if((type = request.getParameter("type"))!=null) {
			switch(type) {
				case "temperature":
					this.eventType = "temperature";
					this.parseTemperatureParameter(request);
				case "gas":
					this.eventType = "gas";
					this.parseGasParameter(request);
			}
			
			URL url = new URL("https://maker.ifttt.com/trigger/alert/with/key/icISbsI1EL3PgyhFElAKW-qOy3TQWjj1W66Ht6YhhVN?value1=" +
					 this.eventType+"&value2="+this.value);
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			LOGGER.info(con.getResponseCode()+"");
			LOGGER.info("Avviso inviato");
		}else
			LOGGER.info("ERRORE nel parsing dei parametri");
	}

	private void parseGasParameter(HttpServletRequest request) {
		if(request.getParameter("value") != null){
			this.value = request.getParameter("value") +"m3";		
		}else
			this.value = "Nan";	
	}

	private void parseTemperatureParameter(HttpServletRequest request) {
		if(request.getParameter("value") != null){
			this.value = request.getParameter("value");		
		}else
			this.value = "Nan";
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
