package reactiveplan.entities;

import java.util.*;

public class Schedule {

    HashMap<Employee, List<SlotList>> employeesCalendar;
    HashMap<Employee, Integer> currentSlotIds;
    DaySlot replan;

    List<String> plannedFeatures;

    public Schedule(HashMap<Employee, List<DaySlot>> agenda) {
        employeesCalendar = new HashMap<>();
        currentSlotIds = new HashMap<>();
        plannedFeatures = new ArrayList<>();
        for (Employee e : agenda.keySet()) {
            int currentSlotId = createSlotAgenda(e, agenda.get(e));
            currentSlotIds.put(e, currentSlotId);
        }
    }

    public void setReplan(DaySlot replan) { this.replan = replan; }

    /*
    Copy constructor
     */
    public Schedule(Schedule origin) {
        employeesCalendar = new HashMap<>();
        currentSlotIds = new HashMap<>();
        for (Employee e : origin.getEmployeesCalendar().keySet()) {
            List<SlotList> slotLists = new ArrayList<>();
            for (SlotList slotList : origin.getEmployeesCalendar().get(e)) {
                LinkedHashMap<Integer, DaySlot> daySlots = new LinkedHashMap<>();
                for (Integer id : slotList.getDaySlots().keySet()) {
                    daySlots.put(id, new DaySlot(slotList.getDaySlot(id)));
                }
                slotLists.add(new SlotList(daySlots));
            }
            employeesCalendar.put(e, slotLists);
            currentSlotIds.put(e, origin.getCurrentSlotIds().get(e));
        }
        plannedFeatures = origin.plannedFeatures;
    }

    public List<String> getPlannedFeatures() {
        return plannedFeatures;
    }

    public void setPlannedFeatures(List<String> plannedFeatures) {
        this.plannedFeatures = plannedFeatures;
    }

    public HashMap<Employee, List<SlotList>> getEmployeesCalendar() { return employeesCalendar; }
    public void setEmployeesCalendar(HashMap<Employee, List<SlotList>> employeesCalendar) { this.employeesCalendar = employeesCalendar; }

    public HashMap<Employee, Integer> getCurrentSlotIds() { return currentSlotIds; }
    public void setCurrentSlotIds(HashMap<Employee, Integer> currentSlotIds) { this.currentSlotIds = currentSlotIds; }

    public double getRatio(Employee e) {
        double totalDuration = 0.0;
        double workHours = 0.0;
        for (SlotList slotList : employeesCalendar.get(e)) {
            Double duration = slotList.getTotalDuration();
            totalDuration += duration;
            if (slotList.getSlotStatus().equals(SlotStatus.Used))
                workHours += duration;
        }
        return workHours / totalDuration;
    }

    private int createSlotAgenda(Employee e, List<DaySlot> agenda) {
        Collections.sort(agenda);
        int currentSlotId = 0;
        List<SlotList> slotAgenda = new ArrayList<>();
        int i = 0;
        //While there are agenda slots to initialize
        while (i < agenda.size()) {
            int beginSlotId = agenda.get(i).getId();
            if (currentSlotId < beginSlotId) currentSlotId = beginSlotId;
            //Initialize the new SlotList instance with the first slot
            LinkedHashMap<Integer, DaySlot> daySlotHashMap = new LinkedHashMap<>();
            daySlotHashMap.put(agenda.get(i).getId(), agenda.get(i));
            ++i;
            //While the following slots are of the same status, keep adding them to SlotList instance
            while (i < agenda.size() &&
                    ( agenda.get(i).getStatus().equals(SlotStatus.Free) && agenda.get(i-1).getStatus().equals(SlotStatus.Free) ||
                            agenda.get(i).getStatus().equals(SlotStatus.Used) && agenda.get(i-1).getStatus().equals(SlotStatus.Used)
                            && agenda.get(i).getFeature().equals(agenda.get(i-1).getFeature())))  {
                if (currentSlotId < agenda.get(i).getId()) currentSlotId = agenda.get(i).getId();
                daySlotHashMap.put(agenda.get(i).getId(), agenda.get(i));
                ++i;
            }
            slotAgenda.add(new SlotList(daySlotHashMap));
        }
        employeesCalendar.put(e, slotAgenda);
        return currentSlotId;
    }

    public boolean scheduleFeature(PlannedFeature pf, List<PlannedFeature> previousFeatures) {
        if (plannedFeatures.contains(pf.getFeature().getName())) {
            return true;
        }
        else {
            return scheduleFeature(pf, getLatestPlannedFeature(previousFeatures));
        }
    }

    private DaySlot getLatestPlannedFeature(List<PlannedFeature> previousFeatures) {
        if (previousFeatures == null || previousFeatures.isEmpty())
            return null;
        DaySlot last = getPlannedFeatureEndSlot(previousFeatures.get(0));
        for (int i = 1; i < previousFeatures.size(); ++i) {
            DaySlot current = getPlannedFeatureEndSlot(previousFeatures.get(i));
            if (last.compareTo(current) < 0) {
                last = current;
            }
        }
        return last;
    }

