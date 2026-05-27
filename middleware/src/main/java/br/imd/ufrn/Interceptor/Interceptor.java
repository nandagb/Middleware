package br.imd.ufrn.Interceptor;

import br.imd.ufrn.Exceptions.RemoteException;

public interface Interceptor {
    void before( InvocationContext context ) throws RemoteException;
    void after( InvocationContext context ) throws RemoteException;
}
