package playground.ciarif.retailers;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import opendap.util.gui.warning_box;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.population.PlanElement;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.facilities.ActivityFacility;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.core.router.util.AStarLandmarksFactory;
import org.matsim.core.router.util.PreProcessLandmarks;

import playground.ciarif.retailers.IO.FileRetailerReader;
import playground.ciarif.retailers.IO.LinksRetailerReader;
import playground.ciarif.retailers.data.LinkRetailersImpl;
import playground.ciarif.retailers.data.Retailer;
import playground.ciarif.retailers.data.Retailers;
import playground.ciarif.retailers.utils.ReRoutePersons;

public class RetailersLocationListener implements StartupListener, IterationEndsListener, BeforeMobsimListener{
	
	private final static Logger log = Logger.getLogger(RetailersLocationListener.class);
	
	//Base_Config arguments
	public final static String CONFIG_GROUP = "Retailers";
	public final static String CONFIG_RETAILERS = "retailers";
	public final static String CONFIG_STRATEGY_TYPE = "strategyType";
	//public final static String CONFIG_POP_SUM_TABLE = "populationSummaryTable";
	//public final static String CONFIG_RET_SUM_TABLE = "retailersSummaryTable";
	
	//private variables
	
	private final FreespeedTravelTimeCost timeCostCalc = new FreespeedTravelTimeCost();
	private final PreProcessLandmarks preprocess = new PreProcessLandmarks(timeCostCalc);
	private PlansCalcRoute pcrl = null;
	//private boolean sequential = false;
	private boolean parallel = false;
	private Map<Id, ActivityFacility> controlerFacilities;
	private String facilityIdFile = null;
	private Retailers retailers;
	//private ArrayList<ActivityFacility> retailersFacilities = new ArrayList<ActivityFacility>();
	private Controler controler;
	private Map<Id, PersonImpl> persons;
	private LinksRetailerReader lrr;
	
	// public methods
	
	public void notifyStartup(StartupEvent event) {
		
		this.controler = event.getControler();
		this.controlerFacilities = controler.getFacilities().getFacilities();
		preprocess.run(controler.getNetwork());
		pcrl = new PlansCalcRoute(controler.getNetwork(),timeCostCalc, timeCostCalc, new AStarLandmarksFactory(preprocess));

		this.controler = event.getControler();
		this.controlerFacilities = controler.getFacilities().getFacilities();
		this.persons = controler.getPopulation().getPersons();
		
		//The characteristics of retailers are read
		this.facilityIdFile = controler.getConfig().findParam(CONFIG_GROUP,CONFIG_RETAILERS);
		if (this.facilityIdFile == null) {throw new RuntimeException("In config file, param = "+CONFIG_RETAILERS+" in module = "+CONFIG_GROUP+" not defined!");}
		else { 
			this.retailers = new FileRetailerReader (this.controlerFacilities, this.facilityIdFile).readRetailers(this.controler);
		}
		
		this.lrr = new LinksRetailerReader (controler, retailers);
		lrr.init();
		
	}
	
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		if (parallel) {
			
		}
	}
	
	public void notifyIterationEnds(IterationEndsEvent event) {
		
		// The model is run only every "n" iterations
		if (controler.getIteration()%30==0 && controler.getIteration()>0){
			// TODO maybe need to add if sequential statement
			for (Retailer r : this.retailers.getRetailers().values()) {
				r.runStrategy(lrr.getFreeLinks());
				lrr.updateFreeLinks();
				new ReRoutePersons().run(r.getMovedFacilities(), controler.getNetwork(), persons, pcrl);  
			}
		}
	}
}
