package br.imd.ufrn.Interceptor;

import java.util.ArrayList;
import java.util.List;

public class InterceptorRegistry {
    private List<Interceptor> interceptors;

    public InterceptorRegistry() {
        this.interceptors = new ArrayList<>();
    }

    public void registerInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<Interceptor> getInterceptors() {
        return this.interceptors;
    }
}
