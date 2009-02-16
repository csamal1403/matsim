/* *********************************************************************** *
 * project: org.matsim.*
 * Scenario.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.controler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.matsim.basic.signalsystems.BasicLightSignalSystems;
import org.matsim.basic.signalsystemsconfig.BasicLightSignalSystemConfiguration;
import org.matsim.config.Config;
import org.matsim.facilities.Facilities;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.matsim.gbl.Gbl;
import org.matsim.lightsignalsystems.MatsimLightSignalSystemConfigurationReader;
import org.matsim.lightsignalsystems.MatsimLightSignalSystemsReader;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkChangeEventsParser;
import org.matsim.network.NetworkFactory;
import org.matsim.network.NetworkLayer;
import org.matsim.network.TimeVariantLinkImpl;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.Population;
import org.matsim.world.MatsimWorldReader;
import org.matsim.world.World;
import org.xml.sax.SAXException;

/**
 * Provides convenient methods to load often used data. The methods ensure that possibly
 * dependent data is loaded as well (e.g. network requires the world to be loaded first,
 * plans can depend upon world, network, facilities).
 *
 * @author mrieser
 */
public class ScenarioData {
	private final String worldFileName;
	private final String networkFileName;
	private final String facilitiesFileName;
	private final String populationFileName;
	private boolean isTimeVariantNetwork = false;
	private String changEventsInputFile;

	private boolean worldLoaded = false;
	private boolean networkLoaded = false;
	private boolean facilitiesLoaded = false;
	private boolean populationLoaded = false;
	
	private World world = null;
	private NetworkLayer network = null;
	private Facilities facilities = null;
	private Population population = null;
	private BasicLightSignalSystems signalSystems = null;
	private List<BasicLightSignalSystemConfiguration> signalSystemConfigurations = null;
	
	private final NetworkFactory networkFactory;
	private Config config;

	private static final Logger log = Logger.getLogger(ScenarioData.class);

	/**
	 * Loads the data from the locations specified in the configuration.
	 * The configuration must already be loaded at this point.
	 *
	 * @param config
	 */
	public ScenarioData(final Config config) {
		this(config, null);
	}

	public ScenarioData(final Config config, final NetworkFactory factory) {
		this.worldFileName = config.world().getInputFile();
		this.networkFileName = config.network().getInputFile();
		this.isTimeVariantNetwork = config.network().isTimeVariantNetwork();
		this.changEventsInputFile = config.network().getChangeEventsInputFile();
		this.facilitiesFileName = config.facilities().getInputFile();
		this.populationFileName = config.plans().getInputFile();
		this.config = config;
		if (factory == null) {
			this.networkFactory = new NetworkFactory();
			if (this.isTimeVariantNetwork){
				this.networkFactory.setLinkPrototype(TimeVariantLinkImpl.class);
			}
		} else {
			this.networkFactory = factory;
		}
	}

	public ScenarioData(final String worldFileName, final String networkFileName,
			final String facilitiesFileName, final String populationFileName) {
		this.worldFileName = worldFileName;
		this.networkFileName = networkFileName;
		this.facilitiesFileName = facilitiesFileName;
		this.populationFileName = populationFileName;
		this.networkFactory = new NetworkFactory();
	}

	public World getWorld() throws RuntimeException {
		if (!this.worldLoaded) {
			if (this.worldFileName != null) {
				log.info("loading world from " + this.worldFileName);
				this.world = Gbl.getWorld();
				try {
					new MatsimWorldReader(this.world).parse(this.worldFileName);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				this.world = Gbl.getWorld();
			}
			this.worldLoaded = true;
		}
		return this.world;
	}

	public NetworkLayer getNetwork() throws RuntimeException {
		if (!this.networkLoaded) {
			getWorld(); // make sure the world is loaded
			log.info("loading network from " + this.networkFileName);
			this.network = new NetworkLayer(this.networkFactory);

			try {
				new MatsimNetworkReader(this.network).parse(this.networkFileName);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			this.world.setNetworkLayer(this.network);
			this.world.complete();

			if ((this.changEventsInputFile != null) && this.isTimeVariantNetwork){
				log.info("loading network change events from " + this.changEventsInputFile);
				NetworkChangeEventsParser parser = new NetworkChangeEventsParser(this.network);
				try {
					parser.parse(this.changEventsInputFile);
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.network.setNetworkChangeEvents(parser.getEvents());
			}

			this.networkLoaded = true;
		}
		return this.network;
	}

	public Facilities getFacilities() throws RuntimeException {
		if (!this.facilitiesLoaded) {
			if (this.facilitiesFileName != null) {
				getWorld(); // make sure the world is loaded
				log.info("loading facilities from " + this.facilitiesFileName);
				this.facilities = (Facilities)this.world.createLayer(Facilities.LAYER_TYPE, null);
				try {
					new MatsimFacilitiesReader(this.facilities).parse(this.facilitiesFileName);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				this.facilities = (Facilities)this.world.createLayer(Facilities.LAYER_TYPE, null);
			}
			this.world.complete();
			this.facilitiesLoaded = true;
		}
		return this.facilities;
	}

	public Population getPopulation() throws RuntimeException {
		if (!this.populationLoaded) {
			this.population = new Population(Population.NO_STREAMING);
			// make sure that world, facilities and network are loaded as well
			getWorld();
			getNetwork();
			getFacilities();

			log.info("loading population from " + this.populationFileName);
			try {
				new MatsimPopulationReader(this.population, this.network).parse(this.populationFileName);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			this.population.printPlansCount();

			this.populationLoaded = true;
		}
		return this.population;
	}
	
	public BasicLightSignalSystems getSignalSystems() {
		if ((this.config == null) || (this.config.signalSystems() == null)){
			throw new IllegalStateException("SignalSystems can only be loaded if set in config");
		}
		if (this.signalSystems == null) {
			this.signalSystems = new BasicLightSignalSystems();
			MatsimLightSignalSystemsReader reader = new MatsimLightSignalSystemsReader(this.signalSystems);
			log.info("loading signalsystems from " + this.config.signalSystems().getSignalSystemFile());
			reader.readFile(this.config.signalSystems().getSignalSystemFile());
		}
		return this.signalSystems;
	}
	
	public List<BasicLightSignalSystemConfiguration> getSignalSystemsConfiguration() {
		if ((this.config == null) || (this.config.signalSystems() == null)){
			throw new IllegalStateException("SignalSystems can only be loaded if set in config");
		}
		if (this.signalSystemConfigurations == null){
			this.signalSystemConfigurations = new ArrayList<BasicLightSignalSystemConfiguration>();
			MatsimLightSignalSystemConfigurationReader reader = new MatsimLightSignalSystemConfigurationReader(this.signalSystemConfigurations);
			log.info("loading signalsystemsconfiguration from " + this.config.signalSystems().getSignalSystemConfigFile());
			reader.readFile(this.config.signalSystems().getSignalSystemConfigFile());
		}
		return this.signalSystemConfigurations;
	}
	
}
