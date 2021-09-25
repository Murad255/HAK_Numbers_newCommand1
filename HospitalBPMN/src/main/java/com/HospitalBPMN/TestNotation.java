package com.HospitalBPMN;

import com.HospitalBPMN.MQTT.IotModules;
import com.HospitalBPMN.MQTT.ModuleType;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class TestNotation implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        System.out.println( "TestNotation" );
        try {
            String nextGoal= (String) delegateExecution.getVariable("nextGoal");
            String nameExecutor= (String) delegateExecution.getVariable("nameExecutor");
            String nextMacro= (String)  delegateExecution.getVariable("nextMacro");
            boolean isUp =(boolean) delegateExecution.getVariable("isUp");
            String liftLevel= (String) delegateExecution.getVariable("liftLevel");
            String Table= (String) delegateExecution.getVariable("Table");

            IotModules.myCallback.StackMessageClear();
            IotModules.InDevicesData(ModuleType.robotArm, "MArm", "<Task><Program>163</Program></Task>");
            IotModules.waitingStatusСonfirmed("MArm");
        }
        catch (Exception ex){
            try {

                IotModules.Begin(delegateExecution.getProcessEngine());
            }
            catch (Exception exe){}

            System.out.println( "Error MoveToGoal:"+ex.getMessage() );
            throw new BpmnError("MoveToGoalError");

        }

        System.out.println( "TestNotation" );

    }
}
