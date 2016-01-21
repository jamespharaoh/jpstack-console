package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormField.FormType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@PrototypeComponent ("textFormFieldRenderer")
@Accessors (fluent = true)
public
class TextFormFieldRenderer<Container>
	implements FormFieldRenderer<Container,String> {

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	String label;

	@Getter @Setter
	Boolean nullable;

	@Getter @Setter
	Align align;

	@Getter @Setter
	Map<String,String> presets =
		new LinkedHashMap<> ();

	// details

	@Getter
	boolean fileUpload = false;

	// utilities

	public
	TextFormFieldRenderer<Container> addPreset (
			@NonNull String preset) {

		presets.put (
			preset,
			preset);

		return this;

	}

	// implementation

	@Override
	public
	void renderTableCellList (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			boolean link,
			int colspan) {

		out.writeFormat (
			"<td",

			colspan > 1
				? stringFormat (
					" colspan=\"%h\"",
					colspan)
				: "",

			"%s",
			align != null
				? stringFormat (
					" style=\"text-align: %h\"",
					align.toString ())
				: "",

			">%s</td>\n",
			interfaceToHtmlSimple (
				container,
				interfaceValue,
				link));

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		out.writeFormat (
			"<td>%s</td>\n",
			interfaceToHtmlComplex (
				container,
				interfaceValue));

	}

	@Override
	public
	void renderTableRow (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label ());

		renderTableCellProperties (
			out,
			container,
			interfaceValue);

		out.writeFormat (
			"</tr>\n");

	}

	@Override
	public
	void renderFormRow (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull Optional<String> error,
			@NonNull FormType formType) {

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label (),
			"<td>");

		renderFormInput (
			submission,
			out,
			container,
			interfaceValue,
			formType);

		if (
			isPresent (
				error)
		) {

			out.writeFormat (
				"<br>\n",
				"%h",
				error.get ());

		}

		out.writeFormat (
			"</td>\n",
			"</tr>\n");

	}

	@Override
	public
	void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull FormType formType) {

		out.writeFormat (
			"<input",
			" type=\"text\"",
			" id=\"%h\"",
			name (),
			" name=\"%h\"",
			name (),
			" value=\"%h\"",
			formValuePresent (
					submission)
				? formValue (
					submission)
				: interfaceValue.or (
					""),
			" size=\"%h\"",
			FormField.defaultSize,
			">");

		if (
			isNotEmpty (
				presets ())
		) {

			out.writeFormat (
				"<br>");

			for (
				Map.Entry<String,String> presetEntry
					: presets ().entrySet ()
			) {

				out.writeFormat (
					"\n<button",
					" onclick=\"%h\"",
					stringFormat (
						"$('#%j').val ('%j'); return false",
						name (),
						presetEntry.getValue ()),
					">%h</button>",
					presetEntry.getKey ());

			}

		}

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			@NonNull FormType formType) {

		if (
			in (
				formType,
				FormType.create,
				FormType.perform,
				FormType.search)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j\").val (\"\");\n",
				indent,
				name);

		} else if (
			in (
				formType,
				FormType.update)
		) {

			javascriptWriter.writeFormat (
				"%s$(\"#%j\").val (\"%j\");\n",
				indent,
				name,
				interfaceValue.or (""));

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission) {

		return submission.hasParameter (
			name ());

	}

	String formValue (
			@NonNull FormFieldSubmission submission) {

		return submission.parameter (
			name ());

	}

	@Override
	public
	Either<Optional<String>,String> formToInterface (
			@NonNull FormFieldSubmission submission) {

		String formValue =
			formValue (
				submission);

		if (

			nullable ()

			&& equal (
				formValue,
				"")

		) {

			return successResult (
				Optional.<String>absent ());

		}

		return successResult (
			Optional.fromNullable (
				formValue));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue,
			boolean link) {

		return stringFormat (
			"%h",
			interfaceValue.or (""));

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Optional<String> interfaceValue) {

		return interfaceToHtmlSimple (
			container,
			interfaceValue,
			true);

	}

	// data

	public static
	enum Align {
		left,
		center,
		right;
	}

}
