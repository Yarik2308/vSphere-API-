package com.vsphere.core.services;

import com.vmware.vim25.AlarmInfo;
import com.vmware.vim25.AlarmState;
import com.vmware.vim25.DatastoreHostMount;
import com.vmware.vim25.mo.*;
import com.vsphere.api.exceptions.ForbiddenException;
import com.vsphere.core.models.*;
import org.springframework.context.annotation.ComponentScan;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@ComponentScan
public class Service implements ServiceInterface{
    private Folder folder;

    public Service() throws ForbiddenException {
        // Дефолтные значения нужны при сборке проекта. Нужно использовать реально существуещего юзера
        // Получаем url указаный при запуске через cmd
        String url_ip = System.getProperty("url", "def_url");
        // Получаем username указаный при запуске через cmd
        String username = System.getProperty("username", "def_username");
        // Получаем password указаный при запуске через cmd
        String password = System.getProperty("password", "def_password");

        String url = "https://" + url_ip + "/sdk";

        System.out.println("Connecting");
        try {
            // Получаем ServiceContent. Пример: https://10.240.3.120/mob/?moid=ServiceInstance&doPath=content
            ServiceInstance si = new ServiceInstance(new URL(url), username, password, true);
            System.out.println("Connection Successful");
            // Получаем rootFolder группы. Пример: https://10.240.3.120/mob/?moid=group%2dd1
            folder = si.getRootFolder();
        } catch (Exception e){
            // Если логин, пароль или url не верны
            e.printStackTrace();
            throw new ForbiddenException();
        }
    }

    // Получаем множество ManagedEntity в которых лежат модели кластеров ClusterComputeResource из rootFolder.
    private ManagedEntity[] getManagedEntityCCR() throws RemoteException {
        return new InventoryNavigator(folder).searchManagedEntities("ClusterComputeResource");
    }

    // Из ClusterComputeResource проблематично достать виртуалки => ищем их через rootFolder
    private ManagedEntity[] getManagedEntityVM() throws RemoteException {
        return new InventoryNavigator(folder).searchManagedEntities("VirtualMachine");
    }

    //////////////////////////////////////////////////////////////////////////////////////// Cluster

