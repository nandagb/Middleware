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
        }
    }

    public Class<?> getServiceClass(String id){
        return remoteServices.get(id);
    }

    public Method getMethod(Class<?> serviceClass, String HTTPmethod,  String route) {
        for (Method method : serviceClass.getDeclaredMethods()) {
            if (methodExists(method, HTTPmethod, route)) {
                System.out.println("Método anotado com " + HTTPmethod + ": " + method.getName());
                return method;
            }
        }

        return null;
    }

    private boolean methodExists(Method method, String httpMethod, String methodRoute) {
        switch (httpMethod) {
            case "GET":
                if (method.isAnnotationPresent(Get.class)) {
                    return method.getAnnotation(Get.class).value().equals(methodRoute);
                }
                break;
        }

        return false;
    }
}
