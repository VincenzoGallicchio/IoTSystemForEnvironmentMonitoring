package DataProcessingComponents;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JsonArray;

/**
 * Servlet implementation class DataUpdater
 */
@WebServlet(name="/DataUpdater",urlPatterns = {"/data_updater"})
public class DataUpdater extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataUpdater() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletContext servletContext = getServletContext();
        SensorData sD = (SensorData) servletContext.getAttribute( "realTimeMeasurement" );
        if(sD == null)
        	sD = new SensorData(0.0f, 0.0f, "Nessun dato", 0.0f, "Nessun Dato");
        sendResponse(sD, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	private void sendResponse(SensorData sD, HttpServletResponse response) throws IOException {
		JsonArray jsonArray = new JsonArray();
		
		jsonArray.add(sD.getTemperature());
		jsonArray.add(sD.getHumidity());
		jsonArray.add(sD.getGas());
		jsonArray.add('"'+sD.getLight()+'"');
		jsonArray.add('"'+sD.getInfrared()+'"');
		PrintWriter out = response.getWriter();
		out.print(jsonArray);
		out.flush();
		out.close();	
	}
}
