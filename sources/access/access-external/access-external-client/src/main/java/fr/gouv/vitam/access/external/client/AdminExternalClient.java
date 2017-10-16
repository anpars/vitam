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
package fr.gouv.vitam.access.external.client;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.access.external.api.AdminCollections;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientNotFoundException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientServerException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalNotFoundException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.AccessUnauthorizedException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.external.client.BasicClient;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.ProcessQuery;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.administration.AccessionRegisterSummaryModel;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.administration.ContextModel;
import fr.gouv.vitam.common.model.administration.FileFormatModel;
import fr.gouv.vitam.common.model.administration.FileRulesModel;
import fr.gouv.vitam.common.model.administration.IngestContractModel;
import fr.gouv.vitam.common.model.administration.ProfileModel;
import fr.gouv.vitam.common.model.administration.SecurityProfileModel;
import fr.gouv.vitam.common.model.processing.ProcessDetail;
import fr.gouv.vitam.common.model.processing.WorkFlow;

/**
 * Admin External Client Interface
 */
public interface AdminExternalClient extends BasicClient, OperationStatusClient {

    /**
     * checkDocuments
     *
     *
     * @param vitamContext the vitam context
     * @param documentType
     * @param stream
     * @return response
     * @throws VitamClientException
     */
    Response checkDocuments(VitamContext vitamContext, AdminCollections documentType,
        InputStream stream)
        throws VitamClientException;


    /**
     * importDocuments
     *
     *
     * @param vitamContext the vitam context
     * @param documentType
     * @param stream
     * @param filename
     * @return the status
     * @throws AccessExternalClientNotFoundException
     * @throws AccessExternalClientException
     */
    Status createDocuments(VitamContext vitamContext, AdminCollections documentType,
        InputStream stream, String filename)
        throws AccessExternalClientException;

    /**
     * Find formats.
     * 
     *
     * @param vitamContext the vitam context
     * @param select select query
     * @return list of formats
     * @throws VitamClientException
     */
    RequestResponse<FileFormatModel> findFormats(VitamContext vitamContext, JsonNode select)
        throws VitamClientException;

    /**
     * Find rules.
     * 
     *
     * @param vitamContext the vitam context
     * @param select select query
     * @return list of rules
     * @throws VitamClientException
     */
    RequestResponse<FileRulesModel> findRules(VitamContext vitamContext, JsonNode select)
        throws VitamClientException;

    /**
     * Find entry contracts.
     * 
     *
     * @param vitamContext the vitam context
     * @param select select query
     * @return list of ingest contrats
     * @throws VitamClientException
     */
    RequestResponse<IngestContractModel> findIngestContracts(VitamContext vitamContext,
        JsonNode select)
        throws VitamClientException;


    /**
     * Find access contracts.
     * 
     *
     * @param vitamContext the vitam context
     * @param select select query
     * @return list of access contrats
     * @throws VitamClientException
     */
    RequestResponse<AccessContractModel> findAccessContracts(VitamContext vitamContext,
        JsonNode select)
        throws VitamClientException;


    /**
     * Find contexts.
     * 
     *
     * @param vitamContext the vitam context
     * @param select select query
     * @return list of contexts
     * @throws VitamClientException
     */
    RequestResponse<ContextModel> findContexts(VitamContext vitamContext, JsonNode select)
        throws VitamClientException;

    /**
     * Find profiles.
     * 
     *
     * @param vitamContext the vitam context
     * @param select select query
     * @return list of profiles
     * @throws VitamClientException
     */
    RequestResponse<ProfileModel> findProfiles(VitamContext vitamContext, JsonNode select)
        throws VitamClientException;

    /**
     * Find accessing registers.
     * 
     *
     * @param vitamContext the vitam context
     * @param select select query
     * @return list of accessing registers
     * @throws VitamClientException
     */
    RequestResponse<AccessionRegisterSummaryModel> findAccessionRegister(
        VitamContext vitamContext, JsonNode select)
        throws VitamClientException;

