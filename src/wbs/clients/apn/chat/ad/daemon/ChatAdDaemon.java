package wbs.clients.apn.chat.ad.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.clients.apn.chat.ad.model.ChatAdTemplateRec;
import wbs.clients.apn.chat.bill.logic.ChatCreditCheckResult;
import wbs.clients.apn.chat.bill.logic.ChatCreditLogic;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserDao;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandObjectHelper;

import com.google.common.base.Optional;

@Log4j
@SingletonComponent ("chatAdDaemon")
public
class ChatAdDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	ChatCreditLogic chatCreditLogic;

	@Inject
	ChatMiscLogic chatLogic;

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserDao chatUserDao;

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	Random random;

	@Inject
	TextObjectHelper textHelper;

	// details

	@Override
	protected
	String getThreadName () {
		return "ChatAd";
	}

	@Override
	protected
	int getDelayMs () {
		return 60 * 1000;
	}

	@Override
	protected
	String generalErrorSource () {
		return "chat ad daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error sending chat ads in background";
	}

	// implementation

	@Override
	protected
	void runOnce () {

		log.debug ("Looking for users to send an ad to");

		// get a list of users who have passed their ad time

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<ChatUserRec> chatUsers =
			chatUserHelper.findWantingAd ();

		transaction.close ();

		// then call doChatUserAd for each one

		for (ChatUserRec chatUser
				: chatUsers) {

			try {

				doChatUserAd (
					chatUser.getId ());

			} catch (Exception exception) {

				exceptionLogic.logThrowable (
					"daemon",
					"ChatAdDaemon",
					exception,
					Optional.<Integer>absent (),
					false);

			}

		}

	}

	private
	void doChatUserAd (
			int chatUserId) {

		log.debug (
			stringFormat (
				"Attempting to send ad to %s",
				chatUserId));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// find the user

		ChatUserRec chatUser =
			chatUserHelper.find (chatUserId);

		ChatRec chat =
			chatUser.getChat ();

		// check he really is due an ad

		Date now =
			new Date ();

		if (chatUser.getNextAd ().getTime () > now.getTime ())
			return;

		// do a credit and number check

		ChatCreditCheckResult creditCheckResult =
			chatCreditLogic.userCreditCheck (
				chatUser);

		if (
			creditCheckResult.failed ()
		) {

			log.info (
				stringFormat (
					"Skipping ad to %s (%s)",
					objectManager.objectPath (chatUser),
					creditCheckResult.details ()));

		} else if (chatUser.getFirstJoin () == null) {

			log.info (
				stringFormat (
					"Skipping ad to %s (never fully joined)",
					objectManager.objectPath (chatUser)));

		} else if (! chatUser.getNumber ().getNumber ().startsWith ("447")
				|| chatUser.getNumber ().getNumber ().length () != 12) {

			log.info (
				stringFormat (
					"Skipping ad to %s (not a mobile number)",
					objectManager.objectPath (chatUser)));

		} else {

			// work out credit

			int approvedCredit =
				+ chatUser.getCredit ()
				- chatUser.getCreditPending ()
				- chatUser.getCreditPendingStrict ();

			// pick an ad

			List<ChatAdTemplateRec> adTemplates =
				new ArrayList<ChatAdTemplateRec> (
					chatUser.getChat ().getChatAdTemplates ());

			TextRec text = null;

			while (! adTemplates.isEmpty ()) {

				int templateNumber =
					random.nextInt (adTemplates.size ());

				ChatAdTemplateRec chatAdTemplate =
					adTemplates.get (templateNumber);

				// pick the text

				if (chatUser.getOrient () == Orient.gay
						&& chatUser.getGender () == Gender.male) {

					text =
						chatAdTemplate.getGayMaleText ();

				} else if (chatUser.getOrient () == Orient.gay
						&& chatUser.getGender () == Gender.female) {

					text =
						chatAdTemplate.getGayFemaleText ();

				} else {

					text =
						chatAdTemplate.getGenericText ();

				}

				// check for the place holder

				boolean hasCreditPlaceholder =
					text.getText ().contains ("{credit}");

				boolean wantCreditPlaceholder =
					approvedCredit > 0;

				if (hasCreditPlaceholder == wantCreditPlaceholder)
					break;

				adTemplates.remove (templateNumber);

			}

			if (text == null) {

				log.info (
					stringFormat (
						"Skipping ad to %s (no suitable ads configured)",
						objectManager.objectPath (chatUser)));

			} else {

				// replace placeholder with credit

				String messageString =
					text.getText ().replace (
						"{credit}",
						String.format (
							"%d.%02d",
							approvedCredit / 100,
							approvedCredit % 100));

				// send the message

				log.info (
					stringFormat (
						"Sending ad to %s: %s",
						objectManager.objectPath (chatUser),
						messageString));

				TextRec messageText =
					textHelper.findOrCreate (
						messageString);

				ServiceRec adService =
					serviceHelper.findByCode (
						chat,
						"ad");

				chatSendLogic.sendMessageMagic (
					chatUser,
					Optional.<Integer>absent (),
					messageText,
					commandHelper.findByCode (chat, "magic"),
					adService,
					commandHelper.findByCode (chat, "join_next").getId ());

			}

		}

		// set his next ad time

		chatUserLogic.scheduleAd (chatUser);

		transaction.commit ();

	}

}