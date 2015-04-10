package wbs.clients.apn.chat.supervisor.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.List;

import javax.inject.Inject;

import lombok.experimental.Accessors;
import wbs.clients.apn.chat.contact.model.ChatContactNoteObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatContactNoteRec;
import wbs.clients.apn.chat.core.console.ChatConsoleHelper;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.reporting.console.StatsPeriod;

@Accessors (fluent = true)
@PrototypeComponent ("chatSupervisorNotesPart")
public
class ChatSupervisorNotesPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatConsoleHelper chatHelper;

	@Inject
	ChatContactNoteObjectHelper chatContactNoteHelper;

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ConsoleObjectManager consoleObjectManager;

	@Inject
	TimeFormatter timeFormatter;

	// state

	StatsPeriod statsPeriod;

	ChatRec chat;

	List<ChatContactNoteRec> chatContactNotes;

	// implementation

	@Override
	public
	void prepare () {

		statsPeriod =
			(StatsPeriod)
			parameters.get ("statsPeriod");

		chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		// get notes

		chatContactNotes =
			chatContactNoteHelper.findByTimestamp (
				chat,
				statsPeriod.toInterval ());

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Note</th>\n",
			"<th>User</th>\n",
			"<th>Monitor</th>\n",
			"<th>By</th>\n",
			"<th>Timestamp</th>\n",
			"</tr>\n");

		for (ChatContactNoteRec chatContactNote
				: chatContactNotes) {

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				chatContactNote.getNotes (),

				"%s\n",
				consoleObjectManager.tdForObjectMiniLink (
					chatContactNote.getUser (),
					chatContactNote.getUser ().getChat ()),

				"%s\n",
				consoleObjectManager.tdForObjectMiniLink (
					chatContactNote.getMonitor (),
					chatContactNote.getMonitor ().getChat ()),

				"%s\n",
				consoleObjectManager.tdForObjectMiniLink (
					chatContactNote.getConsoleUser ()),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					chatMiscLogic.timezone (
						chat),
					dateToInstant (
						chatContactNote.getTimestamp ())),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
