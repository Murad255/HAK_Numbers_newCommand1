package com.HospitalBPMN.MQTT;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.eclipse.paho.client.mqttv3.*;

import javax.swing.plaf.PanelUI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import java.util.*;

public  class IotModules {
    public static MqttAsyncClient myClient;
    public static MqttDevice myCallback;
    public static ArrayList<String> RobotArmName;

    // static String serverUrl = "tcp://172.16.1.33:1883";
   //static String serverUrl ="tcp://mqtt.eclipseprojects.io:1883";
    static String serverUrl = "tcp://172.16.2.25:1883";
    static String login = "user";
    static String password = "quantumsystems";

    static String inDevices = "userM/devices/in";
    static String outDevices = "userM/devices/out";

    static String inWatcher = "userM/watcher/in";
    static String outWatcher = "userM/watcher/out";

    static int Qos = 0;
    static int UID = 0;
    public static  String button1Name="button1";
    public static  String RobotArm1Name;
    static  public boolean ExecutionTaskFlag=false;

    static public void Begin(ProcessEngine delegate) throws Exception{
        myClient = new MqttAsyncClient(serverUrl, UUID.randomUUID().toString());

        myCallback = new MqttDevice(delegate);
        myClient.setCallback(myCallback);

        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setUserName(login);
        conOpt.setPassword(password.toCharArray());

        IMqttToken token = myClient.connect(conOpt);
        token.waitForCompletion();

        myClient.subscribe(outDevices, Qos);
        myClient.subscribe(outWatcher, Qos);
        RobotArmName = new ArrayList<String>();

    }

    public static  boolean Reconnect() throws MqttException {

        try {
            MqttConnectOptions conOpt = new MqttConnectOptions();
            conOpt.setUserName(login);
            conOpt.setPassword(password.toCharArray());

            IMqttToken token = myClient.connect(conOpt);
            token.waitForCompletion();

            myClient.subscribe(outDevices, Qos);
            myClient.subscribe(outWatcher, Qos);
        }
        catch (Exception ex){
            return  false;
        }
        return  true;
    }
    static public  void InDevices(String message)throws Exception {
        myClient.publish(inDevices,new MqttMessage(message.getBytes()) );
    }
    static public  void InDevices(String message, String topik)throws Exception {
        myClient.publish(topik,new MqttMessage(message.getBytes()) );
    }
    static public  void InDevicesData(String Name, String data)throws Exception {
        String msg = "<Module><Name>"+Name+"</Name><UID>" + UID +
                "</UID><ModuleType>all</ModuleType><Data>" + data +
                "</Data></Module>";

        myClient.publish(inDevices,new MqttMessage(msg.getBytes()) );
        UID++;
    }
    static public  void InDevicesData(ModuleType moduleType, String Name, String data)throws Exception {
        String msg = "<Module><Name>"+Name+"</Name><ModuleType>"+moduleType.toString()+"</ModuleType><UID>" + UID +
                "</UID> <Data>" + data +
                "</Data></Module>";

        myClient.publish(inDevices,new MqttMessage(msg.getBytes()) );
        UID++;
    }

    static public  void InWatchCleent(String message)throws Exception {
        myClient.publish(inWatcher,new MqttMessage(message.getBytes()) );
    }


    //ожидание принятия сообщения "start" с смартфона-клиента
    public static void WatсherAvailable()throws Exception {
        InWatchCleent("ожидание запуска");
        myCallback.pastMessageClear();
        while(true){
            Thread.sleep(10);
            if(myCallback.pastMessage.getToken().equals(outWatcher)){
                String str= myCallback.pastMessage.getMessage();
                if(str.equals("start"))return;
            }
        }
    }

//    // todo
//    public static boolean RollCall()throws Exception{
//        myCallback.StackMessageClear();
//        IotModules.InDevicesData(ModuleType.all,"all","<Request>status</Request>");
//        Thread.sleep(500);
//       if( myCallback.getStackMessage().empty())return false;
//
//       while (!myCallback.getStackMessage().empty()){
//           Message mes = myCallback.getStackMessage().pop();
//           if(mes.getToken().equals(outDevices)){
//               String name = findText(mes.getMessage(),"Name");
//               String ModuleType = findText(mes.getMessage(),"ModuleType");
//               if((name.length()>0)&&(ModuleType.length()>0)){
//                   int type = Integer.parseInt(ModuleType);
//
//                   }
//               }
//           }
//       return false;
//    }

    public static String SelectRobotArm()throws Exception{
        String nameExecutor;
        Stack<String> stackMessage = new Stack<String>();
        int numPast = IotModules.myCallback.getStackMessage().size();
        IotModules.InDevicesData(ModuleType.robotArm,"all","<Request>status</Request>");
        Thread.sleep(800);

        int newMessagesCount = IotModules.myCallback.getStackMessage().size()-numPast;
        if(newMessagesCount<=0) throw  new Exception("Robot Arm not Found");

        for(int i = 0; i < newMessagesCount ; i++){
            Message mes = IotModules.myCallback.getStackMessage().pop();
            if(mes.isModule()){
                if(mes.getName().length()>0 && mes.getStatus()==0&&mes.getModuleType()==ModuleType.robotArm) stackMessage.add(mes.getName());
            }
        }

        if(stackMessage.size()>0){
            if(stackMessage.size()==1)     return  stackMessage.pop();
            else {
                final Random random = new Random();
                int randNum = random.nextInt()%stackMessage.size();
                return stackMessage.get(randNum);
            }
        }
        else throw  new Exception("Robot Arm not Found");

    }

    //проверить отправку статуса
    public static boolean  statusСonfirmed(String name ){
        Message outMes = myCallback.findMessage(name);
        if(outMes==null) return  false;
        if(outMes.getStatus()==0)return  true;
        else return false;
    }

    public static boolean  statusСonfirmed(String name,int howStatus ){
        Message outMes = myCallback.findMessage(name);
        if(outMes==null) return  false;
        if(outMes.getStatus()==howStatus)return  true;
        else return false;
    }

    //ожидание статуса 0 (устройство свободно для команд)
    public static void  waitingStatusСonfirmed(String name ) throws Exception {
        boolean status;
        int statusCounter = 0;
        InDevicesData(ModuleType.all,name,"<Request>status</Request>");
        do{
            status = statusСonfirmed(name);
            if(statusCounter++>15){
                statusCounter=0;
                InDevicesData(ModuleType.all,name,"<Request>status</Request>");
            }
            Thread.sleep(150);
        }while (!status);
    }

    public static boolean  waitStatusСonfirmed(String name,int mulles )  {

        try {
            InDevicesData(ModuleType.all,button1Name,"<Request>status</Request>");
            //проверяем ответ 10 раз в течение периода приёма
            for (int i = 0; i<10; i++){
                Thread.sleep(mulles/10);
                if(statusСonfirmed(name)) return  true;
            }
            return false;
        }
        catch (Exception ex){
            return false;
        }
    }

    public static boolean ButtonIsPressed() throws Exception {
        //myCallback.pastMessageClear();
        Message mes =  myCallback.findMessage(button1Name);
        if (findText( mes.getData(), "Pin16").equals("1"))return  true;
        else  return false;
    }

    public static String findText(String str, String findContext)throws  Exception
    {
        int find1 = str.indexOf("<" + findContext + ">");
        int find2 = str.indexOf("</" + findContext + ">");
        String findStr = "";
        for (int i = find1 + ("<" + findContext + ">").length(); i < find2; i++)
        {
            findStr += (char)str.getBytes()[i];
        }

        return findStr;
    }

}