/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.population.PopulationUtils;

import javax.inject.Inject;

/**
 * Design decisions:<ul>
 * <li> Class is final since there is an interface.  Please use interface directly. kai, nov'11
 * </ul>
 */
public final class DefaultAgentFactory implements AgentFactory {

	private final Scenario scenario;
	private final EventsManager eventsManager;
	private final MobsimTimer mobsimTimer;

	@Inject
	public DefaultAgentFactory(Scenario scenario, EventsManager eventsManager, MobsimTimer mobsimTimer) {
		this.scenario = scenario;
		this.eventsManager = eventsManager;
		this.mobsimTimer = mobsimTimer;
	}

	@Override
	public MobsimDriverAgent createMobsimAgentFromPerson(final Person p) {

		PersonDriverAgentImpl agent = new PersonDriverAgentImpl(p.getSelectedPlan(),scenario, eventsManager, mobsimTimer); 
		// ( BasicPlanAgentImpl (inside PersonDriverAgentImpl) makes the plan unmodifiable. )
		
		return agent;
	}

}
