package wbs.console.combo;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.responder.ConsoleFile;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("simpleResponderBuilder")
@ConsoleModuleBuilderHandler
public
class SimpleResponderBuilder
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer simpleContainerSpec;

	@BuilderSource
	SimpleResponderSpec simpleResponderSpec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String responderName;
	String responderBeanName;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"build");

		setDefaults ();

		buildResponder (
			taskLogger);

	}

	void buildResponder (
			@NonNull TaskLogger taskLogger) {

		consoleModule.addResponder (
			responderName,
			consoleModule.beanResponder (
				taskLogger,
				responderBeanName));

	}

	// defaults

	void setDefaults () {

		name =
			simpleResponderSpec.name ();

		responderName =
			ifNull (
				simpleResponderSpec.responderName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.newBeanNamePrefix (),
					capitalise (name)));

		responderBeanName =
			ifNull (
				simpleResponderSpec.responderBeanName (),
				stringFormat (
					"%s%sResponder",
					simpleContainerSpec.newBeanNamePrefix (),
					capitalise (name)));

	}

}