    /**
     * Get the accession register details matching the given query
     *
     *
     * @param vitamContext the vitam context
     * @param id the id of accession register
     * @param query The DSL Query as a JSON Node
     * @return The AccessionregisterDetails list as a response jsonNode
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientServerException
     * @throws AccessExternalClientNotFoundException
     * @throws AccessUnauthorizedException
     */
    RequestResponse getAccessionRegisterDetail(VitamContext vitamContext, String id,
        JsonNode query)
        throws InvalidParseOperationException, AccessExternalClientServerException,
        AccessExternalClientNotFoundException;

    /**
     * Import a set of contracts after passing the validation steps. If all the contracts are valid, they are stored in
     * the collection and indexed. </BR>
     * The input is invalid in the following situations : </BR>
     * <ul>
     * <li>The json is invalid</li>
     * <li>The json contains 2 ore many contracts having the same name</li>
     * <li>One or more mandatory field is missing</li>
     * <li>A field has an invalid format</li>
     * <li>One or many contracts elready exist in the database</li>
     * </ul>
     * 
     *
     * @param vitamContext the vitam context
     * @param contracts as InputStream
     * @param collection the collection name
     * @return Vitam response
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientException
     */
    RequestResponse importContracts(VitamContext vitamContext, InputStream contracts,
        AdminCollections collection)
        throws InvalidParseOperationException, AccessExternalClientException;

    /**
     * Update the given access contract by query dsl
     * 
     *
     * @param vitamContext the vitam context
     * @param id the given id of the access contract
     * @param queryDsl the given dsl query
     * @return Response status ok or vitam error
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientException
     */
    RequestResponse updateAccessContract(VitamContext vitamContext, String id,
        JsonNode queryDsl)
        throws InvalidParseOperationException, AccessExternalClientException;

    /**
     * Update the given ingest contract by query dsl
     * 
     *
     * @param vitamContext the vitam context
     * @param id the given id of the ingest contract
     * @param queryDsl the given dsl query
     * @return Response status ok or vitam error
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientException
     */
    RequestResponse updateIngestContract(VitamContext vitamContext, String id,
        JsonNode queryDsl)
        throws InvalidParseOperationException, AccessExternalClientException;


    /**
     * Create a profile metadata after passing the validation steps. If profile are json and valid, they are stored in
     * the collection and indexed. </BR>
     * The input is invalid in the following situations : </BR>
     * <ul>
     * <li>The json of file is invalid</li>
     * <li>One or more mandatory field is missing</li>
     * <li>A field has an invalid format</li>
     * <li>Profile already exist in the database</li>
     * </ul>
     *
     *
     * @param vitamContext the vitam context
     * @param profiles as Json InputStream
     * @return Vitam response
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientException
     */
    RequestResponse createProfiles(VitamContext vitamContext, InputStream profiles)
        throws InvalidParseOperationException, AccessExternalClientException;


    /**
     * Save profile file (xsd, rng, ...) corresponding to the profile metadata. As the id of profile metadata is
     * required, this method should be called after creation of profile metadata
     *
     * The profile file will be saved in storage with the name of id of profile metadata
     *
     *
     * @param vitamContext the vitam context
     * @param profileMetadataId
     * @param profile as InputStream
     * @return Vitam response
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientException
     */
    RequestResponse importProfileFile(VitamContext vitamContext, String profileMetadataId,
        InputStream profile)
        throws InvalidParseOperationException, AccessExternalClientException;


    /**
     * Download the profile file according to profileMetadataId
     * 
     *
     * @param vitamContext the vitam context
     * @param profileMetadataId
     * @return Response
     */
    Response downloadProfileFile(VitamContext vitamContext, String profileMetadataId)
        throws AccessExternalClientException, AccessExternalNotFoundException;

