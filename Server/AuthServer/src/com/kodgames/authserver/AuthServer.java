package com.kodgames.authserver;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.authserver.config.AuthZdbConfig;
import com.kodgames.authserver.start.NetInitializer;
import com.kodgames.authserver.start.ServerConfigInitializer;
import com.kodgames.corgi.core.util.rsa.RsaConfig;

import limax.xmlconfig.Service;

public class AuthServer
{
	static private Logger logger = LoggerFactory.getLogger(AuthServer.class);

	public static void main(String[] args)
		throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
		CertificateException, IOException, InterruptedException
	{
		Service.addRunAfterEngineStartTask(() -> {
			try {
				ServerConfigInitializer.getInstance().init("/auth.conf");
//				ServerConfigInitializer.getInstance().initProperties("/auth.properties");
				ServerConfigInitializer.getInstance().initJson("/auth.json");
				RsaConfig.getInstance().parse();
				AuthZdbConfig.getInstance().walk();
				
			} catch (IOException e) {
				logger.error("AuthServer start failed! : {}", e);
			}
			try
			{
				NetInitializer.getInstance().init();
			}
			catch (Exception e)
			{
				logger.error("AuthServer start failed! : {}", e);
			}
		});

		Service.run(Object.class.getResource("/zdb_config.xml").getPath());
	}
}
