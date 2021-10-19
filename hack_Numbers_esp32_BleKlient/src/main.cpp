/*
    программа для определения уровня сигнала BLE 
    и отправки его на сервер
*/

#include <Arduino.h>

#define WIFI_SSID "Logo"
#define WIFI_PASSWORD "040303b"
#define POST_URL "http://192.168.250.40:3000/"
#define SCAN_TIME 30     // seconds
#define WAIT_WIFI_LOOP 5 // around 4 seconds for 1 loop
#define SLEEP_TIME 300   // seconds

#include <Arduino.h>
#include <sstream>

#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEScan.h>
#include <BLEAdvertisedDevice.h>

#include <WiFi.h>
#include <WiFiMulti.h>
#include <esp_wifi.h>

#include <HTTPClient.h>

#include "soc/soc.h"
#include "soc/rtc_cntl_reg.h"

WiFiMulti wifiMulti;
std::stringstream ss;
bool data_sent = false;
int wait_wifi_counter = 0;

class MyAdvertisedDeviceCallbacks : public BLEAdvertisedDeviceCallbacks
{
  void onResult(BLEAdvertisedDevice advertisedDevice)
  {
    log_i("Advertised Device: %s \n", advertisedDevice.toString().c_str());
  }
};

void setup()
{

  Serial.begin(9600);
  Serial.println("ESP32 BLE Scanner");

  // disable brownout detector to maximize battery life
  Serial.println("disable brownout detector");
  WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0);

  Serial.println("BLEDevice::init()");
  BLEDevice::init("");
  //с синтековским чипом
  // put your main code here, to run repeatedly:
  BLEScan *pBLEScan = BLEDevice::getScan(); //create new scan
  pBLEScan->setAdvertisedDeviceCallbacks(new MyAdvertisedDeviceCallbacks());
  pBLEScan->setActiveScan(true); //active scan uses more power, but get results faster
  pBLEScan->setInterval(0x50);
  pBLEScan->setWindow(0x30);

  Serial.println("Start BLE scan for %d seconds...\n");

  BLEScanResults foundDevices = pBLEScan->start(SCAN_TIME);
  int count = foundDevices.getCount();
  ss << "[";
  for (int i = 0; i < count; i++)
  {
    if (i > 0)
    {
      ss << ",";
    }
    BLEAdvertisedDevice d = foundDevices.getDevice(i);
    ss << "{\"Address\":\"" << d.getAddress().toString() << "\",\"Rssi\":" << d.getRSSI();

    if (d.haveName())
    {
      ss << ",\"Name\":\"" << d.getName() << "\"";
    }

    if (d.haveAppearance())
    {
      ss << ",\"Appearance\":" << d.getAppearance();
    }

    if (d.haveManufacturerData())
    {
      std::string md = d.getManufacturerData();
      uint8_t *mdp = (uint8_t *)d.getManufacturerData().data();
      char *pHex = BLEUtils::buildHexData(nullptr, mdp, md.length());
      ss << ",\"ManufacturerData\":\"" << pHex << "\"";
      free(pHex);
    }

    if (d.haveServiceUUID())
    {
      ss << ",\"ServiceUUID\":\"" << d.getServiceUUID().toString() << "\"";
    }

    if (d.haveTXPower())
    {
      ss << ",\"TxPower\":" << (int)d.getTXPower();
    }

    ss << "}";
  }
  ss << "]";

  Serial.println("Scan done!");

  wifiMulti.addAP(WIFI_SSID, WIFI_PASSWORD);
}

void loop()
{
  // wait for WiFi connection
  if ((wifiMulti.run() == WL_CONNECTED))
  {
    Serial.println("WiFi Connected");

    // HTTP POST BLE list
    HTTPClient http;

    Serial.println("[HTTP] Payload:\n%s", ss.str().c_str());
    Serial.println("[HTTP] Begin...");

    // configure traged server and url
    http.begin(POST_URL);

    // start connection and send HTTP header
    int httpCode = http.POST(ss.str().c_str());

    // httpCode will be negative on error
    if (httpCode > 0)
    {
      // HTTP header has been send and Server response header has been handled
      Serial.println("[HTTP] GET... code: %d\n", httpCode);

      // file found at server
      if (httpCode == HTTP_CODE_OK)
      {
        Serial.println("[HTTP] Response:\n%s", http.getString());
      }
    }
    else
    {
      Serial.println("[HTTP] GET... failed, error: %s\n", http.errorToString(httpCode).c_str());
    }

    http.end();
    data_sent = true;
  }

  // wait WiFi connected
  if (data_sent || (wait_wifi_counter > WAIT_WIFI_LOOP))
  {
    esp_sleep_enable_timer_wakeup(SLEEP_TIME * 1000000); // translate second to micro second

    Serial.println("Enter deep sleep for %d seconds...\n", SLEEP_TIME);

    esp_wifi_stop();
    esp_deep_sleep_start();
  }
  else
  {
    wait_wifi_counter++;

    Serial.println("Waiting count: %d\n", wait_wifi_counter);
  }
}
// /*********
//   Руи Сантос
//   Более подробно о проекте на: http://randomnerdtutorials.com
// *********/