    /**
     * import a set of context
     * 
     *
     * @param vitamContext the vitam context
     * @param contexts
     * @return Vitam response
     * @throws InvalidParseOperationException
     * @throws AccessExternalClientServerException
     */
    RequestResponse importContexts(VitamContext vitamContext, InputStream contexts)
        throws InvalidParseOperationException, AccessExternalClientServerException;

    /**
     * Update the context by query dsl
     * 
     *
     * @param vitamContext the vitam context
     * @param id
     * @param queryDsl
     * @return Vitam response
     * @throws AccessExternalClientException
     * @throws InvalidParseOperationException
     */
    RequestResponse updateContext(VitamContext vitamContext, String id, JsonNode queryDsl)
        throws AccessExternalClientException, InvalidParseOperationException;

    /**
     *
     * @param vitamContext the vitam context
     * @param query
     * @throws AccessExternalClientServerException
     * @throws InvalidParseOperationException
     * @throws AccessUnauthorizedException
     */
    RequestResponse checkTraceabilityOperation(VitamContext vitamContext, JsonNode query)
        throws AccessExternalClientServerException, InvalidParseOperationException, AccessUnauthorizedException;

    /**
     * Download the traceability operation file according to operationId
     *
     *
     * @param vitamContext the vitam context
     * @param operationId
     * @throws AccessExternalClientServerException
     * @throws AccessUnauthorizedException
     */
    Response downloadTraceabilityOperationFile(VitamContext vitamContext,
        String operationId)
        throws AccessExternalClientServerException, AccessUnauthorizedException;

    /**
     * Check the existence of objects in the context of an audit
     * 
     *
     * @param vitamContext the vitam context
     * @param auditOption
     * @return Status
     * @throws AccessExternalClientServerException
     */
    RequestResponse launchAudit(VitamContext vitamContext, JsonNode auditOption)
        throws AccessExternalClientServerException;


    /**
     * Find a format by its id.
     * 
     *
     * @param vitamContext the vitam context
     * @param formatId the formatId
     * @return a format
     * @throws VitamClientException
     */
    RequestResponse<FileFormatModel> findFormatById(VitamContext vitamContext,
        String formatId)
        throws VitamClientException;

    /**
     * Find a rule by its id.
     * 
     *
     * @param vitamContext the vitam context
     * @param ruleId the rule id
     * @return a rule
     * @throws VitamClientException
     */
    RequestResponse<FileRulesModel> findRuleById(VitamContext vitamContext, String ruleId)
        throws VitamClientException;

    /**
     * Find an entry contract by its id.
     * 
     *
     * @param vitamContext the vitam context
     * @param contractId the contract id
     * @return an ingest contract
     * @throws VitamClientException
     */
    RequestResponse<IngestContractModel> findIngestContractById(VitamContext vitamContext,
        String contractId)
        throws VitamClientException;


    /**
     * Find an access contracts by its id.
     * 
     *
     * @param vitamContext the vitam context
     * @param contractId the contract id
     * @return an access contract
     * @throws VitamClientException
     */
    RequestResponse<AccessContractModel> findAccessContractById(VitamContext vitamContext,
        String contractId)
        throws VitamClientException;


    /**
     * Find a context by its id
     * 
     *
     * @param vitamContext the vitam context
     * @param contextId the context id
     * @return a context
     * @throws VitamClientException
     */
    RequestResponse<ContextModel> findContextById(VitamContext vitamContext,
        String contextId)
        throws VitamClientException;

    /**
     * Find a profile by its id.
     * 
     *
     * @param vitamContext the vitam context
     * @param profileId the profile tId
     * @return a profile
     * @throws VitamClientException
     */
    RequestResponse<ProfileModel> findProfileById(VitamContext vitamContext,
        String profileId)
        throws VitamClientException;

