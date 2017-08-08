/*******************************************************************************
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
 *******************************************************************************/
package fr.gouv.vitam.access.external.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.access.external.api.AccessExtAPI;
import fr.gouv.vitam.access.internal.client.AccessInternalClient;
import fr.gouv.vitam.access.internal.client.AccessInternalClientFactory;
import fr.gouv.vitam.access.internal.common.exception.AccessInternalClientNotFoundException;
import fr.gouv.vitam.access.internal.common.exception.AccessInternalClientServerException;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.database.parser.request.single.SelectParserSingle;
import fr.gouv.vitam.common.error.VitamError;
import fr.gouv.vitam.common.exception.AccessUnauthorizedException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamThreadAccessException;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.parameter.ParameterHelper;
import fr.gouv.vitam.common.security.SanityChecker;
import fr.gouv.vitam.common.server.application.AsyncInputStreamHelper;
import fr.gouv.vitam.common.thread.VitamThreadPoolExecutor;
import fr.gouv.vitam.common.thread.VitamThreadUtils;
import fr.gouv.vitam.logbook.common.exception.LogbookClientException;

/**
 * AccessResourceImpl implements AccessResource
 */
@Path("/access-external/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@javax.ws.rs.ApplicationPath("webresources")
public class LogbookExternalResourceImpl {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(LogbookExternalResourceImpl.class);
    private static final String ACCESS_EXTERNAL_MODULE = "LOGBOOK_EXTERNAL";
    private static final String CODE_VITAM = "code_vitam";
    private static final String EVENT_ID_PROCESS = "evIdProc";
    private static final String OB_ID = "obId";

    /**
     * Constructor
     *
     */
    public LogbookExternalResourceImpl() {
        LOGGER.debug("LogbookExternalResource initialized");
    }

    /***** LOGBOOK OPERATION - START *****/

    /**
     * GET with request in body
     *
     * @param query DSL as String
     * @return Response contains the list of logbook operations
     */
    @GET
    @Path("/operations")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response selectOperation(JsonNode query) {
        Integer tenantId = ParameterHelper.getTenantParameter();
        VitamThreadUtils.getVitamSession().setRequestId(GUIDFactory.newRequestIdGUID(tenantId));

        Status status;
        try (AccessInternalClient client = AccessInternalClientFactory.getInstance().getClient()) {
            SanityChecker.checkJsonAll(query);
            RequestResponse<JsonNode> result = client.selectOperation(query);
            int st = result.isOk() ? Status.OK.getStatusCode() : result.getHttpCode();
            return Response.status(st).entity(result).build();
        } catch (final LogbookClientException e) {
            LOGGER.error(e);
            status = Status.INTERNAL_SERVER_ERROR;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (final InvalidParseOperationException e) {
            LOGGER.error(e);
            status = Status.PRECONDITION_FAILED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (AccessUnauthorizedException e) {
            LOGGER.error("Contract access does not allow ", e);
            status = Status.UNAUTHORIZED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        }
    }

    /**
     * @param operationId the operation id
     * @param queryDsl the query
     * @return the response with a specific HTTP status
     */

    @GET
    @Path("/operations/{id_op}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOperationById(@PathParam("id_op") String operationId, JsonNode queryDsl) {
        Integer tenantId = ParameterHelper.getTenantParameter();
        VitamThreadUtils.getVitamSession().setRequestId(GUIDFactory.newRequestIdGUID(tenantId));

        Status status;
        try (AccessInternalClient client = AccessInternalClientFactory.getInstance().getClient()) {
            SanityChecker.checkJsonAll(queryDsl);
            SanityChecker.checkParameter(operationId);
            final SelectParserSingle parser = new SelectParserSingle();
            Select select = new Select();
            parser.parse(select.getFinalSelect());
            parser.addCondition(QueryHelper.eq(EVENT_ID_PROCESS, operationId));
            queryDsl = parser.getRequest().getFinalSelect();
            RequestResponse<JsonNode> result = client.selectOperationById(operationId, queryDsl);
            int st = result.isOk() ? Status.OK.getStatusCode() : result.getHttpCode();
            return Response.status(st).entity(result).build();
        } catch (final LogbookClientException e) {
            LOGGER.error(e);
            status = Status.INTERNAL_SERVER_ERROR;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (final InvalidParseOperationException e) {
            LOGGER.error(e);
            status = Status.PRECONDITION_FAILED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (InvalidCreateOperationException e) {
            LOGGER.error(e);
            status = Status.BAD_REQUEST;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (AccessUnauthorizedException e) {
            LOGGER.error("Contract access does not allow ", e);
            status = Status.UNAUTHORIZED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        }
    }

    /**
     * gets the unit life cycle based on its id
     *
     * @param unitLifeCycleId the unit life cycle id
     * @param queryDsl the query
     * @return the unit life cycle
     *
     */
    @GET
    @Path("/unitlifecycles/{id_lc}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnitLifeCycle(@PathParam("id_lc") String unitLifeCycleId, JsonNode queryDsl) {
        Integer tenantId = ParameterHelper.getTenantParameter();
        VitamThreadUtils.getVitamSession().setRequestId(GUIDFactory.newRequestIdGUID(tenantId));

        Status status;
        try (AccessInternalClient client = AccessInternalClientFactory.getInstance().getClient()) {
            SanityChecker.checkJsonAll(queryDsl);
            SanityChecker.checkParameter(unitLifeCycleId);
            final SelectParserSingle parser = new SelectParserSingle();
            Select select = new Select();
            parser.parse(select.getFinalSelect());
            parser.addCondition(QueryHelper.eq(OB_ID, unitLifeCycleId));
            queryDsl = parser.getRequest().getFinalSelect();
            RequestResponse<JsonNode> result = client.selectUnitLifeCycleById(unitLifeCycleId, queryDsl);
            int st = result.isOk() ? Status.OK.getStatusCode() : result.getHttpCode();
            return Response.status(st).entity(result).build();
        } catch (final LogbookClientException e) {
            LOGGER.error(e);
            status = Status.PRECONDITION_FAILED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (final InvalidParseOperationException e) {
            LOGGER.error(e);
            status = Status.PRECONDITION_FAILED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (InvalidCreateOperationException e) {
            LOGGER.error(e);
            status = Status.BAD_REQUEST;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (AccessUnauthorizedException e) {
            LOGGER.error("Contract access does not allow ", e);
            status = Status.UNAUTHORIZED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        }
    }

    /**
     * gets the object group life cycle based on its id
     *
     * @param objectGroupLifeCycleId the object group life cycle id
     * @param queryDsl the query
     * @return the object group life cycle
     */
    @GET
    @Path("/objectgrouplifecycles/{id_lc}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObjectGroupLifeCycle(@PathParam("id_lc") String objectGroupLifeCycleId,
        JsonNode queryDsl) {
        Integer tenantId = ParameterHelper.getTenantParameter();
        VitamThreadUtils.getVitamSession().setRequestId(GUIDFactory.newRequestIdGUID(tenantId));

        Status status;
        try (AccessInternalClient client = AccessInternalClientFactory.getInstance().getClient()) {
            SanityChecker.checkJsonAll(queryDsl);
            SanityChecker.checkParameter(objectGroupLifeCycleId);
            final SelectParserSingle parser = new SelectParserSingle();
            Select select = new Select();
            parser.parse(select.getFinalSelect());
            parser.addCondition(QueryHelper.eq(OB_ID, objectGroupLifeCycleId));
            queryDsl = parser.getRequest().getFinalSelect();
            RequestResponse<JsonNode> result =
                client.selectObjectGroupLifeCycleById(objectGroupLifeCycleId, queryDsl);
            int st = result.isOk() ? Status.OK.getStatusCode() : result.getHttpCode();
            return Response.status(st).entity(result).build();
        } catch (final LogbookClientException e) {
            LOGGER.error(e);
            status = Status.PRECONDITION_FAILED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (final InvalidParseOperationException e) {
            LOGGER.error(e);
            status = Status.PRECONDITION_FAILED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (InvalidCreateOperationException e) {
            LOGGER.error(e);
            status = Status.BAD_REQUEST;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        } catch (AccessUnauthorizedException e) {
            LOGGER.error("Contract access does not allow ", e);
            status = Status.UNAUTHORIZED;
            return Response.status(status).entity(getErrorEntity(status, e.getLocalizedMessage())).build();
        }
    }

    /***** LIFE CYCLES - END *****/

    private VitamError getErrorEntity(Status status, String message) {
        String aMessage =
            (message != null && !message.trim().isEmpty()) ? message
                : (status.getReasonPhrase() != null ? status.getReasonPhrase() : status.name());

        return new VitamError(status.name()).setHttpCode(status.getStatusCode()).setContext(ACCESS_EXTERNAL_MODULE)
            .setState(CODE_VITAM).setMessage(status.getReasonPhrase()).setDescription(aMessage);
    }
    
    private InputStream getErrorStream(Status status, String message) {
        String aMessage =
            (message != null && !message.trim().isEmpty()) ? message
                : (status.getReasonPhrase() != null ? status.getReasonPhrase() : status.name());
        try {
            return JsonHandler.writeToInpustream(new VitamError(status.name())
                .setHttpCode(status.getStatusCode()).setContext(ACCESS_EXTERNAL_MODULE)
                .setState(CODE_VITAM).setMessage(status.getReasonPhrase()).setDescription(aMessage));
        } catch (InvalidParseOperationException e) {
            return new ByteArrayInputStream("{ 'message' : 'Invalid VitamError message' }".getBytes());
        }
    }

    @GET
    @Path(AccessExtAPI.TRACEABILITY_API + "/{idOperation}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public void downloadTraceabilityFile(@PathParam("idOperation") String operationId,
        @Suspended final AsyncResponse asyncResponse) {
        try {
            ParametersChecker.checkParameter("Traceability operation should be filled", operationId);
    
            Integer tenantId = ParameterHelper.getTenantParameter();
            VitamThreadUtils.getVitamSession().setRequestId(GUIDFactory.newRequestIdGUID(tenantId));
    
            VitamThreadPoolExecutor.getDefaultExecutor()
                .execute(() -> downloadTraceabilityOperationFile(operationId, asyncResponse));
        } catch (IllegalArgumentException | VitamThreadAccessException e) {
            LOGGER.error(e);
            final Response errorResponse = Response.status(Status.PRECONDITION_FAILED)
                .entity(getErrorStream(Status.PRECONDITION_FAILED, e.getMessage()))
                .build();
            AsyncInputStreamHelper.asyncResponseResume(asyncResponse, errorResponse);
        }
    }

    private void downloadTraceabilityOperationFile(String operationId, final AsyncResponse asyncResponse) {
        AsyncInputStreamHelper helper;

        try (AccessInternalClient client = AccessInternalClientFactory.getInstance().getClient()) {

            final Response response = client.downloadTraceabilityFile(operationId);
            helper = new AsyncInputStreamHelper(asyncResponse, response);
            final ResponseBuilder responseBuilder =
                Response.status(Status.OK)
                    .header("Content-Disposition", response.getHeaderString("Content-Disposition"))
                    .type(response.getMediaType());
            helper.writeResponse(responseBuilder);
        } catch (final InvalidParseOperationException | IllegalArgumentException exc) {
            LOGGER.error(exc);
            final Response errorResponse = Response.status(Status.PRECONDITION_FAILED)
                .entity(getErrorStream(Status.PRECONDITION_FAILED, exc.getMessage()))
                .build();
            AsyncInputStreamHelper.asyncResponseResume(asyncResponse, errorResponse);
        } catch (final AccessInternalClientServerException exc) {
            LOGGER.error(exc.getMessage(), exc);
            final Response errorResponse =
                Response.status(Status.INTERNAL_SERVER_ERROR).entity(getErrorStream(Status.INTERNAL_SERVER_ERROR, 
                    exc.getMessage())).build();
            AsyncInputStreamHelper.asyncResponseResume(asyncResponse, errorResponse);
        } catch (final AccessInternalClientNotFoundException exc) {
            LOGGER.error(exc.getMessage(), exc);
            final Response errorResponse =
                Response.status(Status.NOT_FOUND).entity(getErrorStream(Status.NOT_FOUND, exc.getMessage())).build();
            AsyncInputStreamHelper.asyncResponseResume(asyncResponse, errorResponse);
        } catch (AccessUnauthorizedException e) {
            LOGGER.error("Contract access does not allow ", e);
            final Response errorResponse =
                Response.status(Status.UNAUTHORIZED).entity(getErrorStream(Status.UNAUTHORIZED, e.getMessage())).build();
            AsyncInputStreamHelper.asyncResponseResume(asyncResponse, errorResponse);
        }
    }

}
