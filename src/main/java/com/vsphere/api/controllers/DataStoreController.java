package com.vsphere.api.controllers;

import com.vsphere.api.exceptions.NotFoundException;
import com.vsphere.core.services.Service;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

//@RestController // JSON
@Controller
@RequestMapping("/dataStores")
public class DataStoreController {

    // JSON
//    @GetMapping("")
//    public List<DataStoreModel> getDataStores() throws NotFoundException {
//            try {
//                return new Service().getDataStoreModels();
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new NotFoundException();
//            }
//    }

    @GetMapping("")
    public String getDataStores(Model model) throws NotFoundException {
        try {
            model.addAttribute("dataStores", new Service().getDataStoreModels());
            return "dataStores";
        } catch (Exception e) {
            e.printStackTrace();
            throw new NotFoundException();
        }
    }

    // JSON
//    @GetMapping("/{dataStoreName}")
//    public DataStoreModel getDataStoreByName(@PathVariable String dataStoreName) throws NotFoundException {
//        try{
//            DataStoreModel dataStoreModel = new Service().getDataStoreModelByName(dataStoreName);
//            if(dataStoreModel==null){
//                throw new NotFoundException();
//            }
//            return dataStoreModel;
//        } catch (Exception e){
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    @GetMapping("/{dataStoreName}")
    public String getDataStoreByName(@PathVariable String dataStoreName, Model model) throws NotFoundException {
        try{
            model.addAttribute("dataStore", new Service().getDataStoreModelByName(dataStoreName));
            return "dataStore";
        } catch (Exception e){
            e.printStackTrace();
            throw new NotFoundException();
        }
    }

    // JSON
//    @GetMapping("/withVirtualMachines")
//    public List<DataStoreModel> getDataStoresWithVirtualMachines() throws NotFoundException {
//        try {
//            return new Service().getDataStoreModelsWithVirtualMachines();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    // JSON
//    @GetMapping("/withHostsAndVirtualMachines")
//    public List<DataStoreModel> getDataStoresWithHostsAndVirtualMachines() throws NotFoundException {
//        try {
//            return new Service().getDataStoreModelsWithHostsAndVirtualMachines();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

}
