package com.HospitalBPMN.MQTT;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.HospitalBPMN.MQTT.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;



public class MqttDevice implements MqttCallback {

	Stack<Message> messages = new Stack<Message>();
	Map< String, Message> robotArmMessages = new HashMap< String, Message>();
	Map< String, Message> machineToolMessages = new HashMap< String, Message>();
	Map< String, Message> modulesMessages = new HashMap< String, Message>();

	private  ProcessEngine processEngine;
	private RuntimeService runtimeService;

	public Message pastMessage;
	public  MqttDevice(ProcessEngine process){
		processEngine=process;
	}

	public void pastMessageClear() throws Exception {
		pastMessage = new Message("","");
	}

	public void StackMessageClear(){messages.clear(); }

	public Stack<Message> getStackMessage(){
		return  messages;
	}

	public  Message findMessage(String Name){
		try {


			int len = messages.size();
			//если есть сообщения, просматриваем все
			while (len > 0) {
				Message mes = messages.get(len - 1);
				if (mes != null)
					if (mes.getName().equals(Name)) {
						messages.remove(mes);
						return mes;
					}
				len--;
			}
		}
		catch (Exception ex){
			System.out.println( "Error findMessage:"+ex.getMessage() );
			return null;
		}
		return null;
	}


	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {

		/*
		* Здесь выполняется обработка принимаемых данных
		*
		* Вызов по сигналу
		 runtimeService.signalEventReceived("Signal_2");
		*
		* Вызов по сообщению в подпроцессе
			runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
					runtimeService.messageEventReceived("Message_1", "Message_1");
		*
		* Вызов по сообщению в главном процессе
		runtimeService.createMessageCorrelation("startReplaseMessage").
							setVariable("name1", "value1")
							.correlate();
		*
		*
		* */
		System.out.println(topic+"\t"+message);
		Message msg= new Message(message.toString(),topic);
		messages.push(msg);
		pastMessage=msg;
		try {
			if (msg.getToken().equals("userM/watcher/out")) {
				runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();

				if (msg.getMessage().equals("start1")) {
					runtimeService.createMessageCorrelation("TestMessage").
							setVariable("name1", "value1")
							.correlate();
				}
				else if (msg.getMessage().equals("start")) {
					runtimeService.createMessageCorrelation("startReplaseMessage").
							setVariable("name1", "value1")
							.correlate();
				}
				else if (msg.getMessage().equals("stop")) {
					runtimeService.createMessageCorrelation("TerminateMessage").
							setVariable("name1", "value1")
							.correlate();
				}
				else if (msg.getMessage().equals("StartCar")) {
					runtimeService.createMessageCorrelation("DeliveryMessage").
							setVariable("nameExecutor", "RobotOmronCar")
							.setVariable("nextGoal", "BeforeS1")
							.setVariableLocal("nextGoal2", "BeforeS1")
							.correlate();				}
				else if (msg.getMessage().equals("StartCar2")) {

					runtimeService.createMessageCorrelation("DeliveryMessage3").
							setVariable("name1", "value1")
							.correlate();
				}
				else if(IotModules.findText(msg.getMessage(),"Watcher").length()>=0)
				{
					String Level = IotModules.findText(msg.getMessage(),"Level");
					String Table = IotModules.findText(msg.getMessage(),"Table");
					String Interaction = IotModules.findText(msg.getMessage(),"Interaction");
					String Goal = IotModules.findText(msg.getMessage(),"Goal");
					boolean ToGoal = Boolean.parseBoolean( IotModules.findText(msg.getMessage(),"ToGoal"));
					boolean ToHome = Boolean.parseBoolean(IotModules.findText(msg.getMessage(),"ToHome"));
					boolean GoBack = Boolean.parseBoolean(IotModules.findText(msg.getMessage(),"GoBack"));

					String liftHeigh;
					boolean isUp;

					switch (Integer.parseInt( Level)){
						case 0:
							liftHeigh = "0";
							break;
						case 1:
							liftHeigh = "620";
							break;
						case 2:
							liftHeigh = "360";
							break;
						case 3:
							liftHeigh = "90";
							break;
						default:
							liftHeigh = "0";

					}

					if(Interaction.equals("unload")) isUp=false;
					else isUp = true;

					//runtimeService.createMessageCorrelation("DeliveryMessage").
					runtimeService.createMessageCorrelation("DeliveryMessage").
							setVariable("nameExecutor", "RobotOmronCar")
							.setVariable("nextGoal", Goal)
							.setVariable("liftLevel", liftHeigh)
							.setVariable("isUp", isUp)
							.setVariable("nextMacro ", "connect")
							.setVariable("ToGoal", ToGoal)
							.setVariable("ToHome", ToHome)
							.setVariable("GoBack", GoBack)
							.correlate();
				}
			}
		}
		catch (Exception ex){
			System.out.println( "Error messageArrived:"+ex.getMessage() );

		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}


	
}
