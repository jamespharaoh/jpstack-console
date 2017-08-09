package wbs.api.resource;

import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.pathhandler.DelegatingPathHandler;
import wbs.web.pathhandler.PathHandler;

@Accessors (fluent = true)
@PrototypeComponent ("apiVariable")
public
class ApiVariable
	implements PathHandler {

	// dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	RequestContext requestContext;

	@WeakSingletonDependency
	@NamedDependency ("rootPathHandler")
	DelegatingPathHandler delegatingPathHandler;

	// properties

	@Getter @Setter
	String resourceName;

	@Getter @Setter
	String variableName;

	// implementation

	@Override
	public
	WebFile processPath (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String localPath) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processPath");

		) {

			// get value from path

			Matcher localPathMatcher =
				pathPattern.matcher (
					localPath);

			if (! localPathMatcher.matches ())
				throw new RuntimeException ();

			String variableValue =
				requiredValue (
					localPathMatcher.group (1));

			requestContext.request (
				variableName,
				variableValue);

			// continue processing request

			String localPathRest =
				requiredValue (
					localPathMatcher.group (2));

			String pathNext =
				stringFormat (
					"%s",
					resourceName,
					"/{%s}",
					variableName,
					"%s",
					emptyStringIfNull (
						localPathRest));

			return delegatingPathHandler.processPath (
				taskLogger,
				pathNext);

		}

	}

	static
	Pattern pathPattern =
		Pattern.compile (
			"^/([^/]+)(/.+)?$");

}
