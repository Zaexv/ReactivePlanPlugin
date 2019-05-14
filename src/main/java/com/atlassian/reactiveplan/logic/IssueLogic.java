package com.atlassian.reactiveplan.logic;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.LinkCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.query.Query;


public class IssueLogic {

    private static IssueLogic logic;

    private IssueService issueService;

    private ProjectService projectService;

    private SearchService searchService;

    private static final Logger log = LoggerFactory.getLogger(IssueLogic.class);

    private IssueLogic(IssueService issueService, ProjectService projectService, SearchService searchService) {
        this.issueService = issueService;
        this.projectService = projectService;
        this.searchService = searchService;
    }

    /* Se aplica el patrón Singleton para crear una única Instancia de la Lógica. */
    public static IssueLogic getInstance(IssueService issueService, ProjectService projectService,
                                         SearchService searchService) {
        if (logic == null) {
            logic = new IssueLogic(issueService, projectService, searchService);
        }
        return logic;
    }

    public Issue getIssueByKey(String issueKey, ApplicationUser user) {
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        jqlClauseBuilder.field("key").eq(issueKey);
        return doIssueQuery(user, jqlClauseBuilder);
    }

    public Issue getIssueById(String issueId,ApplicationUser user) {
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        jqlClauseBuilder.field("id").eq(issueId);
        return doIssueQuery(user, jqlClauseBuilder);
    }


    public IssueResult getIssue(ApplicationUser user, String issueKey) {
        return issueService.getIssue(user, issueKey);
    }

    public Collection<IssueType> getIssueTypesByProject(String projectKey) {
        Project proj = ComponentAccessor.getProjectManager().getProjectByCurrentKey(projectKey);
        Collection<IssueType> issueTypes = ComponentAccessor.getIssueTypeSchemeManager().getIssueTypesForProject(proj);
        ArrayList<IssueType> filteredIssueTypes = new ArrayList<IssueType>();

        // Remove subtypes from list (you cannot create a subtask from outside
        // an issue, so it doesn't make sense to include it
        Iterator<IssueType> iter = issueTypes.iterator();
        https: // open.spotify.com/track/5W3cjX2J3tjhG8zb6u0qHn
        while (iter.hasNext()) {
            IssueType it = iter.next();
            if (!it.isSubTask()) {
                filteredIssueTypes.add(it);
            }
        }

        return filteredIssueTypes;
    }

    public Collection<Issue> getIssueDependencies(Issue issue){
        //Se asumirá que hay una dependencia si hay otro issue enlazado sea cual sea su estado.


         List<IssueLink> links  =       ComponentAccessor.getIssueLinkManager().getOutwardLinks(issue.getId());
         Collection<Issue> dependencies = new ArrayList<> ();

         for (IssueLink issueLink : links){
             dependencies.add(issueLink.getDestinationObject());
         }

         return dependencies;
    }

    public Collection<Issue> getProjectIssues (ApplicationUser user, String projectKey) {

        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        Query query = jqlClauseBuilder.project(projectKey).buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();

        SearchResults searchResults = null;
        try {
            searchResults = searchService.search(user, query, pagerFilter);
        } catch (SearchException e) {
            e.printStackTrace();
        }
        return searchResults != null ? searchResults.getIssues() : null;

    }


    public Collection<Issue> getOpenedProjectIssues(ApplicationUser user, String projectKey){
        //TODO hacer una función que sólo devuela los issues abiertos.
        return null;
    }





    private Issue doIssueQuery(ApplicationUser user, JqlClauseBuilder jqlClauseBuilder) {
        Query query = jqlClauseBuilder.buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        SearchResults searchResults = null;
        try {
            searchResults = searchService.search(user, query, pagerFilter);
        } catch (SearchException e) {
            e.printStackTrace();
        }
        // It must be 0 or 1
        List<Issue> list = searchResults.getIssues();

        return list.size() == 1 ? list.get(0) : null;
    }




}
