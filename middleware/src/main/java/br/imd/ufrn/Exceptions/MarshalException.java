package br.imd.ufrn.Exceptions;

public class MarshalException extends RemoteException{
    public MarshalException(String message, int code) {
        super(message, code);
    }
}
