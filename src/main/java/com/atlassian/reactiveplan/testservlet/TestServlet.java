package com.atlassian.reactiveplan.testservlet;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.reactiveplan.logic.IssueLogic;
import com.atlassian.reactiveplan.logic.ProjectLogic;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactiveplan.entities.Employee;
import reactiveplan.entities.Feature;
import reactiveplan.jiraconverter.JiraToReplanConverter;
import reactiveplan.jsonhandler.ReplanOptimizerRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestServlet extends HttpServlet{
    private static final Logger log = LoggerFactory.getLogger(TestServlet.class);

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


    public TestServlet(IssueService issueService, ProjectService projectService,
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
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();

        // TEST1:
        // testIssueLogic_getIssueByKey(resp, user,"PDP-7");

        // TEST2:
        //testIssueLogic_getProjectIssues(resp, user);

        //TEST3:

        //testProjectLogic_getProjectRoles(resp);

        //Test4:

        //testProjectLogic_getProjectUsersWithRole(resp);

        //Test5:
        //testJiraToReplanConverter_issueToFeature(resp, user);

        //Test6:
        testJiraToReplanConverter_getDefaultCalendar(resp);


    }

    private void testJiraToReplanConverter_getDefaultCalendar(HttpServletResponse resp) throws IOException {
        ProjectLogic prlogic = ProjectLogic.getInstance(issueService,projectService,searchService);
        Project pr =  prlogic.getProjectByKey("PDP");
        IssueLogic issueLogic = IssueLogic.getInstance(issueService,projectService,searchService);
        ApplicationUser currentuser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        //TODO Should I separar esto en una función extra para la lógica?

        Set<ApplicationUser> userset = new HashSet<>();
        for(ProjectRole role : prlogic.getProjectRoles()){
           userset.addAll(prlogic.getProjectUsersWithRole(role,pr));
        }
        Set<Employee> employeeset = new HashSet<>();
        for(ApplicationUser appuser : userset){
            Employee e = JiraToReplanConverter.applicationUserToEmployee(appuser, prlogic.getRolesOfUserInProject(appuser,pr));
            e.setCalendar(JiraToReplanConverter.getDefaultCalendar(8,5,2));
            employeeset.add(e);
        }

        for(ApplicationUser appuser : userset){
            resp.getWriter().write("<html><body><b> AppUser </b>" +
                    "</br>" +
                    "</br>" +

                    appuser.toString() +  "</br>" +


                    "</body></html>");
        }

        for(Employee emp : employeeset){
            resp.getWriter().write("<html><body><b> Employee </b>" +
                    "</br>" +
                    "</br>" +

                    emp.toString() +  "</br>" +
                    emp.getCalendar().toString() +  "</br>" +

                    "</body></html>");
        }

        Collection<Issue> issues = issueLogic.getProjectIssues(currentuser,"PDP");
        Collection<Feature> features = new ArrayList<>();
        for(Issue issue : issues){
            Feature f = JiraToReplanConverter.issueToFeature(issue,issueLogic.getIssueDependencies(issue));
            features.add(f);

        }

        ReplanOptimizerRequest ror = new ReplanOptimizerRequest(employeeset,features);

        Gson gson = new Gson();

        String request = gson.toJson(ror);

        resp.getWriter().write(features.toString());

        resp.getWriter().write(request);






    }

    private void testJiraToReplanConverter_issueToFeature(HttpServletResponse resp, ApplicationUser user) throws IOException {
        IssueLogic logic =  IssueLogic.getInstance(issueService,projectService,searchService);
        Collection<Issue> issues =  logic.getProjectIssues(
            user,"PDP"
    );

        for(Issue issue : issues){
            Feature f = JiraToReplanConverter.issueToFeature(issue,logic.getIssueDependencies(issue));

            resp.getWriter().write("<html><body><b> ISSUE </b>" +
                    "</br>" +
                    "</br>" +
                    issue.getSummary() + "</br>" +
                    issue.getDescription() + "</br>" +
                    issue.getAssignee() + "</br>" +
                    issue.getCreator() +  "</br>" +
                    issue.getStatusId() + "</br>" +
                    issue.getStatus() + "</br>" +
                    issue.getIssueType() + "</br>" +
                    issue.getDueDate() +  "</br>" +
                    issue.getKey() +  "</br>" +
                    logic.getIssueDependencies(issue).toString() +  "</br>" +
                    "<b> Feature </b><br/>" +  f.toString() + "</br>" +
                    f.getPriority() +  "</br>" +

                    "</body></html>");
        }
    }

    private void testProjectLogic_getProjectUsersWithRole(HttpServletResponse resp) throws IOException {
        ProjectLogic prlogic = ProjectLogic.getInstance(issueService,projectService,searchService);
        Project pr =  prlogic.getProjectByKey("PDP");
        resp.setContentType("text/html");
        for (ProjectRole prole : prlogic.getProjectRoles()
             ) {

            for (ApplicationUser appuser : prlogic.getProjectUsersWithRole(prole,pr)
                 ) {


                resp.getWriter().write("<html><body>This is the Project USer Role I love " +
                        "</br>" +
                        "</br>" +
                        prole.getName() + "</br>" +
                        appuser.getName()+ "</br>" +
                        "Prueba de que esto funciona </br>" +
                        "</body></html>");

            }

        }
    }


    /* Private Test Functions
                                                     `;-.          ___,
                                                      `.`\_...._/`.-"`
                                                        \        /      ,
                                                        /()   () \    .' `-._
                                                       |)  .    ()\  /   _.'
                                                       \  -'-     ,; '. <
                                                        ;.__     ,;|   > \
                                                       / ,    / ,  |.-'.-'
                                                      (_/    (_/ ,;|.<`
                                                        \    ,     ;-`
                                                         >   \    /
                                                        (_,-'`> .'
                                                             (_,'
    *
    * */
    private void testIssueLogic_getIssueByKey(HttpServletResponse resp, ApplicationUser user,String issueKey) throws IOException {
        IssueLogic issueLogic = IssueLogic.getInstance(issueService,projectService,searchService);
        Issue issue = issueLogic.getIssueByKey(issueKey, user);
        resp.setContentType("text/html");
        resp.getWriter().write("<html><body>This is the issue I love " +
                "</br>" +
                "</br>" +
                issue.getSummary() + "</br>" +
                issue.getDescription() + "</br>" +
                issue.getAssignee().toString() + "</br>" +
                issue.getCreator() +  "</br>" +
                issue.getStatusId() + "</br>" +
                issue.getStatus() + "</br>" +
                issue.getIssueType() + "</br>" +
                issue.getDueDate() +  "</br>" +
                issue.getKey() +  "</br>" +
                "Prueba de que esto funciona </br>" +
                "</body></html>");
    }

    private void testIssueLogic_getProjectIssues(HttpServletResponse resp, ApplicationUser user) throws IOException {
        IssueLogic issueLogic = IssueLogic.getInstance(issueService,projectService,searchService);
        Collection<Issue> issues = issueLogic.getProjectIssues(user,"PDP");
        resp.setContentType("text/html");

        for(Issue issue : issues) {
            resp.getWriter().write("<html><body>This is the issue I love " +
                    "</br>" +
                    "</br>" +
                    issue.getSummary() + "</br>" +

                    issue.getCreated() + "</br>" +
                    issue.getDescription() + "</br>" +
                    issue.getAssignee().toString() + "</br>" +
                    issue.getCreator() + "</br>" +
                    issue.getStatusId() + "</br>" +
                    issue.getPriority() + "</br>" +
                    issue.getStatus() + "</br>" +
                    issue.getStatus().getStatusCategory().getPrimaryAlias() + "</br>" +
                    issue.getIssueType() + "</br>" +
                    issue.getDueDate() + "</br>" +
                    issue.getKey() + "</br>" +
                    "Prueba de que esto funciona </br>" +
                    "</body></html>");
        }
    }

    private void testProjectLogic_getProjectRoles(HttpServletResponse resp) throws IOException {
        ProjectLogic prlogic = ProjectLogic.getInstance(issueService,projectService,searchService);

        Collection<ProjectRole> proles = prlogic.getProjectRoles();

        for (ProjectRole pr : proles) {
            resp.setContentType("text/html");
            resp.getWriter().write("<html><body>Those are the Project Roles I love " +
                    "</br>" +
                    "</br>" +
                    pr.getId() + "</br>" +
                    pr.getName() + "</br>" +
                    pr.getDescription() + "</br>" +

                    "Prueba de que esto funciona </br>" +
                    "</body></html>");
        }
    }

}