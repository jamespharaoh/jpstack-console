package wbs.console.forms;

import static wbs.utils.etc.LogicUtils.equalSafe;
import static wbs.utils.etc.Misc.eitherGetLeft;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NumberUtils.equalToOne;
import static wbs.utils.etc.NumberUtils.equalToTwo;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.isError;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitColon;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("hiddenFormField")
@DataClass ("hidden-form-field")
public
class HiddenFormField <Container, Generic, Native>
	implements FormField <Container, Generic, Native, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

	@Getter
	Boolean virtual = false;

	@Getter
	Boolean group = false;

	@DataAttribute
	@Getter @Setter
	Boolean large = false;

	@DataAttribute
	@Getter @Setter
	Boolean fileUpload = false;

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	Optional <Optional <Generic>> implicitValue;

	@DataAttribute
	@Getter @Setter
	String viewPriv;

	@Getter @Setter
	Set <ScriptRef> scriptRefs =
		new LinkedHashSet<> ();

	@Getter @Setter
	FormFieldAccessor <Container, Native> accessor;

	@Getter @Setter
	FormFieldNativeMapping <Container, Generic, Native> nativeMapping;

	@Getter @Setter
	FormFieldInterfaceMapping <Container, Generic, String> csvMapping;

	// implementation

	@Override
	public
	boolean canView (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canView");

		) {

			if (
				isNull (
					viewPriv)
			) {
				return true;
			}

			List <String> privParts =
				stringSplitColon (
					viewPriv);

			if (
				equalToOne (
					privParts.size ())
			) {

				String privCode =
					privParts.get (0);

				return privChecker.canRecursive (
					transaction,
					(Record <?>) container,
					privCode);

			} else if (
				equalToTwo (
					privParts.size ())
			) {

				String delegatePath =
					privParts.get (0);

				String privCode =
					privParts.get (1);

				Record <?> delegate =
					genericCastUnchecked (
						objectManager.dereferenceRequired (
							transaction,
							container,
							delegatePath,
							hints));

				return privChecker.canRecursive (
					transaction,
					delegate,
					privCode);

			} else {

				throw new RuntimeException ();

			}

		}

	}

	@Override
	public
	void renderFormAlwaysHidden (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormAlwaysHidden");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <String> interfaceValue =
				requiredValue (
					eitherGetLeft (
						csvMapping.genericToInterface (
							transaction,
							container,
							hints,
							genericValue)));

			if (
				formValuePresent (
					submission,
					formName)
			) {

				interfaceValue =
					Optional.of (
						formToInterface (
							submission,
							formName));

			}

			htmlWriter.writeFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"%h-%h\"",
				formName,
				name (),
				" value=\"%h\"",
				interfaceValue.or (""),
				">\n");

		}

	}

	@Override
	public
	void implicit (
			@NonNull Transaction parentTransaction,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"implicit");

		) {

			if (
				optionalIsNotPresent (
					implicitValue)
			) {
				return;
			}

			Optional <Native> nativeValue =
				requiredValue (
					nativeMapping.genericToNative (
						transaction,
						container,
						implicitValue.get ()));

			accessor.write (
				transaction,
				container,
				nativeValue);

		}

	}

	@Override
	public
	UpdateResult <Generic, Native> update (
			@NonNull Transaction parentTransaction,
			@NonNull FormFieldSubmission submission,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"update");

		) {

			// do nothing if no value present in form

			if (
				! formValuePresent (
					submission,
					formName)
			) {

				return new UpdateResult<Generic,Native> ()

					.updated (
						false)

					.error (
						Optional.<String>absent ());

			}

			// get interface value from form

			String newInterfaceValue =
				formToInterface (
					submission,
					formName);

			// convert to generic

			Either <Optional <Generic>, String> interfaceToGenericResult =
				csvMapping.interfaceToGeneric (
					transaction,
					container,
					hints,
					optionalOf (
						newInterfaceValue));

			if (
				isError (
					interfaceToGenericResult)
			) {

				return new UpdateResult<Generic,Native> ()

					.updated (
						false)

					.error (
						Optional.of (
							interfaceToGenericResult.right ().value ()));

			}

			Optional <Generic> newGenericValue =
				interfaceToGenericResult.left ().value ();

			// convert to native

			Optional <Native> newNativeValue =
				requiredValue (
					nativeMapping.genericToNative (
						transaction,
						container,
						newGenericValue));

			// get the current value, if it is the same, do nothing

			Optional <Native> oldNativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> oldGenericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						oldNativeValue));

			if (
				equalSafe (
					oldGenericValue,
					newGenericValue)
			) {

				return new UpdateResult <Generic, Native> ()

					.updated (
						false)

					.error (
						Optional.absent ());

			}

			// set the new value

			accessor.write (
				transaction,
				container,
				newNativeValue);

			return new UpdateResult<Generic,Native> ()

				.updated (
					true)

				.oldGenericValue (
					oldGenericValue)

				.newGenericValue (
					newGenericValue)

				.oldNativeValue (
					oldNativeValue)

				.newNativeValue (
					newNativeValue)

				.error (
					optionalAbsent ());

		}

	}

	private
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.hasParameter (
			stringFormat (
				"%h-%h",
				formName,
				name ()));

	}

	private
	String formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.parameter (
			stringFormat (
				"%h-%h",
				formName,
				name ()));

	}

}
