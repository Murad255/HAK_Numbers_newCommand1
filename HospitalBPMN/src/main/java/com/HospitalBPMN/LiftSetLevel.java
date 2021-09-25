package com.HospitalBPMN;

import com.HospitalBPMN.MQTT.IotModules;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class LiftSetLevel implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        System.out.println( "LiftSetLevel" );
        String level = (String) delegateExecution.getVariable("liftLevel");

        try {
            Thread.sleep(150);
            IotModules.myCallback.StackMessageClear();
            IotModules.InDevices(level,"motor/pos");
            IotModules.waitingStatusСonfirmed("lift1");
        }
        catch (Exception ex){

            boolean coonectionStatus =  IotModules.Reconnect();

            System.out.println( "Error LiftDown:"+ex.getMessage() );
            throw new BpmnError("MoveToGoalError");
        }

    }
}
