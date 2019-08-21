package com.vsphere.core.models;

import com.vmware.vim25.AlarmState;
import lombok.Data;

import java.util.Date;

@Data
public class AlarmModel {

//    private String name;
//
//    private String description;

    private String overallStatus;

    private ObjectModel object;

    private Date time;

    public AlarmModel(AlarmState alarmState, ObjectModel objectModel){
        this.overallStatus = alarmState.getOverallStatus().toString();
        this.time = alarmState.getTime().getTime();
        this.object = objectModel;
    }

}

