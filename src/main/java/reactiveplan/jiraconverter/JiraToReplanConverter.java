package reactiveplan.jiraconverter;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import reactiveplan.entities.Employee;
import reactiveplan.entities.Feature;
import reactiveplan.entities.PriorityLevel;
import reactiveplan.entities.Skill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class JiraToReplanConverter {

    public static Feature issueToFeature(Issue issue, Collection<Issue> issueDependencies){

        String name = issue.getKey();
        Double duration = 0.0;
        if(issue.getEstimate() != null) {
            duration = Double.parseDouble(issue.getEstimate().toString());
        }

        String priority;
        if(issue.getPriority() != null) {
            priority = issue.getPriority().getName();
        } else{
            priority = "undefined";
        }
        PriorityLevel priorityLevel = priorityToPriorityLevel(issue.getPriority());
        List<Feature> previousFeatures = new ArrayList<>();
        List<Skill> requiredSkills = getSkillsFromIssue(issue);

        if(issueDependencies != null && !issueDependencies.isEmpty()){
            for(Issue i : issueDependencies){
                Feature f = issueToFeature(i,null);
                previousFeatures.add(f);
            }
        }

        return new Feature(name, priorityLevel, duration, previousFeatures, requiredSkills);
    }
    /* Utiliza las prioridades por defecto de JIRA */
    public static PriorityLevel priorityToPriorityLevel(Priority priority){

        PriorityLevel priorityLevel = null;

        if(priority != null){
            String priorityName = priority.getName();

            switch(priorityName.toLowerCase()){
                case "highest":
                    priorityLevel =  PriorityLevel.getPriorityByLevel(1);
                    break;
                case "high":
                    priorityLevel =  PriorityLevel.getPriorityByLevel(2);
                    break;

                case "medium":
                    priorityLevel =  PriorityLevel.getPriorityByLevel(3);
                    break;
                case"low":
                    priorityLevel =  PriorityLevel.getPriorityByLevel(4);
                    break;
                case "lowest":
                    priorityLevel =  PriorityLevel.getPriorityByLevel(5);
                    break;
                default:
                    priorityLevel =  PriorityLevel.getPriorityByLevel(3);
                    break;
            }

        } else {
            PriorityLevel.getPriorityByLevel(5);
        }

        return priorityLevel;
    }

    public static Skill projectRoleToSkill(ProjectRole prole){

        return new Skill(prole.getName());
    }

    public static Employee applicationUserToEmployee(ApplicationUser appUser,Collection<ProjectRole> userRoles){
        List<Skill> skills = new ArrayList<>();
        for(ProjectRole role : userRoles){
          skills.add(projectRoleToSkill(role));
        }
        return new Employee(appUser.getName(),skills);
    }

    public static Employee setDefaultCalendarToEmployee(Employee employee,int dailyHours, int numWeeks){

        return null;//TODO
    }


    private static List<Skill> getSkillsFromIssue(Issue issue){
        /* Se deben poner las skills en la descripción en el siguiente formato:
        *
        *   _skills_ skill1 skill2 skill3 endskills
        *
        * */

        StringTokenizer st = new StringTokenizer(issue.getDescription());
        List<Skill> skillslist = new ArrayList<>();
        String token = st.nextToken();
        boolean skills = false;
        while(st.hasMoreTokens() && !token.equals("endskills")){
            if(skills){
               skillslist.add(new Skill(token));
           }
            if(token.equalsIgnoreCase("_skills_")){
                skills = true;
            }
            token = st.nextToken();
        }

        return skillslist;
    }


}
