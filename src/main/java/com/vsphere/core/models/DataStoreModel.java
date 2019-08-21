package com.vsphere.core.models;

import com.vmware.vim25.mo.Datastore;
import lombok.Data;

import java.util.List;

@Data
public class DataStoreModel {
    private String name;

    private String url;

    private String type;

    private long capacity; //Byte

    private long freeSpace; // Byte

    private String clusterName;

    private List<HostModel> hostsList;

    private List<VirtualMachineModel> virtualMachinesList;

    private List<AlarmModel> alarmsList;

    public DataStoreModel(Datastore dataStore){
        this.name = dataStore.getName();
        this.url = dataStore.getSummary().getUrl();
        this.type = dataStore.getSummary().getType();
        this.capacity = dataStore.getSummary().getCapacity();
        this.freeSpace = dataStore.getSummary().getFreeSpace();
    }

    DataStoreModel(String name){
        this.name = name;
    }

    public void setClusterName(String clusterName){
        this.clusterName = clusterName;
    }

}
