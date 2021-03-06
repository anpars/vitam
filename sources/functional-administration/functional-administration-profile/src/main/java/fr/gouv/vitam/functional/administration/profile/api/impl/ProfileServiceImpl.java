/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019) <p> contact.vitam@culture.gouv.fr <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently. <p> This software is governed by the CeCILL 2.1 license under French law and
 * abiding by the rules of distribution of free software. You can use, modify and/ or redistribute the software under
 * the terms of the CeCILL 2.1 license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". <p> As a counterpart to the access to the source code and rights to copy, modify and
 * redistribute granted by the license, users are provided only with a limited warranty and the software's author, the
 * holder of the economic rights, and the successive licensors have only limited liability. <p> In this respect, the
 * user's attention is drawn to the risks associated with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software, that may mean that it is complicated to
 * manipulate, and that also therefore means that it is reserved for developers and experienced professionals having
 * in-depth computer knowledge. Users are therefore encouraged to load and test the software's suitability as regards
 * their requirements in conditions enabling the security of their systems and/or data to be ensured and, more
 * generally, to use and operate it in the same conditions as regards security. <p> The fact that you are presently
 * reading this means that you have had knowledge of the CeCILL 2.1 license and that you accept its terms.
 */

package fr.gouv.vitam.functional.administration.profile.api.impl;

import static fr.gouv.vitam.common.LocalDateUtil.now;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.database.builder.query.action.UpdateActionHelper;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.database.builder.request.single.Update;
import fr.gouv.vitam.common.database.parser.request.adapter.SingleVarNameAdapter;
import fr.gouv.vitam.common.database.parser.request.single.SelectParserSingle;
import fr.gouv.vitam.common.database.parser.request.single.UpdateParserSingle;
import fr.gouv.vitam.common.database.server.DbRequestResult;
import fr.gouv.vitam.common.error.VitamCode;
import fr.gouv.vitam.common.error.VitamError;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.common.model.administration.ProfileFormat;
import fr.gouv.vitam.common.model.administration.ProfileModel;
import fr.gouv.vitam.common.model.administration.ProfileStatus;
import fr.gouv.vitam.common.parameter.ParameterHelper;
import fr.gouv.vitam.common.security.SanityChecker;
import fr.gouv.vitam.common.stream.VitamAsyncInputStreamResponse;
import fr.gouv.vitam.functional.administration.common.Profile;
import fr.gouv.vitam.functional.administration.common.exception.ProfileNotFoundException;
import fr.gouv.vitam.functional.administration.common.exception.ReferentialException;
import fr.gouv.vitam.functional.administration.common.server.FunctionalAdminCollections;
import fr.gouv.vitam.functional.administration.common.server.MongoDbAccessAdminImpl;
import fr.gouv.vitam.functional.administration.counter.SequenceType;
import fr.gouv.vitam.functional.administration.counter.VitamCounterService;
import fr.gouv.vitam.functional.administration.profile.api.ProfileService;
import fr.gouv.vitam.functional.administration.profile.core.ProfileManager;
import fr.gouv.vitam.functional.administration.profile.core.ProfileValidator;
import fr.gouv.vitam.functional.administration.profile.core.ProfileValidator.RejectionCause;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClient;
import fr.gouv.vitam.logbook.operations.client.LogbookOperationsClientFactory;
import fr.gouv.vitam.storage.engine.client.StorageClient;
import fr.gouv.vitam.storage.engine.client.StorageClientFactory;
import fr.gouv.vitam.storage.engine.client.exception.StorageAlreadyExistsClientException;
import fr.gouv.vitam.storage.engine.client.exception.StorageNotFoundClientException;
import fr.gouv.vitam.storage.engine.client.exception.StorageServerClientException;
import fr.gouv.vitam.storage.engine.common.exception.StorageNotFoundException;
import fr.gouv.vitam.storage.engine.common.model.StorageCollectionType;
import fr.gouv.vitam.storage.engine.common.model.request.ObjectDescription;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageAlreadyExistException;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageNotFoundException;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageServerException;
import fr.gouv.vitam.workspace.client.WorkspaceClient;
import fr.gouv.vitam.workspace.client.WorkspaceClientFactory;

