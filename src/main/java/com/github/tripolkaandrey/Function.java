package com.github.tripolkaandrey;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;
import java.util.TreeMap;

public class Function {
    private static final TreeMap<Integer, Integer> averageTempToHeatingTemp;
    private static final String BAD_REQUEST_ERROR_MESSAGE = "Please pass current temperature as integer via request param";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Unknown error occurred";

    static {
        averageTempToHeatingTemp = new TreeMap<>();
        averageTempToHeatingTemp.put(10, 30);
        averageTempToHeatingTemp.put(5, 44);
        averageTempToHeatingTemp.put(0, 57);
        averageTempToHeatingTemp.put(-5, 70);
        averageTempToHeatingTemp.put(-10, 83);
        averageTempToHeatingTemp.put(-15, 95);

    }

    @FunctionName("temperatureFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "getHeatingTemperature",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS)
                    HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        final String temperatureParam = request.getQueryParameters().get("temperature");
        logRequest(context, temperatureParam);

        try {
            Integer temperature = Integer.parseInt(temperatureParam);
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(getHeatingTemp(temperature))
                    .build();
        } catch (NumberFormatException ex) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(BAD_REQUEST_ERROR_MESSAGE)
                    .build();
        } catch (Exception ex) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(INTERNAL_SERVER_ERROR_MESSAGE)
                    .build();
        }
    }

    private Integer getHeatingTemp(Integer temp) {
        if(temp < 0) {
            return averageTempToHeatingTemp.ceilingEntry(temp).getValue();
        } else {
            var result = averageTempToHeatingTemp.ceilingEntry(temp);
            return  result == null ? temp : result.getValue();
        }
    }

    private void logRequest(final ExecutionContext context, String param) {
        context.getLogger().info(String.format("Triggered for request with value: %s", param));
    }
}
