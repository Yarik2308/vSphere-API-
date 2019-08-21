package com.vsphere.core.models;

import com.vmware.vim25.mo.*;
import lombok.Data;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClusterModel extends Model{

    private String name;

    private int numHosts;

    private int numCpuCores;

    private short numCpuThreads;

    private int totalCpu; // MHz

    private int cpuUsage; // MHz

    private long totalMemory; // Byte

    private Integer memoryUsage; // MB

    private long capacity; //Byte

    private long freeSpace; // Byte

    private List<HostModel> hostsList;

    private List<VirtualMachineModel> virtualMachinesList;

    private List<DataStoreModel> dataStoresList;


    private List<AlarmModel> alarmsList;

    // Конструктор без информации
    public ClusterModel(){}

    // Конструктор со всех информацией об Класторе, его Хостах, Виртуалок и ДатаСторов
    public ClusterModel(ClusterComputeResource ccr) throws RemoteException {
        getClusterModelInfo(ccr);

        getHostModelsAndCpuUsage(ccr);
        getVirtualMachineModels(ccr);
        getDataStoreModelsAndCapacityAndStorageUsage(ccr);
    }

    // Добавление в модель Кластера информации о нем и имена Хостов, Виртуалок и ДатаСторв
    public ClusterModel getClusterModelWithHostVirtualMachineAndDataStoreNames(ClusterComputeResource ccr) throws RemoteException {
        getClusterModelInfo(ccr);

        getHostNamesAndCpuUsage(ccr);
        getVirtualMachineNames(ccr);
        getDataStoreNamesAndCapacityAndStorageUsage(ccr);

        return this;
    }

    private void getClusterModelInfo(ClusterComputeResource ccr){
        this.name = ccr.getName();
        this.numHosts = ccr.getHosts().length;
        this.numCpuCores = +ccr.getSummary().getNumCpuCores();
        this.numCpuThreads = ccr.getSummary().getNumCpuThreads();
        this.totalCpu = ccr.getSummary().getTotalCpu();
        this.totalMemory = ccr.getSummary().getTotalMemory();

        hostsList = new ArrayList<>();
        virtualMachinesList = new ArrayList<>();
        dataStoresList = new ArrayList<>();

        this.cpuUsage = 0;
        this.memoryUsage = 0;
        this.capacity = 0;
        this.freeSpace = 0;
    }

    // Получение моделей Хостов со всей информацией и суммирование их CPU и Memory
    private void getHostModelsAndCpuUsage(ClusterComputeResource ccr) throws RemoteException {
        HostSystem[] hostSystems = ccr.getHosts();

        for(HostSystem hs: hostSystems){
            this.memoryUsage = this.memoryUsage + getHostMemoryUsage(hs); // MB
            this.cpuUsage = this.cpuUsage + getHostCpuUsage(hs);
            //this.hostsList.add(hs.getName());
            this.hostsList.add(new HostModel(hs));
        }
    }

    // Получение моделей Хостов только с именами и суммирование их CPU и Memory
    private void getHostNamesAndCpuUsage(ClusterComputeResource ccr) throws RemoteException {
        HostSystem[] hostSystems = ccr.getHosts();

        for(HostSystem hostSystem: hostSystems){
            this.memoryUsage = this.memoryUsage + getHostMemoryUsage(hostSystem); // MB
            this.cpuUsage = this.cpuUsage + getHostCpuUsage(hostSystem);
            //this.hostsList.add(hs.getName());
            this.hostsList.add(new HostModel(hostSystem.getName()));
        }
    }

    // Получение моделей Виртуалок со всей информацией
    // Используемое ими CPU и Memory учитывается в Хостах
    private void getVirtualMachineModels(ClusterComputeResource ccr) throws RemoteException {
        VirtualMachine[] virtualMachines = ccr.getResourcePool().getVMs();

        for(VirtualMachine virtualMachine: virtualMachines){
            //this.virtualMachinesList.add(virtualMachine.getName());
            this.virtualMachinesList.add(new VirtualMachineModel(virtualMachine));
        }
    }

    // Получение модели Виртуалок только с именами
    // Используемое ими CPU и Memory учитывается в Хостах
    private void getVirtualMachineNames(ClusterComputeResource ccr) throws RemoteException {
        VirtualMachine[] virtualMachines = ccr.getResourcePool().getVMs();

        for(VirtualMachine virtualMachine: virtualMachines){
            //this.virtualMachinesList.add(virtualMachine.getName());
            this.virtualMachinesList.add(new VirtualMachineModel(virtualMachine.getName()));
        }
    }

    // Получение моделей ДатаСторов со всей информауией и суммирование их Вместимости и Свободного места
    private void getDataStoreModelsAndCapacityAndStorageUsage(ClusterComputeResource ccr){
        Datastore[] dataStores = ccr.getDatastores();

        for(Datastore dataStore: dataStores){
            this.capacity = this.capacity + getDataStoreCapacity(dataStore);
            this.freeSpace = this.freeSpace + getDataStoreFreeSpace(dataStore);
            //this.dataStoresList.add(dataStore.getName());
            this.dataStoresList.add(new DataStoreModel(dataStore));
        }
    }

    // Получение моделей ДатаСторов с именами и суммирование их Вместимости и Свободного места
    private void getDataStoreNamesAndCapacityAndStorageUsage(ClusterComputeResource ccr){
        Datastore[] dataStores = ccr.getDatastores();

        for(Datastore dataStore: dataStores){
            this.capacity = this.capacity + getDataStoreCapacity(dataStore);
            this.freeSpace = this.freeSpace + getDataStoreFreeSpace(dataStore);
            //this.dataStoresList.add(dataStore.getName());
            this.dataStoresList.add(new DataStoreModel(dataStore.getName()));
        }
    }
}
