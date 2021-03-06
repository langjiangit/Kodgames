package com.kodgames.authserver.service.gmtools;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kodgames.authserver.service.global.GlobalService;
import com.kodgames.corgi.core.service.ServiceContainer;
import com.kodgames.gmtools.handler.GmtHandlerAnnotation;
import com.kodgames.gmtools.handler.IGmtoolsHandler;

@GmtHandlerAnnotation(handler = "UpdateLibraryVersionHandler")
public class UpdateLibraryVersionHandler implements IGmtoolsHandler
{
	private static final Logger logger = LoggerFactory.getLogger(UpdateLibraryVersionHandler.class);

	public HashMap<String, Object> getResult(Map<String, Object> args)
	{
		HashMap<String, Object> result = new HashMap<>();
		result.put("result", 1);
		String platform = null;
		String version = null;
		String description = null;
		String url = null;
		Boolean forceUpdate = null;
		try
		{
			platform = (String)args.get("platform");
			version = (String)args.get("library_version");
			description = (String)args.get("library_description");
			url = (String)args.get("library_url");
			forceUpdate = (Boolean)args.get("force_update");
		}
		catch (Throwable t)
		{
			logger.error("UpdateLibraryVersionHandler args error : {}", args);
			result.put("data", 0);

			return result;
		}

		GlobalService service = ServiceContainer.getInstance().getPublicService(GlobalService.class);
		result.put("data", service.updateLibraryVersion(platform, version, description, url, forceUpdate));

		return result;
	}
}
