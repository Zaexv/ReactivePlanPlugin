package com.atlassian.reactiveplan.servlet;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.reactiveplan.logic.IssueLogic;
import com.atlassian.reactiveplan.logic.ProjectLogic;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactiveplan.entities.Employee;
import reactiveplan.jiraconverter.JiraToReplanConverter;
import reactiveplan.jiraconverter.ReplanToJiraConverter;
import reactiveplan.jsonhandler.ReplanOptimizerRequest;
import reactiveplan.jsonhandler.ReplanOptimizerResponse;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

public class ReplanServlet extends HttpServlet{
    private static final Logger log = LoggerFactory.getLogger(ReplanServlet.class);


    private static final String MAIN_SCREEN = "/templates/replanMain.vm";
    private static final String LIST_ISSUES_TEMPLATE = "/templates/list.vm";
    private static final String LIST_PLAN_TEMPLATE = "/templates/replanList.vm";



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



        //Variables del método GET
        /*Session*/
        HttpSession session = req.getSession();
        /* Request Parameters*/
        String action = Optional.ofNullable(req.getParameter("actionType")).orElse("");
        resp.setContentType("text/html;charset=utf-8");

        /*Initializing Data Structures */
        Collection<Issue> projectIssues = null;
        Set<Employee> employees = null;
        Map<String,Object> context = new HashMap<>();
        /*Initializing Logics*/
        IssueLogic issLogic = IssueLogic.getInstance(issueService,projectService,searchService);
        ProjectLogic prlogic = ProjectLogic.getInstance(issueService,projectService,searchService);

        /*Obtaining Users & Project Objects */
        Set<ApplicationUser> userset;
        Project pr;

        Gson gson = new Gson();

        switch(action){
           case "getProject":


                projectIssues =  issLogic.getAllProjectIssues(authenticationContext.getLoggedInUser(),req.getParameter("project-key"));
                context.put("issues",projectIssues);
                templateRenderer.render(LIST_ISSUES_TEMPLATE, context, resp.getWriter());
                break;

            case "getIssuesToPlan":
                String projectKey = req.getParameter("project-key");
                String versionKey = req.getParameter("version-key");
                session.setAttribute("project-key", projectKey);
                session.setAttribute("version-key", versionKey);
                context.put("version-key", versionKey);
                context.put("project-key",projectKey);
                userset = prlogic.getAllProjectUsers(projectKey);

                pr = prlogic.getProjectByKey(projectKey);
                employees = JiraToReplanConverter.applicationUsersToEmployees(userset,prlogic,pr);
                context.put("employees",employees);

                if(versionKey.equals("")) { //Por defecto, planifica todos los issues en el proyecto.

                    projectIssues =  issLogic.getAllProjectIssues(authenticationContext.getLoggedInUser(),
                            req.getParameter("project-key"));

                        context.put("features",JiraToReplanConverter.issuesToFeatures(projectIssues,issLogic));
                        templateRenderer.render(LIST_ISSUES_TEMPLATE, context, resp.getWriter());
                    }
                 else{
                    Version version = pr.getVersions() //Sólo debería haber una versión
                            .stream().filter(v -> v.getName().equals(versionKey)).findFirst().orElse(null);
                    if( version != null){
                        projectIssues =  issLogic.getOpenedProjectIssuesByVersion(authenticationContext.getLoggedInUser(),projectKey, version.getName());
                            context.put("version", version);
                            context.put("features",JiraToReplanConverter.issuesToFeatures(projectIssues,issLogic));
                            templateRenderer.render(LIST_ISSUES_TEMPLATE, context, resp.getWriter());
                    } else {
                        resp.getWriter().write("Error, no se ha podido hacer el plan :(, la version con nombre " + versionKey +
                                " no existe");
                    }
                }


                break;


            case "getProjectPlan":

               projectKey = session.getAttribute("project-key").toString();
               versionKey = session.getAttribute("version-key").toString();

                userset = prlogic.getAllProjectUsers(projectKey);
                pr = prlogic.getProjectByKey(projectKey);
                employees = JiraToReplanConverter.applicationUsersToEmployees(userset, prlogic, pr);

                if(versionKey.equals("")) { //Por defecto, planifica todos los issues en el proyecto.

                    projectIssues =  issLogic.getAllProjectIssues(authenticationContext.getLoggedInUser(),
                            projectKey);

                    ReplanOptimizerRequest replanRequest = new ReplanOptimizerRequest(employees,
                            JiraToReplanConverter.
                                    issuesToFeatures(projectIssues, issLogic));
                    String response = replanRequest.doRequest();
                    if (response == null) {
                        resp.getWriter().write("Error, no se ha podido hacer el plan :(");
                    } else {
                        ReplanOptimizerResponse plan =
                                gson.fromJson(response, ReplanOptimizerResponse.class);

                        session.setAttribute("plan", response);
                        context.put("plan", plan);
                        context.put("issues",projectIssues);
                        //resp.getWriter().write(response);
                        templateRenderer.render(LIST_PLAN_TEMPLATE, context, resp.getWriter());
                    }
                } else{
                    Version version = pr.getVersions() //Sólo debería haber una versión
                            .stream().filter(v -> v.getName().equals(versionKey)).findFirst().orElse(null);
                    if( version != null){

                        projectIssues =  issLogic.getOpenedProjectIssuesByVersion(authenticationContext.getLoggedInUser(),projectKey, version.getName());
                        ReplanOptimizerRequest replanRequest = new ReplanOptimizerRequest(employees,
                                JiraToReplanConverter.
                                        issuesToFeatures(projectIssues, issLogic));
                        String response = replanRequest.doRequest();
                        if (response == null) {
                            resp.getWriter().write("Error, no se ha podido hacer el plan :(");
                        } else {
                            ReplanOptimizerResponse plan =
                                    gson.fromJson(response, ReplanOptimizerResponse.class);
                            context.put("version", version);
                            context.put("plan", plan);
                            context.put("issues",projectIssues);
                            session.setAttribute("plan", response);
                            //resp.getWriter().write(response);
                            templateRenderer.render(LIST_PLAN_TEMPLATE, context, resp.getWriter());
                        }




                    } else {
                        resp.getWriter().write("Error, no se ha podido hacer el plan :(, la version con nombre " + versionKey +
                                " no existe");
                    }



                }
                break;

            default:

                templateRenderer.render(MAIN_SCREEN, context, resp.getWriter());
                break;

        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        HttpSession session = req.getSession();
        Gson gson = new Gson();
        ReplanOptimizerResponse response =
                gson.fromJson(session.getAttribute("plan").toString(), ReplanOptimizerResponse.class);
        IssueLogic issueLogic = IssueLogic.getInstance(issueService,projectService,searchService);
        ReplanToJiraConverter.persistAllFeaturesToJira(user,response.getEmployees(),issueLogic, new Date());

    }

}