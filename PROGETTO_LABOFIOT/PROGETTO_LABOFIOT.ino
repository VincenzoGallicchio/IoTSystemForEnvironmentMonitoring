#include <LiquidCrystal.h>
#include "DHT.h"
#include <WiFiNINA.h>
#include <HttpClient.h>
#include <timer.h>

#define DHTPIN 5     // Digital pin connected to the DHT sensor
#define DHTTYPE DHT11 
#define MOVEMENT_THRESHOLD 5

//initialize the library with the numbers of the interface pins
LiquidCrystal lcd(7, 6, 4, 3, 2, 1);
WiFiClient client;
// Initialize DHT sensor.
DHT dht(DHTPIN, DHTTYPE);

WiFiServer server(80);

float roundedVoltage = 0;

const byte buzzerPin = 0; // the number of the buzzer pin
const byte powerSavingButtonPin = 11; // the number of the power saving button pin
const byte sensorPin = 12; // the number of the infrared motion sensor pin

auto timer = timer_create_default(); // create a timer with default settings

String temp;
String humid;
String gas;
String light; // Define a variable to save the ADC value
float voltage = 0.0;            // calculated voltage
String hours = "";

boolean isLightOn;
boolean animalDetected = false;
boolean alarmPermission= true;

int numberOfMinutes = 0;
int currentMinutes = 0;

char serverIP[] = "192.168.43.116";
char serverIPHost[] = "Host: 192.168.43.116";
char ssid[] = "HUAWEI P20 lite";  // your network SSID (name)
char pass[] = "vincenzo123";       // your network password (use for WPA, or use as key for WEP)
int status = WL_IDLE_STATUS;     // the Wifi radio's status

//volatile is used for the variable to be shared across functions
volatile unsigned long timeToSense = 2000;
volatile boolean switchGo = true;

boolean criticalPowerMode = false;

//battery voltage to service hours function digital representation
const float cheapHashMap[9][2] {
  {5.0, 9.0},
  {5.5, 8.7},
  {6.0, 7.8},
  {6.5, 6.0},
  {7.0, 4.0},
  {7.5, 2.9},
  {8.0, 1.0},
  {8.5, 0.4},
  {9.0, 0.1}
};

float getHoursLeft(float key){
  for(int i = 0 ; i < 9 ; i++){
    if(cheapHashMap[i][0] == key)
      return cheapHashMap[i][1];
  }
  return 0.0;
}

void setup(){
  Serial.begin(9600);
  connectToWiFi();
  //attaching interrupt to the button
  attachInterrupt(11, savePower, CHANGE);
  initializeDevices();

  //timed events schedulation
  timer.every(6000, getBatteryVoltage);
  timer.every(60000, updateTime);
  startWebServer();
}

void loop() {
  timer.tick(); // tick the timer
  switchGo = true; //to handle button double push

  humid = getHumidity();
  temp = getTemperature();
  gas = readGas();
  light = detectLight();
  animalDetected = (digitalRead(sensorPin) > 0);
  displayAll(); //display on the lcd meaningful data

  sendDataToEdgeServer(); //REST API HTTP GET
  if(!criticalPowerMode)
    checkForHTTPRequest();
    
  delay(timeToSense);
}
void startWebServer(){
  // start the web server on port 80
  server.begin();
}

void checkForHTTPRequest(){
  WiFiClient client = server.available();   // listen for incoming clients

  if (client) {                             // if you get a client,
    Serial.println("new client");           // print a message out the serial port
    String currentLine = "";                // make a String to hold incoming data from the client
    while (client.connected()) {            // loop while the client's connected
      if (client.available()) {             // if there's bytes to read from the client,
        char c = client.read();             // read a byte, then
        Serial.write(c);                    // print it out the serial monitor
        if (c == '\n') {                    // if the byte is a newline character

          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
            // and a content-type so the client knows what's coming, then a blank line:
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html");
            client.println();
            break;
          }
          else {      // if you got a newline, then clear currentLine:
            currentLine = "";
          }
        }
        else if (c != '\r') {    // if you got anything else but a carriage return character,
          currentLine += c;      // add it to the end of the currentLine
        }

        // Check to see if the client request was "GET /H" or "GET /L":
        if (currentLine.endsWith("GET /POWERMODEON")) {
            makeSound();
            timeToSense = 5000;
        }
        if (currentLine.endsWith("GET /POWERMODEOFF")) {
            makeSound2();
            timeToSense = 2000;
        }
      }
    }
    // close the connection:
    client.stop();
    Serial.println("client disconnected");
  }
}

