/* *********************************************************************** *
 * project: org.matsim.*
 * TestControler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package playground.ikaddoura.parkAndRide.pR;


import java.io.IOException;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.MobsimFactory;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimFactory;
import org.matsim.vis.otfvis.OTFFileWriterFactory;

import playground.ikaddoura.parkAndRide.pRscoring.ParkAndRideScoringFunctionFactory;

/**
 * @author Ihab
 *
 */
public class ParkAndRideMain {
	
	static String configFile;
	static String prFacilityFile;
	
	static int prCapacity;
	static double transferPenalty;
	
	static double addRemoveProb;
	static int addRemoveDisable;
	
	static double changeLocationProb;
	static int changeLocationDisable;
	
	static double timeAllocationProb;
	static int timeAllocationDisable;
	
	public static void main(String[] args) throws IOException {
		
//		configFile = "../../shared-svn/studies/ihab/parkAndRide/inputBerlin/berlinConfig.xml";
//		prFacilityFile = "../../shared-svn/studies/ihab/parkAndRide/inputBerlin/PRfacilities_berlin.txt";
//		prCapacity = 100;
//		transferPenalty = 0.;
//		
//		addRemoveProb = 0.05;
//		addRemoveDisable = 500;
//		
//		changeLocationProb = 0.05;
//		changeLocationDisable = 500;
//		
//		timeAllocationProb = 0.1;
//		timeAllocationDisable = 500;
		
		
//		**************************************************
		
		configFile = args[0];
		prFacilityFile = args[1];
		prCapacity = Integer.parseInt(args[2]);
		transferPenalty = Double.parseDouble(args[3]);
		
		addRemoveProb = Double.parseDouble(args[4]);
		addRemoveDisable = Integer.parseInt(args[5]);
		
		changeLocationProb = Double.parseDouble(args[6]);
		changeLocationDisable = Integer.parseInt(args[7]);
		
		timeAllocationProb = Double.parseDouble(args[8]);
		timeAllocationDisable = Integer.parseInt(args[9]);
	
		ParkAndRideMain main = new ParkAndRideMain();
		main.run();
	}
	
	private void run() {
		
		Controler controler = new Controler(configFile);
		controler.setOverwriteFiles(true);
		controler.addSnapshotWriterFactory("otfvis", new OTFFileWriterFactory());
		
		PRFileReader prReader = new PRFileReader(prFacilityFile);
		Map<Id, ParkAndRideFacility> id2prFacility = prReader.getId2prFacility();

		final AdaptiveCapacityControl adaptiveControl = new AdaptiveCapacityControl(id2prFacility, prCapacity);
		
		controler.addControlerListener(new ParkAndRideControlerListener(controler, adaptiveControl, id2prFacility, addRemoveProb, addRemoveDisable, changeLocationProb, changeLocationDisable, timeAllocationProb, timeAllocationDisable));
		
		PlanCalcScoreConfigGroup planCalcScoreConfigGroup = controler.getConfig().planCalcScore();	
		ParkAndRideScoringFunctionFactory scoringfactory = new ParkAndRideScoringFunctionFactory(planCalcScoreConfigGroup, controler.getNetwork(), transferPenalty);
		controler.setScoringFunctionFactory(scoringfactory);
		
		final MobsimFactory mf = new QSimFactory();
		
		controler.setMobsimFactory(new MobsimFactory() {
			private QSim mobsim;

			@Override
			public Mobsim createMobsim(Scenario sc, EventsManager eventsManager) {
				mobsim = (QSim) mf.createMobsim(sc, eventsManager);
				mobsim.addMobsimEngine(adaptiveControl);
				return mobsim;
			}
		});
			
		controler.run();
	}
}
	
