#include <Arduino.h>
//#include "BluetoothSerial.h"
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define temperatureCelsius

// даем название BLE-серверу:
#define bleServerName "dhtESP32"

#define SERVICE_UUID "91bad492-b950-4226-aa2b-4ede9fa42f59"

BLECharacteristic dhtTemperatureCelsiusCharacteristics("cba1d466-344c-4be3-ab3f-189f80dd7518", BLECharacteristic::PROPERTY_NOTIFY);
BLEDescriptor dhtTemperatureCelsiusDescriptor(BLEUUID((uint16_t)0x2902));

BLECharacteristic dhtHumidityCharacteristics("ca73b3ba-39f6-4ab3-91ae-186dc9577d99", BLECharacteristic::PROPERTY_NOTIFY);
BLEDescriptor dhtHumidityDescriptor(BLEUUID((uint16_t)0x2903));

bool deviceConnected = false;

// задаем функции обратного вызова onConnect() и onDisconnect():
class MyServerCallbacks : public BLEServerCallbacks
{
  void onConnect(BLEServer *pServer)
  {
    deviceConnected = true;
  };
  void onDisconnect(BLEServer *pServer)
  {
    deviceConnected = false;
  }
};

void setup()
{

  // запускаем последовательную коммуникацию:
  Serial.begin(115200);

  // создаем BLE-устройство:
  BLEDevice::init(bleServerName);

  // создаем BLE-сервер:
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // создаем BLE-сервис:
  BLEService *dhtService = pServer->createService(SERVICE_UUID);

  // создаем BLE-характеристики и BLE-дескриптор: bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml

  dhtService->addCharacteristic(&dhtTemperatureCelsiusCharacteristics);
  dhtTemperatureCelsiusDescriptor.setValue("DHT temperature Celsius");
  //  "Температура в Цельсиях"
  dhtTemperatureCelsiusCharacteristics.addDescriptor(new BLE2902());

  dhtService->addCharacteristic(&dhtHumidityCharacteristics);
  dhtHumidityDescriptor.setValue("DHT humidity");
  //  "Влажность"
  dhtHumidityCharacteristics.addDescriptor(new BLE2902());

  // запускаем сервис:
  dhtService->start();

  // запускаем рассылку оповещений:
  pServer->getAdvertising()->start();
  Serial.println("Waiting a client connection to notify...");
  //  "Ждем подключения клиента, чтобы отправить уведомление..."
}

void loop()
{
  if (deviceConnected)
  {
    // считываем температуру в градусах Цельсия (по умолчанию):
    float t = 12.4;
    // считываем влажность:
    float h = 12.4;

    // отправляем уведомление о том,
    static char temperatureCTemp[7];
    dtostrf(t, 6, 2, temperatureCTemp);
    // задаем значение для температурной характеристики (Цельсий)
    // и отправляем уведомление подключенному клиенту:
    dhtTemperatureCelsiusCharacteristics.setValue(temperatureCTemp);
    dhtTemperatureCelsiusCharacteristics.notify();
    Serial.print("Temperature Celsius: ");
    //  "Температура в градусах Цельсия: "
    Serial.print(t);
    Serial.print(" *C");

    // отправляем уведомление о том,
    // что с датчика DHT считаны данные о влажности:
    static char humidityTemp[7];
    dtostrf(h, 6, 2, humidityTemp);
    // задаем значение для влажностной характеристики
    // и отправляем уведомление подключенному клиенту:
    dhtHumidityCharacteristics.setValue(humidityTemp);
    dhtHumidityCharacteristics.notify();
    Serial.print(" - Humidity: ");
    //  " - Влажность: "
    Serial.print(h);
    Serial.println(" %");

    delay(10000);
  }
}
// #define LED_BUILTIN 0

// //BluetoothSerial ESP_BT;
// void BlincCount(int count, int time);

// BLEServer *pServer = NULL;
// BLECharacteristic * pTxCharacteristic;
// bool deviceConnected = false;
// bool oldDeviceConnected = false;
// uint8_t txValue = 0;

// #define SERVICE_UUID           "6E400001-B5A3-F393-E0A9-E50E24DCCA9E" // UART service UUID
// #define CHARACTERISTIC_UUID_RX "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
// #define CHARACTERISTIC_UUID_TX "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"

// class MyServerCallbacks: public BLEServerCallbacks {

//     void onConnect(BLEServer* pServer) {
//       deviceConnected = true;
//     };

//     void onDisconnect(BLEServer* pServer) {
//       deviceConnected = false;
//     }
// };

// class MyCallbacks: public BLECharacteristicCallbacks {
//     void onWrite(BLECharacteristic *pCharacteristic) {
//       std::string rxValue = pCharacteristic->getValue();

//       if (rxValue.length() > 0) {
//         Serial.println("*********");
//         Serial.print("Received Value: ");
//         for (int i = 0; i < rxValue.length(); i++)
//           Serial.print(rxValue[i]);

//         Serial.println();
//         Serial.println("*********");
//       }
//     }
// };

// void setup() {
//   Serial.begin(9600);

//   // Create the BLE Device
//   BLEDevice::init("BLE tracker");

//   // Create the BLE Server
//   pServer = BLEDevice::createServer();
//   pServer->setCallbacks(new MyServerCallbacks());

//   // Create the BLE Service
//   BLEService *pService = pServer->createService(SERVICE_UUID);

//   // Create a BLE Characteristic
//   pTxCharacteristic = pService->createCharacteristic(
// 										CHARACTERISTIC_UUID_TX,
// 										BLECharacteristic::PROPERTY_NOTIFY
// 									);

//   pTxCharacteristic->addDescriptor(new BLE2902());

//   BLECharacteristic * pRxCharacteristic = pService->createCharacteristic(
// 											 CHARACTERISTIC_UUID_RX,
// 											BLECharacteristic::PROPERTY_WRITE
// 										);

//   pRxCharacteristic->setCallbacks(new MyCallbacks());

//   // Start the service
//   pService->start();

//   // Start advertising
//   pServer->getAdvertising()->start();
//   Serial.println("Waiting a client connection to notify...");

// }

// void loop() {

//     if (deviceConnected) {
//         pTxCharacteristic->setValue(&txValue, 1);
//         pTxCharacteristic->notify();
//         txValue++;
//         Serial.print("C");
// 		delay(10); // bluetooth stack will go into congestion, if too many packets are sent
// 	}

//     // disconnecting
//     if (!deviceConnected && oldDeviceConnected) {
//         delay(500); // give the bluetooth stack the chance to get things ready
//         pServer->startAdvertising(); // restart advertising
//         Serial.println("start advertising");
//         oldDeviceConnected = deviceConnected;
//     }
//     // connecting
//     if (deviceConnected && !oldDeviceConnected) {
// 		// do stuff here on connecting
//         oldDeviceConnected = deviceConnected;
//     }
//     // Serial.println();
//    // delay(100);
// }

// void BlincCount(int count, int time)
// {
//   for (int i = 0; i < count; i++)
//   {
//     digitalWrite(LED_BUILTIN, 1);
//     delay(time);
//     digitalWrite(LED_BUILTIN, 0);
//     delay(time * 2);
//     digitalWrite(LED_BUILTIN, 1);
//     delay(time);
//     digitalWrite(LED_BUILTIN, 0);
//   }
// }