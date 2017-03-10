package com.sai.pumpkin.controller;

import com.sai.pumpkin.service.PumpkinService;
import lombok.Data;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by saipkri on 10/03/17.
 */
@Data
public class DiffSourceController {
    private String diff;
    private final PumpkinService pumpkinService = new PumpkinService();


    public DiffSourceController() {
        HttpServletRequest rq = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String artifactId = rq.getParameter("artifactId");
        String groupId = rq.getParameter("groupId");
        String oldGit = rq.getParameter("oldGit");
        String filePath = rq.getParameter("filePath");
        String newGit = rq.getParameter("newGit");
        String gitRevision = rq.getParameter("gitRevision");

        if (gitRevision != null && gitRevision.trim().length() > 0) {
            diff = pumpkinService.bulkDiffSource(groupId, artifactId, gitRevision);
        } else {
            diff = pumpkinService.diffSource(groupId, artifactId, filePath, oldGit, newGit);
        }

    }
}
