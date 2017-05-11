package wbs.sms.number.core.console;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.part.PagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("numberSubscriptionsActivePart")
public
class NumberSubscriptionsActivePart
	implements ComponentFactory <PagePart> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized dependencies

	@UninitializedDependency
	Provider <NumberSubscriptionsPart> numberSubscriptionsPartProvider;

	// implementation

	@Override
	public
	PagePart makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return numberSubscriptionsPartProvider.get ()

				.activeOnly (
					true);

		}

	}

}
