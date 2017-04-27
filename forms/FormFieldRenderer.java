package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import fj.data.Either;

public
interface FormFieldRenderer <Container, Interface> {

	default
	boolean fileUpload () {
		return false;
	}

	default
	FormField.Align listAlign () {
		return FormField.Align.left;
	}

	default
	FormField.Align propertiesAlign () {
		return FormField.Align.left;
	}

	void renderFormTemporarilyHidden (
			FormFieldSubmission submission,
			FormatWriter htmlWriter,
			Container container,
			Map <String, Object> hints,
			Optional <Interface> interfaceValue,
			FormType formType,
			String formName);

	void renderFormInput (
			TaskLogger parentTaskLogger,
			FormFieldSubmission submission,
			FormatWriter htmlWriter,
			Container container,
			Map <String, Object> hints,
			Optional <Interface> interfaceValue,
			FormType formType,
			String formName);

	void renderFormReset (
			TaskLogger parentTaskLogger,
			FormatWriter htmlWriter,
			Container container,
			Optional <Interface> interfaceValue,
			String formName);

	default
	void renderHtmlTableCellList (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		htmlTableCellOpen (
			htmlStyleRuleEntry (
				"text-align",
				listAlign ().name ()),
			htmlColumnSpanAttribute (
				colspan),
			htmlClassAttribute (
				presentInstances (
					htmlClass (
						interfaceValue))));

		renderHtmlSimple (
			parentTaskLogger,
			htmlWriter,
			container,
			hints,
			interfaceValue,
			link);

		htmlTableCellClose ();

	}

	default
	void renderHtmlTableCellProperties (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue,
			@NonNull Boolean link,
			@NonNull Long colspan) {

		htmlTableCellOpen (
			htmlStyleRuleEntry (
				"text-align",
				propertiesAlign ().name ()),
			htmlColumnSpanAttribute (
				colspan));

		renderHtmlSimple (
			parentTaskLogger,
			htmlWriter,
			container,
			hints,
			interfaceValue,
			link);

		htmlTableCellClose ();

	}

	void renderHtmlSimple (
			TaskLogger parentTaskLogger,
			FormatWriter htmlWriter,
			Container container,
			Map <String, Object> hints,
			Optional <Interface> interfaceValue,
			boolean link);

	default
	void renderHtmlComplex (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Interface> interfaceValue) {

		renderHtmlSimple (
			parentTaskLogger,
			htmlWriter,
			container,
			hints,
			interfaceValue,
			true);

	}

	boolean formValuePresent (
			FormFieldSubmission submission,
			String formName);

	default
	Either <Optional <Interface>, String> formToInterface (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		throw new UnsupportedOperationException ();

	}

	default
	Optional <String> htmlClass (
			@NonNull Optional <Interface> interfaceValue) {

		return optionalAbsent ();

	}

}
