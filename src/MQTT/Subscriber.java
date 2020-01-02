package MQTT;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 */
public class Subscriber implements MqttCallback {

    private final int qos = 1;
    private MqttClient client;

    public Subscriber() throws MqttException {
        String username = "ujrhcfvo";
        String password = "kb4eCv1UCJG_";
        String clientId = "MQTT-Java-Example";
     
        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        conOpt.setUserName(username);
        conOpt.setPassword(password.toCharArray());

        this.client = new MqttClient("tcp://farmer.cloudmqtt.com:12306", clientId, new MemoryPersistence());
        this.client.setCallback(this);
        this.client.connect(conOpt);

        this.client.subscribe("EnviromentOne", qos);
    }

    public void sendMessage(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        this.client.publish(topic, message); // Blocking publish
    }

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost because: " + cause);
        System.exit(1);
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
        System.out.println(String.format("[%s] %s", topic, new String(message.getPayload())));
    }
}