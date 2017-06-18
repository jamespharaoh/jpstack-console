package wbs.apn.chat.user.image.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.etc.Misc.toEnumRequired;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.user.image.model.ChatUserImageType;

@PrototypeComponent ("chatUserImageUploadPart")
public
class ChatUserImageUploadPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency ("chatUserImageUploadFormType")
	ConsoleFormType <ChatUserImageUploadForm> formType;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ConsoleForm <ChatUserImageUploadForm> form;

	ChatUserImageType chatUserImageType;

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

			chatUserImageType =
				toEnumRequired (
					ChatUserImageType.class,
					requestContext.stuffString (
						"chatUserImageType"));

			form =
				formType.buildResponse (
					transaction,
					emptyMap (),
					new ChatUserImageUploadForm ());

			if (requestContext.post ()) {

				form.update (
					transaction);

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlParagraphWrite (
				formatWriter,
				"Please upload the photo or video.");

			form.outputFormTable (
				transaction,
				formatWriter,
				"post",
				requestContext.resolveLocalUrlFormat (
					"/chatUser.%s.upload",
					chatUserImageType.name ()),
				"upload file");

		}

	}

}
