package wbs.console.forms;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.misc.ConsoleUserHelper;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("timestampFormFieldInterfaceMapping")
public
class TimestampFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, Instant, String> {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper preferences;

	// properties

	@Getter @Setter
	String name;

	@Getter @Setter
	TimestampFormFieldSpec.Format format;

	// implementation

	@Override
	public
	Either <Optional <Instant>, String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> interfaceValue) {

		if (
			enumNotEqualSafe (
				format,
				TimestampFormFieldSpec.Format.timestamp)
		) {

			throw new RuntimeException ();

		} else if (

			optionalIsNotPresent (
				interfaceValue)

			|| stringIsEmpty (
				optionalGetRequired (
					interfaceValue))

		) {

			return successResult (
				optionalAbsent ());

		} else {

			try {

				return successResult (
					optionalOf (
						preferences.timestampStringToInstant (
							interfaceValue.get ())));

			} catch (IllegalArgumentException exception) {

				return errorResultFormat (
					"Please enter a valid timestamp for %s",
					name ());

			}

		}

	}

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Instant> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.of (
					""));

		}

		switch (format) {

		case timestamp:

			return successResult (
				Optional.of (
					preferences.timestampWithTimezoneString (
						genericValue.get ())));

		case date:

			return successResult (
				Optional.of (
					preferences.dateStringShort (
						genericValue.get ())));

		case time:

			return successResult (
				Optional.of (
					preferences.timeString (
						genericValue.get ())));

		default:

			throw new RuntimeException ();

		}

	}

}
