package wbs.console.formaction;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.web.utils.HtmlAttributeUtils.htmlTargetAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.core.ConsoleForm;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("consoleFormActionPart")
public
class ConsoleFormActionPart <FormState, History>
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	ConsoleFormActionHelper <FormState, History> helper;

	@Getter @Setter
	ConsoleFormType <FormState> actionFormType;

	@Getter @Setter
	String name;

	@Getter @Setter
	String heading;

	@Getter @Setter
	String helpText;

	@Getter @Setter
	String submitLabel;

	@Getter @Setter
	String localFile;

	@Getter @Setter
	String historyHeading;

	@Getter @Setter
	ConsoleFormType <History> historyFormType;

	// state

	ConsoleForm <FormState> actionForm;
	ConsoleForm <History> historyForm;

	List <History> history;

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

			// prepare action

			Map <String, Object> formHints =
				helper.formHints (
					transaction);

			FormState formState =
				helper.constructFormState (
					transaction);

			helper.updatePassiveFormState (
				transaction,
				formState);

			actionForm =
				actionFormType.buildResponse (
					transaction,
					formHints,
					formState);

			actionForm.setDefaults (
				transaction);

			// prepare history

			if (
				isNotNull (
					historyFormType)
			) {

				historyForm =
					historyFormType.buildResponse (
						transaction,
						formHints,
						helper.history (
							transaction));

			}

		}

	}

	@Override
	public
	Set <HtmlLink> links () {

		return actionForm.styles ();

	}

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return actionForm.scriptRefs ();

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

			// write heading

			htmlHeadingTwoWrite (
				formatWriter,
				heading);

			// write help

			if (
				isNotNull (
					helpText)
			) {

				htmlParagraphWrite (
					formatWriter,
					helpText);

			}

			// write preamble

			helper.writePreamble (
				transaction,
				formatWriter,
				isNotNull (
					submitLabel));

			// write form

			if (
				isNotNull (
					submitLabel)
			) {

				actionForm.outputFormTable (
					transaction,
					formatWriter,
					"post",
					requestContext.resolveLocalUrl (
						localFile),
					submitLabel,
					presentInstances (
						optionalMapRequired (
							helper.formTarget (),
							htmlTargetAttribute ())));

			}

			// write history

			if (
				isNotNull (
					historyForm)
			) {

				htmlHeadingTwoWrite (
					formatWriter,
					historyHeading);

				historyForm.outputListTable (
					transaction,
					formatWriter,
					true);

			}

		}

	}

}
