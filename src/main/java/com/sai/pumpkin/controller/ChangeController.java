package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.GitLogResponse;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 09/03/17.
 */
@Data
public class ChangeController {

    private Date from = null;
    private Date to = null;
    private final List<TimeUnit> units = Stream.of(TimeUnit.values()).sorted((a, b) -> a.toString().compareTo(b.toString())).collect(toList());
    private long relative;
    private List<GitLogResponse> responses;
    private TimeUnit unit;
    private final PumpkinService pumpkinService = new PumpkinService();
    private boolean absoluteMode, relativeMode;

    public ChangeController() {

    }

    public void searchAbsolute() {
        absoluteMode = false;
        relativeMode = false;
        responses = pumpkinService.changeWithinRange(from.getTime(), to.getTime());
        absoluteMode = true;
    }

    public void searchRelative() {
        absoluteMode = false;
        relativeMode = false;
        responses = pumpkinService.changeRelative(relative, unit);
        relativeMode = true;
    }
}
