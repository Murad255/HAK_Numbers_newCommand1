package com.HospitalBPMN;

import com.HospitalBPMN.MQTT.IotModules;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class ConfigProcess implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        //IotModules.InWatchCleent("всё хорошо");
        System.out.println( "Проверка оборудования" );
        IotModules.Begin(delegateExecution.getProcessEngine());
    }
}