    private boolean scheduleFeature(PlannedFeature pf, DaySlot lastPreviousFeatureEndSlot) {

        SlotList slotAgenda = getFirstAvailableSlot(pf, lastPreviousFeatureEndSlot);

        if (slotAgenda == null) {
            return false;
        }

        if (lastPreviousFeatureEndSlot == null) {
            DaySlot beginSlot = slotAgenda.getDaySlot(slotAgenda.getBeginSlotId());
            lastPreviousFeatureEndSlot = new DaySlot(-1, beginSlot.getWeek(), beginSlot.getDayOfWeek() - 1, beginSlot.getBeginHour(), beginSlot.getBeginHour(), SlotStatus.Free);
        } if (replan.compareTo(lastPreviousFeatureEndSlot) > 0) lastPreviousFeatureEndSlot = replan;
        updateAgenda(pf, slotAgenda, lastPreviousFeatureEndSlot);
        return true;
    }

    public void forcedSchedule(PlannedFeature pf, List<DaySlot> daySlots) {
        Collections.sort(daySlots);
        List<Integer> daySlotsIds = new ArrayList<>();
        for (DaySlot daySlot : daySlots) daySlotsIds.add(daySlot.getId());
        pf.setSlotIds(daySlotsIds);
        pf.setBeginHour(daySlots.get(0).getBeginHourAbsolute());
        pf.setEndHour(daySlots.get(daySlots.size()-1).getEndHourAbsolute());
    }

