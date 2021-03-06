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
package fr.gouv.vitam.metadata.api;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.database.builder.request.multiple.UpdateMultiQuery;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamThreadAccessException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.metadata.api.exception.MetaDataAlreadyExistException;
import fr.gouv.vitam.metadata.api.exception.MetaDataDocumentSizeException;
import fr.gouv.vitam.metadata.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.metadata.api.exception.MetaDataNotFoundException;

import org.bson.Document;

import java.util.List;

/**
 * MetaData interface for database operations
 */
public interface MetaData {

    // Unit
    /**
     * insert Unit
     *
     * @param insertRequest as String { $roots: roots, $query : query, $filter : multi, $data : data}
     *
     * @throws InvalidParseOperationException Throw if json format is not correct
     * @throws IllegalArgumentException Throw if arguments of insert query is invalid
     * @throws MetaDataNotFoundException Throw if parent of this unit is not found
     * @throws MetaDataAlreadyExistException Throw if Unit id already exists
     * @throws MetaDataExecutionException Throw if error occurs when send Unit to database
     * @throws MetaDataDocumentSizeException Throw if Unit size is too big
     */
    public void insertUnit(JsonNode insertRequest)
        throws InvalidParseOperationException, IllegalArgumentException, MetaDataNotFoundException,
        MetaDataAlreadyExistException, MetaDataExecutionException, MetaDataDocumentSizeException;


    /**
     * @param operationId
     * @return the list of documents
     */
    List<Document> selectAccessionRegisterOnObjectGroupByOperationId(String operationId);

    /**
     * Search UNITs by Select {@link Select}Query
     *
     * @param selectQuery the query of type JsonNode
     * @return JsonNode {$hits{},$context{},$result:[{}....{}],} <br>
     *         $context will be added later (Access)</br>
     *         $result array of units(can be empty)
     * @throws InvalidParseOperationException Thrown when json format is not correct
     * @throws MetaDataExecutionException Throw if error occurs when send Unit to database
     * @throws MetaDataDocumentSizeException Throw if Unit size is too big
     * @throws MetaDataNotFoundException Throw if unit by id not found
     *
     */
    public RequestResponse<JsonNode> selectUnitsByQuery(JsonNode selectQuery)
        throws InvalidParseOperationException, MetaDataExecutionException,
        MetaDataDocumentSizeException, MetaDataNotFoundException;

    /**
     * Search ObjectGroups by Select {@link Select}Query
     *
     * @param selectQuery the query of type JsonNode
     * @return JsonNode {$hits{},$context{},$result:[{}....{}],} <br>
     *         $context will be added later (Access)</br>
     *         $result array of units(can be empty)
     * @throws InvalidParseOperationException Thrown when json format is not correct
     * @throws MetaDataExecutionException Throw if error occurs when send Unit to database
     * @throws MetaDataDocumentSizeException Throw if Unit size is too big
     * @throws MetaDataNotFoundException Throw if unit by id not found
     *
     */
    RequestResponse<JsonNode> selectObjectGroupsByQuery(JsonNode selectQuery)
        throws MetaDataExecutionException, InvalidParseOperationException,
        MetaDataDocumentSizeException, MetaDataNotFoundException;
    /**
     * Search UNITs by Id {@link Select}Query <br>
     * for this method, the roots will be filled<br>
     * for example request :{
     * <h3>$roots:[{id:"id"}]</h3>,<br>
     * $query{}, ..}
     *
     * @param selectQuery the select query of type JsonNode
     * @param unitId the unit id for query
     * @return JsonNode {$hits{},$context{},$result:[{}....{}],} <br>
     *         $context will be added later (Access)</br>
     *         $result array of units(can be empty)
     * @throws InvalidParseOperationException Thrown when json format is not correct
     * @throws MetaDataExecutionException Throw if error occurs when send Unit to database
     * @throws MetaDataDocumentSizeException Throw if Unit size is too big
     * @throws MetaDataNotFoundException Throw if unit by id not found
     *
     */
    public RequestResponse<JsonNode> selectUnitsById(JsonNode selectQuery, String unitId)
        throws InvalidParseOperationException, MetaDataExecutionException,
        MetaDataDocumentSizeException, MetaDataNotFoundException;

