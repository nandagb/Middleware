package br.imd.ufrn.Exceptions;

public class LifecycleException extends RemoteException {
    public LifecycleException(String message, int code) {
        super(message, code);
    }
}
