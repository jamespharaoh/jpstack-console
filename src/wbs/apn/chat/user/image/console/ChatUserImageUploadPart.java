package wbs.apn.chat.user.image.console;

import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostActionMultipart;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.apn.chat.user.image.model.ChatUserImageType;
import wbs.console.forms.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatUserImageUploadPart")
public
class ChatUserImageUploadPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	ConsoleModule chatUserImageConsoleModule;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	// state

	FormFieldSet formFieldSet;

	ChatUserImageType chatUserImageType;

	ChatUserImageUploadForm uploadForm;

	// implementation

	@Override
	public
	void prepare () {

		formFieldSet =
			chatUserImageConsoleModule.formFieldSets ().get (
				"uploadForm");

		chatUserImageType =
			toEnum (
				ChatUserImageType.class,
				(String) requestContext.stuff ("chatUserImageType"));

		uploadForm =
			new ChatUserImageUploadForm ();

		if (requestContext.post ()) {

			formFieldLogic.update (
				requestContext,
				formFieldSet,
				uploadForm,
				ImmutableMap.of (),
				"upload");

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlParagraphWrite (
			"Please upload the photo or video.");

		// form open

		htmlFormOpenPostActionMultipart (
			requestContext.resolveLocalUrl (
				stringFormat (
					"/chatUser.%s.upload",
					chatUserImageType.name ())));

		// form fields

		htmlTableOpenDetails ();

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			formFieldSet,
			Optional.absent (),
			uploadForm,
			ImmutableMap.of (),
			FormType.perform,
			"upload");

		htmlTableClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"upload file\"",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

}
