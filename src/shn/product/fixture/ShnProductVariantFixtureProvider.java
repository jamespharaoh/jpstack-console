package shn.product.fixture;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.io.FileUtils.fileReaderBuffered;
import static wbs.utils.string.PlaceholderUtils.placeholderMapCurlyBraces;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.FixturesLogic;
import wbs.framework.logging.LogContext;

import wbs.platform.event.logic.EventFixtureLogic;

import wbs.utils.csv.CsvReader;
import wbs.utils.io.SafeBufferedReader;

import shn.core.model.ShnDatabaseObjectHelper;
import shn.core.model.ShnDatabaseRec;
import shn.product.model.ShnProductVariantTypeObjectHelper;
import shn.product.model.ShnProductVariantTypeRec;
import shn.product.model.ShnProductVariantValueObjectHelper;

public
class ShnProductVariantFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@SingletonDependency
	FixturesLogic fixturesLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShnProductVariantTypeObjectHelper productVariantTypeHelper;

	@SingletonDependency
	ShnProductVariantValueObjectHelper productVariantValueHelper;

	@SingletonDependency
	ShnDatabaseObjectHelper shnDatabaseHelper;

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

			createProductVariantTypes (
				transaction);

			createProductVariantValues (
				transaction);

		}

	}

	// private implementation

	private
	void createProductVariantTypes (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createProductVariantTypes");

			SafeBufferedReader reader =
				fileReaderBuffered (
					"config/test-data/shn-product-variant-types.csv");

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
				Map <String, String> lineMap
					: csvReader.readAsMap (
						productVariantTypeColumnNames,
						reader)
			) {

				Map <String, String> productVariantTypeParams =
					placeholderMapCurlyBraces (
						productVariantTypeColumnMap,
						fixturesLogic.placeholderFunction (
							emptyMap (),
							lineMap));

				eventFixtureLogic.createRecordAndEvents (
					transaction,
					"SHN product variant",
					productVariantTypeHelper,
					shnDatabase,
					productVariantTypeParams,
					emptySet ());

			}

			transaction.flush ();

		}

	}

	private
	void createProductVariantValues (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createProductVariantValues");

			SafeBufferedReader reader =
				fileReaderBuffered (
					"config/test-data/shn-product-variant-values.csv");

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
				Map <String, String> lineMap
					: csvReader.readAsMap (
						productVariantValueColumnNames,
						reader)
			) {

				ShnProductVariantTypeRec productVariantType =
					productVariantTypeHelper.findByCodeRequired (
						transaction,
						shnDatabase,
						mapItemForKeyRequired (
							lineMap,
							"type-code"));

				Map <String, String> productVariantValueParams =
					placeholderMapCurlyBraces (
						productVariantValueColumnMap,
						fixturesLogic.placeholderFunction (
							emptyMap (),
							lineMap));

				eventFixtureLogic.createRecordAndEvents (
					transaction,
					"SHN product variant",
					productVariantValueHelper,
					productVariantType,
					productVariantValueParams,
					emptySet ());

			}

			transaction.flush ();

		}

	}

	// data

	private final static
	List <String> productVariantTypeColumnNames =
		ImmutableList.of (
			"code",
			"description",
			"public-title");

	private final static
	List <String> productVariantValueColumnNames =
		ImmutableList.of (
			"type-code",
			"code",
			"description",
			"public-title");

	private final static
	Map <String, String> productVariantTypeColumnMap =
		ImmutableMap.<String, String> builder ()

		.put (
			"code",
			"{code}")

		.put (
			"description",
			"{description}")

		.put (
			"public-title",
			"{public-title}")

		.build ()

	;

	private final static
	Map <String, String> productVariantValueColumnMap =
		ImmutableMap.<String, String> builder ()

		.put (
			"code",
			"{code}")

		.put (
			"description",
			"{description}")

		.put (
			"public-title",
			"{public-title}")

		.build ()

	;

}