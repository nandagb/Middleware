package br.imd.ufrn.Invoker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;

import br.imd.ufrn.Annotations.Get;
import br.imd.ufrn.Annotations.RemoteObject;

public class LookupService {
    public HashMap<String, Class<?>> remoteObjects;

    public LookupService() {
        remoteObjects = new HashMap<>();
    }

    public void register(Object object) {
        Class<?> clazz = object.getClass();

        if(clazz.isAnnotationPresent(RemoteObject.class)) {
            RemoteObject annotation = clazz.getAnnotation(RemoteObject.class);
            String id = annotation.value();
            System.out.println("Salvando Remote Object com id: " + id);
            remoteObjects.put(id, clazz);

            // for (Method method : clazz.getDeclaredMethods()) {
            //     if (method.isAnnotationPresent((Class<? extends Annotation>) Get.class)) {
            //         System.out.println("Método anotado com Get");
            //     }
            // }
        }
    }

    public Class<?> getRomoteObject(String id){
        return remoteObjects.get(id);
    }
}
