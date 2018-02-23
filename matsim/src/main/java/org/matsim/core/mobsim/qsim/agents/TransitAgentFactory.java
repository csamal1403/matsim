/* *********************************************************************** *
 * project: org.matsim.*
 * TransitAgentFactory.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package org.matsim.core.mobsim.qsim.agents;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.pt.MobsimDriverPassengerAgent;

import javax.inject.Inject;


public class TransitAgentFactory implements AgentFactory {

	final private Scenario scenario;
	final private EventsManager eventsManager;
	final private MobsimTimer mobsimTimer;

	@Inject
	public TransitAgentFactory(Scenario scenario, EventsManager eventsManager, MobsimTimer mobsimTimer) {
		this.scenario = scenario;
		this.eventsManager = eventsManager;
		this.mobsimTimer = mobsimTimer;
	}

	@Override
	public MobsimDriverPassengerAgent createMobsimAgentFromPerson(final Person p) {
		MobsimDriverPassengerAgent agent = TransitAgent.createTransitAgent(p, scenario, eventsManager, mobsimTimer);
		return agent;
	}

}
