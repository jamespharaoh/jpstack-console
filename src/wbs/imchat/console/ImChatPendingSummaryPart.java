package wbs.imchat.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlDivClose;
import static wbs.web.utils.HtmlBlockUtils.htmlDivOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteHtml;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.encodeNewlineToBr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.forms.context.FormContext;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryEditableScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.imchat.model.ImChatCustomerDetailValueRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatProfileRec;
import wbs.imchat.model.ImChatRec;

@PrototypeComponent ("imChatPendingSummaryPart")
public
class ImChatPendingSummaryPart
	extends AbstractPagePart {

	// dependencies

	@SingletonDependency
	@NamedDependency ("imChatPendingSummaryCustomerFormContextBuilder")
	FormContextBuilder <ImChatCustomerRec> customerFormContextBuilder;

	@SingletonDependency
	ImChatMessageConsoleHelper imChatMessageHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	@NamedDependency ("imChatPendingSummaryMessageFormContextBuilder")
	FormContextBuilder <ImChatMessageRec> messageFormContextBuilder;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	@NamedDependency ("imChatPendingSummaryProfileFormContextBuilder")
	FormContextBuilder <ImChatProfileRec> profileFormContextBuilder;

	// state

	FormContext <ImChatCustomerRec> customerFormContext;
	FormContext <ImChatProfileRec> profileFormContext;
	FormContext <ImChatMessageRec> messageFormContext;

	ImChatMessageRec message;
	ImChatConversationRec conversation;
	ImChatCustomerRec customer;
	ImChatProfileRec profile;
	ImChatRec imChat;

	boolean canSupervise;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> of (

			JqueryScriptRef.instance,
			JqueryEditableScriptRef.instance,

			ConsoleApplicationScriptRef.javascript (
				"/js/im-chat.js")

		);

	}

	@Override
	public
	Set <HtmlLink> links () {

		return ImmutableSet.<HtmlLink> of (

			HtmlLink.applicationCssStyle (
				"/style/im-chat.css")

		);

	}

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			// load data

			message =
				imChatMessageHelper.findFromContextRequired (
					transaction);

			conversation =
				message.getImChatConversation ();

			customer =
				conversation.getImChatCustomer ();

			profile =
				conversation.getImChatProfile ();

			imChat =
				customer.getImChat ();

			// setup forms

			customerFormContext =
				customerFormContextBuilder.build (
					transaction,
					emptyMap (),
					customer);

			profileFormContext =
				profileFormContextBuilder.build (
					transaction,
					emptyMap (),
					profile);

			messageFormContext =
				messageFormContextBuilder.build (
					transaction,
					emptyMap (),
					message);

			// misc

			canSupervise =
				privChecker.canRecursive (
					transaction,
					imChat,
					"supervisor");

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlDivOpen (
				htmlClassAttribute (
					"layout-container"));

			htmlTableOpen (
				htmlClassAttribute (
					"layout"));

			htmlTableRowOpen ();

			htmlTableCellOpen (
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"width",
						"50%")));

			goCustomerSummary (
				transaction);

			goCustomerDetails ();

			htmlTableCellClose ();

			htmlTableCellOpen (
				htmlStyleAttribute (
					htmlStyleRuleEntry (
						"width",
						"50%")));

			goProfileSummary (
				transaction);

			goCustomerNotes (
				transaction);

			htmlTableCellClose ();

			htmlTableRowClose ();

			htmlTableClose ();

			htmlDivClose ();

			goHistory (
				transaction);

		}

	}

	void goCustomerDetails () {

		htmlHeadingThreeWrite (
			"Customer details");

		htmlTableOpenDetails ();

		for (
			ImChatCustomerDetailTypeRec detailType
				: imChat.getCustomerDetailTypes ()
		) {

			if (

				detailType.getRestricted ()

				&& ! canSupervise

			) {
				continue;
			}

			ImChatCustomerDetailValueRec detailValue =
				customer.getDetails ().get (
					detailType.getId ());

			htmlTableDetailsRowWrite (
				detailType.getName (),
				ifNotNullThenElseEmDash (
					detailValue,
					() -> detailValue.getValue ()));

		}

		htmlTableClose ();

	}

	void goCustomerSummary (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goCustomerSummary");

		) {

			htmlHeadingThreeWrite (
				"Customer summary");

			customerFormContext.outputDetailsTable (
				transaction);

		}

	}

	private
	void goProfileSummary (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goProfileSummary");

		) {

			htmlHeadingThreeWrite (
				"Profile summary");

			profileFormContext.outputDetailsTable (
				transaction);

		}

	}

	private
	void goCustomerNotes (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goCustomerNotes");

		) {

			htmlHeadingThreeWrite (
				"Notes");

			htmlParagraphWriteHtml (
				encodeNewlineToBr (
					customer.getNotesText () != null
						? customer.getNotesText ().getText ()
						: ""),
				htmlIdAttribute (
					stringFormat (
						"im-chat-customer-note-%s",
						integerToDecimalString (
							customer.getId ()))),
				htmlClassAttribute (
					"im-chat-customer-note-editable"));


		}
	}

	private
	void goHistory (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHistory");

		) {

			htmlHeadingThreeWrite (
				"Conversation history");

			// retrieve messages

			List <ImChatMessageRec> messages =
				new ArrayList<> (
					conversation.getMessagesIn ());

			List <ImChatMessageRec> historyRequests =
				Lists.reverse (
					messages);

			// create message table

			htmlTableOpenList ();

			// header

			htmlTableRowOpen ();

			messageFormContext.outputTableHeadings (
				transaction);

			htmlTableRowClose ();

			// row

			for (
				ImChatMessageRec historyRequest
					: historyRequests
			) {

				if (
					isNotNull (
						historyRequest.getPartnerImChatMessage ())
				) {

					ImChatMessageRec historyReply =
						historyRequest.getPartnerImChatMessage ();

					htmlTableRowOpen (
						htmlClassAttribute (
							classForMessage (
								historyReply)));

					messageFormContext.outputTableCellsList (
						transaction,
						historyReply,
						true);

					htmlTableRowClose ();

				}

				htmlTableRowOpen (
					htmlClassAttribute (
						presentInstances (

					Optional.of (
						classForMessage (
							historyRequest)),

					optionalIf (
						referenceEqualWithClass (
							ImChatMessageRec.class,
							message,
							historyRequest),
						() -> "selected")

				)));

				messageFormContext.outputTableCellsList (
					transaction,
					historyRequest,
					true);

				htmlTableRowClose ();

			}

			htmlTableClose ();

		}

	}

	String classForMessage (
			@NonNull ImChatMessageRec message) {

		if (
			isNotNull (
				message.getPrice ())
		) {

			return "message-out-charge";

		} else if (
			isNotNull (
				message.getSenderUser ())
		) {

			return "message-out";

		} else {

			return "message-in";

		}

	}

}