// #include "BLEDevice.h"

// // по умолчанию температура будет в градусах Цельсия,
// // но если вам нужны градусы Фаренгейта, закомментируйте строчку ниже:
// #define temperatureCelsius

// // задаем название для BLE-сервера
// // (это другая ESP32, на которой запущен серверный скетч):
// #define bleServerName "dhtESP32"

// // UUID для сервиса:
// static BLEUUID dhtServiceUUID("91bad492-b950-4226-aa2b-4ede9fa42f59");

//   // UUID для температурной характеристики (градусы Цельсия):
//   static BLEUUID temperatureCharacteristicUUID("cba1d466-344c-4be3-ab3f-189f80dd7518");

// // UUID для влажностной характеристики:
// static BLEUUID humidityCharacteristicUUID("ca73b3ba-39f6-4ab3-91ae-186dc9577d99");

// // переменные, используемые для определения того,
// // нужно ли начинать подключение или завершено ли подключение:
// static boolean doConnect = false;
// static boolean connected = false;

// // адрес периферийного устройства;
// // (он должен быть найден во время сканирования):
// static BLEAddress *pServerAddress;

// // характеристики, данные которых мы хотим прочесть:
// static BLERemoteCharacteristic* temperatureCharacteristic;
// static BLERemoteCharacteristic* humidityCharacteristic;

// // включение/выключение уведомлений:
// const uint8_t notificationOn[] = {0x1, 0x0};
// const uint8_t notificationOff[] = {0x0, 0x0};

// // подключаемся к BLE-серверу,
// // у которого есть название, сервис и характеристики:
// bool connectToServer(BLEAddress pAddress) {
//    BLEClient* pClient = BLEDevice::createClient();

//   // подключаемся к удаленному BLE-серверу:
//   pClient->connect(pAddress);
//   Serial.println(" - Connected to server");
//              //  " – Подключились к серверу"

//   // считываем UUID искомого сервиса:
//   BLERemoteService* pRemoteService = pClient->getService(dhtServiceUUID);
//   if (pRemoteService == nullptr) {
//     Serial.print("Failed to find our service UUID: ");
//              //  "Не удалось найти UUID нашего сервиса: "
//     Serial.println(dhtServiceUUID.toString().c_str());
//     return (false);
//   }

//   // считываем UUID искомых характеристик:
//   temperatureCharacteristic = pRemoteService->getCharacteristic(temperatureCharacteristicUUID);
//   humidityCharacteristic = pRemoteService->getCharacteristic(humidityCharacteristicUUID);

//   if (temperatureCharacteristic == nullptr || humidityCharacteristic == nullptr) {
//     Serial.print("Failed to find our characteristic UUID");
//              //  "Не удалось найти UUID нашей характеристики"
//     return false;
//   }
//   Serial.println(" - Found our characteristics");
//              //  " – Наши характеристики найдены"

//   // присваиваем характеристикам функции обратного вызова:
//   temperatureCharacteristic->registerForNotify(temperatureNotifyCallback);
//   humidityCharacteristic->registerForNotify(humidityNotifyCallback);
// }

// // функция обратного вызова, которая будет вызвана
// // при получении оповещения от другого устройства:
// class MyAdvertisedDeviceCallbacks: public BLEAdvertisedDeviceCallbacks {
//   void onResult(BLEAdvertisedDevice advertisedDevice) {
//     // проверяем, совпадает ли название
//     // BLE-сервера, рассылающего оповещения:
//     if (advertisedDevice.getName() == bleServerName) {
//       // мы нашли, что искали,
//       // поэтому сканирование можно завершить:
//       advertisedDevice.getScan()->stop();
//       // сохраняем адрес устройства, рассылающего оповещения:
//       pServerAddress = new BLEAddress(advertisedDevice.getAddress());
//       // задаем индикатор, дающий понять,
//       // что мы готовы подключиться:
//       doConnect = true;
//       Serial.println("Device found. Connecting!");
//                  //  "Устройство найдено. Подключаемся!"
//     }
//   }
// };