void savePower(){
  if(timeToSense == 2000 && switchGo){
    Serial.println("POWERMODE ON");
    timeToSense = 5000;
    switchGo = false;
  }else if(timeToSense == 5000 && switchGo){
    Serial.println("POWERMODE OFF");
    timeToSense = 2000;
    switchGo = false;
  }
}

void initializeDevices(){
  //initialize the buzzer
  pinMode(buzzerPin, OUTPUT); 
  //initialize the humidity and temp sensor
  dht.begin(); 
  //set up the LCD's number of columns and rows:
  lcd.begin(16, 2);
  //initialize the interruption button for power saving
  pinMode(powerSavingButtonPin, INPUT);
  pinMode(sensorPin, INPUT); // initialize the infrared sensor pin as input
  
  makeSound();
  getBatteryVoltage();
}

void updateTime(){
  Serial.println(numberOfMinutes);
  numberOfMinutes ++;
  if(numberOfMinutes > 4 && !alarmPermission)
    alarmPermission = true;
}

float readGas(){
  float gas = analogRead(A0);
  if(gas >150.0 && alarmPermission)
    makeGetCall(gas, "gas");
   
  return gas;
}

float detectLight(){
  float convertValue = analogRead(A1);
  return convertValue;
}

void sendDataToEdgeServer(){ 
  Serial.println("\nStarting connection to server...");
  if (client.connect(serverIP, 8080)) {
    Serial.println("Data sent to server");
    // Make a HTTP request:
    client.println("GET /IoTEdgeNode/SensorDataProcessor?temp=" + temp + "&gas="+gas+
    "&humid=" + humid + "&light=" + light +"&infrared="+animalDetected+" HTTP/1.1");
    client.println(serverIPHost); 
    client.println();
  }else
    Serial.println("no connection");   
}

void displayAll()
{
  lcd.setCursor(0,0);
  lcd.print("T:");
  lcd.print(temp);
  lcd.print(" ");
  lcd.print("H:");
  lcd.print(humid);
  lcd.setCursor(0,1);
  lcd.print("G:");
  lcd.print(gas);
  lcd.print(" Hr:");
  lcd.print(hours);
  batterylevel(15, 1);
}

void makeSound(){
  tone(buzzerPin,  261.63); // DO
  delay(200);
  tone(buzzerPin,  330); // MI
  delay(200);
  tone(buzzerPin,  392); //SOL 
  delay(200);
  noTone(buzzerPin);
}

void makeSound2(){
  tone(buzzerPin,  392); //SOL 
  delay(200);
  tone(buzzerPin,  330); // MI
  delay(200);
  tone(buzzerPin,  261.63); // DO
  delay(200);
  noTone(buzzerPin);
}

float getTemperature(){
  float temperature = dht.readTemperature();
  if (isnan(temperature))   
    return 0;
  if(temperature > 27.0 && alarmPermission){
    makeGetCall(temperature, "temperature");
  }
  return temperature;
}

//kg/m³
float getHumidity(){
  float humidity = dht.readHumidity();
  if (isnan(humidity)) {
    return 0;
  }
  return humidity;
}

void makeGetCall(float warningValue, String type){ 
  String string = (String)warningValue;
  Serial.println("\nStarting connection to server...");
  // if you get a connection, report back via serial:
  if (client.connect(serverIP, 8080)) {
    Serial.println("connected to server");
    // Make a HTTP request:
    client.println("GET /IoTEdgeNode/AlertModule?value=" + string + "&type=" + type + " HTTP/1.1");
    client.println(serverIPHost);
    client.println();
  }else
    Serial.println("no connection");

  numberOfMinutes = 0;
  alarmPermission = false;
}