    /**
     * Find an accession register by its id.
     * 
     *
     * @param vitamContext the vitam context
     * @param accessionRegisterId the accession register id
     * @return an accession register
     * @throws VitamClientException
     */
    RequestResponse<AccessionRegisterSummaryModel> findAccessionRegisterById(
        VitamContext vitamContext, String accessionRegisterId)
        throws VitamClientException;

    /**
     * Find an entry contract by its id.
     *
     * @param vitamContext the vitam context
     * @param query select query
     * @return an ingest contract
     * @throws VitamClientException
     */
    RequestResponse<AgenciesModel> findAgencies(VitamContext vitamContext, JsonNode query) throws VitamClientException;

    /**
     * Find an accession register by its id.
     *
     * @param vitamContext the vitam context
     * @param agencyById the agency id
     * @return an accession register
     * @throws VitamClientException
     */
    RequestResponse<AgenciesModel> findAgencyByID(
        VitamContext vitamContext, String agencyById)
        throws VitamClientException;

    /**
     * Updates the given security profile by query dsl
     *
     * @param vitamContext the vitam context
     * @param identifier the identifier of the security profile to update
     * @param queryDsl the given dsl query
     * @return Response status ok or vitam error
     * @throws VitamClientException
     */
    RequestResponse updateSecurityProfile(VitamContext vitamContext, String identifier,
        JsonNode queryDsl)
        throws VitamClientException;

    /**
     * Find security profiles by query dsl.
     *
     * @param vitamContext the vitam context
     * @param select select query
     * @return list of security profiles
     * @throws VitamClientException
     */
    RequestResponse<SecurityProfileModel> findSecurityProfiles(VitamContext vitamContext,
        JsonNode select)
        throws VitamClientException;

    /**
     * Find a security profile by its identifier.
     *
     *
     * @param vitamContext the vitam context
     * @param identifier the identifier of the security profile
     * @return a security profile
     * @throws VitamClientException
     */
    RequestResponse<SecurityProfileModel> findSecurityProfileById(VitamContext vitamContext,
        String identifier)
        throws VitamClientException;

    /**
     * Update the detail of the profile
     * 
     * @param vitamContext
     * @param profileMetadataId
     * @param queryDsl
     * @return a profile
     * @throws AccessExternalClientException
     */
    RequestResponse updateProfile(VitamContext vitamContext, String profileMetadataId, JsonNode queryDsl)
        throws AccessExternalClientException;

    /**
     * Get the list of operations details
     * 
     *
     * @param vitamContext the vitam context
     * @param query filter query
     * @return list of operations details
     * @throws VitamClientException
     */
    RequestResponse<ProcessDetail> listOperationsDetails(VitamContext vitamContext,
        ProcessQuery query)
        throws VitamClientException;

    /**
     * Update the oprration according to the action
     * 
     *
     * @param vitamContext the vitam context
     * @param actionId
     * @param operationId
     * @return the status
     * @throws VitamClientException
     */
    RequestResponse<ItemStatus> updateOperationActionProcess(VitamContext vitamContext,
        String actionId, String operationId)
        throws VitamClientException;

    /**
     * 
     *
     * @param vitamContext the vitam context
     * @param id
     * @return the details of the operation
     * @throws VitamClientException
     */
    RequestResponse<ItemStatus> getOperationProcessExecutionDetails(
        VitamContext vitamContext, String id)
        throws VitamClientException;

    /**
     * Cancel the operation
     * 
     *
     * @param vitamContext the vitam context
     * @param id
     * @return the status
     * @throws VitamClientException
     * @throws IllegalArgumentException
     */
    RequestResponse<ItemStatus> cancelOperationProcessExecution(VitamContext vitamContext,
        String id)
        throws VitamClientException, IllegalArgumentException;


    // FIXME P1 : is tenant really necessary ?
    /**
     * 
     *
     * @param vitamContext the vitam context@return the Workflow definitions
     * @throws VitamClientException
     */
    RequestResponse<WorkFlow> getWorkflowDefinitions(VitamContext vitamContext) throws VitamClientException;
}
