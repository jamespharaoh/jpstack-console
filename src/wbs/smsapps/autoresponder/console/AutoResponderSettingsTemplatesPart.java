package wbs.smsapps.autoresponder.console;

import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("autoResponderSettingsTemplatesPart")
public
class AutoResponderSettingsTemplatesPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

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

			htmlParagraphWriteFormat (
				formatWriter,
				"This feature is under development and will be available ",
				"soon.");

		}

	}

}