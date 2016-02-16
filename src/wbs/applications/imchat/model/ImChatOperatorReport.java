package wbs.applications.imchat.model;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.record.IdObject;
import wbs.platform.user.model.UserRec;

@Accessors (fluent = true)
@Data
public
class ImChatOperatorReport
	implements IdObject {

	UserRec user;

	Long numFreeMessages;
	Long numBilledMessages;

	@Override
	public
	Integer getId () {
		return user.getId ();
	}

}