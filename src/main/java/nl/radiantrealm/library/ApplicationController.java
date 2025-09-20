package nl.radiantrealm.library;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import nl.radiantrealm.library.http.*;
import nl.radiantrealm.library.http.server.ApplicationRouter;
import nl.radiantrealm.library.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class ApplicationController extends ApplicationRouter {
    protected final Map<String, ApplicationService> serviceMap = new HashMap<>();

    public ApplicationController(int port) {
        super(port);

        register(new PilotLine(), "/pilot");
    }

    protected void registerService(String name, ApplicationService service) {
        serviceMap.put(name, service);
    }

    protected void handlePilotLine(HttpRequest request) throws Exception {
        JsonObject object = HttpException.tryFunction(JsonUtils::getJsonObject, request.getRequestBody(), new HttpResponse(
                StatusCode.BAD_REQUEST,
                "Invalid JSON body."
        ));

        String action = HttpException.tryFunction(JsonUtils::getJsonString, object, "action", new HttpResponse(
                StatusCode.BAD_REQUEST,
                "Provide an action."
        ));

        switch (action) {
            case "start-service" -> getService(object).start();
            case "stop-service" -> getService(object).stop();
            case "status-service" -> {
                ApplicationService service = getService(object);

                JsonObject responseBody = service.status();
                responseBody.addProperty("is_running", service.isRunning.get());
                responseBody.addProperty("running_since", service.runningSince.get());
                request.sendResponse(StatusCode.OK, responseBody);
            }

            case "command-service" -> {
                ApplicationService service = getService(object);

                JsonObject payload = HttpException.tryFunction(JsonUtils::getJsonObject, object, "payload", new HttpResponse(
                        StatusCode.BAD_REQUEST,
                        "Provide a payload body."
                ));

                service.command(payload);
                request.sendStatusResponse(StatusCode.OK);
            }

            case "service-list" -> {
                JsonArray array = new JsonArray();

                for (Map.Entry<String, ApplicationService> entry : serviceMap.entrySet()) {
                    ApplicationService service = entry.getValue();

                    JsonObject properties = new JsonObject();
                    properties.addProperty("name", entry.getKey());
                    properties.addProperty("running", service.isRunning.get());
                    properties.addProperty("type", service.serviceType());
                    array.add(properties);
                }

                JsonObject responseBody = new JsonObject();
                responseBody.add("services", array);
                request.sendResponse(StatusCode.OK, responseBody);
            }
        }
    }

    protected ApplicationService getService(JsonObject object) throws Exception {
        String service = HttpException.tryFunction(JsonUtils::getJsonString, object, "service", new HttpResponse(
                StatusCode.BAD_REQUEST,
                "Provide a service."
        ));

        ApplicationService applicationService = serviceMap.get(service);

        if (applicationService == null) {
            throw new HttpException(new HttpResponse(
                    StatusCode.NOT_FOUND,
                    "No service found."
            ));
        }

        return applicationService;
    }

    protected class PilotLine implements RequestHandler {

        @Override
        public void handle(HttpRequest request) throws Exception {
            handlePilotLine(request);
        }
    }
}
