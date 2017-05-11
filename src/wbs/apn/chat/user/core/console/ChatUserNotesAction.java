package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.user.core.model.ChatUserNoteObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatUserNotesAction")
public
class ChatUserNotesAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserNoteObjectHelper chatUserNoteHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatUserNotesResponder");

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

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			// check params

			String noteString =
				stringTrim (
					requestContext.parameterRequired (
						"note"));

			if (
				stringIsEmpty (
					noteString)
			) {

				requestContext.addError (
					"Please enter a note");

				return null;

			}

			// create note

			TextRec noteText =
				textHelper.findOrCreate (
					transaction,
					noteString);

			chatUserNoteHelper.insert (
				transaction,
				chatUserNoteHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setTimestamp (
					transaction.now ())

				.setUser (
					userConsoleLogic.userRequired (
						transaction))

				.setText (
					noteText)

			);

			// wrap up

			transaction.commit ();

			requestContext.addNotice (
				"Note added");

			return null;

		}

	}

}
