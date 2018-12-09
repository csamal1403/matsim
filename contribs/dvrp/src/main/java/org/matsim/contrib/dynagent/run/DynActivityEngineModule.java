package org.matsim.contrib.dynagent.run;

import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.ActivityEngineModule;
import org.matsim.core.mobsim.qsim.components.QSimComponentKeysRegistry;

public class DynActivityEngineModule extends AbstractQSimModule {
	public final static String COMPONENT_NAME = "DynActivityEngine";

	@Override
	protected void configureQSim() {
		bind(DynActivityEngine.class).asEagerSingleton();
		this.addQSimComponentBinding( COMPONENT_NAME ).to( DynActivityEngine.class ) ;
	}

	public static void configureComponents( QSimComponentKeysRegistry components ) {
		components.removeNamedComponent(ActivityEngineModule.COMPONENT_NAME);
		components.addNamedAnnotation(COMPONENT_NAME );
	}
}
