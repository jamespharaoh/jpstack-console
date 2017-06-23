package shn.show.fixture;

import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.io.FileUtils.fileReaderBuffered;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

import wbs.utils.csv.CsvReader;
import wbs.utils.io.SafeBufferedReader;

import shn.core.model.ShnDatabaseObjectHelper;
import shn.core.model.ShnDatabaseRec;
import shn.show.model.ShnShowGuestObjectHelper;
import shn.show.model.ShnShowGuestRec;
import shn.show.model.ShnShowPresenterObjectHelper;
import shn.show.model.ShnShowPresenterRec;
import shn.show.model.ShnShowTypeObjectHelper;
import shn.show.model.ShnShowTypeRec;

public
class ShnShowFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	ShnDatabaseObjectHelper shnDatabaseHelper;

	@SingletonDependency
	ShnShowGuestObjectHelper showGuestHelper;

	@SingletonDependency
	ShnShowPresenterObjectHelper showPresenterHelper;

	@SingletonDependency
	ShnShowTypeObjectHelper showTypeHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// public implementation

	@Override
	public
	void createFixtures (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createFixtures");

		) {

			createPresenters (
				transaction);

			createGuests (
				transaction);

			createShowTypes (
				transaction);

		}

	}

	// private implementation

	private
	void createPresenters (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createPresenters");

			SafeBufferedReader reader =
				fileReaderBuffered (
					"config/test-data/shn-show-presenters.csv");

		) {

			CsvReader csvReader =
				new CsvReader ()

				.skipHeader (
					true);

			ShnDatabaseRec shnDatabase =
				shnDatabaseHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					wbsConfig.defaultSlice (),
					"test");

			for (
				List <String> line
					: csvReader.readAsList (
						reader)
			) {

				String name =
					listItemAtIndexRequired (
						line,
						0l);

				String code =
					simplifyToCodeRequired (
						name);

				ShnShowPresenterRec showPresenter =
					showPresenterHelper.insert (
						transaction,
						showPresenterHelper.createInstance ()

					.setDatabase (
						shnDatabase)

					.setCode (
						code)

					.setName (
						name)

					.setDescription (
						name)

				);

				eventFixtureLogic.createEvents (
					transaction,
					"SHN Show",
					shnDatabase,
					showPresenter,
					ImmutableMap.<String, Object> builder ()

					.put (
						"code",
						code)

					.put (
						"name",
						name)

					.put (
						"description",
						name)

					.build ()

				);

			}

			transaction.flush ();

		}

	}

	private
	void createGuests (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createGuests");

			SafeBufferedReader reader =
				fileReaderBuffered (
					"config/test-data/shn-show-guests.csv");

		) {

			CsvReader csvReader =
				new CsvReader ()

				.skipHeader (
					true);

			ShnDatabaseRec shnDatabase =
				shnDatabaseHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					wbsConfig.defaultSlice (),
					"test");

			for (
				List <String> line
					: csvReader.readAsList (
						reader)
			) {

				String name =
					listItemAtIndexRequired (
						line,
						0l);

				String code =
					simplifyToCodeRequired (
						name);

				ShnShowGuestRec showGuest =
					showGuestHelper.insert (
						transaction,
						showGuestHelper.createInstance ()

					.setDatabase (
						shnDatabase)

					.setCode (
						code)

					.setName (
						name)

					.setDescription (
						name)

				);

				eventFixtureLogic.createEvents (
					transaction,
					"SHN Show",
					shnDatabase,
					showGuest,
					ImmutableMap.<String, Object> builder ()

					.put (
						"code",
						code)

					.put (
						"name",
						name)

					.put (
						"description",
						name)

					.build ()

				);

			}

			transaction.flush ();

		}

	}

	private
	void createShowTypes (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createShowTypes");

			SafeBufferedReader reader =
				fileReaderBuffered (
					"config/test-data/shn-show-types.csv");

		) {

			CsvReader csvReader =
				new CsvReader ()

				.skipHeader (
					true);

			ShnDatabaseRec shnDatabase =
				shnDatabaseHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					wbsConfig.defaultSlice (),
					"test");

			for (
				List <String> line
					: csvReader.readAsList (
						reader)
			) {

				String code =
					listItemAtIndexRequired (
						line,
						0l);

				String description =
					listItemAtIndexRequired (
						line,
						1l);

				ShnShowTypeRec showType =
					showTypeHelper.insert (
						transaction,
						showTypeHelper.createInstance ()

					.setDatabase (
						shnDatabase)

					.setCode (
						code)

					.setDescription (
						description)

				);

				eventFixtureLogic.createEvents (
					transaction,
					"SHN Show",
					shnDatabase,
					showType,
					ImmutableMap.<String, Object> builder ()

					.put (
						"code",
						code)

					.put (
						"description",
						description)

					.build ()

				);

			}

			transaction.flush ();

		}

	}

}