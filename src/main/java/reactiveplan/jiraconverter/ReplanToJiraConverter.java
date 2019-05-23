package reactiveplan.jiraconverter;

import reactiveplan.entities.DaySlot;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ReplanToJiraConverter {


            public static Timestamp getIssueBeginDateFromCalendar(List<DaySlot> calendar, String issueKey, Date versionStartDate ) {

                Calendar c = Calendar.getInstance();
                c.setTime(versionStartDate);

               List<DaySlot> filteredCalendar = calendar.stream().filter(dia -> dia.getFeature() != null)
                       .filter(day -> day.getFeature().equalsIgnoreCase(issueKey)).collect(Collectors.toList());
               DaySlot firstDay = filteredCalendar.get(0);
                c.add(Calendar.WEEK_OF_YEAR, firstDay.getWeek());
                c.add(Calendar.DATE, firstDay.getDayOfWeek());

             return new Timestamp(c.getTimeInMillis());
            }
            
            public static Timestamp getIssueEndDateFromCalendar(List<DaySlot> calendar, String issueKey, Date versionStartDate ) {

                Calendar c = Calendar.getInstance();
                c.setTime(versionStartDate);

                List<DaySlot> filteredCalendar = calendar.stream().filter(dia ->  dia.getFeature() != null).filter(day -> day.getFeature().equalsIgnoreCase(issueKey)).collect(Collectors.toList());

                if(!filteredCalendar.isEmpty()) {
                    DaySlot lastDay = filteredCalendar.get(filteredCalendar.size() - 1);
                    c.add(Calendar.WEEK_OF_YEAR, lastDay.getWeek());
                    c.add(Calendar.DATE, lastDay.getDayOfWeek());
                }
                return new Timestamp(c.getTimeInMillis());
            }

}
