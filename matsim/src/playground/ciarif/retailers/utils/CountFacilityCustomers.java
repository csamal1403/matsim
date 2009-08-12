package playground.ciarif.retailers.utils;


import org.matsim.api.basic.v01.population.PlanElement;
import org.matsim.core.controler.Controler;
import org.matsim.core.facilities.ActivityFacility;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;

public class CountFacilityCustomers {
	
	private Controler controler;
	private ActivityFacility facility;

	public CountFacilityCustomers(ActivityFacility facility,Controler controler) {
		this.controler=controler;
		this.facility=facility;
	}

	public int countCustomers() {
		int customersCount = 0;
		 for (PersonImpl p: controler.getPopulation().getPersons().values()) {
			PlanImpl plan = p.getSelectedPlan();
			for (PlanElement pe : plan.getPlanElements()) {
				if (pe instanceof ActivityImpl) {
					ActivityImpl act = (ActivityImpl) pe;
					if (facility.equals(act.getFacilityId())) {
						customersCount=customersCount+1;
					}
				}
			}
		}
		return customersCount;
	}
}
	

