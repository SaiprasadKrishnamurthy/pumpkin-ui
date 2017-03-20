package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.ReleaseExpectation;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;

import javax.faces.context.FacesContext;
import java.util.List;

@Data
public class ReleaseExpectationController {

    private List<ReleaseExpectation> tests;
    private final PumpkinService pumpkinService = new PumpkinService();
    private String result;

    public ReleaseExpectationController() {
        tests = pumpkinService.allTests();
    }

    public void run() {
        String name = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("name");
        ReleaseExpectation r = tests.stream().filter(releaseExpectation -> releaseExpectation.getName().equals(name)).findFirst().get();
        System.out.println(r);
        pumpkinService.saveTest(r);
        result = pumpkinService.execTest(r.getName());
    }
}
