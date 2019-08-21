package com.vsphere.api.controllers;

import com.vsphere.api.exceptions.NotFoundException;
import com.vsphere.core.models.VirtualMachineModel;
import com.vsphere.core.services.Service;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

//@RestController // JSON
@Controller
@RequestMapping("/virtualMachines")
public class VirtualMachineController {

    // JSON
//    @GetMapping("")
//    public List<VirtualMachineModel> getVirtualMachines() throws NotFoundException {
//        try{
//            return new Service().getVirtualMachineModels();
//        } catch (Exception e){
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    @GetMapping("")
    public String getVirtualMachines(Model model) throws NotFoundException {
        try{
            model.addAttribute("virtualMachines", new Service().getVirtualMachineModels());
            return "virtualMachines";
        } catch (Exception e){
            e.printStackTrace();
            throw new NotFoundException();
        }
    }

    // JSON
//    @GetMapping("/{virtualMachineName}")
//    public VirtualMachineModel getVirtualMachineByName(@PathVariable String virtualMachineName) throws NotFoundException {
//        try{
//            VirtualMachineModel virtualMachineModel = new Service().getVirtualMachineModelByName(virtualMachineName);
//            if(virtualMachineModel==null){
//                throw new NotFoundException();
//            }
//            return virtualMachineModel;
//        } catch (Exception e){
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    @GetMapping("/{virtualMachineName}")
    public String getVirtualMachineByName(@PathVariable String virtualMachineName, Model model) throws NotFoundException {
        try{
            VirtualMachineModel virtualMachineModel = new Service().getVirtualMachineModelByName(virtualMachineName);
            if(virtualMachineModel==null){
                throw new NotFoundException();
            }
            model.addAttribute("virtualMachine", virtualMachineModel);
            return "virtualMachine";
        } catch (Exception e){
            e.printStackTrace();
            throw new NotFoundException();
        }
    }

    // JSON
//    @GetMapping("/withDataStores")
//    public List<VirtualMachineModel> getVirtualMachinesWithDataStores() throws NotFoundException {
//        try{
//            return new Service().getVirtualMachineModelsWithDataStores();
//        } catch (Exception e){
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    // JSON
//    @GetMapping("/withHostsAndDataStores")
//    public List<VirtualMachineModel> getVirtualMachinesWithHostsAndDataStores() throws NotFoundException {
//        try{
//            return new Service().getVirtualMachineModelsWithHostAndDataStores();
//        } catch (Exception e){
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

}
