package wbs.console.misc;

import javax.inject.Named;
import javax.inject.Provider;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.config.WbsSpecialConfig;

import wbs.utils.thread.ThreadManager;
import wbs.utils.thread.ThreadManagerImplementation;

import wbs.web.pathhandler.DelegatingPathHandler;
import wbs.web.pathhandler.PathHandler;

@SingletonComponent ("consoleMiscComponents")
public
class ConsoleMiscComponents {

	// unitialized dependencies

	@UninitializedDependency
	Provider <DelegatingPathHandler> delegatingPathHandlerProvider;

	@UninitializedDependency
	Provider <ThreadManagerImplementation> threadManagerImplementationProvider;

	// components

	@SingletonComponent ("threadManager")
	public
	ThreadManager threadManager () {

		return threadManagerImplementationProvider.get ()

			.exceptionTypeCode (
				"console");

	}

	@SingletonComponent ("rootPathHandler")
	@Named
	public
	PathHandler rootPathHandler () {

		return delegatingPathHandlerProvider.get ();

	}

	@SingletonComponent ("wbsSpecialConfig")
	public
	WbsSpecialConfig wbsSpecialConfig () {

		return new WbsSpecialConfig ()

			.assumeNegativeCache (
				false);

	}

}
