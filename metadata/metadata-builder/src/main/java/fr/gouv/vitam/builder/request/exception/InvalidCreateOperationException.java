/*******************************************************************************
 * This file is part of Vitam Project.
 *
 * Copyright Vitam (2012, 2015)
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated
 * by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you
 * accept its terms.
 *******************************************************************************/
package fr.gouv.vitam.builder.request.exception;

/**
 * Invalid create operation for request construction
 *
 *
 *
 */
public class InvalidCreateOperationException extends Exception {

    private static final long serialVersionUID = 8781463864293772663L;

    /**
     *
     */
    public InvalidCreateOperationException() {}

    /**
     * @param arg0
     */
    public InvalidCreateOperationException(final String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public InvalidCreateOperationException(final Throwable arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public InvalidCreateOperationException(final String arg0, final Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public InvalidCreateOperationException(final String arg0, final Throwable arg1,
        final boolean arg2, final boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

}
