package wbs.apn.chat.help.logic;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.platform.user.model.UserRec;

import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageRec;

import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatHelpLogLogic {

	ChatHelpLogRec createChatHelpLogIn (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			MessageRec message,
			String text,
			Optional <CommandRec> command,
			Boolean queue);

	ChatHelpLogRec createChatHelpLogOut (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			Optional <ChatHelpLogRec> replyTo,
			Optional <UserRec> user,
			MessageRec message,
			Optional <ChatMessageRec> chatMessage,
			String text,
			Optional <CommandRec> command);

}