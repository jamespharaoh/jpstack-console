package wbs.console.combo;

import javax.inject.Provider;

import wbs.console.module.ConsoleManager;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.Responder;

public abstract
class AbstractConsoleFormActionHelper <FormState>
	implements ConsoleFormActionHelper <FormState> {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	// utility methods

	protected
	Responder responder (
			String responderName) {

		Provider <Responder> responderProvider =
			consoleManager.responder (
				responderName,
				true);

		return responderProvider.get ();

	}

}
