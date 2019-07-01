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
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.reactiveplan.exception.ReplanException;
import com.atlassian.reactiveplan.logic.IssueLogic;
import com.atlassian.reactiveplan.logic.ProjectLogic;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactiveplan.entities.DaySlot;
import reactiveplan.entities.Employee;
import reactiveplan.converter.JiraToReplanConverter;
import reactiveplan.converter.ReplanToJiraConverter;
import reactiveplan.jsonhandler.ReplanOptimizerRequest;
import reactiveplan.jsonhandler.ReplanOptimizerResponse;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Scanned
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

        /*Initializing Data Structures*/
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

        try {
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


                if(projectKey == null )
                    throw new ReplanException("Project do not exist. Please enter a valid project name");
                if(versionKey == null )
                    throw new ReplanException("Version do not exist. Plase enter a valid version name.");


                pr = prlogic.getProjectByKey(projectKey);
                userset = prlogic.getAllProjectUsers(projectKey);
                employees = JiraToReplanConverter.applicationUsersToEmployees(userset,prlogic,pr);
                context.put("employees",employees);



                if(versionKey.equals("Total Project")) { //Por defecto, planifica todos los issues en el proyecto.



                    projectIssues =  issLogic.getAllProjectIssues(authenticationContext.getLoggedInUser(),
                            req.getParameter("project-key"));

                        context.put("features",JiraToReplanConverter.issuesToFeatures(projectIssues,issLogic));
                        templateRenderer.render(LIST_ISSUES_TEMPLATE, context, resp.getWriter());
                }
                 else{

                    Version version = pr.getVersions() //Sólo debería haber una versión
                            .stream().filter(v -> v.getName().equals(versionKey)).findFirst().orElse(null);

                    if(version != null){
                        projectIssues =  issLogic.getOpenedProjectIssuesByVersion(authenticationContext.getLoggedInUser(),projectKey, version.getName());
                            context.put("version", version);
                            context.put("features",JiraToReplanConverter.issuesToFeatures(projectIssues,issLogic));
                            templateRenderer.render(LIST_ISSUES_TEMPLATE, context, resp.getWriter());
                    } else {
                        context.put("error", new ReplanException("Version with name " + versionKey +  " do not exists."));
                        context.put("projects", prlogic.getAllProjects());
                        context.put("versions", prlogic.getAllVersions());
                        templateRenderer.render(MAIN_SCREEN, context, resp.getWriter());
                    }
                }
                break;


            case "getProjectPlan":

               projectKey = session.getAttribute("project-key").toString();
               versionKey = session.getAttribute("version-key").toString();

                userset = prlogic.getAllProjectUsers(projectKey);
                pr = prlogic.getProjectByKey(projectKey);


                if(versionKey.equals("") || versionKey.equals("Total Project")) { //Si se selecciona total project, planifica todo el proyecto.
                    employees = JiraToReplanConverter.applicationUsersToEmployees(userset, prlogic, pr);
                    projectIssues =  issLogic.getAllProjectIssues(authenticationContext.getLoggedInUser(),
                            projectKey);

                    ReplanOptimizerRequest replanRequest = new ReplanOptimizerRequest(employees,
                            JiraToReplanConverter.
                                    issuesToFeatures(projectIssues, issLogic));
                    context.put("calendar",getCalendar(replanRequest));
                    String response = replanRequest.doRequest();
                    if (response == null) {
                        context.put("projects", prlogic.getAllProjects());
                        context.put("versions", prlogic.getAllVersions());
                        context.put("error", new ReplanException("Can't connect to server. Can't make plan."));
                        templateRenderer.render(MAIN_SCREEN, context, resp.getWriter());

                    } else {
                        ReplanOptimizerResponse plan =
                                gson.fromJson(response, ReplanOptimizerResponse.class);
                        session.setAttribute("plan", response);
                        context.put("plan", plan);
                        context.put("unplannedfeatures", plan.getUnplannedFeatures(replanRequest.getFeatureRequest()));
                        templateRenderer.render(LIST_PLAN_TEMPLATE, context, resp.getWriter());
                    }
                } else{
                    Version version = pr.getVersions() //Sólo debería haber una versión
                            .stream().filter(v -> v.getName().equals(versionKey)).findFirst().orElse(null);
                    if( version != null){
                        employees = JiraToReplanConverter.applicationUsersToEmployees(userset, prlogic,pr ,version);
                        projectIssues =  issLogic.getOpenedProjectIssuesByVersion(authenticationContext.getLoggedInUser(),projectKey, version.getName());
                        ReplanOptimizerRequest replanRequest = new ReplanOptimizerRequest(employees,
                                JiraToReplanConverter.
                                        issuesToFeatures(projectIssues, issLogic));

                        context.put("calendar",getCalendar(replanRequest));

                        String response = replanRequest.doRequest();
                        if (response == null) {
                            resp.getWriter().write("Error, no se ha podido hacer el plan :(");
                        } else {
                            ReplanOptimizerResponse plan =
                                    gson.fromJson(response, ReplanOptimizerResponse.class);
                            context.put("version", version);
                            context.put("plan", plan);
                            context.put("unplannedfeatures", plan.getUnplannedFeatures(replanRequest.getFeatureRequest()));
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
                context.put("projects",prlogic.getAllProjects());
                context.put("versions",prlogic.getAllVersions());
                templateRenderer.render(MAIN_SCREEN, context, resp.getWriter());
                break;

        }
        } catch (Exception e){

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            System.out.println(sStackTrace);

                    context.put("error", e);
                    context.put("projects", prlogic.getAllProjects());
                    context.put("versions", prlogic.getAllVersions());
                    templateRenderer.render(MAIN_SCREEN, context, resp.getWriter());

        }
    }

    private List<DaySlot> getCalendar(ReplanOptimizerRequest request){
        //Cojo el primer calendario que haya.
        return request.getEmployeesRequest().iterator().next().getCalendar();
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        ProjectLogic prlogic = ProjectLogic.getInstance(issueService,projectService,searchService);
        Map<String,Object> context = new HashMap<>();
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        HttpSession session = req.getSession();
        String projectKey = session.getAttribute("project-key").toString();
        String versionKey = session.getAttribute("version-key").toString();
        try{
        Project pr = prlogic.getProjectByKey(projectKey);
        Version ver = pr.getVersions()
                .stream().filter(v -> v.getName().equals(versionKey)).findFirst().orElse(null);

        Gson gson = new Gson();
        ReplanOptimizerResponse response =
                gson.fromJson(session.getAttribute("plan").toString(), ReplanOptimizerResponse.class);
        IssueLogic issueLogic = IssueLogic.getInstance(issueService,projectService,searchService);

        if(ver != null){
            ReplanToJiraConverter.persistAllFeaturesToJira(user,response.getEmployees(),issueLogic, ver.getStartDate());
        } else {
            ReplanToJiraConverter.persistAllFeaturesToJira(user,response.getEmployees(),issueLogic, new Date());

        }



        if (ver != null){
            context.put("version",ver);
            context.put("calendar",JiraToReplanConverter.getCalendarFromVersion(8.0,ver,5));
        } else {

            /*Añado el calendario con los valores por defecto de planificaciones a 4 semanas. */
            context.put("calendar",JiraToReplanConverter.getDefaultCalendar(8.0,5,4));
        }

        context.put("plan", response);
        context.put("success",true);
        templateRenderer.render(LIST_PLAN_TEMPLATE, context, resp.getWriter());

        }
        catch (Exception e ){

            context.put("projects",prlogic.getAllProjects());
            context.put("versions",prlogic.getAllVersions());
            context.put("error", e);
            templateRenderer.render(MAIN_SCREEN, context, resp.getWriter());
    }


    }

}