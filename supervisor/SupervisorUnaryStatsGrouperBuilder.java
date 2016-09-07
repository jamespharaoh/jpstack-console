package wbs.console.supervisor;

import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.reporting.UnaryStatsGrouper;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("supervisorUnaryStatsGrouperBuilder")
@ConsoleModuleBuilderHandler
public
class SupervisorUnaryStatsGrouperBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <UnaryStatsGrouper> unaryStatsGrouperProvider;

	// builder

	@BuilderParent
	SupervisorConfigSpec container;

	@BuilderSource
	SupervisorUnaryStatsGrouperSpec spec;

	@BuilderTarget
	SupervisorConfigBuilder supervisorConfigBuilder;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String name =
			spec.name ();

		String label =
			spec.label ();

		supervisorConfigBuilder.statsGroupersByName ().put (
			name,
			unaryStatsGrouperProvider.get ()

				.label (
					label));

	}

}
