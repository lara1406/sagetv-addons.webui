/*
 * Created on Jul 29, 2004 by viecili
 */
package viecili.jrss.generator.elem;

/**
 * Thrown when a required parameter is passed to a constructor or method and its value is not valid or is <code>null</code>.
 * 
 * @author Henrique A. Viecili
 */
public class InvalidRequiredParamException extends RuntimeException {

    private static final long serialVersionUID = 5251416752061529983L;

    /**
     * Constructs an <code>InvalidRequiredParamException</code> with no detail message.
     */
    public InvalidRequiredParamException() {
        super();
    }

    /**
     * Constructs an <code>InvalidRequiredParamException</code> with the specified detail message.
     * @param message The detail message
     */
    public InvalidRequiredParamException(String message) {
        super(message);
    }
}
