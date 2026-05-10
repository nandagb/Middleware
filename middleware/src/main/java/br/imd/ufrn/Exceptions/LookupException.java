package br.imd.ufrn.Exceptions;

public class LookupException extends RemoteException {
    public LookupException(String message, int code) {
        super(message, code);
    }
}
