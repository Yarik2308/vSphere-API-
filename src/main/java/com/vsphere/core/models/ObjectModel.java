package com.vsphere.core.models;

import lombok.Data;

/**
 * Object for AlarmModel to pass Cluster, Host, VirtualMachine or DataStore Name and what it belongs to
 */

@Data
public class ObjectModel {

    String name; // Cluster, Host, VirtualMachine or DataStore Name

    // В качестве объекта передается строка, которая в дальнейшем булет использована для перехода по ссылке
    // 127.0.0.1:8080/clusters/<clusterName>
    String object; // "clusters", "hosts", "virtualMachines" or "dataStores"

    public ObjectModel(String name, String object) {
        this.name = name;
        this.object = object;
    }
}