    // Получаем все кластреы
    public List<ClusterModel> getClusterModels() throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();
        List<ClusterModel> clusterModelsListForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntities){
            // Получаем модели кластеров из ManagedEntity
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            // Создаем модель кластера с именами Хостов, Виртуалок и ДатаСтров
            // Создаютя модели Хоств, Виртуалки и Датастора, но они содержат только имена
            ClusterModel clusterModel = new ClusterModel().getClusterModelWithHostVirtualMachineAndDataStoreNames(ccr);

            clusterModelsListForReturn.add(clusterModel);
        }
        return clusterModelsListForReturn;
    }

    // Получаем кластер по имени
    public ClusterModel getClusterModelByName(String clusterName) throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();

        for(ManagedEntity managedEntity: managedEntities){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            if(ccr.getName().equals(clusterName)){
                // Создаем модель кластера с информацией об Хостах, Виртуалках и Датасторов
                ClusterModel clusterModel = new ClusterModel(ccr);
                // Кластер - объединение Хостов, Виртуалок и ДатаСторов => Ищем какому объекту принадлежит сигнал тревоги
                clusterModel.setAlarmsList(getAlarms(ccr.getTriggeredAlarmState()));

                return clusterModel;
            }
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////// Host

    // Получаем все хосты из HostSystem[] (Чтоб не было повторв кода)
    private List<HostModel> getHostModels(HostSystem[] hostSystems) throws RemoteException {
        List<HostModel> hostModelsListForReturn = new ArrayList<>();
        for(HostSystem hs: hostSystems){
            hostModelsListForReturn.add(new HostModel(hs));
        }
        return hostModelsListForReturn;
    }

    // Получаем все хосты без информации об Виртуалках, ДатаСторах и Сигналов тревоги
    public List<HostModel> getHostModels() throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();
        List<HostModel> hostModelsListForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntities){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            HostSystem[] hostSystems = ccr.getHosts();
            hostModelsListForReturn.addAll(getHostModels(hostSystems));
        }
        return hostModelsListForReturn;
    }

    // Получаем Хоста по имени с Инфыормацией об Виртуалках, ДатаСторах и Сигналов тревоги
    public HostModel getHostModelByName(String hostName) throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();

        for(ManagedEntity managedEntity: managedEntities){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            HostSystem[] hostSystems = ccr.getHosts();
            for(HostSystem hostSystem: hostSystems){
                if(hostSystem.getName().equals(hostName)){
                    HostModel hostModel = new HostModel(hostSystem);
                    // Получаем список Сигналов тревоги
                    hostModel.setAlarmsList(getAlarms(hostSystem.getTriggeredAlarmState()));
                    // Получаем список ДатаСторов
                    hostModel.setDataStoresList(getDataStoreModelsByHostSystem(hostSystem));
                    // Получаем списко Виртуалок
                    hostModel.setVirtualMachinesList(getVirtualMachineModelsByHostSystem(hostSystem));

                    return hostModel;
                }
            }
        }
        return null;
    }

    // Получение Моделей хостов со всей информацией через DataStore
    // Через ДатаСторы нельзя получить информацию об Хостах => находим Хосты через их ManagedObjectReference
    // Заранее берется весь список моделей Хостов, чтобы не получать его от rootFolder в цикле
    // TODO: Подумать над скоростью поиска
    private List<HostModel> getHostModelsByDataStores(Datastore dataStore, List<HostModel> allHostModelsList) {
        DatastoreHostMount[] dataStoreHostMounts = dataStore.getHost();
        List<HostModel> hostModelsListForReturn = new ArrayList<>(dataStoreHostMounts.length);

        for(DatastoreHostMount datastoreHostMount: dataStoreHostMounts){
            String Val =  datastoreHostMount.getKey().getVal();
            for(HostModel hostModel: allHostModelsList){
                if(Val.equals(hostModel.getMorVal())){

                    hostModelsListForReturn.add(hostModel);
                    break;
                }
            }
        }
        return hostModelsListForReturn;
    }

    // Получение Моделей хостов с именем через DataStore
    // Через ДатаСторы нельзя получить информацию об Хостах => находим Хосты через их ManagedObjectReference
    // Заранее берется весь список моделей Хостов, чтобы не получать его от rootFolder в цикле
    // TODO: Подумать над скоростью поиска
    private List<HostModel> getHostModelNamesByDataStores(Datastore dataStore, List<HostModel> allHostModelsList) {
        DatastoreHostMount[] dataStoreHostMounts = dataStore.getHost();
        List<HostModel> hostModelsListForReturn = new ArrayList<>(dataStoreHostMounts.length);

        for(DatastoreHostMount datastoreHostMount: dataStoreHostMounts){
            String Val =  datastoreHostMount.getKey().getVal();
            for(HostModel hostModel: allHostModelsList){
                if(Val.equals(hostModel.getMorVal())){

                    hostModelsListForReturn.add(new HostModel(hostModel.getName()));
                    break;
                }
            }
        }
        return hostModelsListForReturn;
    }

    // Получение Моделей хостов со всей информацией через VirtualMachine
    // Через Виртуалку нельзя получить информацию об Хостах => находим Хосты через их ManagedObjectReference
    // Заранее берется весь список моделей Хостов, чтобы не получать его от rootFolder в цикле
    // TODO: Подумать над скоростью поиска
    private HostModel getHostModelByVirtualMachine(VirtualMachine virtualMachine, List<HostModel> allHostModelsList) {
        String morVal = virtualMachine.getRuntime().getHost().getVal();

        for(HostModel hostModel: allHostModelsList){
            if(hostModel.getMorVal().equals(morVal)){
                return hostModel;
            }
        }
        return null;
    }

    // Получение Моделей хостов с Сигналами тревоги и ДатаСторами
    public List<HostModel> getHostModelsWithDataStores() throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();
        List<HostModel> hostModelsListForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntities){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            HostSystem[] hostSystems = ccr.getHosts();
            for(HostSystem hs: hostSystems){
                HostModel hostModel = new HostModel(hs);
                // Получаем списка Сигналов тревоги
                hostModel.setAlarmsList(getAlarms(hs.getTriggeredAlarmState()));
                // Получение списка ДатаСторов
                hostModel.setDataStoresList(getDataStoreModelsByHostSystem(hs));

                hostModelsListForReturn.add(hostModel);
            }
        }
        return hostModelsListForReturn;
    }

    // Получение Моделей хостов с Сигналами тревоги и Виртуалками
    public List<HostModel> getHostModelsWithVirtualMachines() throws RemoteException {
        ManagedEntity[] managedEntitiesCCR = getManagedEntityCCR();

        List<HostModel> hostModelsListForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntitiesCCR){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            HostSystem[] hostSystems = ccr.getHosts();
            for(HostSystem hostSystem: hostSystems){
                HostModel hostModel = new HostModel(hostSystem);
                // Получаем списка Сигналов тревоги
                hostModel.setAlarmsList(getAlarms(hostSystem.getTriggeredAlarmState()));
                // Получаем списка Виртуалок
                hostModel.setVirtualMachinesList(getVirtualMachineModelsByHostSystem(hostSystem));

                hostModelsListForReturn.add(hostModel);
            }
        }
        return hostModelsListForReturn;
    }

    // Получение Моделей хостов с Сигналами тревоги, Виртуалками и ДатаСторами
    public List<HostModel> getHostModelsWthVirtualMachinesAndDataStores()
            throws RemoteException {
        ManagedEntity[] managedEntitiesCCR = getManagedEntityCCR();

        List<HostModel> hostModelsListForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntitiesCCR){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            HostSystem[] hostSystems = ccr.getHosts();
            for(HostSystem hostSystem: hostSystems){
                HostModel hostModel = new HostModel(hostSystem);
                // Получаем списка Сигналов тревоги
                hostModel.setAlarmsList(getAlarms(hostSystem.getTriggeredAlarmState()));
                // Получаем списка Виртуалок
                hostModel.setVirtualMachinesList(getVirtualMachineModelsByHostSystem(hostSystem));
                // Получаем списка ДатаСторв
                hostModel.setDataStoresList(getDataStoreModelsByHostSystem(hostSystem));

                hostModelsListForReturn.add(hostModel);
            }
        }
        return hostModelsListForReturn;
    }

    ///////////////////////////////////////////////////////////////////////////////////// VirtualMachine

    // Получение моделей Виртуалок с именаим Кластера и Хоста
    public List<VirtualMachineModel> getVirtualMachineModels() throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityVM();
        List<VirtualMachineModel> virtualMachineModelsListForReturn = new ArrayList<>();
        List<HostModel> allHostModelsList = getHostModels();

        for(ManagedEntity managedEntity: managedEntities){
            VirtualMachine virtualMachine = (VirtualMachine) managedEntity;
            VirtualMachineModel virtualMachineModel = new VirtualMachineModel(virtualMachine);
            // Находим модель Хоста через Виртуалку
            HostModel hostModel = getHostModelByVirtualMachine(virtualMachine, allHostModelsList);
            if(hostModel!=null) {
                virtualMachineModel.setClusterAndHostName(hostModel.getClusterName(), hostModel.getName());
            }
            //virtualMachineModel.setAlarmsList(getAlarms(virtualMachine.getTriggeredAlarmState()));

            virtualMachineModelsListForReturn.add(virtualMachineModel);
        }
        return virtualMachineModelsListForReturn;
    }

    // Получаем модель Виртуалки с Сигналами тревоги, Хостом и ДатаСторами
    private VirtualMachineModel getVirtualMachineModelWithHostAndDataStores(VirtualMachine virtualMachine,
                                                                            List<HostModel> allHostModelsList)
            throws RemoteException {
        VirtualMachineModel virtualMachineModel = new VirtualMachineModel(virtualMachine);
        // Получаем спиок Сигналов тревоги
        virtualMachineModel.setAlarmsList(getAlarms(virtualMachine.getTriggeredAlarmState()));
        // Получаем модель Хоста
        HostModel hostModel = getHostModelByVirtualMachine(virtualMachine, allHostModelsList);
        if(hostModel!=null) {
            virtualMachineModel.setClusterAndHostName(hostModel.getClusterName(), hostModel.getName());
        }
        // Получаес список ДатаСторов
        virtualMachineModel.setDataStoresList(getDataStoreModelsByVirtualMachine(virtualMachine));

        return virtualMachineModel;
    }

    // Получаем модель Виртуалки по имени
    public VirtualMachineModel getVirtualMachineModelByName(String virtualMachineName) throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityVM();
        // Заранее получаем список Хостов
        List<HostModel> allHostModelsList = getHostModels();

        for(ManagedEntity managedEntity: managedEntities){
            VirtualMachine virtualMachine = (VirtualMachine) managedEntity;

            if(virtualMachine.getName().equals(virtualMachineName)){
                return getVirtualMachineModelWithHostAndDataStores(virtualMachine, allHostModelsList);
            }
        }
        return null;
    }

    // Получаем список Виртуалок через HostSystem
    private List<VirtualMachineModel> getVirtualMachineModelsByHostSystem(HostSystem hostSystem) throws RemoteException {
        VirtualMachine[] virtualMachines = hostSystem.getVms();
        List<VirtualMachineModel> virtualMachineModelsListForReturn = new ArrayList<>();

        for(VirtualMachine virtualMachine: virtualMachines){
            VirtualMachineModel virtualMachineModel = new VirtualMachineModel(virtualMachine);

            // Получаем имена Кластера и Хоста
            virtualMachineModel.setClusterAndHostName(hostSystem.getParent().getName(), hostSystem.getName());
            // Получаем список Сигналов иревоги
            virtualMachineModel.setAlarmsList(getAlarms(virtualMachine.getTriggeredAlarmState()));

            virtualMachineModelsListForReturn.add(virtualMachineModel);
        }
        return virtualMachineModelsListForReturn;
    }

    // Получаем список Виртуалок через DataStore
    private List<VirtualMachineModel> getVirtualMachineModelsByDataStore(Datastore datastore) throws RemoteException {
        VirtualMachine[] virtualMachines = datastore.getVms();
        List<VirtualMachineModel> virtualMachineModelsListForReturn = new ArrayList<>();
        List<HostModel> allHostModelsList = getHostModels(); // Заранее получаем список Хостов

        for(VirtualMachine virtualMachine: virtualMachines){
            VirtualMachineModel virtualMachineModel = new VirtualMachineModel(virtualMachine);
            HostModel hostModel = getHostModelByVirtualMachine(virtualMachine, allHostModelsList);
            if(hostModel!=null) {
                virtualMachineModel.setClusterAndHostName(hostModel.getClusterName(), hostModel.getName());
            }
            virtualMachineModel.setAlarmsList(getAlarms(virtualMachine.getTriggeredAlarmState()));

            virtualMachineModelsListForReturn.add(virtualMachineModel);
        }
        return virtualMachineModelsListForReturn;
    }


    public List<VirtualMachineModel> getVirtualMachineModelsWithDataStores()
            throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityVM();
        List<VirtualMachineModel> virtualMachineModelsListForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntities){
            VirtualMachine vm = (VirtualMachine) managedEntity;
            VirtualMachineModel virtualMachineModel = new VirtualMachineModel(vm);
            virtualMachineModel.setAlarmsList(getAlarms(vm.getTriggeredAlarmState()));
            virtualMachineModel.setDataStoresList(getDataStoreModelsByVirtualMachine(vm));

            virtualMachineModelsListForReturn.add(virtualMachineModel);
        }
        return virtualMachineModelsListForReturn;
    }

    public List<VirtualMachineModel> getVirtualMachineModelsWithHostAndDataStores()
            throws RemoteException{
        ManagedEntity[] managedEntities = getManagedEntityVM();

        List<VirtualMachineModel> virtualMachineModelsListForReturn = new ArrayList<>();
        List<HostModel> allHostModelsList = getHostModels();

        for(ManagedEntity managedEntity: managedEntities){
            VirtualMachine virtualMachine = (VirtualMachine) managedEntity;

            virtualMachineModelsListForReturn.add(getVirtualMachineModelWithHostAndDataStores(virtualMachine, allHostModelsList));
        }
        return virtualMachineModelsListForReturn;
    }

    ////////////////////////////////////////////////////////////////////////////////////////// DataStore

    // Получаем список моделей ДатаСторов с именем Кластера и Хоста
    public List<DataStoreModel> getDataStoreModels() throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();
        List<DataStoreModel> dataStoreModelsListForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntities){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            Datastore[] dataStores = ccr.getDatastores();
            List<HostModel> allHostModelsList = getHostModels(ccr.getHosts());

            for(Datastore dataStore: dataStores){
                DataStoreModel dataStoreModel = new DataStoreModel(dataStore);
                //dataStoreModel.setAlarmsList(getAlarms(dataStore.getTriggeredAlarmState()));
                dataStoreModel.setHostsList(getHostModelNamesByDataStores(dataStore, allHostModelsList));
                dataStoreModel.setClusterName(ccr.getName());

                dataStoreModelsListForReturn.add(dataStoreModel);
            }
        }
        return dataStoreModelsListForReturn;
    }

    // Получение ДатаСтора по имени с именем Кластреа, моделями Хостов, Виртуалок и Сигналами тревоги
    public DataStoreModel getDataStoreModelByName(String dataStoreName) throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();

        for(ManagedEntity managedEntity: managedEntities){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            Datastore[] dataStores = ccr.getDatastores();
            List<HostModel> allHostModelsList = getHostModels(ccr.getHosts());

            for(Datastore datastore: dataStores){
                if(datastore.getName().equals(dataStoreName)){
                    DataStoreModel dataStoreModel = new DataStoreModel(datastore);
                    // Получаем список Сигналов тревоги
                    dataStoreModel.setAlarmsList(getAlarms(datastore.getTriggeredAlarmState()));
                    // Получение моделей Хостов
                    dataStoreModel.setHostsList(getHostModelsByDataStores(datastore, allHostModelsList));
                    // Получение имение Кластера
                    dataStoreModel.setClusterName(ccr.getName());
                    // Получение моделей Виртуалок
                    dataStoreModel.setVirtualMachinesList(getVirtualMachineModelsByDataStore(datastore));

                    return dataStoreModel;
                }
            }
        }
        return null;
    }

    // Получение моделей ДатаСторв через HostSystem
    private List<DataStoreModel> getDataStoreModelsByHostSystem(HostSystem hostSystem) throws RemoteException {
        Datastore[] dataStores = hostSystem.getDatastores();
        List<DataStoreModel> dataStoreModelListForReturn = new ArrayList<>(dataStores.length);
        for(Datastore ds: dataStores){
            DataStoreModel dataStoreModel = new DataStoreModel(ds);
            // Получение списка Сигналов тревоги
            dataStoreModel.setAlarmsList(getAlarms(ds.getTriggeredAlarmState()));

            dataStoreModelListForReturn.add(dataStoreModel);
        }
        return dataStoreModelListForReturn;
    }

    // Получение моделей ДатаСторв через VirtualMachine
    private List<DataStoreModel> getDataStoreModelsByVirtualMachine(VirtualMachine virtualMachine) throws RemoteException {
        Datastore[] dataStores = virtualMachine.getDatastores();
        List<DataStoreModel> dataStoreModelsListForReturn = new ArrayList<>();

        for(Datastore ds: dataStores){
            DataStoreModel dataStoreModel = new DataStoreModel(ds);
            // Получение списка Сигналов тревоги
            dataStoreModel.setAlarmsList(getAlarms(ds.getTriggeredAlarmState()));

            dataStoreModelsListForReturn.add(dataStoreModel);
        }
        return dataStoreModelsListForReturn;
    }

    public List<DataStoreModel> getDataStoreModelsWithVirtualMachines() throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();
        List<DataStoreModel> dataStoreModelsListForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntities){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            Datastore[] dataStores = ccr.getDatastores();
            for(Datastore ds: dataStores){
                DataStoreModel dataStoreModel = new DataStoreModel(ds);
                dataStoreModel.setAlarmsList(getAlarms(ds.getTriggeredAlarmState()));
                dataStoreModel.setVirtualMachinesList(getVirtualMachineModelsByDataStore(ds));
                dataStoreModelsListForReturn.add(dataStoreModel);
            }
        }
        return dataStoreModelsListForReturn;
    }

    public List<DataStoreModel> getDataStoreModelsWithHostsAndVirtualMachines() throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();
        List<DataStoreModel> dataStoreModelsListForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntities){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            Datastore[] dataStores = ccr.getDatastores();
            List<HostModel> allHostModelsList = getHostModels(ccr.getHosts());

            for(Datastore datastore: dataStores){
                DataStoreModel dataStoreModel = new DataStoreModel(datastore);
                dataStoreModel.setAlarmsList(getAlarms(datastore.getTriggeredAlarmState()));
                dataStoreModel.setHostsList(getHostModelsByDataStores(datastore, allHostModelsList));
                dataStoreModel.setVirtualMachinesList(getVirtualMachineModelsByDataStore(datastore));
                dataStoreModel.setClusterName(ccr.getName());
                dataStoreModelsListForReturn.add(dataStoreModel);
            }
        }
        return dataStoreModelsListForReturn;
    }

    ////////////////////////////////////////////////////////////////////// Alarm

    // Получаем списко всех Сигналов тревоги
    public List<AlarmModel> getAlarms() throws RemoteException {
        ManagedEntity[] managedEntities = getManagedEntityCCR();
        List<AlarmModel> alarmModelsForReturn = new ArrayList<>();

        for(ManagedEntity managedEntity: managedEntities){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            AlarmState[] alarmStates = ccr.getTriggeredAlarmState();
            for(AlarmState alarmState: alarmStates){
                alarmModelsForReturn.add(new AlarmModel(alarmState, getObjectByMorVal(alarmState.getEntity().getVal())));
            }
        }

        return alarmModelsForReturn;
    }

    // Получаем списко Сигналов тревоги через AlarmState
    private List<AlarmModel> getAlarms(AlarmState[] alarmStates) throws RemoteException {
        if(alarmStates==null){
            return new ArrayList<>();
        }

        List<AlarmModel> alarmModelsForReturn = new ArrayList<>(alarmStates.length);
        for(AlarmState alarmState: alarmStates){
            alarmModelsForReturn.add(new AlarmModel(alarmState, getObjectByMorVal(alarmState.getEntity().getVal())));
        }
        return alarmModelsForReturn;
    }

    // Objects: Cluster, Host, DataStore or VirtualMachine

