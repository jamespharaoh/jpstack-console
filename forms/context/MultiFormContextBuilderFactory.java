package wbs.console.forms.context;

import static wbs.utils.collection.MapUtils.mapTransformToMap;

import java.util.Map;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import wbs.console.forms.types.FormType;
import wbs.console.module.ConsoleModule;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
class MultiFormContextBuilderFactory <Container>
	implements ComponentFactory <
		MultiFormContextBuilder <Container>
	> {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency
	ConsoleModule chatBroadcastConsoleModule;

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized dependencies

	@UninitializedDependency
	Provider <MultiFormContextBuilder <Container>>
		multiFormContextBuilderProvider;

	// properties

	@Getter @Setter
	ConsoleModule consoleModule;

	@Getter @Setter
	Class <Container> objectClass;

	@Getter @Setter
	String formName;

	@Getter @Setter
	FormType formType;

	@Getter @Setter
	Map <String, String> fields;

	// implementation

	@Override
	public
	MultiFormContextBuilder <Container> makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		return multiFormContextBuilderProvider.get ()

			.objectClass (
				objectClass)

			.formName (
				formName)

			.formType (
				formType)

			.fieldSets (
				mapTransformToMap (
					fields,
					(name, fieldsName) ->
						name,
					(name, fieldsName) ->
						consoleModule.formFieldSetRequired (
							fieldsName,
							objectClass)))

		;

	}

}
