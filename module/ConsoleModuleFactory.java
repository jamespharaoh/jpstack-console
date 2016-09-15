package wbs.console.module;

import static wbs.utils.string.StringUtils.hyphenToCamel;

import javax.inject.Named;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;

@Accessors (fluent = true)
public
class ConsoleModuleFactory
	implements ComponentFactory {

	// singleton dependencies

	@SingletonDependency
	@Named
	Builder consoleModuleBuilder;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleModuleImplementation> consoleModuleProvider;

	// properties

	@Getter @Setter
	ConsoleModuleSpec consoleSpec;

	// implementation

	@Override
	public
	Object makeComponent () {

		ConsoleModuleImplementation consoleModule =
			consoleModuleProvider.get ();

		SimpleConsoleBuilderContainer container =
			new SimpleConsoleBuilderContainerImplementation ()

			.newBeanNamePrefix (
				hyphenToCamel (
					consoleSpec.name ()))

			.existingBeanNamePrefix (
				hyphenToCamel (
					consoleSpec.name ()));

		consoleModuleBuilder.descend (
			container,
			consoleSpec.builders (),
			consoleModule,
			MissingBuilderBehaviour.error);

		return consoleModule;

	}

}
