package com.castagnolofugale.videomanagementservice.model;

import java.io.Serializable;
//stati che possono essere attribuiti agli oggetti video
public enum VideoInformationStatus implements Serializable {
    WAITINGUPLOAD, //quando viene creata l'entry con le info ma non è stato ancora caricato
    UPLOADED,     //video caricato ma non processato

}
