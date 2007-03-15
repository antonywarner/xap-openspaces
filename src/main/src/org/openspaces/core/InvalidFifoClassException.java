package org.openspaces.core;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * This exception is thrown during write operation when the Entry's class FIFO
 * mode already been defined and a later write operation define different FIFO mode.
 * It is a wrapper for {@link com.j_spaces.core.InvalidFifoClassException}.
 *
 * @author kimchy
 */
public class InvalidFifoClassException extends InvalidDataAccessResourceUsageException {

    private com.j_spaces.core.InvalidFifoClassException e;

    public InvalidFifoClassException(com.j_spaces.core.InvalidFifoClassException e) {
        super(e.getMessage(), e);
        this.e = e;
    }

    /**
     * Return invalid className.
     *
     * @see com.j_spaces.core.InvalidFifoClassException#getClassName()
     */
    public String getClassName() {
        return e.getClassName();
    }

    /**
     * Returns <code>true</code> if this class defined as FIFO, otherwise <code>false</code>.
     *
     * @see com.j_spaces.core.InvalidFifoClassException#isFifoClass()
     */
    public boolean isFifoClass() {
        return e.isFifoClass();
    }

}
