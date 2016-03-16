package wbs.smsapps.manualresponder.console;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.misc.TimeFormatter;
import wbs.console.module.ConsoleManager;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.UrlParams;
import wbs.platform.user.model.UserObjectHelper;
import wbs.smsapps.manualresponder.console.ManualResponderSharedReportSimplePart.SearchForm;
import wbs.smsapps.manualresponder.model.ManualResponderReportObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

@PrototypeComponent ("manualResponderSharedReportOperatorPart")
public
class ManualResponderSharedReportOperatorPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	FormFieldLogic formFieldLogic;

	@Inject @Named
	ConsoleModule manualResponderSharedReportConsoleModule;

	@Inject
	ManualResponderReportObjectHelper manualResponderReportHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserObjectHelper userHelper;

	// state

	FormFieldSet searchFormFieldSet;
	FormFieldSet resultsFormFieldSet;

	SearchForm searchForm;

	List<ManualResponderReportRec> reports;
	List<Integer> users;
	List<Long> num_count;

	String outputTypeParam;

	// implementation

	@Override
	public
	void prepare () {

		searchFormFieldSet =
			manualResponderSharedReportConsoleModule.formFieldSets ().get (
				"simpleReportSearch");

		resultsFormFieldSet =
			manualResponderSharedReportConsoleModule.formFieldSets ().get (
				"simpleReportResults");

		// get search form

		LocalDate today =
			LocalDate.now ();

		Interval todayInterval =
			today.toInterval ();

		searchForm =
			new SearchForm ()

			.start (
				todayInterval.getStart ().toInstant ())

			.end (
				todayInterval.getEnd ().toInstant ());

		formFieldLogic.update (
			requestContext,
			searchFormFieldSet,
			searchForm,
			ImmutableMap.of (),
			"report");

		// perform search

		reports =
			manualResponderReportHelper.findByProcessedTime (
				new Interval (
					searchForm.start (),
					searchForm.end ()))

			.stream ()

			.filter (report ->
				privChecker.canRecursive (
					report.getManualResponder (),
					"supervisor")
				|| privChecker.canRecursive (
					report.getProcessedByUser (),
					"manage"))

			.collect (
				Collectors.toList ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		// search form

		printFormat (
			"<form method=\"get\">\n");

		printFormat (
			"<table class=\"details\">\n");

		formFieldLogic.outputFormRows (
			requestContext,
			formatWriter,
			searchFormFieldSet,
			Optional.absent (),
			searchForm,
			ImmutableMap.of (),
			FormType.search,
			"report");

		printFormat (
			"<tr>\n",
			"<th>Actions</th>\n",
			"<td><input",
			" type=\"submit\"",
			" value=\"search\"",
			"></td>\n",
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

		UrlParams urlParams =
			new UrlParams ()

			.set (
				"start",
				timeFormatter.instantToTimestampString  (
					timeFormatter.defaultTimezone (),
					searchForm.start ()))

			.set (
				"end",
				timeFormatter.instantToTimestampString  (
					timeFormatter.defaultTimezone (),
					searchForm.end ()));

		printFormat (
			"<p><a",
			" href=\"%h\"",
			urlParams.toUrl (
				requestContext.resolveLocalUrl (
					"/manualResponderSharedReport.operatorCsv")),
			">Download CSV File</a></p>\n");

		List<Integer> users =
			new ArrayList<> ();

		List<Long> numCount =
			new ArrayList<> ();

		for (
			ManualResponderReportRec report
				: reports
		) {

			Integer userId =
				report.getUser ().getId ();

			Long num =
				report.getNum ();

			if (users.contains (userId)){

				Integer index =
					users.indexOf (userId);

				numCount.set (
					index,
					num + numCount.get (index));

			} else {

				users.add (
					userId);

				numCount.add (
					(long) num);

			}

		}

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<th>Operator</th>\n",
			"<th>Count</th>");

		/*formFieldLogic.outputTableHeadings (
				out,
				resultsFormFieldSet);*/

		for (
			Integer user
				: users
		) {

			printFormat (
				"<tr>\n");

			printFormat (
					"%s\n",
					objectManager.tdForObjectMiniLink (
						//manualResponderHelper.find(service)));
						userHelper.find (user)));

			/*
			printFormat (
				"<td>%h</td>\n",
				userHelper.find (user).getUsername ());
			*/

			printFormat (
				"<td>%h</td>\n",
				numCount.get (
					users.indexOf (user)
				).toString ());

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

		printFormat (
			"<p><a",
			" href=\"%h\"",
			urlParams.toUrl (
				requestContext.resolveLocalUrl (
					"/manualResponderSharedReport.operatorCsv")),
			">Download CSV File</a></p>\n");

	}

}