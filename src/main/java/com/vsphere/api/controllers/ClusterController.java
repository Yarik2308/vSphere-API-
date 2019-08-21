package com.vsphere.api.controllers;

import com.vsphere.api.exceptions.NotFoundException;
import com.vsphere.core.services.Service;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//@RestController // JSON
@Controller
@RequestMapping("/clusters")
public class ClusterController {

    // Json
//    @GetMapping("")
//    public List<ClusterModel> getClusterModels() throws NotFoundException {
//        try{
//            return new Service().getClusterModels();
//        } catch (Exception e){
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    @GetMapping("")
    public String getClusterModels(Model model) throws NotFoundException {
        try{
            model.addAttribute("clusters", new Service().getClusterModels());
            return "clusters";
        } catch (Exception e){
            e.printStackTrace();
            throw new NotFoundException();
        }
    }

    // Json
//    @GetMapping("/{clusterName}")
//    public ClusterModel getClusterModelByName(@PathVariable String clusterName) throws NotFoundException {
//        try{
//            ClusterModel clusterModel = new Service().getClusterModelByName(clusterName);
//            if(clusterModel==null){
//                throw new NotFoundException();
//            }
//            return clusterModel;
//        } catch (Exception e){
//            e.printStackTrace();
//            throw new NotFoundException();
//        }
//    }

    @GetMapping("/{clusterName}")
    public String getClusterModelByName(@PathVariable String clusterName, Model model) throws NotFoundException {
        try{

            model.addAttribute("cluster", new Service().getClusterModelByName(clusterName));

            return "cluster";
        } catch (Exception e){
            e.printStackTrace();
            throw new NotFoundException();
        }
    }

}
