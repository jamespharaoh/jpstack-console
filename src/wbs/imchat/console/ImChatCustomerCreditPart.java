package wbs.imchat.console;

import static wbs.utils.etc.Misc.max;
import static wbs.utils.etc.OptionalUtils.ifNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;

import java.util.List;

import javax.inject.Named;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import wbs.console.forms.FormField.FormType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleModule;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.imchat.model.ImChatCustomerCreditRec;
import wbs.imchat.model.ImChatCustomerRec;

@PrototypeComponent ("imChatCustomerCreditPart")
public
class ImChatCustomerCreditPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	@Named
	ConsoleModule imChatCustomerConsoleModule;

	@SingletonDependency
	ImChatCustomerConsoleHelper imChatCustomerHelper;

	@SingletonDependency
	ImChatCustomerCreditConsoleHelper imChatCustomerCreditHelper;

	// state

	FormFieldSet customerFormFields;
	FormFieldSet creditFormFields;
	FormFieldSet creditHistoryFormFields;

	ImChatCustomerRec customer;
	List<ImChatCustomerCreditRec> creditHistory;

	ImChatCustomerCreditRequest request;
	Optional<UpdateResultSet> updateResultSet;

	// implementation

	@Override
	public
	void prepare () {

		customerFormFields =
			imChatCustomerConsoleModule.formFieldSets ().get (
				"credit-summary");

		creditFormFields =
			imChatCustomerConsoleModule.formFieldSets ().get (
				"credit-request");

		creditHistoryFormFields =
			imChatCustomerConsoleModule.formFieldSets ().get (
				"credit-history");

		request =
			ifNotPresent (

			optionalCast (
				ImChatCustomerCreditRequest.class,
				requestContext.request (
					"imChatCustomerCreditRequest")),

			Optional.of (
				new ImChatCustomerCreditRequest ())

		);

		request.customer (
			imChatCustomerHelper.findRequired (
				requestContext.stuffInteger (
					"imChatCustomerId")));

		updateResultSet =
			optionalCast (
				UpdateResultSet.class,
				requestContext.request (
					"imChatCustomerCreditUpdateResults"));

		creditHistory =
			imChatCustomerCreditHelper.findByIndexRange (
				request.customer (),
				max (0l, request.customer.getNumCredits () - 10l),
				request.customer.getNumCredits ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		requestContext.flushNotices ();

		htmlHeadingTwoWrite (
			"Customer details");

		formFieldLogic.outputDetailsTable (
			formatWriter,
			customerFormFields,
			request.customer (),
			ImmutableMap.of ());

		htmlHeadingTwoWrite (
			"Apply credit");

		formFieldLogic.outputFormTable (
			requestContext,
			formatWriter,
			creditFormFields,
			updateResultSet,
			request,
			ImmutableMap.of (),
			"post",
			requestContext.resolveLocalUrl (
				"/imChatCustomer.credit"),
			"apply credit",
			FormType.perform,
			"credit");

		htmlHeadingTwoWrite (
			"Recent credit history");

		formFieldLogic.outputListTable (
			formatWriter,
			creditHistoryFormFields,
			Lists.reverse (
				creditHistory),
			true);

	}

}