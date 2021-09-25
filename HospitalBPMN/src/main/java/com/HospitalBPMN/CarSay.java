package com.HospitalBPMN;

import com.HospitalBPMN.MQTT.IotModules;
import com.HospitalBPMN.MQTT.ModuleType;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class CarSay implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            System.out.println( "CarSay" );
            String nameExecutor = (String) delegateExecution.getVariable("nameExecutor");
            String spokenText = (String) delegateExecution.getVariable("spokenText");
            IotModules.myCallback.StackMessageClear();
            IotModules.InDevicesData(ModuleType.delivery, nameExecutor, "<Task> say " + spokenText + "</Task>");
            IotModules.waitingStatusСonfirmed(nameExecutor);

        }
        catch (Exception ex){
            try {

                IotModules.Begin(delegateExecution.getProcessEngine());
            }
            catch (Exception exe){}

            System.out.println( "Error MoveToGoal:"+ex.getMessage() );
            throw new BpmnError("MoveToGoalError");

        }
    }
}
