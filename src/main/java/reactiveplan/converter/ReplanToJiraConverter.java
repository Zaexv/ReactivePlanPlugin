package reactiveplan.converter;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.reactiveplan.logic.IssueLogic;
import reactiveplan.entities.DaySlot;
import reactiveplan.entities.Employee;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ReplanToJiraConverter {


            public static String getIssueBeginDateFromCalendar(List<DaySlot> calendar, String issueKey, Date versionStartDate ) {
                //TODO test
                Calendar c = Calendar.getInstance();
                c.setTime(versionStartDate);

               List<DaySlot> filteredCalendar = calendar.stream().filter(dia -> dia.getFeature() != null)
                       .filter(day -> day.getFeature().equalsIgnoreCase(issueKey)).collect(Collectors.toList());
               DaySlot firstDay = filteredCalendar.get(0);
                c.add(Calendar.WEEK_OF_YEAR, firstDay.getWeek());
                c.add(Calendar.DATE, firstDay.getDayOfWeek());

                SimpleDateFormat sdf = new SimpleDateFormat("d/MMM/yy");


             return sdf.format(new Date(c.getTimeInMillis())).toLowerCase();
            }
            
            public static String getIssueEndDateFromCalendar(List<DaySlot> calendar, String issueKey, Date versionStartDate ) {
                //TODO test
                Calendar c = Calendar.getInstance();
                c.setTime(versionStartDate);

                List<DaySlot> filteredCalendar = calendar.stream().filter(dia ->  dia.getFeature() != null).filter(day -> day.getFeature().equalsIgnoreCase(issueKey)).collect(Collectors.toList());

                if(!filteredCalendar.isEmpty()) {
                    DaySlot lastDay = filteredCalendar.get(filteredCalendar.size() - 1);
                    c.add(Calendar.WEEK_OF_YEAR, lastDay.getWeek());
                    c.add(Calendar.DATE, lastDay.getDayOfWeek());
                }

                //Hay que tener especial cuidado con el formato porque puede petar.
                SimpleDateFormat sdf = new SimpleDateFormat("d/MMM/yy");
                return sdf.format(new Date(c.getTimeInMillis())).toLowerCase();
            }


            public static void persistAllFeaturesToJira(ApplicationUser user, Set<Employee> plannedEmployees, IssueLogic logic,
                                                        Date versionStartDate){
                //TODO esto probablemente haya una forma más elegante de hacerlo.
                Set<String> issuesAlreadyPersisted = new HashSet<>();
                String issuebegindate;
                String issueenddate;

                for(Employee e : plannedEmployees){
                    for(DaySlot ds : e.getCalendar()){
                        if(ds.getFeature() != null){
                            if(!issuesAlreadyPersisted.contains(ds.getFeature())){
                                issuesAlreadyPersisted.add(ds.getFeature());
                                issuebegindate = getIssueBeginDateFromCalendar
                                        (e.getCalendar(),ds.getFeature(),versionStartDate);
                                issueenddate = getIssueEndDateFromCalendar
                                        (e.getCalendar(), ds.getFeature(),versionStartDate);
                                logic.handleIssueEdit(user,ds.getFeature(),issueenddate,issuebegindate,e.getName());
                            }
                        }
                    }
                }
            }

            public static String daySlotToDate(Date startDate, DaySlot daySlot  ){
                Calendar c = Calendar.getInstance();
                c.setTime(startDate);
                c.add(Calendar.WEEK_OF_YEAR, daySlot.getWeek());
                c.add(Calendar.DATE, daySlot.getDayOfWeek());

                SimpleDateFormat sdf = new SimpleDateFormat("d/MMM/yy");

                return sdf.format(new Date(c.getTimeInMillis())).toLowerCase();
            }
}
