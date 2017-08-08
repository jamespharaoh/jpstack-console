package wbs.api.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.api.mvc.ApiFile;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("apiModuleBuilder")
public
class ApiModuleBuilder
	implements Builder <TaskLogger> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <ApiFile> apiFile;

	@PrototypeDependency
	ComponentProvider <BuilderFactory <?, TaskLogger>> builderFactoryProvider;

	// collection dependencies

	@PrototypeDependency
	@ApiModuleBuilderHandler
	Map <Class <?>, ComponentProvider <Object>> apiModuleBuilders;

	// state

	Builder <TaskLogger> builder;

	// init

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			builder =
				builderFactoryProvider.provide (
					taskLogger)

				.contextClass (
					TaskLogger.class)

				.addBuilders (
					taskLogger,
					apiModuleBuilders)

				.create (
					taskLogger)

			;

		}

	}

	// implementation

	@Override
	public
	void descend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object parentObject,
			@NonNull List <?> childObjects,
			@NonNull Object targetObject,
			@NonNull MissingBuilderBehaviour missingBuilderBehaviour) {

		List <Object> firstPass =
			new ArrayList<> ();

		for (
			Object childObject
				: childObjects
		) {

			firstPass.add (
				childObject);

		}

		builder.descend (
			parentTaskLogger,
			parentObject,
			firstPass,
			targetObject,
			missingBuilderBehaviour);

	}

}
