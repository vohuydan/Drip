#include <Arduino.h>
#include <SPI.h>
#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"
#include <Adafruit_CircuitPlayground.h>

#include "BluefruitConfig.h"

#if SOFTWARE_SERIAL_AVAILABLE
  #include <SoftwareSerial.h>
#endif


// Strings to compare incoming BLE messages
String start = "start";
String red = "red";
String readtemp = "temp";
String stp = "stop";
String off = "off";

int  sensorTemp = 0;

/*=========================================================================
    APPLICATION SETTINGS
    -----------------------------------------------------------------------*/
    #define FACTORYRESET_ENABLE         0
    #define MINIMUM_FIRMWARE_VERSION    "0.6.6"
    #define MODE_LED_BEHAVIOUR          "MODE"
/*=========================================================================*/

// Create the bluefruit object, either software serial...uncomment these lines

Adafruit_BluefruitLE_UART ble(BLUEFRUIT_HWSERIAL_NAME, BLUEFRUIT_UART_MODE_PIN);

/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
// Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

/* ...software SPI, using SCK/MOSI/MISO user-defined SPI pins and then user selected CS/IRQ/RST */
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_SCK, BLUEFRUIT_SPI_MISO,
//                             BLUEFRUIT_SPI_MOSI, BLUEFRUIT_SPI_CS,
//                             BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);


// A small helper to show errors on the serial monitor
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}

bool leftButtonPressed;
bool rightButtonPressed;
bool currleft;
bool currright;
const int float1 = 9;
bool currfloat1;
const int float2 = 6;
bool currfloat2;
int buttonState = 0; 
void setup(void)
{
  
  CircuitPlayground.begin();
  pinMode(float1, INPUT_PULLUP);
  pinMode(float2, INPUT_PULLUP);
  currleft = false;
  currright = false;
  currfloat1 = false;
  currfloat2 =false;
  Serial.begin(115200);

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );

  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset"));
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  Serial.println(F("Please use Adafruit Bluefruit LE app to connect in UART mode"));
  Serial.println(F("Then Enter characters to send to Bluefruit"));
  Serial.println();

  ble.verbose(false);  // debug info is a little annoying after this point!

  /* Wait for connection */
  while (! ble.isConnected()) {
      delay(500);
  }
Serial.println("CONECTED:");
  Serial.println(F("******************************"));

  // LED Activity command is only supported from 0.6.6
  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
    // Change Mode LED Activity
    Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }

  // Set module to DATA mode
  Serial.println( F("Switching to DATA mode!") );
  ble.setMode(BLUEFRUIT_MODE_DATA);

  Serial.println(F("******************************"));
 
  CircuitPlayground.setPixelColor(20,20,20,20);
 
  delay(1000);
}
/**************************************************************************/
/*!
   Constantly poll for new command or response data
*/
/**************************************************************************/
void loop(void)
{
  // Save received data to string
  String received = "";
  while ( ble.available() )
  {
    int c = ble.read();
    Serial.print((char)c);
    received += (char)c;
        delay(50);
  }

  leftButtonPressed = CircuitPlayground.leftButton();
  rightButtonPressed = CircuitPlayground.rightButton();

  if (leftButtonPressed && currleft ==false) {
    Serial.print("Left Button: pressed");
    currleft = true;
    char output[8];
    String data = "left";
    Serial.println(data);
    Serial.println();
    data.toCharArray(output,8);
    ble.print(data);
    Serial.println();

    for(int i = 0; i < 11; i++){
      CircuitPlayground.setPixelColor(i,0, 44, 44);
    }
  }else if(leftButtonPressed && currleft == true){
    for(int i = 0; i < 11; i++){
      CircuitPlayground.setPixelColor(i,0, 44, 44);
    }
  }else if(!leftButtonPressed && currleft == true){
    Serial.print("Left Button: Not pressed");
    currleft = false;
    Serial.println();
    CircuitPlayground.clearPixels();
  }

  if (rightButtonPressed && currright ==false) {
    Serial.print("Right Button: pressed");
    currright = true;
    char output[8];
    String data = "right";
    Serial.println(data);
    Serial.println();
    data.toCharArray(output,8);
    ble.print(data);
    Serial.println();

    for(int i = 0; i < 11; i++){
      CircuitPlayground.setPixelColor(i,44, 44, 0);
    }
  }else if(rightButtonPressed && currright == true){
    for(int i = 0; i < 11; i++){
      CircuitPlayground.setPixelColor(i,44, 44, 0);
    }
  }else if(!rightButtonPressed && currright == true){
    Serial.print("Left Button: Not pressed");
    currright = false;
    Serial.println();
    CircuitPlayground.clearPixels();
  }

  if (digitalRead(float1) == LOW  && currfloat1 ==false){
    Serial.println("Float 1: pressed");
    currfloat1 = true;
    char output[8];
    String data = "float1";
    Serial.println(data);
    Serial.println();
    data.toCharArray(output,8);
    ble.print(data);
    Serial.println();
  }else if (digitalRead(float1) == HIGH && currfloat1 == true){
    currfloat1= false;
    Serial.println("Float 1: off");
  }
  
  if (digitalRead(float2) == LOW  && currfloat2 ==false){
    Serial.println("Float 2: pressed");
    currfloat2 = true;
    char output[8];
    String data = "float2";
    Serial.println(data);
    Serial.println();
    data.toCharArray(output,8);
    ble.print(data);
    Serial.println();
  }else if (digitalRead(float2) == HIGH && currfloat2 == true){
    currfloat2= false;
    Serial.println("Float 2: off");
  }
  
  if(red == received){
    Serial.println("RECEIVED RED!!!!"); 
       for(int i = 0; i < 11; i++){
      CircuitPlayground.setPixelColor(i,221, 44, 44);
    }
    delay(50);
  }else if(off == received){
    Serial.println("RECEIVED OFF!!!!"); 
      CircuitPlayground.clearPixels();

  }
 
  else if(readtemp == received){
       
    sensorTemp = CircuitPlayground.temperature(); // returns a floating point number in Centigrade
    Serial.println("Read temperature sensor"); 
    delay(10);

   //Send data to Android Device
    char output[8];
    String data = "";
    data += sensorTemp;
    Serial.println(data);
    data.toCharArray(output,8);
    ble.print(data);
  }
 
  else if (stp == received){
      CircuitPlayground.clearPixels();

    }
    
  }

  
 

 
