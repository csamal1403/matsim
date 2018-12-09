package org.matsim.contrib.socnetsim.jointtrips.qsim;

import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.socnetsim.qsim.NetsimWrappingQVehicleProvider;
import org.matsim.contrib.socnetsim.sharedvehicles.qsim.PopulationAgentSourceWithVehicles;
import org.matsim.core.config.Config;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.PopulationModule;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.agents.DefaultAgentFactory;
import org.matsim.core.mobsim.qsim.agents.TransitAgentFactory;
import org.matsim.core.mobsim.qsim.components.QSimComponentKeysRegistry;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngineModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class JointQSimModule extends AbstractQSimModule {
	static final public String JOINT_MODES_DEPARTURE_HANDLER = "JointModesDepartureHandler";
	static final public String JOINT_PASSENGER_UNBOARDING = "JoinPassengerUnboarding";
	static final public String AGENTS_SOURCE_WITH_VEHICLES = "AgentSourceWithVehicles";
	static final public String REPLACEMENT_QNETSIM_ENGINE = "ReplacementQNetsimEngine";

	@Override
	protected void configureQSim() {
		this.addQSimComponentBinding( JOINT_MODES_DEPARTURE_HANDLER ).to( JointModesDepartureHandler.class ) ;
		this.addQSimComponentBinding( JOINT_PASSENGER_UNBOARDING ).to( PassengerUnboardingAgentFactory.class ) ;
		this.addQSimComponentBinding( JOINT_MODES_DEPARTURE_HANDLER ).to( JointModesDepartureHandler.class ) ;
		this.addQSimComponentBinding( AGENTS_SOURCE_WITH_VEHICLES ).to( PopulationAgentSourceWithVehicles.class ) ;
		this.addQSimComponentBinding( REPLACEMENT_QNETSIM_ENGINE ).to( QNetsimEngine.class ) ;
	}

	@Provides
	@Singleton
	JointModesDepartureHandler provideJoinModesDepartureHandler(QNetsimEngine netsimEngine) {
		return new JointModesDepartureHandler(netsimEngine);
	}

	@Provides
	@Singleton
	PopulationAgentSourceWithVehicles providePopulationAgentSourceWithVehicles(Population population,
			PassengerUnboardingAgentFactory passAgentFactory, QSim qsim) {
		return new PopulationAgentSourceWithVehicles(population, passAgentFactory, qsim);
	}

	@Provides
	@Singleton
	PassengerUnboardingAgentFactory providePassengerUnboardingAgentFactory(Config config, QSim qsim,
			QNetsimEngine netsimEngine) {
		return new PassengerUnboardingAgentFactory(
				config.transit().isUseTransit() ? new TransitAgentFactory(qsim) : new DefaultAgentFactory(qsim),
				new NetsimWrappingQVehicleProvider(netsimEngine));
	}
	
	static public void configureComponents( QSimComponentKeysRegistry components ) {
		components.removeNamedComponent(QNetsimEngineModule.COMPONENT_NAME);
		components.removeNamedComponent(PopulationModule.COMPONENT_NAME);
		
		components.addNamedAnnotation(REPLACEMENT_QNETSIM_ENGINE );
		components.addNamedAnnotation(JOINT_MODES_DEPARTURE_HANDLER );
		components.addNamedAnnotation(JOINT_PASSENGER_UNBOARDING );
		components.addNamedAnnotation(AGENTS_SOURCE_WITH_VEHICLES );
	}
}
