/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.common.logging;

import ch.qos.logback.classic.Logger;

/**
 * logger using SLF4J from Logback
 *
 *
 *
 */
final class LogbackLogger extends AbstractVitamLogger {
    private static final long serialVersionUID = -7588688826950608830L;
    /**
     * Internal logger
     */
    private final transient Logger logger; //NOSONAR keep it non static

    /**
     *
     * @param logger
     */
    public LogbackLogger(final Logger logger) {
        super(logger.getName());
        this.logger = logger;
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(final String msg) {
        if (logger.isTraceEnabled()) {
            logger.trace(getLoggerMethodAndLine() + msg);
        }
    }

    @Override
    public void trace(final String format, final Object arg) {
        if (logger.isTraceEnabled()) {
            logger.trace(getLoggerMethodAndLine() + format, arg);
        }
    }

    @Override
    public void trace(final String format, final Object argA, final Object argB) {
        if (logger.isTraceEnabled()) {
            logger.trace(getLoggerMethodAndLine() + format, argA, argB);
        }
    }

    @Override
    public void trace(final String format, final Object... argArray) {
        if (logger.isTraceEnabled()) {
            logger.trace(getLoggerMethodAndLine() + format, argArray);
        }
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        if (logger.isTraceEnabled()) {
            logger.trace(getLoggerMethodAndLine() + msg, t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLoggerMethodAndLine() + msg);
        }
    }

    @Override
    public void debug(final String format, final Object arg) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLoggerMethodAndLine() + format, arg);
        }
    }

    @Override
    public void debug(final String format, final Object argA, final Object argB) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLoggerMethodAndLine() + format, argA, argB);
        }
    }

    @Override
    public void debug(final String format, final Object... argArray) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLoggerMethodAndLine() + format, argArray);
        }
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        if (logger.isDebugEnabled()) {
            logger.debug(getLoggerMethodAndLine() + msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(final String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(getLoggerMethodAndLine() + msg);
        }
    }

    @Override
    public void info(final String format, final Object arg) {
        if (logger.isInfoEnabled()) {
            logger.info(getLoggerMethodAndLine() + format, arg);
        }
    }

    @Override
    public void info(final String format, final Object argA, final Object argB) {
        if (logger.isInfoEnabled()) {
            logger.info(getLoggerMethodAndLine() + format, argA, argB);
        }
    }

    @Override
    public void info(final String format, final Object... argArray) {
        if (logger.isInfoEnabled()) {
            logger.info(getLoggerMethodAndLine() + format, argArray);
        }
    }

    @Override
    public void info(final String msg, final Throwable t) {
        if (logger.isInfoEnabled()) {
            logger.info(getLoggerMethodAndLine() + msg, t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(final String msg) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLoggerMethodAndLine() + msg);
        }
    }

    @Override
    public void warn(final String format, final Object arg) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLoggerMethodAndLine() + format, arg);
        }
    }

    @Override
    public void warn(final String format, final Object... argArray) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLoggerMethodAndLine() + format, argArray);
        }
    }

    @Override
    public void warn(final String format, final Object argA, final Object argB) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLoggerMethodAndLine() + format, argA, argB);
        }
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        if (logger.isWarnEnabled()) {
            logger.warn(getLoggerMethodAndLine() + msg, t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(final String msg) {
        logger.error(getLoggerMethodAndLine() + msg);
    }

    @Override
    public void error(final String format, final Object arg) {
        logger.error(getLoggerMethodAndLine() + format, arg);
    }

    @Override
    public void error(final String format, final Object argA, final Object argB) {
        logger.error(getLoggerMethodAndLine() + format, argA, argB);
    }

    @Override
    public void error(final String format, final Object... argArray) {
        logger.error(getLoggerMethodAndLine() + format, argArray);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        logger.error(getLoggerMethodAndLine() + msg, t);
    }
}