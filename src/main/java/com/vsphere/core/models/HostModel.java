package com.vsphere.core.models;

import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.HostSystem;
import lombok.Data;

import java.rmi.RemoteException;
import java.util.List;

@Data
public class HostModel extends Model{
    private String name; //IP

    private String fullName; // HyperVisor

    private String connectionState;

    private String overallStatus;

    private String model;

    private String morVal; //  ManagedObjectReference

    private Integer cpuUsage; // MHz

    private int cpuMhz; // Mhz of 1 Core

    private short numCpuCores;

    private short numCpuThreads;

    private String cpuModel;

    private long memorySize; // Byte Capacity

    private Integer memoryUsage; // MB

    private int numNICs;

    private Integer upTime; //Seconds

    private long capacity; //Byte

    private long freeSpace; // Byte

    private String clusterName;

    private List<VirtualMachineModel> virtualMachinesList;

    private List<DataStoreModel> dataStoresList;

    private List<AlarmModel> alarmsList;

    public HostModel(HostSystem hostSystem) throws RemoteException {
        this.name = hostSystem.getName();
        this.fullName = hostSystem.getSummary().getConfig().getProduct().getFullName();
        this.connectionState = hostSystem.getRuntime().getConnectionState().toString();
        this.overallStatus = hostSystem.getOverallStatus().toString();
        this.model = hostSystem.getSummary().getHardware().getModel();
        this.morVal = hostSystem.getMOR().getVal();
        this.cpuUsage = hostSystem.getSummary().getQuickStats().getOverallCpuUsage();
        this.cpuMhz = hostSystem.getSummary().getHardware().getCpuMhz();
        this.numCpuCores = hostSystem.getSummary().getHardware().getNumCpuCores();
        this.numCpuThreads = hostSystem.getSummary().getHardware().getNumCpuThreads();
        this.cpuModel = hostSystem.getSummary().getHardware().getCpuModel();
        this.memorySize = hostSystem.getSummary().getHardware().getMemorySize();
        this.memoryUsage = hostSystem.getSummary().getQuickStats().getOverallMemoryUsage();
        this.numNICs = hostSystem.getSummary().getHardware().getNumNics();
        this.upTime = hostSystem.getSummary().getQuickStats().getUptime();
        this.clusterName = hostSystem.getParent().getName();
        this.capacity = 0;
        this.freeSpace = 0;
        getCapacityAndFreeSpace(hostSystem);
    }

    public HostModel(String name){
        this.name = name;
    }

    // получение Вместимости и Свободного места Датасторв, подключеных к Хосту
    private void getCapacityAndFreeSpace(HostSystem hostSystem) throws RemoteException {
        Datastore[] dataStores = hostSystem.getDatastores();

        for(Datastore datastore: dataStores){
            this.capacity = this.capacity + getDataStoreCapacity(datastore);
            this.freeSpace = this.freeSpace + getDataStoreFreeSpace(datastore);
        }
    }
}
