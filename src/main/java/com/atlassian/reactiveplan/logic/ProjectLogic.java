package com.atlassian.reactiveplan.logic;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.reactiveplan.exception.ReplanException;
import jdk.nashorn.internal.objects.annotations.Constructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectLogic {


    private static ProjectLogic logic;

    private IssueService issueService;

    private ProjectService projectService;

    private SearchService searchService;

    private static final Logger log = LoggerFactory.getLogger(IssueLogic.class);

    private ProjectLogic(IssueService issueService, ProjectService projectService, SearchService searchService) {
        this.issueService = issueService;
        this.projectService = projectService;
        this.searchService = searchService;
    }

    /* Se aplica el patrón Singleton para crear una única Instancia de la Lógica. */

    public static ProjectLogic getInstance(IssueService issueService, ProjectService projectService,
                                         SearchService searchService) {
        if (logic == null) {
            logic = new ProjectLogic(issueService, projectService, searchService);
        }
        return logic;
    }

    public Project getProjectByKey(String projectKey) throws ReplanException {

        try {

            return ComponentAccessor.getProjectManager().getProjectByCurrentKey(projectKey) ;
        }
         catch (Exception e){
             throw new ReplanException("Project with Key " + projectKey +  " do not exists.");
         }

    }

    public List<Project> getAllProjects(){
        return ComponentAccessor.getProjectManager().getProjects();
    }

    public List<Project> getProjectLeadBy(ApplicationUser user){
        return ComponentAccessor.getProjectManager().getProjectsLeadBy(user);
    }

    public Collection<ProjectRole> getProjectRoles(){
      ProjectRoleManager pRoleManager =  ComponentAccessor.getComponent(ProjectRoleManager.class);
      return pRoleManager.getProjectRoles();
    }

    public Collection<ProjectRole> getRolesOfUserInProject(ApplicationUser appUser, Project project){
        ProjectRoleManager pRoleManager =  ComponentAccessor.getComponent(ProjectRoleManager.class);
        Collection<ProjectRole> userRoles = new ArrayList<>();
        for(ProjectRole prole : getProjectRoles()){
           if(pRoleManager.isUserInProjectRole(appUser,prole,project)){
               userRoles.add(prole);
           }
        }
        return userRoles;
    }

    public Set<ApplicationUser> getProjectUsersWithRole(ProjectRole role, Project project) throws ReplanException {

        if(project == null ) throw new ReplanException ("Project not found. Please enter a valid Project Key");

        ProjectRoleManager pRoleManager =  ComponentAccessor.getComponent(ProjectRoleManager.class);
       ProjectRoleActors pra = pRoleManager.getProjectRoleActors(role,project);
       return pra.getApplicationUsers();
    }

    public Set<ApplicationUser> getAllProjectUsers(String projectKey) throws ReplanException {
        Project pr = this.getProjectByKey(projectKey);
        Set<ApplicationUser> userset = new HashSet<>();
        for(ProjectRole role : this.getProjectRoles()){
            userset.addAll(this.getProjectUsersWithRole(role,pr));
        }

        return userset;
    }


    public Set<Version> getAllVersions(){
        Set<Version> versions = new HashSet<>();

        for(Project pr : getAllProjects()){
            versions.addAll(pr.getVersions());

        }
        return versions;
    }

    public Version getProjectVersionByName(String version, Project project){

        return project.getVersions().stream().filter(versionObject -> versionObject.getName().equalsIgnoreCase(version)).findFirst().get();

    }




}
