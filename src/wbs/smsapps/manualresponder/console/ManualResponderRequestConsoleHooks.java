package wbs.smsapps.manualresponder.console;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHooks;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestSearch;

@SingletonComponent ("manualResponderRequestConsoleHooks")
public
class ManualResponderRequestConsoleHooks
	implements ConsoleHooks <ManualResponderRequestRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderConsoleHelper manualResponderHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull Transaction parentTransaction,
			@NonNull Object searchObject) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"applySearchFilter");

		) {

			ManualResponderRequestSearch search =
				genericCastUnchecked (
					searchObject);

			search

				.filter (
					true);

			// manual responders

			ImmutableList.Builder <Long> manualRespondersBuilder =
				ImmutableList.builder ();

			for (
				ManualResponderRec manualResponder
					: manualResponderHelper.findAll (
						transaction)
			) {

				if (
					! privChecker.canRecursive (
						transaction,
						manualResponder,
						"supervisor")
				) {
					continue;
				}

				manualRespondersBuilder.add (
					manualResponder.getId ());

			}

			// users

			ImmutableList.Builder <Long> usersBuilder =
				ImmutableList.builder ();

			for (
				UserRec user
					: userHelper.findAll (
						transaction)
			) {

				if (
					! privChecker.canRecursive (
						transaction,
						user,
						"supervisor")
				) {
					continue;
				}

				usersBuilder.add (
					user.getId ());

			}

			search

				.filterManualResponderIds (
					manualRespondersBuilder.build ())

				.filterProcessedByUserIds (
					usersBuilder.build ());

		}

	}

}