/**
 * The implementation of the profile servie This implementation manage creation, update, ... profiles with any given
 * format (xsd, rng)
 */
public class ProfileServiceImpl implements ProfileService {
    private static final String PROFILE_SERVICE_ERROR = "Profile service Error";

    private static final String FUNCTIONAL_MODULE_PROFILE = "FunctionalModule-Profile";

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ProfileServiceImpl.class);

    private static final String PROFILE_IS_MANDATORY_PATAMETER = "profiles parameter is mandatory";
    private static final String PROFILES_IMPORT_EVENT = "STP_IMPORT_PROFILE_JSON";
    private static final String PROFILES_UPATE_EVENT = "STP_UPDATE_PROFILE_JSON";
    private static final String PROFILES_FILE_IMPORT_EVENT = "STP_IMPORT_PROFILE_FILE";
    private static final String PROFILE_NOT_FOUND = "Update a not found profile";
    private static final String OP_PROFILE_STORAGE = "OP_PROFILE_STORAGE";
    private static final String UPDATED_DIFFS = "updatedDiffs";

    private static final String THE_PROFILE_STATUS_MUST_BE_ACTIVE_OR_INACTIVE_BUT_NOT =
        "The profile status must be ACTIVE or INACTIVE but not ";
    public static final String PROFILE_FORMAT_SHOULD_BE_XSD_OR_RNG = "Profile Format should be XSD or RNG : ";
    public static final String PROFILE_IDENTIFIER_ALREADY_EXISTS_IN_DATABASE =
        "Profile identifier already exists in database ";
    public static final String PROFILE_IDENTIFIER_MUST_BE_STRING = "Profile identifier shoud be a string ";
    private final MongoDbAccessAdminImpl mongoAccess;
    private final LogbookOperationsClient logBookclient;
    private final WorkspaceClientFactory workspaceClientFactory;
    private static final String DEFAULT_STORAGE_STRATEGY = "default";
    private final VitamCounterService vitamCounterService;
    private final ProfileManager manager;
    private static final String _TENANT = "_tenant";
    private static final String _ID = "_id";

    /**
     * Constructor
     *
     * @param mongoAccess MongoDB client
     * @param workspaceClientFactory
     * @param vitamCounterService
     */
    public ProfileServiceImpl(MongoDbAccessAdminImpl mongoAccess, WorkspaceClientFactory workspaceClientFactory,
        VitamCounterService vitamCounterService) {
        this.mongoAccess = mongoAccess;
        this.workspaceClientFactory = workspaceClientFactory;
        this.vitamCounterService = vitamCounterService;
        logBookclient = LogbookOperationsClientFactory.getInstance().getClient();
        manager = new ProfileManager(logBookclient);
    }


    @Override
    public RequestResponse<ProfileModel> createProfiles(List<ProfileModel> profileModelList)
        throws VitamException {
        ParametersChecker.checkParameter(PROFILE_IS_MANDATORY_PATAMETER, profileModelList);

        if (profileModelList.isEmpty()) {
            return new RequestResponseOK<>();
        }
        boolean slaveMode = vitamCounterService
            .isSlaveFunctionnalCollectionOnTenant(SequenceType.PROFILE_SEQUENCE.getCollection(),
                ParameterHelper.getTenantParameter());

        manager.logStarted(PROFILES_IMPORT_EVENT, null);

        final Set<String> profileIdentifiers = new HashSet<>();
        final Set<String> profileNames = new HashSet<>();
        ArrayNode profilesToPersist;

        final VitamError error =
            getVitamError(VitamCode.PROFILE_FILE_IMPORT_ERROR.getItem(), "Global create profile error").setHttpCode(
                Response.Status.BAD_REQUEST.getStatusCode());

        try {

            for (final ProfileModel pm : profileModelList) {


                // if a profile have and id
                if (null != pm.getId()) {
                    error.addToErrors(getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(),
                        RejectionCause.rejectIdNotAllowedInCreate(pm.getName()).getReason()));
                    continue;
                }

                // if a profile with the same identifier is already treated mark the current one as duplicated
                if (ParametersChecker.isNotEmpty(pm.getIdentifier())) {
                    if (profileIdentifiers.contains(pm.getIdentifier())) {
                        error.addToErrors(
                            getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(), "Duplicate profiles")
                                .setMessage(
                                    "Profile identifier " + pm.getIdentifier() + " already exists in the json"));
                        continue;
                    } else {
                        profileIdentifiers.add(pm.getIdentifier());
                    }
                }

                // if a profile with the same name is already treated mark the current one as duplicated
                if (profileNames.contains(pm.getName())) {
                    error.addToErrors(getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(), "Duplicate profiles")
                        .setMessage("Profile name " + pm.getName() + " already exists in the json"));
                    continue;
                }


                // mark the current profile as treated
                profileNames.add(pm.getName());

                // validate profile
                if (manager.validateProfile(pm, error)) {
                    pm.setId(GUIDFactory.newProfileGUID(ParameterHelper.getTenantParameter()).getId());
                }
                if (pm.getTenant() == null) {
                    pm.setTenant(ParameterHelper.getTenantParameter());
                }

                if (slaveMode) {
                    final Optional<ProfileValidator.RejectionCause> result =
                        manager.checkDuplicateInIdentifierSlaveModeValidator().validate(pm);
                    result.ifPresent(t -> error
                        .addToErrors(new VitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem()).setMessage(result
                            .get().getReason())));
                }

            }

            if (null != error.getErrors() && !error.getErrors().isEmpty()) {
                // log book + application log
                // stop
                final String errorsDetails =
                    error.getErrors().stream().map(c -> c.getMessage() + " : " + c.getDescription())
                        .collect(Collectors.joining(","));
                manager.logValidationError(PROFILES_IMPORT_EVENT, null, errorsDetails);
                return error;
            }


            profilesToPersist = JsonHandler.createArrayNode();
            for (final ProfileModel pm : profileModelList) {
                setIdentifier(slaveMode, pm);

                final ObjectNode profileNode = (ObjectNode) JsonHandler.toJsonNode(pm);
                JsonNode jsonNode = profileNode.remove(VitamFieldsHelper.id());
                /* contract is valid, add it to the list to persist */

                if (jsonNode != null) {
                    profileNode.set(_ID, jsonNode);
                }
                JsonNode hashTenant = profileNode.remove(VitamFieldsHelper.tenant());
                if (hashTenant != null) {
                    profileNode.set(_TENANT, hashTenant);
                }
                profilesToPersist.add(profileNode);
            }
            // at this point no exception occurred and no validation error detected
            // persist in collection
            // profilesToPersist.values().stream().map();
            // TODO: 3/28/17 create insertDocuments method that accepts VitamDocument instead of ArrayNode, so we can
            // use Profile at this point
            mongoAccess.insertDocuments(profilesToPersist, FunctionalAdminCollections.PROFILE).close();
        } catch (final Exception exp)

        {
            final String err = new StringBuilder("Import profiles error : ").append(exp.getMessage()).toString();
            manager.logFatalError(PROFILES_IMPORT_EVENT, null, err);
            return getVitamError(VitamCode.PROFILE_FILE_IMPORT_ERROR.getItem(), err).setHttpCode(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        manager.logSuccess(PROFILES_IMPORT_EVENT, null, null);


        return new RequestResponseOK<ProfileModel>().addAllResults(profileModelList)
            .setHttpCode(Response.Status.CREATED.getStatusCode());
    }

    private void setIdentifier(boolean slaveMode, ProfileModel pm)
        throws InvalidCreateOperationException, InvalidParseOperationException, ReferentialException {
        if (!slaveMode) {
            String code = vitamCounterService.getNextSequenceAsString(ParameterHelper.getTenantParameter(),
                SequenceType.PROFILE_SEQUENCE.getName());
            pm.setIdentifier(code);
        }
    }


    @Override
    public RequestResponse importProfileFile(String profileIdentifier,
        InputStream profileFile)
        throws VitamException {

        final ProfileModel profileMetadata = findByIdentifier(profileIdentifier);
        final VitamError vitamError =
            getVitamError(VitamCode.PROFILE_FILE_IMPORT_ERROR.getItem(), "Global import profile error").setHttpCode(
                Response.Status.BAD_REQUEST.getStatusCode());
        if (null == profileMetadata) {
            LOGGER.error("No profile metadata found with identifier : " + profileIdentifier +
                ", to import the file, the metadata profile must be created first");

            manager.logValidationError(PROFILES_FILE_IMPORT_EVENT, profileIdentifier,
                "No profile metadata found with identifier : " + profileIdentifier +
                    ", to import the file, the metadata profile must be created first");
            return vitamError.addToErrors(getVitamError(VitamCode.PROFILE_FILE_IMPORT_ERROR.getItem(),
                "No profile metadata found with identifier : " + profileIdentifier +
                    ", to import the file, the metadata profile must be created first"));
        }

        manager.logStarted(PROFILES_FILE_IMPORT_EVENT, profileMetadata.getId());


        boolean cannotCopy = false;
        try {
            // To create a copy of inputstream, validateProfileFile use DocumentDuilder wich in some case close the
            // stream.
            byte[] byteArray = IOUtils.toByteArray(profileFile);

            final InputStream profileFileCopy = new ByteArrayInputStream(byteArray);

            /*
             * Validate the stream
             */
            boolean isValide = manager.validateProfileFile(profileMetadata, profileFileCopy, vitamError);

            if (!isValide) {
                final String errorsDetails =
                    vitamError.getErrors().stream().map(c -> c.getMessage()).collect(Collectors.joining(","));
                manager.logValidationError(PROFILES_FILE_IMPORT_EVENT, profileMetadata.getId(),
                    "Profile file validate error : " + errorsDetails);
                return vitamError;
            }

            profileFile = new ByteArrayInputStream(byteArray);

            byteArray = null;
        } catch (final IOException e) {
            cannotCopy = true;
        }



        String extention = "xsd";
        if (profileMetadata.getFormat().equals(ProfileFormat.RNG)) {
            extention = "rng";
        }

        Integer tenantId = ParameterHelper.getTenantParameter();
        final String containerName =
            String.format("%d_profiles_%s_%s", tenantId, profileMetadata.getIdentifier(), now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        final String fileName =
            String.format("%d_profile_%s_%s.%s", tenantId, profileMetadata.getIdentifier(), now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")), extention);
        final String uri = String.format("%s/%s", extention, fileName);


        final String oldPath = profileMetadata.getPath();
        final String newPath = fileName;


        // Final path in the workspace : tenant_profiles/format(xsd|rng)/tenant_profile_id.format(xsd|rng)

        try (WorkspaceClient workspaceClient = workspaceClientFactory.getClient()) {
            workspaceClient.createContainer(containerName);
            workspaceClient.putObject(containerName, uri, profileFile);

            // If the copy of the stream is not processed then, we use workspace to save the file then read it to
            // validate it
            if (cannotCopy) {
                final InputStream fileFromWorkSpace =
                    workspaceClient.getObject(containerName, uri).readEntity(InputStream.class);
                boolean isValide = manager.validateProfileFile(profileMetadata, fileFromWorkSpace, vitamError);

                if (!isValide) {
                    final String errorsDetails =
                        vitamError.getErrors().stream().map(c -> c.getMessage()).collect(Collectors.joining(","));
                    manager.logValidationError(PROFILES_FILE_IMPORT_EVENT, profileMetadata.getId(),
                        "Profile file validate error : " + errorsDetails);
                    workspaceClient.deleteContainer(containerName, true);
                    return vitamError;
                }
            }


            final StorageClientFactory storageClientFactory = StorageClientFactory.getInstance();

            final ObjectDescription description = new ObjectDescription();
            description.setWorkspaceContainerGUID(containerName);
            description.setWorkspaceObjectURI(uri);

            try (final StorageClient storageClient = storageClientFactory.getClient()) {

                storageClient.storeFileFromWorkspace(DEFAULT_STORAGE_STRATEGY, StorageCollectionType.PROFILES, fileName,
                    description);
                manager.logInProgress(OP_PROFILE_STORAGE, profileMetadata.getId(), StatusCode.OK);



                profileMetadata.setPath(fileName);


                final UpdateParserSingle updateParserActive = new UpdateParserSingle(new SingleVarNameAdapter());
                Update update = new Update();
                update.setQuery(eq("Identifier", profileMetadata.getIdentifier()));
                update.addActions(
                    UpdateActionHelper.set("Path", fileName),
                    UpdateActionHelper.set("LastUpdate", LocalDateUtil.now().toString()));
                updateParserActive.parse(update.getFinalUpdate());
                final JsonNode queryDsl = updateParserActive.getRequest().getFinalUpdate();

                mongoAccess.updateData(queryDsl, FunctionalAdminCollections.PROFILE).close();


            } catch (ReferentialException | StorageAlreadyExistsClientException | StorageNotFoundClientException |
                InvalidCreateOperationException |
                StorageServerClientException | ContentAddressableStorageNotFoundException e) {
                final String err =
                    new StringBuilder("Import profiles error : ").append(e.getMessage()).toString();
                LOGGER.error(err, e);
                manager.logFatalError(OP_PROFILE_STORAGE, profileMetadata.getId(), err);
                return getVitamError(VitamCode.GLOBAL_INTERNAL_SERVER_ERROR.getItem(), err).setHttpCode(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            } finally {
                workspaceClient.deleteContainer(containerName, true);
            }
        } catch (ContentAddressableStorageAlreadyExistException | ContentAddressableStorageServerException e) {
            String err =
                new StringBuilder("Import profiles storage workspace error : ").append(e.getMessage()).toString();
            LOGGER.error(err, e);
            manager.logFatalError(OP_PROFILE_STORAGE, profileMetadata.getId(), err);
            return getVitamError(VitamCode.GLOBAL_INTERNAL_SERVER_ERROR.getItem(), err).setHttpCode(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        } finally {
            if (null != profileFile) {
                try {
                    profileFile.close();
                } catch (final IOException e) {
                    LOGGER.error("Error while closing the profile file stream!");
                }
            }
        }

        String wellFormedJson = null;
        try {
            final ObjectNode object = JsonHandler.createObjectNode();
            final ObjectNode msg = JsonHandler.createObjectNode();
            msg.put("updateField", "Path");
            msg.put("oldPath", oldPath);
            msg.put("newPath", newPath);
            object.set("profileUpdate", msg);

            wellFormedJson = SanityChecker.sanitizeJson(object);
        } catch (InvalidParseOperationException e) {
            // Do nothing
        }

        manager.logSuccess(PROFILES_FILE_IMPORT_EVENT, profileMetadata.getId(), wellFormedJson);

        return new RequestResponseOK().setHttpCode(Response.Status.CREATED.getStatusCode());
    }

    @Override
    public Response downloadProfileFile(String profileIdentifier)
        throws ProfileNotFoundException, InvalidParseOperationException, ReferentialException {

        final ProfileModel profileMetadata = findByIdentifier(profileIdentifier);
        if (null == profileMetadata) {
            LOGGER.error("No profile metadata found with id : " + profileIdentifier +
                ", to import the file, the metadata profile must be created first");
            throw new ProfileNotFoundException("No profile metadata found with id : " + profileIdentifier +
                ", to import the file, the metadata profile must be created first");
        }

        if (Strings.isNullOrEmpty(profileMetadata.getPath()) || profileMetadata.getPath().isEmpty()) {
            LOGGER.error("The profile metadata found with an id : " + profileIdentifier +
                ", does not have an xsd or an rng file yet");
            throw new ProfileNotFoundException(
                "The profile metadata found with id : " + profileIdentifier + ", does not have a xsd or rng file yet");
        }

        // A valid operation found : download the related file
        try (StorageClient storageClient = StorageClientFactory.getInstance().getClient()) {

            final Response response = storageClient.getContainerAsync(DEFAULT_STORAGE_STRATEGY,
                profileMetadata.getPath(), StorageCollectionType.PROFILES);
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
            headers.put(HttpHeaders.CONTENT_DISPOSITION, "filename=" + profileMetadata.getPath());
            return new VitamAsyncInputStreamResponse(response,
                Status.OK, headers);
        } catch (final StorageServerClientException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ReferentialException(e);
        } catch (final StorageNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ProfileNotFoundException(e);
        }
    }

    @Override
    public RequestResponse<ProfileModel> updateProfile(String identifier, JsonNode jsonDsl) throws VitamException {

        final ProfileModel profileModel = findByIdentifier(identifier);
        return updateProfile(profileModel, jsonDsl);
    }


    @Override
    public RequestResponse<ProfileModel> updateProfile(ProfileModel profileModel, JsonNode jsonDsl)
        throws VitamException {

        if (profileModel == null) {
            return getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(),
                PROFILE_NOT_FOUND)
                    .setHttpCode(
                        Response.Status.NOT_FOUND.getStatusCode());
        }

        Map<String, List<String>> updateDiffs;
        manager.logStarted(PROFILES_UPATE_EVENT, profileModel.getId());

        if (jsonDsl == null || !jsonDsl.isObject()) {
            manager.logValidationError(PROFILES_UPATE_EVENT, profileModel.getId(),
                "Update query dsl must be an object and not null");
            return getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(),
                "Update query dsl must be an object and not null : " + profileModel.getIdentifier())
                    .setHttpCode(
                        Response.Status.BAD_REQUEST.getStatusCode());
        }

        final VitamError error = getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(),
            "Update profile error : " + profileModel.getIdentifier())
                .setHttpCode(
                    Response.Status.BAD_REQUEST.getStatusCode());

        final JsonNode actionNode = jsonDsl.get(BuilderToken.GLOBAL.ACTION.exactToken());

        for (final JsonNode fieldToSet : actionNode) {
            final JsonNode fieldName = fieldToSet.get(BuilderToken.UPDATEACTION.SET.exactToken());
            if (fieldName != null) {
                final Iterator<String> it = fieldName.fieldNames();
                while (it.hasNext()) {
                    final String field = it.next();
                    final JsonNode value = fieldName.findValue(field);
                    validateUpdateAction(profileModel, error, field, value);
                }
            }
        }

        if (error.getErrors() != null && error.getErrors().size() > 0) {
            final String errorsDetails =
                error.getErrors().stream().map(c -> c.getMessage()).collect(Collectors.joining(","));
            manager.logValidationError(PROFILES_UPATE_EVENT, profileModel.getId(), errorsDetails);

            return error;
        }


        try (DbRequestResult result = mongoAccess.updateData(jsonDsl, FunctionalAdminCollections.PROFILE)) {
            updateDiffs = result.getDiffs();
        } catch (final ReferentialException e) {
            final String err = new StringBuilder("Update profile error : ").append(e.getMessage()).toString();
            manager.logFatalError(PROFILES_UPATE_EVENT, profileModel.getId(), err);
            error.setCode(VitamCode.GLOBAL_INTERNAL_SERVER_ERROR.getItem())
                .setDescription(err)
                .setHttpCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

            return error;
        }
        List<String> diff = updateDiffs.get(profileModel.getId());
        String wellFormedJson = null;
        try {
            final ObjectNode object = JsonHandler.createObjectNode();
            object.put(UPDATED_DIFFS, Joiner.on(" ").join(diff));
            wellFormedJson = SanityChecker.sanitizeJson(object);
        } catch (InvalidParseOperationException e) {
            // Do nothing
        }
        manager.logSuccess(PROFILES_UPATE_EVENT, profileModel.getId(), wellFormedJson);
        return new RequestResponseOK().setHttpCode(Response.Status.OK.getStatusCode());
    }

    /**
     * Validate dsl
     *
     * @param profileModel
     * @param error
     * @param field
     * @param value
     */
    private void validateUpdateAction(ProfileModel profileModel, final VitamError error, final String field,
        final JsonNode value) {
        if (Profile.STATUS.equals(field)) {
            if (!(ProfileStatus.ACTIVE.name().equals(value.asText()) || ProfileStatus.INACTIVE
                .name().equals(value.asText()))) {
                error.addToErrors(getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(),
                    THE_PROFILE_STATUS_MUST_BE_ACTIVE_OR_INACTIVE_BUT_NOT + value.asText())
                        .setHttpCode(
                            Response.Status.BAD_REQUEST.getStatusCode()));
            }
        }

        if (Profile.FORMAT.equals(field)) {
            if (!(ProfileFormat.XSD.name().equals(value.asText()) || ProfileFormat.RNG
                .name().equals(value.asText()))) {
                error.addToErrors(getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(),
                    PROFILE_FORMAT_SHOULD_BE_XSD_OR_RNG + value.asText())
                        .setHttpCode(
                            Response.Status.BAD_REQUEST.getStatusCode()));
            }
        }

        if (ProfileModel.IDENTIFIER.equals(field)) {
            if (!value.isTextual()) {
                error.addToErrors(getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(),
                    PROFILE_IDENTIFIER_MUST_BE_STRING + " : " + value.asText())
                        .setHttpCode(
                            Response.Status.BAD_REQUEST.getStatusCode()));

            } else if (!profileModel.getIdentifier().equals(value.asText())) {
                Optional<RejectionCause> validateIdentifier = manager.createCheckDuplicateInDatabaseValidator()
                    .validate(new ProfileModel().setIdentifier(value.asText()));
                if (validateIdentifier.isPresent()) {
                    error.addToErrors(getVitamError(VitamCode.PROFILE_VALIDATION_ERROR.getItem(),
                        PROFILE_IDENTIFIER_ALREADY_EXISTS_IN_DATABASE + " : " + value.asText())
                            .setHttpCode(
                                Response.Status.BAD_REQUEST.getStatusCode()));
                }
            }
        }
    }

    @Override
    public ProfileModel findByIdentifier(String identifier)
        throws ReferentialException, InvalidParseOperationException {
        SanityChecker.checkParameter(identifier);
        final SelectParserSingle parser = new SelectParserSingle(new SingleVarNameAdapter());
        parser.parse(new Select().getFinalSelect());
        try {
            parser.addCondition(eq(Profile.IDENTIFIER, identifier));
        } catch (final InvalidCreateOperationException e) {
            throw new ReferentialException(e);
        }

        try (DbRequestResult result =
            mongoAccess.findDocuments(parser.getRequest().getFinalSelect(), FunctionalAdminCollections.PROFILE)) {
            final List<ProfileModel> list = result.getDocuments(Profile.class, ProfileModel.class);
            if (list.isEmpty()) {
                return null;
            }
            return list.get(0);
        }
    }

    @Override
    public RequestResponseOK<ProfileModel> findProfiles(JsonNode queryDsl)
        throws ReferentialException, InvalidParseOperationException {
        try (DbRequestResult result =
            mongoAccess.findDocuments(queryDsl, FunctionalAdminCollections.PROFILE)) {
            return result.getRequestResponseOK(queryDsl, Profile.class, ProfileModel.class);
        }
    }


    private VitamError getVitamError(String vitamCode, String error) {
        return new VitamError(vitamCode).setMessage(PROFILE_SERVICE_ERROR).setState("ko")
            .setContext(FUNCTIONAL_MODULE_PROFILE).setDescription(error);
    }

    @Override
    public void close() {
        if (null != logBookclient) {
            logBookclient.close();
        }
    }
}
