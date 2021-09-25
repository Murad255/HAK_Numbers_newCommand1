package com.HospitalBPMN.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@RestController
public class HelloWorldController {

    @GetMapping("/hello")
    public String getHelloWorldMessage() {
        System.out.println( "hello" );
        return "Hello world in " + new Date().toString();
    }

    @GetMapping("/button")
    public ModelAndView getButtonMessage() {
        System.out.println( "button" );
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index.html");
        return modelAndView;
    }

    @GetMapping("/race")
    public ModelAndView getRace() {
        System.out.println( "race" );

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("race.html");
        return modelAndView;
    }

    @GetMapping("/turnOn")
    public void getButtonTurnOn() {
        System.out.println( "turnOn" );
    }

    @GetMapping("/turn")
    public void getButtonTurn() {
        System.out.println( "turn" );
    }

    @GetMapping("/turnOff")
    public void getButtonTurnOff() {
        System.out.println( "turnOff" );
    }

    @PostMapping("/button")
    public String updateSomeData(String value) {
        System.out.println( "PostMapping" );
        return "Hello world in " + value.toString();
    }


}
