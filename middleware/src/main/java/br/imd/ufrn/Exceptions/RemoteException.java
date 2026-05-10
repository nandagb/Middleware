package br.imd.ufrn.Exceptions;

public class RemoteException extends Exception {
    private int code;

    public RemoteException(String errorMessage, int code) {
        super(errorMessage);
        this.code = code;
        // RemoteInvocationFailureException
        // RemoteLookupFailureException
        // RemoteTimeoutException
        // RemoteProxyFailureException
    }

    public int getCode() {
        return this.code;
    }
}
