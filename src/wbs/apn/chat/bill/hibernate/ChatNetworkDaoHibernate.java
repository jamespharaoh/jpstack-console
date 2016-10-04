package wbs.apn.chat.bill.hibernate;

import lombok.NonNull;

import org.hibernate.FlushMode;
import org.hibernate.criterion.Restrictions;

import wbs.apn.chat.bill.model.ChatNetworkDao;
import wbs.apn.chat.bill.model.ChatNetworkRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.network.model.NetworkRec;

@SingletonComponent ("chatNetworkDaoHibernate")
public
class ChatNetworkDaoHibernate
	extends HibernateDao
	implements ChatNetworkDao {

	@Override
	public
	ChatNetworkRec find (
			@NonNull ChatRec chat,
			@NonNull NetworkRec network) {

		return findOne (
			"find (chat, network)",
			ChatNetworkRec.class,

			createCriteria (
				ChatNetworkRec.class,
				"_chatNetwork")

			.add (
				Restrictions.eq (
					"_chatNetwork.chat",
					chat))

			.add (
				Restrictions.eq (
					"_chatNetwork.network",
					network))

			.setCacheable (
				true)

			.setFlushMode (
				FlushMode.MANUAL)

		);

	}

}