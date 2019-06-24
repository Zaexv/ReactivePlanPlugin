package reactiveplan.entities;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Describes an employee who can implement a feature
 * @author Vavou
 *
 */
public class Employee {

	private String name;
	private List<Skill> skills;
	private List<DaySlot> calendar;
	
	/* --- GETTERS / SETTERS --- */

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<Skill> getSkills() {
		return skills;
	}
	public void setSkills(List<Skill> skills) {
		this.skills = skills;
	}
	public List<DaySlot> getCalendar() { return calendar	;}
	public void setCalendar(List<DaySlot> calendar) { this.calendar = calendar; }

	/* --- CONSTRUCTORS --- */

	public Employee() {
        skills = new ArrayList<>();
    }

	public Employee(String name, List<Skill> skills) {
		this.name = name;
		this.skills = skills == null ? new ArrayList<Skill>() : skills;
	}

	public Employee(String name, List<Skill> skills, List<DaySlot> calendar) {
		this.name = name;
		this.skills = skills == null ? new ArrayList<Skill>() : skills;
		this.calendar = calendar;
	}

	/* --- OTHER --- */
	@Override 
	public String toString() {
		List<String> skillNames = new ArrayList<>();
		for (Skill s : getSkills())
			skillNames.add(s.getName());

		return String.format("%s. Skills: [%s].", getName(), String.join(", ", skillNames));
	}

	@Override 
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (getClass() != obj.getClass())
			return false;

		Employee other = (Employee) obj;
		
		return this.getName().equals(other.getName());
	}
	
	@Override
	public int hashCode() {
		return getName().length();
	}

    public List<DaySlot> copyCalendar() {
		List<DaySlot> copy = new ArrayList<>();
		for (DaySlot daySlot : calendar) {
			copy.add(new DaySlot(daySlot));
		}
		return copy;
    }
}
