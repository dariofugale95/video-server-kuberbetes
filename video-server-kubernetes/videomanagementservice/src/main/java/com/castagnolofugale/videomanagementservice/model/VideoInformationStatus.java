package com.castagnolofugale.videomanagementservice.model;

import java.io.Serializable;

public enum VideoInformationStatus implements Serializable {
    WAITINGUPLOAD,
    UPLOADED,
    AVAILABLE,
    NOTAVAILABLE
}
