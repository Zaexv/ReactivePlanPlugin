package reactiveplan.jiraconverter;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import reactiveplan.entities.*;

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
                    priorityLevel =  PriorityLevel.fromValues(1,160);
                    break;
                case "high":
                    priorityLevel =  PriorityLevel.fromValues(2,80);
                    break;
                case "medium":
                    priorityLevel =  PriorityLevel.fromValues(3,40);
                    break;
                case"low":
                    priorityLevel =  PriorityLevel.fromValues(4,20);
                    break;
                case "lowest":
                    priorityLevel =  PriorityLevel.fromValues(5,10);
                    break;
                default:
                    priorityLevel =  PriorityLevel.fromValues(3,40);
                    break;
            }

        } else {
           priorityLevel = PriorityLevel.fromValues(5,10);
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

    public static List<DaySlot> getDefaultCalendar(int dailyHours, int numDays, int numWeeks){
        /*
        Este calendario asume un número concreto de días y semanas a trabajar y que el empleado está libre para todos los issues.
         */


        List<DaySlot> calendar = new ArrayList<>();
        double beginhour = 8.0;
        double endhour = beginhour + dailyHours;
        int id = 0;
        for(int weekNumber = 0; weekNumber < numWeeks; weekNumber++){
            for(int dayNumber = 0; dayNumber < numDays; dayNumber++){
                calendar.add(new DaySlot(id, weekNumber, dayNumber, beginhour,endhour,SlotStatus.Free));
                id++;
            }
        }
        return calendar;
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
