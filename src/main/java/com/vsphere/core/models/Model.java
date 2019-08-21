package com.vsphere.core.models;

import com.vmware.vim25.mo.*;

public class Model {

    /**
     *
     * @param hostSystem HostSystem
     * @return MHz
     */
    int getHostCpuUsage(HostSystem hostSystem){
        return hostSystem.getSummary().getQuickStats().getOverallCpuUsage(); // MHz
    }

    /**
     *
     * @param hostSystem HostSystem
     * @return KB
     */
    Integer getHostMemoryUsage(HostSystem hostSystem){
        return hostSystem.getSummary().getQuickStats().getOverallMemoryUsage(); // MB
    }

    /**
     *
     * @param virtualMachine VirtualMachine
     * @return MHz
     */
    int getVirtualMachineCpuUsage(VirtualMachine virtualMachine){
        return virtualMachine.getSummary().getQuickStats().getOverallCpuUsage(); //MHz
    }

    /**
     *
     * @param virtualMachine VirtualMachine
     * @return MB
     */
    Integer getVirtualMachineMemoryUsage(VirtualMachine virtualMachine){
        return virtualMachine.getSummary().getQuickStats().getGuestMemoryUsage(); //MB
    }

    long getDataStoreCapacity(Datastore dataStore){
        return dataStore.getSummary().getCapacity();
    }

    long getDataStoreFreeSpace(Datastore datastore){
        return datastore.getSummary().getFreeSpace();
    }

    long getDataStoresCapacity(Datastore[] dataStores){
        long capacity = 0;
        for(Datastore datastore: dataStores){
            capacity = capacity + getDataStoreCapacity(datastore);
        }
        return capacity;
    }

    long getDataStoresFreeSpace(Datastore[] dataStores){
        long freeSpace = 0;
        for(Datastore datastore: dataStores){
            freeSpace = freeSpace + getDataStoreFreeSpace(datastore);
        }
        return freeSpace;
    }

}