    private void updateAgenda(PlannedFeature pf, SlotList slotAgenda, DaySlot lastPreviousFeatureEndSlot) {
        LinkedHashMap<Integer, DaySlot> previousAgenda = new LinkedHashMap<>();
        LinkedHashMap<Integer, DaySlot> thisAgenda = new LinkedHashMap<>();
        LinkedHashMap<Integer, DaySlot> laterAgenda = new LinkedHashMap<>();
        double remainingHours = pf.getFeature().getDuration();
        int currentSlotId = currentSlotIds.get(pf.getEmployee());
        for (DaySlot daySlot: slotAgenda.getDaySlots().values()) {
            int cmp = daySlot.compareByDay(lastPreviousFeatureEndSlot);
            if (cmp < 0) {
                //If DaySlot is previous to end of previous feature
                //daySlot.setStatus(SlotStatus.Free);
                previousAgenda.put(daySlot.getId(), daySlot);
            }
            else if (cmp == 0) {
                //If last previous feature is ended today
                if (lastPreviousFeatureEndSlot.getEndHour() - daySlot.getBeginHour() > 0) {
                    DaySlot prevDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                            daySlot.getBeginHour(), lastPreviousFeatureEndSlot.getEndHour(), SlotStatus.Free);
                    previousAgenda.put(currentSlotId, prevDaySlot);
                }

                if (daySlot.getEndHour() - lastPreviousFeatureEndSlot.getEndHour() > 0) {
                    double begin = Math.max(lastPreviousFeatureEndSlot.getEndHour(), daySlot.getBeginHour());
                    double hour = Math.min(begin + remainingHours,
                            daySlot.getEndHour());
                    DaySlot thisDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                            begin, hour, SlotStatus.Used);
                    thisDaySlot.setFeature(pf.getFeature());
                    //If after ending previous feature there are remaining work hours
                    thisAgenda.put(currentSlotId, thisDaySlot);
                    remainingHours -= (hour - begin);
                    if (hour < daySlot.getEndHour()) {
                        DaySlot afterDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                                hour, daySlot.getEndHour(), SlotStatus.Free);
                        //If feature is also finished the same day
                        laterAgenda.put(currentSlotId, afterDaySlot);
                    }

                }
            }
            else {
                if (remainingHours > 0) {
                    double hour = Math.min(remainingHours, daySlot.getDuration());
                    remainingHours -= hour;
                    if (daySlot.getBeginHour() + hour < daySlot.getEndHour()) {
                        DaySlot thisDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                                daySlot.getBeginHour(), daySlot.getBeginHour() + hour, SlotStatus.Used);
                        thisDaySlot.setFeature(pf.getFeature());
                        thisAgenda.put(currentSlotId, thisDaySlot);

                        DaySlot afterDaySlot = new DaySlot(++currentSlotId, daySlot.getWeek(), daySlot.getDayOfWeek(),
                                daySlot.getBeginHour() + hour, daySlot.getEndHour(), SlotStatus.Free);
                        //If feature is also finished the same day
                        laterAgenda.put(currentSlotId, afterDaySlot);
                    } else {
                        daySlot.setStatus(SlotStatus.Used);
                        daySlot.setFeature(pf.getFeature());
                        thisAgenda.put(daySlot.getId(), daySlot);
                    }
                } else {
                    laterAgenda.put(daySlot.getId(), daySlot);
                }
            }
        }
        //Delete old slotList
        List<SlotList> agenda = employeesCalendar.get(pf.getEmployee());
        int k = agenda.indexOf(slotAgenda);
        agenda.remove(k);
        //Add new 3 slotLists in order: after, this, before
        if (laterAgenda.size() > 0) agenda.add(k, new SlotList(laterAgenda));
        if (thisAgenda.size() > 0) agenda.add(k, new SlotList(thisAgenda));
        if (previousAgenda.size() > 0)  agenda.add(k, new SlotList(previousAgenda));
        currentSlotIds.put(pf.getEmployee(), currentSlotId);
        //pf.setSlotIds((List<Integer>) thisAgenda.keySet());
        List<Integer> daySlotIds = new ArrayList<>();
        for (Integer i : thisAgenda.keySet()) {
            daySlotIds.add(i);
        }
        pf.setSlotIds(daySlotIds);
        if (daySlotIds.size() == 0) {
            System.out.println("Trying to schedule " + pf.getFeature().getName() + " of duration " + pf.getFeature().getDuration()
            + " after " + lastPreviousFeatureEndSlot.toString() + " at " + slotAgenda.getDaySlot(slotAgenda.getBeginSlotId()));
            System.out.println(toString());
        }
        pf.setBeginHour(thisAgenda.get(daySlotIds.get(0)).getBeginHourAbsolute());
        pf.setEndHour(thisAgenda.get(daySlotIds.get(thisAgenda.size()-1)).getEndHourAbsolute());

    }

    private SlotList getFirstAvailableSlot(PlannedFeature pf, DaySlot lastPreviousFeatureEndSlot) {
        List<SlotList> employeeAgenda = employeesCalendar.get(pf.getEmployee());
        int i = 0;
        while (i < employeeAgenda.size()) {
            if (employeeAgenda.get(i).isFeatureFit(pf.getFeature(), lastPreviousFeatureEndSlot, replan)) {
                return employeeAgenda.get(i);
            }
            else ++i;
        }
        return null;
    }

    private DaySlot getPlannedFeatureEndSlot(PlannedFeature pf) {
        for (SlotList slotList : employeesCalendar.get(pf.getEmployee())) {
            DaySlot daySlot = slotList.getDaySlot(pf.getEndSlotId());
            if (daySlot != null) return daySlot;
        }
        return null;
    }

    public DaySlot getEndDaySlot(SlotStatus status) {
        DaySlot currentDaySlot = null;
        for (Employee e : employeesCalendar.keySet()) {
            for (SlotList slotList : employeesCalendar.get(e)) {
                for (DaySlot nextDaySlot : slotList.getDaySlots().values()) {
                    if (currentDaySlot == null) {
                        if (status == null || status != null && status.equals(nextDaySlot.getStatus()))
                            currentDaySlot = nextDaySlot;
                    } else if (currentDaySlot.compareTo(nextDaySlot) < 0) {
                        if (status == null || status != null && status.equals(nextDaySlot.getStatus()))
                            currentDaySlot = nextDaySlot;
                    }
                }
            }
        }
        return currentDaySlot;
    }

    @Override
    public String toString() {
        String s = "";
        for (Employee e : employeesCalendar.keySet()) {
            s += "Employee " + e.getName() + " schedule:\n";
            for (SlotList slotList : employeesCalendar.get(e)) {
                s += "\tSlot [" + slotList.getSlotStatus() + "]\n";
                for (DaySlot daySlot : slotList.getDaySlots().values()) {
                    s += "\t\t[" + daySlot.getId() + "][" + daySlot.getFeature() + "] Week " + daySlot.getWeek() +  ", day " + daySlot.getDayOfWeek() + ", begins at " +
                            daySlot.getBeginHour() + " and ends at " + daySlot.getEndHour() + "\n";
                }
            }
        }
        return s;
    }

    public PlannedFeature findJobOf(Feature f) {
        for (Employee e : employeesCalendar.keySet()) {
            List<SlotList> slotLists = employeesCalendar.get(e);
            for (SlotList slotList : slotLists) {
                if (slotList.getSlotStatus().equals(SlotStatus.Used)) {
                    DaySlot daySlot = slotList.getDaySlot(slotList.getBeginSlotId());
                    if (daySlot.getFeature()!= null && daySlot.getFeature().equals(f.getName())) {
                        PlannedFeature pf = new PlannedFeature(f,e);
                        List<Integer> ids = new ArrayList<>();
                        ids.addAll(slotList.getDaySlots().keySet());
                        pf.setSlotIds(ids);
                        pf.setBeginHour(slotList.getDaySlot(slotList.getBeginSlotId()).getBeginHourAbsolute());
                        pf.setEndHour(slotList.getDaySlot(slotList.getEndSlotId()).getEndHourAbsolute());
                        return pf;
                    }
                }
            }
        }
        return null;
    }
}
