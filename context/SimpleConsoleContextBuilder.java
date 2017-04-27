package wbs.console.context;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.helper.spec.PrivKeySpec;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.ResolvedConsoleContextLink;
import wbs.console.module.SimpleConsoleBuilderContainer;
import wbs.console.object.ObjectContext;
import wbs.console.tab.ConsoleContextTab;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("simpleConsoleContextBuilder")
@ConsoleModuleBuilderHandler
public
class SimpleConsoleContextBuilder <
	ObjectType extends Record <ObjectType>
>
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	Provider <ConsoleContextType> contextType;

	@PrototypeDependency
	Provider <ObjectContext> objectContext;

	@PrototypeDependency
	Provider <SimpleConsoleContext> simpleContext;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	SimpleConsoleContextSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String structuralName;
	String contextName;
	String contextTypeName;
	String typeName;
	String title;

	List<PrivKeySpec> privKeySpecs;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			setDefaults ();

			buildContextType ();
			buildSimpleContext ();

			List<ResolvedConsoleContextLink> resolvedContextLinks =
				consoleMetaManager.resolveContextLink (
					name);

			for (
				ResolvedConsoleContextLink resolvedContextLink
					: resolvedContextLinks
			) {

				buildResolvedContexts (
					resolvedContextLink);

				buildResolvedTabs (
					resolvedContextLink);

			}

			ConsoleContextBuilderContainer<ObjectType> nextBuilderContainer =
				new ConsoleContextBuilderContainerImplementation<ObjectType> ()

				.taskLogger (
					container.taskLogger ())

				.consoleHelper (
					null)

				.structuralName (
					structuralName)

				.extensionPointName (
					structuralName)

				.pathPrefix (
					structuralName)

				.newBeanNamePrefix (
					structuralName)

				.existingBeanNamePrefix (
					structuralName)

				.tabLocation (
					"end")

				.friendlyName (
					camelToSpaces (
						structuralName));

			builder.descend (
				taskLogger,
				nextBuilderContainer,
				spec.children (),
				consoleModule,
				MissingBuilderBehaviour.error);

		}

	}

	void buildContextType () {

		consoleModule.addContextType (
			contextType.get ()

			.name (
				contextTypeName));

	}

	void buildSimpleContext () {

		consoleModule.addContext (
			simpleContext.get ()

			.name (
				contextName)

			.typeName (
				contextTypeName)

			.pathPrefix (
				"/" + contextName)

			.global (
				true)

			.title (
				title)

			.privKeySpecs (
				privKeySpecs)

		);

	}

	void buildResolvedContexts (
			@NonNull ResolvedConsoleContextLink resolvedContextLink) {

		for (
			String parentContextName
				: resolvedContextLink.parentContextNames ()
		) {

			String resolvedContextName =
				stringFormat (
					"%s.%s",
					parentContextName,
					resolvedContextLink.localName ());

			consoleModule.addContext (
				simpleContext.get ()

				.name (
					resolvedContextName)

				.typeName (
					contextTypeName)

				.pathPrefix (
					"/" + resolvedContextName)

				.global (
					true)

				.title (
					title)

				.privKeySpecs (
					privKeySpecs)

				.parentContextName (
					parentContextName)

				.parentContextTabName (
					resolvedContextLink.tabName ()));

		}

	}

	void buildResolvedTabs (
			@NonNull ResolvedConsoleContextLink contextLink) {

		consoleModule.addContextTab (
			container.taskLogger (),
			contextLink.tabLocation (),

			contextTab.get ()

				.name (
					contextLink.tabName ())

				.defaultLabel (
					contextLink.tabLabel ())

				.privKeys (
					contextLink.tabPrivKey ())

				.localFile (
					"type:" + name),

			contextLink.tabContextTypeNames ());

	}

	void setDefaults () {

		name =
			spec.name ();

		structuralName =
			name;

		contextName =
			name;

		contextTypeName =
			name;

		typeName =
			ifNull (
				spec.typeName (),
				structuralName);

		title =
			ifNull (
				spec.title (),
				capitalise (
					camelToSpaces (
						structuralName)));

		// TODO fix this

		privKeySpecs =
			ImmutableList.<PrivKeySpec>copyOf (
				Iterables.filter (
					spec.children (),
					PrivKeySpec.class));

	}

}
