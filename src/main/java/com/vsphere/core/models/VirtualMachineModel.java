package com.vsphere.core.models;

import com.vmware.vim25.mo.VirtualMachine;
import lombok.Data;

import java.rmi.RemoteException;
import java.util.List;

@Data
public class VirtualMachineModel {
    private String name;

    private String guestOs;

    private String vmWareToolsRunningStatus;

    private String vmWareToolsVersion;

    private String dnsName; // Host Name

    private String ipAddresses;

    private int cpuUsage; // MHz

    private int numCpuCores;

    private int staticCpuEntitlement;

    private int cpuReservation;

    private int memoryUsage; // MB

    private int staticMemoryEntitlement; // MB

    private int memorySize; // MB

    private  int memoryReservation; // MB

    private long committed; // Byte

    private long uncommitted; // Bytes

    private long unshared; // Bytes

    private String clusterName;

    private String hostName;

    private List<DataStoreModel> dataStoresList;

    private List<AlarmModel> alarmsList;

    public VirtualMachineModel(VirtualMachine virtualMachine) throws RemoteException {
        this.name = virtualMachine.getName();
        this.guestOs = virtualMachine.getConfig().getGuestFullName();
        this.vmWareToolsRunningStatus = virtualMachine.getGuest().getToolsRunningStatus();
        this.vmWareToolsVersion = virtualMachine.getGuest().getToolsVersion();
        this.dnsName = virtualMachine.getGuest().getHostName();
        this.ipAddresses = virtualMachine.getGuest().getIpAddress();
        this.cpuUsage = virtualMachine.getSummary().getQuickStats().getOverallCpuUsage();
        this.staticCpuEntitlement = virtualMachine.getSummary().getQuickStats().getStaticCpuEntitlement();
        this.cpuReservation = virtualMachine.getSummary().getConfig().getCpuReservation();
        this.numCpuCores = virtualMachine.getSummary().getConfig().getNumCpu();
        this.memoryUsage = virtualMachine.getSummary().getQuickStats().getGuestMemoryUsage();
        this.staticMemoryEntitlement = virtualMachine.getSummary().getQuickStats().getStaticMemoryEntitlement();
        this.memorySize = virtualMachine.getSummary().getConfig().getMemorySizeMB();
        this.memoryReservation = virtualMachine.getSummary().getConfig().getMemoryReservation();
        this.committed = virtualMachine.getSummary().getStorage().getCommitted();
        this.uncommitted = virtualMachine.getSummary().getStorage().getUncommitted();
        this.unshared = virtualMachine.getSummary().getStorage().getUnshared();
        try {
            this.clusterName = virtualMachine.getResourcePool().getParent().getName();
        } catch (NullPointerException e){
            this.clusterName = "Unset";
        }
    }

    VirtualMachineModel(String name){
        this.name = name;
    }

    // Из VirtualMachine сложно получить Кластер и Хост => потом добываются через ManagedObjectReference
    public void setClusterAndHostName(String clusterName, String hostName){
        this.clusterName = clusterName;
        this.hostName = hostName;
    }

}
