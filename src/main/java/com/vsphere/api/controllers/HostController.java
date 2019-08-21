package com.vsphere.api.controllers;

import com.vsphere.api.exceptions.NotFoundException;
import com.vsphere.core.models.HostModel;
import com.vsphere.core.services.Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//@RestController //JSON
@Controller
@RequestMapping("/hosts")
public class HostController {

    // For JSON
//    @GetMapping("")
//    public List<HostModel> getHost() throws NotFoundException {
//        try {
//            return new Service().getHostModels();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    @GetMapping("")
    public String getHosts(Model model) throws NotFoundException {
        try {
            model.addAttribute("hosts", new Service().getHostModels());
            return "hosts";
        } catch (Exception e) {
            e.printStackTrace();
            throw new NotFoundException();
        }
    }

    // For JSON
//    @GetMapping("/get/{hostName}")
//    public HostModel getHostModelByName(@PathVariable String hostName) throws NotFoundException {
//        try{
//            HostModel hostModel = new Service().getHostModelByName(hostName);
//            if(hostModel==null){
//                throw new NotFoundException();
//            }
//            return hostModel;
//        } catch (Exception e){
//            throw new NotFoundException();
//        }
//    }

    @GetMapping("/{hostName}")
    public String getHostModelByName(@PathVariable String hostName, Model model) throws NotFoundException {
        try{
            HostModel hostModel = new Service().getHostModelByName(hostName);
            if(hostModel==null){
                throw new NotFoundException();
            }
            model.addAttribute("host", hostModel);
            return "host";
        } catch (Exception e){
            throw new NotFoundException();
        }
    }

    // JSON
//    @GetMapping("/withDataStores")
//    public List<HostModel> getHostsWithDataStores() throws NotFoundException {
//        try {
//            return new Service().getHostModelsWithDataStores();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    // JSON
//    @GetMapping("/withVirtualMachines")
//    public List<HostModel> getHostsWithVirtualMachines() throws NotFoundException {
//        try {
//            return new Service().getHostModelsWithVirtualMachines();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    // JSON
//    @GetMapping("/withVirtualMachinesAndDataStores")
//    public List<HostModel> getHostsWithVirtualMachinesAndDataStores() throws NotFoundException {
//        try {
//            return new Service().getHostModelsWthVirtualMachinesAndDataStores();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

}
