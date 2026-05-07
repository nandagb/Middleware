package br.imd.ufrn.Invoker;

import br.imd.ufrn.HTTP.HTTPRequest;

public class Invoker {
    private LifecycleManager lifecycleManager;
    private LookupService lookup;

    public Invoker(LookupService lookup) {
        this.lifecycleManager = new LifecycleManager();
        this.lookup = lookup;
    }

    public void invoke(HTTPRequest request) {
        String method = request.getMethod();
        String path = request.getPath();

        Class<?> serviceClass = lookup.getServiceClass(path);

        Object remoteObject = lifecycleManager.getInstance(serviceClass);
    }
    
}
