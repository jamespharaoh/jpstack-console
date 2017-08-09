package wbs.imchat.api;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.doesNotContain;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.underscoreToHyphen;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.api.mvc.ApiAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;

import wbs.imchat.model.ImChatCustomerDetailTypeRec;
import wbs.imchat.model.ImChatCustomerDetailValueObjectHelper;
import wbs.imchat.model.ImChatCustomerDetailValueRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatObjectHelper;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatSessionObjectHelper;
import wbs.imchat.model.ImChatSessionRec;
import wbs.web.context.RequestContext;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("imChatDetailsUpdateApiAction")
public
class ImChatDetailsUpdateApiAction
	implements ApiAction {

	// dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	ImChatApiLogic imChatApiLogic;

	@SingletonDependency
	ImChatCustomerDetailValueObjectHelper imChatCustomerDetailValueHelper;

	@SingletonDependency
	ImChatObjectHelper imChatHelper;

	@SingletonDependency
	ImChatSessionObjectHelper imChatSessionHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	public
	Optional <WebResponder> handle (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handler");

		) {

			// decode request

			DataFromJson dataFromJson =
				new DataFromJson ();

			ImChatDetailsUpdateRequest detailsUpdateRequest =
				dataFromJson.fromJson (
					ImChatDetailsUpdateRequest.class,
					requestContext.requestBodyString ());

			// lookup objects

			ImChatRec imChat =
				imChatHelper.findRequired (
					transaction,
					parseIntegerRequired (
						requestContext.requestStringRequired (
							"imChatId")));

			// lookup session

			ImChatSessionRec session =
				imChatSessionHelper.findBySecret (
					transaction,
					detailsUpdateRequest.sessionSecret ());

			ImChatCustomerRec customer =
				session.getImChatCustomer ();

			if (

				isNull (
					session)

				|| ! session.getActive ()

				|| referenceNotEqualWithClass (
					ImChatRec.class,
					customer.getImChat (),
					imChat)

			) {

				return optionalOf (
					imChatApiLogic.failureResponseFormat (
						transaction,
						"session-invalid",
						"The session secret is invalid or the session is no ",
						"longer active"));

			}

			// update details

			for (
				ImChatCustomerDetailTypeRec detailType
					: imChat.getCustomerDetailTypes ()
			) {

				if (
					doesNotContain (
						detailsUpdateRequest.details ().keySet (),
						underscoreToHyphen (
							detailType.getCode ()))
				) {
					continue;
				}

				String stringValue =
					detailsUpdateRequest.details ().get (
						underscoreToHyphen (
							detailType.getCode ()));

				ImChatCustomerDetailValueRec detailValue =
					customer.getDetails ().get (
						detailType.getId ());

				if (
					isNotNull (
						detailValue)
				) {

					if (
						stringEqualSafe (
							detailValue.getValue (),
							stringValue)
					) {
						continue;
					}

					detailValue

						.setValue (
							stringValue);

				} else {

					detailValue =
						imChatCustomerDetailValueHelper.insert (
							transaction,
							imChatCustomerDetailValueHelper.createInstance ()

						.setImChatCustomer (
							customer)

						.setImChatCustomerDetailType (
							detailType)

						.setValue (
							stringValue)

					);

					customer.getDetails ().put (
						detailType.getId (),
						detailValue);

				}

				eventLogic.createEvent (
					transaction,
					"im_chat_customer_detail_updated",
					customer,
					detailType,
					stringValue);

			}

			customer

				.setDetailsCompleted (
					true);

			// create response

			ImChatDetailsUpdateSuccess successResponse =
				new ImChatDetailsUpdateSuccess ()

				.customer (
					imChatApiLogic.customerData (
						transaction,
						customer));

			// commit and return

			transaction.commit ();

			return optionalOf (
				jsonResponderProvider.provide (
					transaction,
					jsonResponder ->
						jsonResponder

				.value (
					successResponse)

			));

		}

	}

}