void connectToWiFi(){
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  Serial.println("Checking Wi-fi status ...");
  // check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    // don't continue
    while (true);
  }
  String fv = WiFi.firmwareVersion();
  if (fv < "1.0.0") {
    Serial.println("Please upgrade the firmware");
  }

  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {
    Serial.print("Attempting to connect to WPA SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network:
    status = WiFi.begin(ssid, pass);

    // wait 10 seconds for connection:
    delay(10000);
  }
  // you're connected now, so print out the data:
  Serial.print("You're connected to the network");
  printCurrentNet();
  printWifiData();
}

void printWifiData() {
  // print your board's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);
}

void printCurrentNet() {
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print the MAC address of the router you're attached to:
  byte bssid[6];
  WiFi.BSSID(bssid);
  Serial.print("BSSID: ");

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.println(rssi);

  // print the encryption type:
  byte encryption = WiFi.encryptionType();
  Serial.print("Encryption Type:");
  Serial.println(encryption, HEX);
  Serial.println();
}

void getBatteryVoltage()
{
  int sensorValue = analogRead(A5); 
  voltage = sensorValue * (5.00 / 1023.00) * 2; //convert the value to a voltage.
  float tempV = getHoursLeft(roundVoltage(voltage));
  if(tempV != 0.0)
    hours = getHoursLeft(roundVoltage(voltage));
  else
    hours = "na";
    
  if (voltage < 6.5 && voltage > 0.5) //voltage considered low battery, voltage <=0.5 indicates the absence of signal, it has to be considered attached to usb
  {
    enterPowerSavingMode();
  }
}

float roundVoltage(float voltage){
  double integral;
  double fractional = modf(voltage, &integral);
  Serial.println(fractional);
  if(fractional == 0 || fractional == 5)
    return voltage;
  else 
    return round(voltage);
}

void enterPowerSavingMode(){
  noInterrupts(); //Once we have low battery low power mode is mandatory, no interruption allowed anymore
  criticalPowerMode = false
  timeToSense = 5000;
}

//draw battery level in position x,y
void batterylevel(int xpos,int ypos)
{
  //read the voltage and convert it to volt
  float curvolt = voltage;
 
  if(curvolt <= 0.5)
  {
    byte batlevel[8] = {
    B01110,
    B11111,
    B10101,
    B10001,
    B11011,
    B11011,
    B11111,
    B11111,
    };
    lcd.createChar(0 , batlevel);
    lcd.setCursor(xpos,ypos);
    lcd.write(byte(0));
  }
  if(curvolt >= 9.0)
  {
    byte batlevel[8] = {
    B01110,
    B11111,
    B11111,
    B11111,
    B11111,
    B11111,
    B11111,
    B11111,
    };
    lcd.createChar(0 , batlevel);
    lcd.setCursor(xpos,ypos);
    lcd.write(byte(0));
  }
  if(curvolt < 9.0 && curvolt >= 8.5)
  {
    byte batlevel[8] = {
    B01110,
    B10001,
    B11111,
    B11111,
    B11111,
    B11111,
    B11111,
    B11111,
    };
    lcd.createChar(0 , batlevel);
    lcd.setCursor(xpos,ypos);
    lcd.write(byte(0));
  }
  if(curvolt < 8.5 && curvolt >= 7.0)
  {
    byte batlevel[8] = {
    B01110,
    B10001,
    B10001,
    B11111,
    B11111,
    B11111,
    B11111,
    B11111,
    };
    lcd.createChar(0 , batlevel);
    lcd.setCursor(xpos,ypos);
    lcd.write(byte(0));
  }
  if(curvolt < 7.0 && curvolt >= 6.5)
  {
    byte batlevel[8] = {
    B01110,
    B10001,
    B10001,
    B10001,
    B11111,
    B11111,
    B11111,
    B11111,
    };
    lcd.createChar(0 , batlevel);
    lcd.setCursor(xpos,ypos);
    lcd.write(byte(0));
  }
  if(curvolt < 6.5 && curvolt > 0.5)
  {
    byte batlevel[8] = {
    B01110,
    B10001,
    B10001,
    B10001,
    B10001,
    B10001,
    B10001,
    B11111,
    };
    lcd.createChar(0 , batlevel);
    lcd.setCursor(xpos,ypos);
    lcd.write(byte(0));
  }
}
