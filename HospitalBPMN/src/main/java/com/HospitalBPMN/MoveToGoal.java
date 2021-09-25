package com.HospitalBPMN;

import com.HospitalBPMN.MQTT.IotModules;
import com.HospitalBPMN.MQTT.ModuleType;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

import static com.HospitalBPMN.MQTT.IotModules.Reconnect;

@Component
public class MoveToGoal implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
       try {
           System.out.println( "MoveToGoal" );
           String nameExecutor = (String) delegateExecution.getVariable("nameExecutor");
           String nextGoal = (String) delegateExecution.getVariable("nextGoal");

           Thread.sleep(150);
           IotModules.myCallback.StackMessageClear();
           IotModules.InDevicesData(ModuleType.delivery, nameExecutor, "<Task><Goal>" + nextGoal + "</Goal></Task>");
           IotModules.waitingStatusСonfirmed(nameExecutor);

       }
       catch (Exception ex){

           boolean coonectionStatus =  IotModules.Reconnect();

           System.out.println( "Error MoveToGoal:"+ex.getMessage() );
           throw new BpmnError("MoveToGoalError");

       }
    }
}
