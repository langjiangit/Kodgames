package com.kodgames.game.service.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.kodgames.corgi.core.constant.GlobalConstants;
import com.kodgames.corgi.core.net.Connection;
import com.kodgames.corgi.core.service.PublicService;
import com.kodgames.corgi.core.session.ConnectionManager;
import com.kodgames.game.common.Constant;
import com.kodgames.message.proto.mail.MailProtoBuf.GCMailRES;
import com.kodgames.message.proto.mail.MailProtoBuf.GCNewMailSYNC;
import com.kodgames.message.proto.mail.MailProtoBuf.MailProto;

import limax.zdb.Procedure;
import xbean.Mail;
import xbean.UserMail;

/**
 * Created by lz on 2016/8/27.
 * 邮件系统
 */
public class MailService extends PublicService
{

	private static final long serialVersionUID = -6277937315709332494L;

	public void sendMail(int type, String msg, int targetId, int sender)
	{
		// FIXME
		// execute()为异步模式, 其回调代码也是在zdb.procedure线程中执行
		// 在回调的时候调用Channel.write可能会有问题
		// 建议修改为call()同步模式

		Procedure.execute(() -> {
			long id = table.Personal_mails.newKey();
			Mail mail;
			switch (type)
			{
				case Constant.MailConstant.TYPE_PERSONAL_MAIL:
					mail = table.Personal_mails.insert(id);
					UserMail um = getUserMail(targetId);
					um.getPersonalMails().add(id);
					break;
				case Constant.MailConstant.TYPE_PUBLIC_MAIL:
					mail = table.Public_mails.insert(id);
					// 检测是否超过存储数量 超过就删除
					clearPublicMails();
					break;
				default:
					throw new RuntimeException("mail type error");
			}
			mail.setId(id);
			mail.setType(type);
			mail.setMsg(msg);
			mail.setTime(System.currentTimeMillis());
			mail.setSender(sender);//暂时client不需要

			return true;
		}, (p, r) -> {
			if (r.isSuccess())
				switch (type)
				{
					case Constant.MailConstant.TYPE_PERSONAL_MAIL:
						noticeNewMail(targetId);
						break;
					case Constant.MailConstant.TYPE_PUBLIC_MAIL:
						broadPublicMail();
						break;
				}
		});
	}

	/**
	 * 检查是否有新邮件
	 * 登陆检查
	 */
	public boolean checkNewMail(int roleId)
	{
		UserMail um = getUserMail(roleId);
		return getNewMailList(roleId, um, um.getLastCheckTime(), false).size() > 0;
	}

	/**
	 * 请求所有邮件
	 * @param connection
	 * @param callback
	 * @param lastTime
     */
	public void getNewMailList(Connection connection, int callback, long lastTime)
	{
		GCMailRES.Builder builder = GCMailRES.newBuilder();
		UserMail um = getUserMail(connection.getRemotePeerID());
		builder.addAllMails(getNewMailList(connection.getRemotePeerID(), um, lastTime, true));
		builder.setOldCheckTime(um.getLastCheckTime());
		long current = System.currentTimeMillis();
		um.setLastCheckTime(current);
		builder.setLastCheckTime(current);

		connection.write(callback, builder.build());
	}

	private List<MailProto> getNewMailList(int roleId, UserMail um, long time, boolean isRead)
	{
		if (time == 0)
		{
			time = table.Role_info.select(roleId).getRoleCreateTime();
		}

		final long lastTime = time;
		long current = System.currentTimeMillis();
		List<MailProto> list = new ArrayList<>();

		List<Long> toClear = new ArrayList<>();
		//====================个人邮件========================
		List<Long> pIdList = um.getPersonalMails();
		pIdList.stream()
			.map(table.Personal_mails::select)
			.map(this::toProto)
			.map(MailProto.Builder::build)
			.sorted((o1, o2) -> (int)(o1.getTime() - o2.getTime()))
			.forEach(p -> {
				if (list.size() >= 100)
				{
					toClear.add(p.getId());
					return;
				}
				if (p.getTime() > current + Constant.MailConstant.MAX_MAIL_STORE_TIME)
				{
					toClear.add(p.getId());
					return;
				}
				if (p.getTime() > lastTime)
					list.add(p);
			});
		if (isRead)
			toClear.stream().forEach(id -> {
				um.getPersonalMails().remove(id);
				table.Personal_mails.delete(id);
			});
		toClear.clear();
		//====================公共邮件========================
		Map<Long, MailProto.Builder> pmMap = new HashMap<>();
		table.Public_mails.get().walk((id, mail) -> {
			pmMap.put(id, toProto(mail));
			return false;
		});
		table.Public_mails.get().getCache().walk((id, mail) -> {
			pmMap.put(id, toProto(mail));
		});
		//删除已经被删除的公共mail id
		List<Long> tempToDelete = um.getAllUserMails().stream().filter(id -> !pmMap.containsKey(id)).collect(Collectors.toList());
		um.getAllUserMails().removeAll(tempToDelete);

		//公共邮件此处只做超时检测 存储数量上限在发送时进行
		pmMap.values().stream().forEach(p -> {
			if (p.getTime() > current + Constant.MailConstant.MAX_MAIL_STORE_TIME)
			{
				toClear.add(p.getId());
				return;
			}
			if (!um.getAllUserMails().contains(p.getId()) && p.getTime() > lastTime)
			{
				if (isRead)
					um.getAllUserMails().add(p.getId());
				list.add(p.build());
			}
		});
		toClear.stream().forEach(table.Personal_mails::delete);
		return list;
	}

	private UserMail getUserMail(int id)
	{
		UserMail um = table.User_mails.select(id);
		if (um == null)
			um = table.User_mails.insert(id);

		return um;
	}

	private void broadPublicMail()
	{
		ConnectionManager.getInstance().broadcastToAllVirtualClients(GlobalConstants.DEFAULT_CALLBACK, GCNewMailSYNC.newBuilder().build());
	}

	private void noticeNewMail(int target)
	{
		Connection conn = ConnectionManager.getInstance().getClientConnection(target);
		if (conn != null) {
			conn.write(GlobalConstants.DEFAULT_CALLBACK, GCNewMailSYNC.newBuilder().build());
		}
	}

	//清理公共邮件
	private void clearPublicMails()
	{
		Map<Long, Mail> temp = new HashMap<>();
		table.Public_mails.get().walk((id, m) -> temp.put(id, m) == null);
		table.Public_mails.get().getCache().walk(temp::put);
		if (temp.size() > Constant.MailConstant.MAX_MAIL_STORE_COUNT)
		{
			List<Mail> list = temp.values().stream().sorted((o1, o2) -> (int)(o1.getTime() - o2.getTime())).collect(Collectors.toList());
			list.subList(Constant.MailConstant.MAX_MAIL_STORE_COUNT, list.size()).stream().map(Mail::getId).forEach(table.Public_mails::delete);
		}
	}

	private MailProto.Builder toProto(Mail m)
	{
		return MailProto.newBuilder().setId(m.getId()).setMsg(m.getMsg()).setType(m.getType()).setTime(m.getTime());
	}
}
