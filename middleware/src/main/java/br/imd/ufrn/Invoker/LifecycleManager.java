package br.imd.ufrn.Invoker;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import br.imd.ufrn.Annotations.RemoteService;
import br.imd.ufrn.Annotations.Singleton;
import br.imd.ufrn.Exceptions.LifecycleException;

public class LifecycleManager {
    private Map<Class<?>, Queue<Object>> pools;
    private Map<Class<?>, Object> staticInstances;

    public LifecycleManager () {
        this.pools = new HashMap();
        this.staticInstances = new HashMap();
    }

    public Object createInstance(Class<?> serviceClass) throws LifecycleException {
        Object remoteObject;
        try {
            remoteObject = serviceClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new LifecycleException("InstantiationException: Erro ao instanciar o objeto remoto " + serviceClass.getName(), 500);
        } catch (IllegalAccessException e) {
            throw new LifecycleException("IllegalAccessException: Erro ao instanciar o objeto remoto " + serviceClass.getName(), 500);
        } catch (IllegalArgumentException e) {
            throw new LifecycleException("IllegalArgumentException: Erro ao instanciar o objeto remoto " + serviceClass.getName(), 500);
        } catch (InvocationTargetException e) {
            throw new LifecycleException("InvocationTargetException: Erro ao instanciar o objeto remoto " + serviceClass.getName(), 500);
        } catch (NoSuchMethodException e) {
            throw new LifecycleException("NoSuchMethodException: Erro ao instanciar o objeto remoto " + serviceClass.getName(), 500);
        } catch (SecurityException e) {
            throw new LifecycleException("SecurityException: Erro ao instanciar o objeto remoto " + serviceClass.getName(), 500);
        }

        return remoteObject;
    }

    public Object getStaticInstance(Class<?> serviceClass) throws LifecycleException {
        if(staticInstances.containsKey(serviceClass)) {
            return staticInstances.get(serviceClass);
        }
        else {
            Object instance = createInstance(serviceClass);
            staticInstances.put(serviceClass, instance);

            return instance;
        }
    }

    public Object getPoolInstance(Class<?> serviceClass) throws LifecycleException {
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

    public Object getInstance(Class<?> serviceClass) throws LifecycleException {
        Object remoteObject;
        if(serviceClass.isAnnotationPresent(Singleton.class)) {
            // System.out.println("pegando instâncias estática de serviço");
            //static instance
            remoteObject = getStaticInstance(serviceClass);
        }
        else {
            System.out.println("pegando instância de pool");
            // per request with pooling
            remoteObject = getPoolInstance(serviceClass);
        }

        return remoteObject;
    }

    public void releaseInstance(Class<?> serviceClass, Object instance) {
        if(serviceClass.isAnnotationPresent(Singleton.class)) {
            return;
        }
        else {
            Queue<Object> pool = pools.get(serviceClass);

            if(pool != null) {
                pool.offer(instance);
            }
        }
    }
    
}
