package wbs.platform.object.settings;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectRemoveAction")
public
class ObjectRemoveAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// properties

	@Getter @Setter
	ConsoleHelper<?> objectHelper;

	@Getter @Setter
	ConsoleHelper<?> parentHelper;

	@Getter @Setter
	Provider<Responder> settingsResponder;

	@Getter @Setter
	Provider<Responder> listResponder;

	@Getter @Setter
	String nextContextTypeName;

	@Getter @Setter
	String editPrivKey;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return settingsResponder.get ();

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			Record <?> ephemeralObject =
				objectHelper.lookupObject (
					transaction,
					requestContext.consoleContextStuffRequired ());

			objectHelper.remove (
				transaction,
				ephemeralObject);

			Record <?> parentObject =
				objectHelper.getParentRequired (
					transaction,
					genericCastUnchecked (
						ephemeralObject));

			eventLogic.createEvent (
				transaction,
				"object_removed_in",
				userConsoleLogic.userRequired (
					transaction),
				objectHelper.getCode (
					genericCastUnchecked (
						ephemeralObject)),
				objectHelper.shortName (),
				parentObject);

			transaction.commit ();

			requestContext.addNotice (
				stringFormat (
					"%s deleted",
					capitalise (
						objectHelper.friendlyName ())));

			ConsoleContextType targetContextType =
				consoleManager.contextType (
					nextContextTypeName,
					true);

			ConsoleContext targetContext =
				consoleManager.relatedContextRequired (
					transaction,
					requestContext.consoleContextRequired (),
					targetContextType);

			consoleManager.changeContext (
				transaction,
				targetContext,
				"/" + parentObject.getId ());

			return listResponder
				.get ();

		}

	}

}
