package com.sai.pumpkin.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by saipkri on 07/03/17.
 */
@Data
@EqualsAndHashCode(exclude = {"id", "uuid"})
public class ChangeSetEntry implements Serializable {
    private String id;
    private String uuid;
    private String filePath;
    private String changeType;
}
