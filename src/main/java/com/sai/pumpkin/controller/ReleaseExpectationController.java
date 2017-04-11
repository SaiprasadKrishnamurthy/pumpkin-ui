package com.sai.pumpkin.controller;

import com.sai.pumpkin.model.ReleaseExpectation;
import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;
import org.primefaces.context.RequestContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
public class ReleaseExpectationController {

    private List<ReleaseExpectation> tests;
    private final PumpkinService pumpkinService = new PumpkinService();
    private String result;
    private String featureText;
    private ReleaseExpectation created = new ReleaseExpectation();

    public ReleaseExpectationController() {
        tests = pumpkinService.allTests();
        created.setFeatureText(pumpkinService.testTemplate());
        System.out.println(created);
        runAll();
    }

    public void update() {
        String name = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("name");
        ReleaseExpectation r = tests.stream().filter(releaseExpectation -> releaseExpectation.getName().equals(name)).findFirst().get();
        System.out.println(r);
        pumpkinService.saveTest(r);
        runAll();
    }

    public void runAll() {
        List<String> collect = tests.stream().map(ReleaseExpectation::getName).map(pumpkinService::execTest).collect(toList());
        result = collect.get(collect.size() - 1);
    }

    public void save() {
        if (tests.stream().anyMatch(r -> r.getName().equals(created.getName()))) {
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Test name must be unique"));
        } else {
            pumpkinService.saveTest(created);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Test saved"));
        }
    }
}
