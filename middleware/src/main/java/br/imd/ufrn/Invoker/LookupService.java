package br.imd.ufrn.Invoker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

import br.imd.ufrn.Annotations.Get;
import br.imd.ufrn.Annotations.RemoteService;

public class LookupService {
    public HashMap<String, Class<?>> remoteServices;

    public LookupService() {
        remoteServices = new HashMap<>();
    }

    public void register(Class<?> serviceClass) {

        if(serviceClass.isAnnotationPresent(RemoteService.class)) {
            RemoteService annotation = serviceClass.getAnnotation(RemoteService.class);
            String id = annotation.value();
            System.out.println("Salvando Remote Object com id: " + id);
            remoteServices.put(id, serviceClass);

            // for (Method method : clazz.getDeclaredMethods()) {
            //     if (method.isAnnotationPresent((Class<? extends Annotation>) Get.class)) {
            //         System.out.println("Método anotado com Get");
            //     }
            // }
        }
    }

    public Class<?> getServiceClass(String id){
        return remoteServices.get(id);
    }
}
