package wbs.sms.messageset.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.messageset.model.MessageSetTypeDao;
import wbs.sms.messageset.model.MessageSetTypeRec;

public
class MessageSetTypeDaoHibernate
	extends HibernateDao
	implements MessageSetTypeDao {

	@Override
	public
	List<MessageSetTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType) {

		return findMany (
			MessageSetTypeRec.class,

			createQuery (
				"FROM MessageSetTypeRec messageSetType " +
				"WHERE messageSetType.parentObjectType = :parentObjectType")

			.setEntity (
				"parentObjectType",
				parentObjectType)

			.list ());

	}

}