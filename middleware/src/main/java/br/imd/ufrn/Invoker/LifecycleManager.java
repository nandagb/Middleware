package br.imd.ufrn.Invoker;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.Annotations.Singleton;

public class LifecycleManager {
    private Map<Class<?>, Queue<Object>> pools;
    private Map<Class<?>, Object> staticInstances;

    public LifecycleManager () {
        this.pools = new HashMap();
        this.staticInstances = new HashMap();
    }

    public Object createInstance(Class<?> serviceClass) {
        Object remoteObject;
        try {
            remoteObject = serviceClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            System.out.println("Erro ao instanciar o objeto remoto " + serviceClass.getName() + ": InstantiationException");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            System.out.println("Erro ao instanciar o objeto remoto " + serviceClass.getName() + ": IllegalAccessException");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println("Erro ao instanciar o objeto remoto " + serviceClass.getName() + ": IllegalArgumentException");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            System.out.println("Erro ao instanciar o objeto remoto " + serviceClass.getName() + ": InvocationTargetException");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            System.out.println("Erro ao instanciar o objeto remoto " + serviceClass.getName() + ": NoSuchMethodException");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (SecurityException e) {
            System.out.println("Erro ao instanciar o objeto remoto " + serviceClass.getName() + ": SecurityException");
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return remoteObject;
    }

    public Object getStaticInstance(Class<?> serviceClass) {
        if(staticInstances.containsKey(serviceClass)) {
            return staticInstances.get(serviceClass);
        }
        else {
            Object instance = createInstance(serviceClass);
            staticInstances.put(serviceClass, instance);

            return instance;
        }
    }

    public Object getPoolInstance(Class<?> serviceClass) {
        Queue<Object> pool = pools.get(serviceClass);

        if(pool == null) {
            pool = new LinkedList<>();
            pools.put(serviceClass, pool);
        }

        if(!pool.isEmpty()) {
            return pool.poll();
        }

        return createInstance(serviceClass);
    }

    public Object getInstance(Class<?> serviceClass) {
        Object remoteObject;
        if(serviceClass.isAnnotationPresent(Singleton.class)) {
            //static instance
            remoteObject = getStaticInstance(serviceClass);
        }
        else {
            // per request with pooling
            remoteObject = getPoolInstance(serviceClass);
        }

        return remoteObject;
    }
    
}
