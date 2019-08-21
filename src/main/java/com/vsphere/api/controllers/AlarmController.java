package com.vsphere.api.controllers;

import com.vsphere.api.exceptions.NotFoundException;
import com.vsphere.core.services.Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alarms")
public class AlarmController {

    @GetMapping("")
    public String getAlarms(Model model) throws NotFoundException {
        try{
            model.addAttribute("alarms", new Service().getAlarms());
            return "alarms";
        } catch (Exception e){
            e.printStackTrace();
            throw new NotFoundException();
        }
    }

}