// // функция обратного вызова, которая будет запущена,
// // если BLE-сервер пришлет вместе с уведомлением
// // корректные данные о температуре:
// static void temperatureNotifyCallback(BLERemoteCharacteristic* pBLERemoteCharacteristic,
//                                         uint8_t* pData, size_t length, bool isNotify) {
//   display.setCursor(34,10);
//   display.print((char*)pData);
//   Serial.print("Temperature: ");  //  "Температура: "
//   Serial.print((char*)pData);
//   #ifdef temperatureCelsius
//     // температура в градусах Цельсия:
//     display.print(" *C");
//     Serial.print(" *C");
//   #else
//     // температура в градусах Фаренгейта:
//     display.print(" *F");
//     Serial.print(" *F");
//   #endif
//   display.display();
// }

// // функция обратного вызова, которая будет запущена,
// // если BLE-сервер пришлет вместе с уведомлением
// // корректные данные о влажности:
// static void humidityNotifyCallback(BLERemoteCharacteristic* pBLERemoteCharacteristic,
//                                     uint8_t* pData, size_t length, bool isNotify) {
//   display.setCursor(34,20);
//   display.print((char*)pData);
//   display.print(" %");
//   display.display();
//   Serial.print(" Humidity: ");  //  " Влажность: "
//   Serial.print((char*)pData);
//   Serial.println(" %");
// }

// void setup() {
//   // настраиваем OLED-дисплей;
//   // параметр «SSD1306_SWITCHCAPVCC» в функции begin() задает,
//   // что напряжение для дисплея будет генерироваться
//   // от внутренней 3.3-вольтовой цепи,
//   // а параметр «0x3C» означает «128x32»:
//   if(!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
//     Serial.println(F("SSD1306 allocation failed"));
//                  //  "Не удалось настроить SSD1306"
//     for(;;); // дальше не продолжаем,
//              // навечно оставшись в блоке loop()
//   }

//   display.clearDisplay();
//   display.setTextSize(1);
//   //display.setBackgroundcolor(BLACK);
//   display.setTextColor(WHITE,0);
//   display.setCursor(30,0);
//   display.print("DHT READINGS");  //  "Данные от DHT-датчика"
//   display.display();

//   // запускаем последовательную коммуникацию:
//   Serial.begin(115200);
//   Serial.println("Starting Arduino BLE Client application...");
//              //  "Запуск клиентского BLE-приложения... "

//   // инициализируем BLE-устройство:
//   BLEDevice::init("");

//   // создаем экземпляр класса «BLEScan» для сканирования
//   // и задаем для этого объекта функцию обратного вызова,
//   // которая будет информировать о том, найдено ли новое устройство;
//   // дополнительно указываем, что нам нужно активное сканирование,
//   // а потом запускаем 30-секундное сканирование:
//   BLEScan* pBLEScan = BLEDevice::getScan();
//   pBLEScan->setAdvertisedDeviceCallbacks(new MyAdvertisedDeviceCallbacks());
//   pBLEScan->setActiveScan(true);
//   pBLEScan->start(30);
// }

// void loop() {
//   // если в переменной «doConnect» значение «true»,
//   // то это значит, что сканирование завершено,
//   // и мы нашли нужный BLE-сервер, к которому хотим подключиться;
//   // теперь пора, собственно, подключиться к нему;
//   // подключившись, мы записываем в «connected» значение «true»:
//   if (doConnect == true) {
//     if (connectToServer(*pServerAddress)) {
//       Serial.println("We are now connected to the BLE Server.");
//                  //  "Подключение к BLE-серверу прошло успешно."
//       // активируем свойство «notify» у каждой характеристики:
//       temperatureCharacteristic->getDescriptor(BLEUUID((uint16_t)0x2902))->writeValue((uint8_t*)notificationOn, 2, true);
//       humidityCharacteristic->getDescriptor(BLEUUID((uint16_t)0x2902))->writeValue((uint8_t*)notificationOn, 2, true);
//       connected = true;
//     } else {
//       Serial.println("We have failed to connect to the server; Restart your device to scan for nearby BLE server again.");
//                  //  "Подключиться к серверу не получилось.
//                  //   Перезапустите устройство, чтобы снова
//                  //   просканировать ближайший BLE-сервер."
//     }
//     doConnect = false;
//   }
//   delay(1000); // делаем секундную задержку между циклами loop()
// }