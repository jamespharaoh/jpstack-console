package wbs.smsapps.ticketer.daemon;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.smsapps.ticketer.model.TicketerRec;
import wbs.smsapps.ticketer.model.TicketerTicketObjectHelper;
import wbs.smsapps.ticketer.model.TicketerTicketRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@PrototypeComponent ("ticketerCommand")
public
class TicketerCommand
	implements CommandHandler {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	Random random;

	@Inject
	TicketerTicketObjectHelper ticketerTicketHelper;

	@Inject
	Provider<MessageSender> messageSender;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"ticketer.ticketer"
		};

	}

	// implementation

	@Override
	public
	InboxAttemptRec handle () {

		TicketerRec ticketer =
			(TicketerRec) (Object)
			objectManager.getParent (
				command);

		// set inbound message stuff

		ServiceRec defaultService =
			serviceHelper.findByCode (
				ticketer,
				"default");

		// load message and stuff

		MessageRec messageIn =
			inbox.getMessage ();

		// work out ticket and message text

		String ticket =
			generateTicket (
				ticketChars,
				8);

		String messageText =
			ticketer.getText ().replaceAll (
				"\\{ticket\\}",
				ticket);

		// send message

		MessageRec messageOut =
			messageSender.get ()
				.threadId (messageIn.getThreadId ())
				.number (messageIn.getNumber ())
				.messageString (messageText)
				.numFrom (ticketer.getNumber ())
				.route (ticketer.getRoute ())
				.service (serviceHelper.findByCode (ticketer, "default"))
				.send ();

		// create ticketer ticket entry

		ticketerTicketHelper.insert (
			new TicketerTicketRec ()

			.setTicketer (
				ticketer)

			.setNumber (
				messageIn.getNumber ())

			.setTicket (
				ticket)

			.setMessage (
				messageOut)

		);

		// process inbox

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (defaultService),
			Optional.<AffiliateRec>absent (),
			command);

	}

	private static final
	String ticketChars =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private
	String generateTicket (
			String sourceChars,
			int length) {

		char[] chars =
			new char [length];

		for (
			int position = 0;
			position < length;
			position++
		) {

			chars [position] =
				sourceChars.charAt (
					random.nextInt (
						sourceChars.length ()));

		}

		return new String (chars);

	}

}
