package wbs.api.mvc;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;
import java.io.Writer;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

import wbs.web.context.RequestContext;
import wbs.web.mvc.WebNotFoundHandler;

@SingletonComponent ("apiNotFoundHandler")
public
class ApiNotFoundHandler
	implements WebNotFoundHandler {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	public
	void handleNotFound (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleNotFound");

		) {

			// log it normally

			taskLogger.errorFormat (
				"Path not found: %s",
				requestContext.requestUri ());

			// create an exception log

			try {

				String path =
					stringFormat (
						"%s%s",
						requestContext.servletPath (),
						optionalOrEmptyString (
							requestContext.pathInfo ()));

				exceptionLogger.logSimple (
					taskLogger,
					"webapi",
					path,
					"Not found",
					"The specified path was not found",
					optionalAbsent (),
					GenericExceptionResolution.ignoreWithThirdPartyWarning);

			} catch (RuntimeException exception) {

				throw taskLogger.fatalFormat (
					"Error creating not found log: %s",
					exception.getMessage ());

			}

			// return an error

			requestContext.sendError (
				404l);

			try (

				Writer writer =
					requestContext.writer ();

				FormatWriter formatWriter =
					new WriterFormatWriter (
						writer);

			) {

				formatWriter.writeLineFormat (
					"404 Not found");

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

	}

}
