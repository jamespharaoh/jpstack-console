package wbs.console.helper.provider;

import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitFullStop;

import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.spec.ConsoleHelperProviderSpec;
import wbs.console.module.ConsoleMetaModuleImplementation;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;

@PrototypeComponent ("consoleHelperProviderMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ConsoleHelperProviderMetaBuilder <
	RecordType extends Record <RecordType>
>
	implements BuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <GenericConsoleHelperProvider <RecordType>>
	genericConsoleHelperProviderProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer contextMetaBuilderContainer;

	@BuilderSource
	ConsoleHelperProviderSpec consoleHelperProviderSpec;

	@BuilderTarget
	ConsoleMetaModuleImplementation consoleMetaModule;

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

		ObjectHelper <RecordType> objectHelper =
			genericCastUnchecked (
				objectManager.objectHelperForObjectNameRequired (
					consoleHelperProviderSpec.objectName ()));

		List <String> packageNameParts =
			stringSplitFullStop (
				objectHelper.objectClass ().getPackage ().getName ());

		String consoleHelperClassName =
			stringFormat (
				"%s.console.%sConsoleHelper",
				joinWithFullStop (
					packageNameParts.subList (
						0,
						packageNameParts.size () - 1)),
				capitalise (
					objectHelper.objectName ()));

		@SuppressWarnings ("unchecked")
		Class <ConsoleHelper <RecordType>> consoleHelperClass =
			(Class <ConsoleHelper <RecordType>>)
			classForNameRequired (
				consoleHelperClassName);

		genericConsoleHelperProviderProvider.get ()

			.consoleHelperProviderSpec (
				consoleHelperProviderSpec)

			.objectHelper (
				objectHelper)

			.consoleHelperClass (
				consoleHelperClass)

			.init (
				taskLogger);

	}

}