    /**
     * Search ObjectGroups by its Id and a Select Query <br>
     * for this method, the roots will be filled<br>
     * for example request :{
     * <h3>$roots:[{id:"id"}]</h3>,<br>
     * $query{}, ..}
     *
     * @param selectQuery the query to filter results and make projections
     * @param objectGroupId the objectgroup id
     * @return JsonNode {$hits{},$context{},$result:[{}....{}],} <br>
     *         $context will be added later (Access)</br>
     *         $result array of objectgroups(can be empty)
     * @throws InvalidParseOperationException Thrown when json format is not correct
     * @throws MetaDataExecutionException Throw if error occurs when send Unit to database
     * @throws MetaDataDocumentSizeException Throw if Unit size is too big
     * @throws MetaDataNotFoundException
     * 
     * 
     */
    RequestResponse<JsonNode> selectObjectGroupById(JsonNode selectQuery, String objectGroupId)
        throws InvalidParseOperationException, MetaDataDocumentSizeException, MetaDataExecutionException,
        MetaDataNotFoundException;

    /**
     * Update UNITs by Id {@link UpdateMultiQuery}Query <br>
     * for this method, the roots will be filled<br>
     * for example request :{
     * <h3>$roots:[{id:"id"}]</h3>,<br>
     * $query{}, ..}
     *
     * @param updateQuery the update query as JsonNode
     * @param unitId the id of Unit for query
     * @return JsonNode {$hits{},$context{},$result:[{}....{}],} <br>
     *         $context will be added later (Access)</br>
     *         $result array of units(can be empty)
     * @throws InvalidParseOperationException Thrown when json format is not correct
     * @throws MetaDataExecutionException Throw if error occurs when send Unit to database
     * @throws MetaDataDocumentSizeException Throw if Unit size is too big
     * @throws MetaDataNotFoundException Throw if unit does not exist
     *
     */
    public RequestResponse<JsonNode> updateUnitbyId(JsonNode updateQuery, String unitId)
        throws MetaDataNotFoundException, InvalidParseOperationException, MetaDataExecutionException,
        MetaDataDocumentSizeException;


    /**
     * @param objectRequest as JsonNode { $roots: roots, $query : query, $filter : multi, $data : data}
     *
     * @throws InvalidParseOperationException Throw if json format is not correct
     * @throws MetaDataNotFoundException Throw if parent of this unit is not found
     * @throws MetaDataAlreadyExistException Throw if Unit id already exists
     * @throws MetaDataExecutionException Throw if error occurs when send Unit to database
     * @throws MetaDataDocumentSizeException Throw if Unit size is too big
     */
    void insertObjectGroup(JsonNode objectRequest) throws InvalidParseOperationException, MetaDataNotFoundException,
        MetaDataAlreadyExistException, MetaDataExecutionException, MetaDataDocumentSizeException;

    /**
     * find the number of archive unit per originating agency for a operationId
     * @param operationId operation id
     * @return the list of documents
     */
    List<Document> selectAccessionRegisterOnUnitByOperationId(String operationId);


    /**
     * 
     * @param updateRequest
     * @param objectId
     * @throws InvalidParseOperationException
     * @throws MetaDataExecutionException 
     */
    void updateObjectGroupId(JsonNode updateRequest, String objectId)
        throws InvalidParseOperationException, MetaDataExecutionException;
    
    /**
     * Flush Unit Index
     * 
     * @throws IllegalArgumentException  if tenant is wrong
     * @throws VitamThreadAccessException  if tenant is wrong
     */
    public void flushUnit() throws IllegalArgumentException, VitamThreadAccessException;
    
    /**
     * Flush ObjectGroup Index
     *
     * @throws IllegalArgumentException  if tenant is wrong
     * @throws VitamThreadAccessException  if tenant is wrong
     */
    public void flushObjectGroup() throws IllegalArgumentException, VitamThreadAccessException;

}
