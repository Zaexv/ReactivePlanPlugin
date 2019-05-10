package reactiveplan.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a feature in a planning
 * Contains 
 * - the feature to do
 * - the employee in charge of the feature
 * - the begin hour in the planning
 * - the end hour in the planning
 * @author Vavou
 *
 */
public class PlannedFeature {
	
	/* --- Attributes --- */
	/**
	 * A frozen feature will NOT be replanned.
	 */
	private boolean frozen;

	/**
	 * The employee who will do the feature
	 */
	private Employee employee;
	
	/**
	 * The feature to do
	 */
	private Feature feature;

	/**
	 * The list of slot ids performing the feature
	 */
	private List<Integer> slotIds;

	private double endHour;
	private double beginHour;
	
	/* --- Getters and setters --- */

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public double getBeginHour() {
		return beginHour;
	}

	public void setBeginHour(double beginHour) {
		this.beginHour = beginHour;
	}

	public double getEndHour() {
		return endHour;
	}

	public void setEndHour(double endHour) {
		this.endHour = endHour;
	}

	public int getBeginSlotId() { return slotIds.get(0); }

	public int getEndSlotId() { return slotIds.get(slotIds.size()-1); }

	public List<Integer> getSlotIds() { return this.slotIds; }

	public void setSlotIds(List<Integer> slotIds) {
		this.slotIds = slotIds;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	
	
	/* --- Constructors --- */

    public PlannedFeature() {}

	/**
	 * Construct a planned feature
	 * @param feature the feature to plan
	 * @param employee the employee who realize the feature
	 */
	public PlannedFeature(Feature feature, Employee employee) {
		this.feature = feature;
		this.employee = employee;
		this.slotIds = new ArrayList<>();
		//beginHour = 0.0;
	}
	
	/**
	 * Copy constructor
	 * @param origin the object to copy
	 */
	public PlannedFeature(PlannedFeature origin) {
		this.employee = origin.getEmployee();
		this.feature = origin.getFeature();
		/*this.beginHour = origin.getBeginHour();
		this.endHour = origin.getEndHour();*/
	}
	
	/* --- Methods --- */
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (getClass() != obj.getClass())
			return false;

		PlannedFeature other = (PlannedFeature) obj;

		return other.getFeature().equals(this.getFeature()) &&
				other.getEmployee().equals(this.getEmployee()) &&
				other.getBeginHour() == this.getBeginHour() &&
				other.getEndHour() == this.getEndHour();
				//other.getBeginHour() == this.getBeginHour() &&
				//other.getEndHour() == this.getEndHour();
	}
	
	/*@Override
	public int hashCode() {
		return new Double(getBeginHour()).intValue();
	}*/
	
	@Override
	public String toString() {
		return String.valueOf(getFeature()) + " done by " + getEmployee() +
				" from " + getBeginSlotId() + " to " + getEndSlotId();
	}

}
