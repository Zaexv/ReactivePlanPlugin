package reactiveplan.jiraconverter;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.reactiveplan.exception.ReplanException;
import com.atlassian.reactiveplan.logic.IssueLogic;
import com.atlassian.reactiveplan.logic.ProjectLogic;
import reactiveplan.entities.*;

import java.util.*;

public class JiraToReplanConverter {

    public static Feature issueToFeature(Issue issue, Collection<Issue> issueDependencies){

        String name = issue.getKey();
        Double duration = 0.50; //Se asumirá una duración mínima de media hora para que se puedan establecer los planes.
        if(issue.getOriginalEstimate() != null) {
            duration = (double) issue.getOriginalEstimate()/(3600);
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

    public static Collection<Feature> issuesToFeatures(Collection<Issue> issues, IssueLogic issueLogic){
        Collection<Feature> features = new ArrayList<>();


        for(Issue issue : issues){
            features.add(issueToFeature(issue, issueLogic.getIssueDependencies(issue)));
        }
        return features;
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


    public static Set<Employee> applicationUsersToEmployees(Set<ApplicationUser> users, ProjectLogic prlogic, Project pr ){

        Set<Employee> employeeset = new HashSet<>();

        for(ApplicationUser appuser : users){
        Employee e = JiraToReplanConverter.applicationUserToEmployee(appuser, prlogic.getRolesOfUserInProject(appuser,pr));

        //Les ponemos el calendario por defecto.
        e.setCalendar(JiraToReplanConverter.getDefaultCalendar(8.0,5,4));
        employeeset.add(e);
        }
        return employeeset;
    }

    public static Set<Employee> applicationUsersToEmployees(Set<ApplicationUser> users, ProjectLogic prlogic, Project pr,
                                                            Version version ) throws ReplanException {

        if(version == null)
            throw new ReplanException("Version not found!");


        Set<Employee> employeeset = new HashSet<>();

        for(ApplicationUser appuser : users){
            Employee e = JiraToReplanConverter.applicationUserToEmployee(appuser, prlogic.getRolesOfUserInProject(appuser,pr));
            e.setCalendar(JiraToReplanConverter.getCalendarFromVersion(8, version, 5));
            employeeset.add(e);
        }
        return employeeset;
    }


    public static List<DaySlot> getDefaultCalendar(double dailyHours, int numDays, int numWeeks){
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

    public static List<DaySlot> getCalendarFromVersion(double dailyHours, Version version, int numDays)
            throws ReplanException {


        if(version == null)
            throw new ReplanException("Version not found!");


       Date startDate =  version.getStartDate();
       if(startDate.compareTo(new Date()) < 0 ){
           startDate = new Date();
       }
       Date releaseDate =  version.getReleaseDate();

       Long timeBetweenDates = releaseDate.getTime() - startDate.getTime();


       if(version.isReleased()) throw new ReplanException("Can't plan released versions!");
       if(timeBetweenDates < 0) throw new ReplanException("Version is Obsolete");

       Double totalnumberdays = (double)timeBetweenDates/(24*3600*1000);

        int numWeeks = (int)(totalnumberdays/numDays);

        return getDefaultCalendar(dailyHours, numDays, numWeeks);
    }

    private static List<Skill> getSkillsFromIssue(Issue issue){
        /* Se deben poner las skills en la descripción en el siguiente formato:
        *
        *   _skills_ skill1 skill2 skill3 endskills
        *
        * */
        StringTokenizer st = new StringTokenizer("");

        if(issue.getDescription() != null){
             st = new StringTokenizer(issue.getDescription());
        }
        List<Skill> skillslist = new ArrayList<>();
        String token =" ";
        boolean skills = false;
        while(st.hasMoreTokens() && !token.equals("endskills")){
            token = st.nextToken();
            if(skills){
               skillslist.add(new Skill(token));
           }
            if(token.equalsIgnoreCase("_skills_")){
                skills = true;
            }
        }
        return skillslist;
    }


}
