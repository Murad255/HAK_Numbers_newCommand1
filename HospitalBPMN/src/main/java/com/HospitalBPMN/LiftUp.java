package com.HospitalBPMN;

import com.HospitalBPMN.MQTT.IotModules;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class LiftUp implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        System.out.println( "LiftUp" );

        try {
            Thread.sleep(150);
            IotModules.myCallback.StackMessageClear();
            IotModules.InDevices("1","motor/offset");
            IotModules.waitingStatusСonfirmed("lift1");
        }
        catch (Exception ex){

            boolean coonectionStatus =  IotModules.Reconnect();

            System.out.println( "Error LiftDown:"+ex.getMessage() );
            throw new BpmnError("MoveToGoalError");

        }
    }
}
