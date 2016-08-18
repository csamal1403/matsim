package org.matsim.contrib.carsharing.events.handlers;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.carsharing.events.EndRentalEvent;
import org.matsim.contrib.carsharing.manager.CSPersonVehicle;
import org.matsim.contrib.carsharing.manager.CarsharingManager;
import org.matsim.contrib.carsharing.qsim.CarSharingVehiclesNew;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.vehicles.Vehicle;

import com.google.inject.Inject;
/** 
 * 
 * @author balac
 */
public class PersonArrivalDepartureHandler implements PersonDepartureEventHandler, PersonLeavesVehicleEventHandler, 
	PersonArrivalEventHandler, PersonEntersVehicleEventHandler {
	public static final String STAGE_FF = "ff_interaction";
	public static final String STAGE_OW = "ow_interaction";

	@Inject	private CarsharingManager carsharingManager;
	@Inject private CSPersonVehicle csPersonVehicles;
	@Inject private CarSharingVehiclesNew carsharingStationsData;
	@Inject EventsManager eventsManager;
	Map<Id<Person>, String> personVehicleArrival = new HashMap<Id<Person>, String>();
	Map<Id<Person>, Id<Link>> personArrivalMap = new HashMap<Id<Person>, Id<Link>>();
	Map<Id<Person>, Id<Vehicle>> personLeavesVehicleMap = new HashMap<Id<Person>, Id<Vehicle>>();

	@Override
	public void reset(int iteration) {
		personLeavesVehicleMap = new HashMap<Id<Person>, Id<Vehicle>>();
		personArrivalMap = new HashMap<Id<Person>, Id<Link>>();
		personVehicleArrival = new HashMap<Id<Person>, String>();
	}
	
	@Override
	public void handleEvent(PersonDepartureEvent event) {
		String legMode = event.getLegMode();
		if (legMode.equals("egress_walk_ff")) {
			String vehId = personLeavesVehicleMap.get(event.getPersonId()).toString();
			Id<Link> linkId = personArrivalMap.get(event.getPersonId());
			carsharingManager.parkVehicle(vehId, linkId);
			//carsharingManager.returnCarsharingVehicle(event.getPersonId(), event.getLinkId(), event.getTime(), vehId);
			this.csPersonVehicles.getVehicleLocationForType(event.getPersonId(), "freefloating").remove(linkId);
			eventsManager.processEvent(new EndRentalEvent(event.getTime(), linkId, event.getPersonId(), vehId));

		}
		else if (legMode.equals("egress_walk_ow")) {
			String vehId = personLeavesVehicleMap.get(event.getPersonId()).toString();
			Id<Link> linkId = personArrivalMap.get(event.getPersonId());
			carsharingManager.parkVehicle(vehId, linkId);
			//carsharingManager.returnCarsharingVehicle(event.getPersonId(), event.getLinkId(), event.getTime(), vehId);
			this.csPersonVehicles.getVehicleLocationForType(event.getPersonId(), "oneway").remove(linkId);
			eventsManager.processEvent(new EndRentalEvent(event.getTime(), linkId, event.getPersonId(), vehId));

		}
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		personLeavesVehicleMap.put(event.getPersonId(), event.getVehicleId());
	
	}	

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		String mode = event.getLegMode();
		
		if (mode.startsWith("free") || 
				mode.startsWith("one")) {			
			
			String vehId = personLeavesVehicleMap.get(event.getPersonId()).toString();
			
			CSVehicle vehicle = this.carsharingStationsData.getCsvehicleIdMap().get(vehId);
			
			Id<Link> linkId = event.getLinkId();
			if (this.csPersonVehicles.getVehicleLocationForType(event.getPersonId(), mode) != null)
				this.csPersonVehicles.getVehicleLocationForType(event.getPersonId(), mode).put(linkId, vehicle);
			else {				
				this.csPersonVehicles.addNewPersonInfo(event.getPersonId());
				this.csPersonVehicles.getVehicleLocationForType(event.getPersonId(), mode).put(linkId, vehicle);

			}
			
		}
		
		personArrivalMap.put(event.getPersonId(), event.getLinkId());
		
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {

		String vehId = event.getVehicleId().toString();
		if (vehId.startsWith("OW")) {
			Id<Link> linkId = personArrivalMap.get(event.getPersonId());

			this.carsharingManager.freeParkingSpot(linkId);
		
		}
	}
	
}
