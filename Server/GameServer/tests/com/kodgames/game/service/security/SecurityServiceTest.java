package com.kodgames.game.service.security;

import com.kodgames.corgi.core.constant.DateTimeConstants;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.corgi.core.util.rsa.RsaConfig;
import com.kodgames.game.common.ServerJson;
import com.kodgames.game.common.rule.RuleManager;
import com.kodgames.game.service.room.RoomIdGenerator;
import com.kodgames.game.service.room.RoomStatusCheckFuncImpl;
import com.kodgames.game.start.*;
import limax.xmlconfig.Service;
import limax.zdb.Procedure;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xbean.RoleInfo;
import xbean.RoleRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static org.junit.Assert.*;

/**
 * Created by Liufei on 2017/5/6.
 */
public class SecurityServiceTest {
    private static Logger logger = LoggerFactory.getLogger(SecurityServiceTest.class);

    @Before
    public void setUp() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Service.addRunAfterEngineStartTask(() -> {
                    try {
                        ServerConfigInitializer.getInstance().init("/game.conf");
                        ServerConfigInitializer.getInstance().initProperties("/game.properties");

                        RuleManager.getInstance().load(Object.class.getResource("/rules.xml").getPath());
                        PurchaseConfig.getInstance().load(Object.class.getResource("/purchase.xml").getPath());
                        WxPromoterConfig.getInstance().load(Object.class.getResource("/wxpromoter.xml").getPath());
                        RsaConfig.getInstance().parse();
                        ServerJson.getInstance().parse();

                        // 初始化RoomId生成器
                        RoomIdGenerator.Init(new RoomStatusCheckFuncImpl());

                        NetInitializer.getInstance().init();
                        TaskInitializer.getInstance().init();

                        // 删除mobile-id表中status != success 的记录
                        MobileIdTableInit.getInstance().walk();
                    } catch (Exception e) {
                        logger.error("Game server start error : {}", e);
                    }
                });

                Service.run(Object.class.getResource("/zdb_config.xml").getPath());
            }
        }).start();
        Thread.sleep(5000);
    }

    @After
    public void tearDown() throws Exception {
        logger.error("stop stop stop stop stop stop");
        Service.stop();
    }

    @Test
    public void getGroup() throws Exception {
        ResetZDB();

        // roleId 1 返回 group2
        int roleId = 1;
        String ip = "202.201.48.16";
        SecurityService service = ServiceContainer.getInstance().getPublicService(SecurityService.class);
        Procedure.call(() -> {
            String group = service.getGroup(roleId, ip);
            assertEquals(group, "group2");
            return true;
        });
    }

    @Test
    public void resetGroups() throws Exception {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
"<IPSelectConfig isOpen=\"true\" defaultGroupName=\"defaultGroup\">" +
"    <IPGroup name=\"group1\">" +
"        <GroupCondition type=\"REGISTER\" compareType=\"LESS\" intMinValue=\"7\" intMaxValue=\"7\" strValue=\"\" />" +
"        <GroupCondition type=\"REGION\" compareType=\"EQUAL\" intMinValue=\"0\" intMaxValue=\"0\" strValue=\"贵阳\" />" +
"    </IPGroup>" +
"    <IPGroup name=\"group2\">" +
"        <GroupCondition type=\"REGISTER\" compareType=\"LESS\" intMinValue=\"7\" intMaxValue=\"7\" strValue=\"\" />" +
"        <GroupCondition type=\"REGION\" compareType=\"NE\" intMinValue=\"0\" intMaxValue=\"0\" strValue=\"贵阳\" />" +
"    </IPGroup>" +
"    <IPGroup name=\"group3\">" +
"        <GroupCondition type=\"COMBAT\" compareType=\"GE\" intMinValue=\"100\" intMaxValue=\"100\" strValue=\"\" />" +
"        <GroupCondition type=\"REGION\" compareType=\"EQUAL\" intMinValue=\"0\" intMaxValue=\"0\" strValue=\"贵阳\" />" +
"    </IPGroup>" +
"</IPSelectConfig>";

        SecurityGroupConfig.getInstance().reload(content, null);
    }

    private void ResetZDB()
    {
        Procedure.call(() -> {
            // roleId: 1, 注册时间1天前
            int roleId = 1;

            RoleInfo roleInfo = table.Role_info.update(roleId);
            if (roleInfo == null)
                roleInfo = table.Role_info.insert(roleId);

            roleInfo.setAccountId(roleId);
            roleInfo.setRoleCreateTime(System.currentTimeMillis() - DateTimeConstants.DAY);

            RoleRecord roleRecord = table.Role_records.update(roleId);
            if (roleRecord == null)
                roleRecord = table.Role_records.insert(roleId);

            // roleId: 2, 注册时间8天前
            roleId = 2;
            roleInfo = table.Role_info.update(roleId);
            if (roleInfo == null)
                roleInfo = table.Role_info.insert(roleId);
            roleInfo.setAccountId(roleId);
            roleInfo.setRoleCreateTime(System.currentTimeMillis() - DateTimeConstants.DAY * 8);

            roleRecord = table.Role_records.update(roleId);
            if (roleRecord == null)
                roleRecord = table.Role_records.insert(roleId);

            return true;
        });
    }
}