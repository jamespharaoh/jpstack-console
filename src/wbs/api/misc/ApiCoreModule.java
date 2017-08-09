package wbs.api.misc;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.api.module.ApiModule;
import wbs.api.mvc.ApiFile;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.file.WebFile;

@SingletonComponent ("apiCoreModule")
public
class ApiCoreModule
	implements ApiModule {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <ApiFile> apiFileProvider;

	@PrototypeDependency
	ComponentProvider <ApiHomeResponder> apiHomeResponderProvider;

	// state

	Map <String, WebFile> files;

	// accessors

	@Override
	public
	Map <String, WebFile> webModuleFiles (
			@NonNull TaskLogger parentTaskLogger) {

		return files;

	}

	// life cycle

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

			files =
				ImmutableMap.<String, WebFile> builder ()

				.put (
					"",
					apiFileProvider.provide (
						taskLogger)

					.getResponderProvider (
						apiHomeResponderProvider)

				)

				.build ()

			;

		}

	}

}
