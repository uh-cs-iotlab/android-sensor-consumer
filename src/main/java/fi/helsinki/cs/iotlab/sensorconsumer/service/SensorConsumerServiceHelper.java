package fi.helsinki.cs.iotlab.sensorconsumer.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService1;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService10;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService11;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService12;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService13;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService14;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService15;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService16;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService17;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService18;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService19;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService2;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService20;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService3;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService4;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService5;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService6;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService7;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService8;
import fi.helsinki.cs.iotlab.sensorconsumer.service.placeholder.SensorConsumerService9;

/**
 * Created by mineraud on 22/12/16.
 */

public class SensorConsumerServiceHelper {

    private static Class<? extends SensorConsumerBaseService> getClassForId(int id) {
        switch (id) {
            case 1:
                return SensorConsumerService1.class;
            case 2:
                return SensorConsumerService2.class;
            case 3:
                return SensorConsumerService3.class;
            case 4:
                return SensorConsumerService4.class;
            case 5:
                return SensorConsumerService5.class;
            case 6:
                return SensorConsumerService6.class;
            case 7:
                return SensorConsumerService7.class;
            case 8:
                return SensorConsumerService8.class;
            case 9:
                return SensorConsumerService9.class;
            case 10:
                return SensorConsumerService10.class;
            case 11:
                return SensorConsumerService11.class;
            case 12:
                return SensorConsumerService12.class;
            case 13:
                return SensorConsumerService13.class;
            case 14:
                return SensorConsumerService14.class;
            case 15:
                return SensorConsumerService15.class;
            case 16:
                return SensorConsumerService16.class;
            case 17:
                return SensorConsumerService17.class;
            case 18:
                return SensorConsumerService18.class;
            case 19:
                return SensorConsumerService19.class;
            case 20:
                return SensorConsumerService20.class;
            default:
                return null;
        }
    }


    private static void startSensorConsumerService(Context context, int id,
                                                  @NonNull SensorConsumerServiceParameters parameters) {
        // Otherwise, we can start the collection service by putting the parameters as an extra
        Intent intent = new Intent(context, getClassForId(id));
        intent.putExtra(SensorConsumerBaseService.PARAMETERS, parameters);
        // Start the service with the intent
        context.startService(intent);
    }

    /**
     * The helper can start a number of services with this function.
     * @param context
     * @param nb
     * @param parameters
     */
    public static void startSensorConsumerServices(Context context, int nb,
                                                   @NonNull SensorConsumerServiceParameters parameters) {
        for (int i = 1; i <= nb; i++) {
            startSensorConsumerService(context, i, parameters);
        }
    }
}