//    private List<AlarmModel> getAlarmsWithKnownRelatedObjectName(AlarmState[] alarmStates, String name){
//        List<AlarmModel> alarmModelsForReturn = new ArrayList<>(alarmStates.length);
//        for(AlarmState alarmState: alarmStates){
//            alarmModelsForReturn.add(new AlarmModel(alarmState, name));
//        }
//        return alarmModelsForReturn;
//    }

    // получаем ObjectModel по ManagedObjectReference
    private ObjectModel getObjectByMorVal(String morVal) throws RemoteException {
        // Проверка виртруалок
        ManagedEntity[] mes = new InventoryNavigator(folder).searchManagedEntities("VirtualMachine");
        for(ManagedEntity managedEntity: mes){
            VirtualMachine vm = (VirtualMachine) managedEntity;
            if(vm.getMOR().getVal().equals(morVal)){
                // В качестве объекта передается строка, которая в дальнейшем булет использована для перехода по ссылке
                // 127.0.0.1:8080/virtualMachines/<virtualMachineName>
                return new ObjectModel(vm.getName(), "virtualMachines");
            }
        }

        mes = new InventoryNavigator(folder).searchManagedEntities("ClusterComputeResource");
        for(ManagedEntity managedEntity: mes){
            ClusterComputeResource ccr = (ClusterComputeResource) managedEntity;
            // Проверка Кластера
            if(ccr.getMOR().getVal().equals(morVal)){
                // В качестве объекта передается строка, которая в дальнейшем булет использована для перехода по ссылке
                // 127.0.0.1:8080/clusters/<clusterName>
                return new ObjectModel(ccr.getName(), "clusters");
            }

            // Проверка Хостов
            HostSystem[] hostSystems = ccr.getHosts();
            for(HostSystem hostSystem: hostSystems){
                if(hostSystem.getMOR().getVal().equals(morVal)){
                    // В качестве объекта передается строка, которая в дальнейшем булет использована для перехода по ссылке
                    // 127.0.0.1:8080/hosts/<hostName>
                    return new ObjectModel(hostSystem.getName(), "hosts");
                }
            }

            // Проверка ДатаСторов
            Datastore[] dataStores = ccr.getDatastores();
            for(Datastore datastore: dataStores){
                if(datastore.getMOR().getVal().equals(morVal)){
                    // В качестве объекта передается строка, которая в дальнейшем булет использована для перехода по ссылке
                    // 127.0.0.1:8080/dataStores/<dataStoreName>
                    return new ObjectModel(datastore.getName(), "dataStores");
                }
            }
        }
        return null;
    }

}
