package wbs.apn.chat.contact.console;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlSpanWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlUtils.htmlLinkWrite;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.extern.log4j.Log4j;

import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.user.core.model.ChatUserAlarmObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserAlarmRec;
import wbs.apn.chat.user.core.model.ChatUserOperatorLabel;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Log4j
@PrototypeComponent ("chatMonitorInboxFormResponder")
public
class ChatMonitorInboxFormResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@SingletonDependency
	ChatUserAlarmObjectHelper chatUserAlarmHelper;

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ChatMonitorInboxRec chatMonitorInbox;
	ChatUserRec userChatUser;
	ChatUserRec monitorChatUser;
	ChatUserAlarmRec alarm;

	@Override
	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/gsm.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

	@Override
	public
	void prepare () {

		Optional<ChatMonitorInboxRec> chatMonitorInboxOptional =
			chatMonitorInboxHelper.find (
				requestContext.stuffInteger (
					"chatMonitorInboxId"));

		if (
			optionalIsNotPresent (
				chatMonitorInboxOptional)
		) {

			requestContext.addError (
				"Chat monitor inbox item not found");

			log.error (
				stringFormat (
					"Chat monitor inbox not found: %d",
					requestContext.stuffInteger (
						"chatMonitorInboxId")));

			return;

		}

		chatMonitorInbox =
			chatMonitorInboxOptional.get ();

		userChatUser =
			chatMonitorInbox.getUserChatUser ();

		monitorChatUser =
			chatMonitorInbox.getMonitorChatUser ();

		alarm =
			chatUserAlarmHelper.find (
				userChatUser,
				monitorChatUser);

	}

	@Override
	public
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		if (chatMonitorInbox == null)
			return;

		// script block open

		htmlScriptBlockOpen ();

		// show inbox

		formatWriter.writeLineFormat (
			"top.show_inbox (true);");

		// set location

		formatWriter.writeLineFormat (
			"top.frames ['main'].location = '%j';",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"%s",
					consoleManager
						.context (
							"chatMonitorInbox",
							true)
						.pathPrefix (),
					"/%u",
					chatMonitorInbox.getId (),
					"/chatMonitorInbox.summary")));

		// script block close

		htmlScriptBlockClose ();

		// style block

		if (
			userChatUser.getOperatorLabel ()
				== ChatUserOperatorLabel.operator
		) {

			htmlStyleBlockOpen ();

			htmlStyleRuleWrite (
				"h2",
				htmlStyleRuleEntry (
					"background",
					"#800000"));

			htmlStyleRuleWrite (
				"table.details th",
				htmlStyleRuleEntry (
					"background",
					"#800000"));

			htmlStyleBlockClose ();

		}

	}

	String doName (
			ChatUserRec chatUser) {

		if (chatUser.getName () != null) {
			return chatUser.getName () + " " + chatUser.getCode ();
		} else {
			return chatUser.getCode ();
		}

	}

	@Override
	public
	void renderHtmlBodyContents () {

		requestContext.flushNotices (
			formatWriter);

		// links

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		htmlLinkWrite (
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			"Queues");

		htmlLinkWrite (
			"javascript:top.show_inbox (false)",
			"Close");

		htmlParagraphClose ();

		// handle deleted chat monitor inbox

		if (chatMonitorInbox == null)
			return;

		// send message

		htmlHeadingTwoWrite (
			stringFormat (
				"Send message as %s",
				userChatUser.getOperatorLabel ()));

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveApplicationUrl (
				stringFormat (
					"%s",
					consoleManager
						.context (
							"chatMonitorInbox",
							true)
						.pathPrefix (),
					"/%u",
					chatMonitorInbox.getId (),
					"/chatMonitorInbox.form")));

		// table open

		htmlTableOpenDetails ();

		// from

		htmlTableDetailsRowWrite (
			"From",
			doName (
				monitorChatUser));

		// to

		htmlTableDetailsRowWrite (
			"To",
			doName (
				userChatUser));

		// message

		htmlTableDetailsRowWriteHtml (
			"Message",
			() -> formatWriter.writeLineFormat (
				"<textarea",
				" name=\"text\"",
				" cols=\"64\"",
				" rows=\"4\"",
				" onkeyup=\"%h\"",
				stringFormat (
					"gsmCharCountMultiple2 (this, %s, %d);",
					"document.getElementById ('chars')",
					ChatMonitorInboxConsoleLogic.SINGLE_MESSAGE_LENGTH
						* ChatMonitorInboxConsoleLogic.MAX_OUT_MONITOR_MESSAGES),
				" onfocus=\"%h\"",
				stringFormat (
					"gsmCharCountMultiple2 (this, %s, %d);",
					"document.getElementById ('chars')",
					ChatMonitorInboxConsoleLogic.SINGLE_MESSAGE_LENGTH
						* ChatMonitorInboxConsoleLogic.MAX_OUT_MONITOR_MESSAGES),
				"></textarea>"));

		// chars

		htmlTableDetailsRowWriteHtml (
			"Chars",
			() -> {

			htmlSpanWrite (
				"",
				htmlIdAttribute (
					"chars"));

			htmlSpanWrite (
				"",
				htmlIdAttribute (
					"messageCount"));

		});

		// table close

		htmlTableClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"send message\"",
			" onclick=\"%h\"",
			alarm == null
				? stringFormat (
					"return confirm ('%j');",
					"send message with no alarm (please ignore this message " +
					"if you already set one)")
				: "",
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"sendAndNote\"",
			" value=\"send and make note\"",
			" onclick=\"%h\"",
			alarm == null
				? stringFormat (
					"return confirm ('%j');",
					"send message with no alarm (please ignore this message " +
					"if you already set one)")
				: "",
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"ignore\"",
			" value=\"don't send anything\"",
			" onclick=\"%h\"",
			alarm == null
				? stringFormat (
					"return confirm ('%j');",
					"ignore message with no alarm (please ignore this " +
					"message if you already set one)")
				: "",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

}