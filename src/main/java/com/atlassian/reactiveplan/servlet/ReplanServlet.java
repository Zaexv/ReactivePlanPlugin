package com.atlassian.reactiveplan.servlet;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.reactiveplan.logic.IssueLogic;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ReplanServlet extends HttpServlet{
    private static final Logger log = LoggerFactory.getLogger(ReplanServlet.class);


    private static final String MAIN_SCREEN = "/templates/replanMain.vm";
    private static final String LIST_ISSUES_TEMPLATE = "/templates/list.vm";


    @JiraImport
    private IssueService issueService;
    @JiraImport
    private ProjectService projectService;
    @JiraImport
    private SearchService searchService;
    @JiraImport
    private TemplateRenderer templateRenderer;
    @JiraImport
    private JiraAuthenticationContext authenticationContext;
    @JiraImport
    private ConstantsManager constantsManager;



    public ReplanServlet(IssueService issueService, ProjectService projectService,
                     SearchService searchService,
                     TemplateRenderer templateRenderer,
                     JiraAuthenticationContext authenticationContext,
                     ConstantsManager constantsManager) {
        this.issueService = issueService;
        this.projectService = projectService;
        this.searchService = searchService;
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.constantsManager = constantsManager;
    }



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {


        String action = Optional.ofNullable(req.getParameter("actionType")).orElse("");
        Map<String,Object> context = new HashMap<>();
        resp.setContentType("text/html;charset=utf-8");
        switch(action){
           case "getProject":
               IssueLogic issLogic = IssueLogic.getInstance(issueService,projectService,searchService);

              Collection<Issue> projectIssues =  issLogic.getProjectIssues(authenticationContext.getLoggedInUser(),req.getParameter("project-key"));
                context.put("issues",projectIssues);
                templateRenderer.render(LIST_ISSUES_TEMPLATE, context, resp.getWriter());
                break;



            default:

                templateRenderer.render(MAIN_SCREEN, context, resp.getWriter());
                break;

        }
    }

}