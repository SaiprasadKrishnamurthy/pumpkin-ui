package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.GitLogResponse;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by saipkri on 09/03/17.
 */
@Data
public class ChangeController {

    private Date timestamp = new Date();
    private List<GitLogResponse> responses;
    private final PumpkinService pumpkinService = new PumpkinService();

    public ChangeController() {

    }
    public void search() {
        responses = pumpkinService.changes(timestamp.getTime());
    }
}
