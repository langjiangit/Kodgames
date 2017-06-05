package com.kodgames.game.start;

import com.kodgames.game.service.mobileBind.MobileBindService;

import limax.zdb.Procedure;
import table.Mobile_id_table;

public class MobileIdTableInit
{
	
	private static MobileIdTableInit instance = new MobileIdTableInit();
	
	public static MobileIdTableInit getInstance()
	{
		return instance;
	}
	
	public void walk()
	{
		//初始化索引
		Procedure.call(() -> {
			Mobile_id_table.get().walk((key, value) -> {
				if(value.getStatus() != MobileBindService.getNormalStatus())
				{
					Mobile_id_table.delete(key);
				}
				return true;
			});
			return true;
		});
	}
}
