/**
Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)

consultation-vitam@culture.gouv.fr

This software is a computer program whose purpose is to implement a digital 
archiving back-office system managing high volumetry securely and efficiently.

This software is governed by the CeCILL 2.1 license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL 2.1
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL 2.1 license and that you accept its terms.
 */
package fr.gouv.vitam.core.database.collections;

import static fr.gouv.vitam.builder.request.construct.QueryHelper.and;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.eq;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.exists;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.gt;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.in;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.isNull;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.lt;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.missing;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.ne;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.nin;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.or;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.path;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.range;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.size;
import static fr.gouv.vitam.builder.request.construct.QueryHelper.term;
import static fr.gouv.vitam.builder.request.construct.UpdateActionHelper.add;
import static fr.gouv.vitam.builder.request.construct.UpdateActionHelper.inc;
import static fr.gouv.vitam.builder.request.construct.UpdateActionHelper.min;
import static fr.gouv.vitam.builder.request.construct.UpdateActionHelper.push;
import static fr.gouv.vitam.builder.request.construct.UpdateActionHelper.set;
import static fr.gouv.vitam.builder.request.construct.UpdateActionHelper.unset;
import static fr.gouv.vitam.builder.request.construct.VitamFieldsHelper.all;
import static fr.gouv.vitam.builder.request.construct.VitamFieldsHelper.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import fr.gouv.vitam.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.api.exception.MetaDataMaxDepthException;
import fr.gouv.vitam.builder.request.construct.Delete;
import fr.gouv.vitam.builder.request.construct.Insert;
import fr.gouv.vitam.builder.request.construct.Select;
import fr.gouv.vitam.builder.request.construct.Update;
import fr.gouv.vitam.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.guid.GUID;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.core.database.collections.MongoDbAccess.VitamCollections;
import fr.gouv.vitam.parser.request.parser.DeleteParser;
import fr.gouv.vitam.parser.request.parser.InsertParser;
import fr.gouv.vitam.parser.request.parser.RequestParser;
import fr.gouv.vitam.parser.request.parser.RequestParserHelper;
import fr.gouv.vitam.parser.request.parser.SelectParser;
import fr.gouv.vitam.parser.request.parser.UpdateParser;


public class DbRequestTest {
	private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(DbRequestTest.class);

	private static final String DATABASE_HOST = "localhost";
	private static final int DATABASE_PORT = 12345;
	private static final boolean CREATE = false;
	private static final boolean DROP = false;
	private static final String MY_INT = "MyInt";
	private static final String CREATED_DATE = "CreatedDate";
	private static final String DESCRIPTION = "Description";
	private static final String TITLE = "Title";
	private static final String MY_BOOLEAN = "MyBoolean";
	private static final String MY_FLOAT = "MyFloat";
	private static final String UNKNOWN_VAR = "unknown";
	private static final String VALUE_MY_TITLE = "MyTitle";
	private static final String ARRAY_VAR = "ArrayVar";
	private static final String ARRAY2_VAR = "Array2Var";
	private static final String EMPTY_VAR = "EmptyVar";
	static final int tenantId = 0;
	static final int platformId = 10;
	static MongoDbAccess mongoDbAccess;
	static MongodExecutable mongodExecutable;
	static MongoClient mongoClient;
	static MongoDbVarNameAdapter mongoDbVarNameAdapter;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		MongodStarter starter = MongodStarter.getDefaultInstance();
		IMongodConfig mongodConfig = new MongodConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(DATABASE_PORT, Network.localhostIsIPv6()))
				.build();

		mongodExecutable = starter.prepare(mongodConfig);
		mongodExecutable.start();
		MongoClientOptions options = MongoDbAccess.getMongoClientOptions();
		
		mongoClient = new MongoClient(new ServerAddress(DATABASE_HOST, DATABASE_PORT), options);
		mongoDbAccess = new MongoDbAccess(mongoClient, "vitam-test", CREATE);
		mongoDbVarNameAdapter = new MongoDbVarNameAdapter();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (DROP) {
			MongoDbAccess.reset();
		}
		mongoDbAccess.closeFinal();
		mongodExecutable.stop();
	}

	/**
	 * Test method for {@link fr.gouv.vitam.database.collections.DbRequest#execRequest(fr.gouv.vitam.request.parser.RequestParser, fr.gouv.vitam.database.collections.Result)}.
	 */
	@Test
	public void testExecRequest() {
		// input data
		GUID uuid = GUIDFactory.newUnitGUID(tenantId);
		try {
			DbRequest dbRequest = new DbRequest();
			// INSERT
			String insertRequestString = createInsertRequestWithUUID(uuid);
			// Now considering insert request and parsing it as in Data Server (POST command)
			InsertParser insertParser = new InsertParser(mongoDbVarNameAdapter);
			try {
				insertParser.parse(insertRequestString);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("InsertParser: {}", insertParser);
			// Now execute the request
			executeRequest(dbRequest, insertParser);

			// SELECT
			String selectRequestString = createSelectRequestWithUUID(uuid);
			// Now considering select request and parsing it as in Data Server (GET command)
			SelectParser selectParser = new SelectParser(mongoDbVarNameAdapter);
			try {
				selectParser.parse(selectRequestString);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", selectParser);
			// Now execute the request
			executeRequest(dbRequest, selectParser);

			// UPDATE
			String updateRequestString = createUpdateRequestWithUUID(uuid);
			// Now considering update request and parsing it as in Data Server (PATCH command)
			UpdateParser updateParser = new UpdateParser(mongoDbVarNameAdapter);
			try {
				updateParser.parse(updateRequestString);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("UpdateParser: {}", updateParser);
			// Now execute the request
			executeRequest(dbRequest, updateParser);

			// SELECT ALL
			selectRequestString = createSelectAllRequestWithUUID(uuid);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				selectParser.parse(selectRequestString);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", selectParser);
			// Now execute the request
			executeRequest(dbRequest, selectParser);

			// DELETE
			String deleteRequestString = createDeleteRequestWithUUID(uuid);
			// Now considering delete request and parsing it as in Data Server (DELETE command)
			DeleteParser deleteParser = new DeleteParser(mongoDbVarNameAdapter);
			try {
				deleteParser.parse(deleteRequestString);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("DeleteParser: " + deleteParser.toString());
			// Now execute the request
			executeRequest(dbRequest, deleteParser);
		} finally {
			// clean
			VitamCollections.Cunit.getCollection().deleteOne(new Document(Unit.ID, uuid.toString()));
		}
	}

	/**
	 * Test method for {@link fr.gouv.vitam.database.collections.DbRequest#execRequest(fr.gouv.vitam.request.parser.RequestParser, fr.gouv.vitam.database.collections.Result)}.
	 */
	@Test
	public void testExecRequestThroughRequestParserHelper() {
		// input data
		GUID uuid = GUIDFactory.newUnitGUID(tenantId);
		try {
			DbRequest dbRequest = new DbRequest();
			RequestParser requestParser = null;
			// INSERT
			String insertRequestString = createInsertRequestWithUUID(uuid);
			// Now considering insert request and parsing it as in Data Server (POST command)
			try {
				requestParser = 
						RequestParserHelper.getParser(insertRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("InsertParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// SELECT
			String selectRequestString = createSelectRequestWithUUID(uuid);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				requestParser = 
						RequestParserHelper.getParser(selectRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// UPDATE
			String updateRequestString = createUpdateRequestWithUUID(uuid);
			// Now considering update request and parsing it as in Data Server (PATCH command)
			try {
				requestParser = 
						RequestParserHelper.getParser(updateRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("UpdateParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// SELECT ALL
			selectRequestString = createSelectAllRequestWithUUID(uuid);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				requestParser = 
						RequestParserHelper.getParser(selectRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// DELETE
			String deleteRequestString = createDeleteRequestWithUUID(uuid);
			// Now considering delete request and parsing it as in Data Server (DELETE command)
			try {
				requestParser = 
						RequestParserHelper.getParser(deleteRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("DeleteParser: " + requestParser.toString());
			// Now execute the request
			executeRequest(dbRequest, requestParser);
		} finally {
			// clean
			VitamCollections.Cunit.getCollection().deleteOne(new Document(Unit.ID, uuid.toString()));
		}
	}


	/**
	 * Test method for {@link fr.gouv.vitam.database.collections.DbRequest#execRequest(fr.gouv.vitam.request.parser.RequestParser, fr.gouv.vitam.database.collections.Result)}.
	 */
	@Test
	public void testExecRequestThroughAllCommands() {
		// input data
	    GUID uuid = GUIDFactory.newUnitGUID(tenantId);
		try {
			DbRequest dbRequest = new DbRequest();
			RequestParser requestParser = null;
			// INSERT
			String insertRequestString = createInsertRequestWithUUID(uuid);
			// Now considering insert request and parsing it as in Data Server (POST command)
			try {
				requestParser = 
						RequestParserHelper.getParser(insertRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("InsertParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// SELECT
			String selectRequestString = createSelectRequestWithUUID(uuid);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				requestParser = 
						RequestParserHelper.getParser(selectRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// UPDATE
			String updateRequestString = clientRichUpdateBuild(uuid);
			// Now considering update request and parsing it as in Data Server (PATCH command)
			try {
				requestParser = 
						RequestParserHelper.getParser(updateRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("UpdateParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// SELECT ALL
			selectRequestString = createSelectAllRequestWithUUID(uuid);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				requestParser = 
						RequestParserHelper.getParser(selectRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// SELECT ALL
			selectRequestString = clientRichSelectAllBuild(uuid);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				requestParser = 
						RequestParserHelper.getParser(selectRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// DELETE
			String deleteRequestString = createDeleteRequestWithUUID(uuid);
			// Now considering delete request and parsing it as in Data Server (DELETE command)
			try {
				requestParser = 
						RequestParserHelper.getParser(deleteRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("DeleteParser: " + requestParser.toString());
			// Now execute the request
			executeRequest(dbRequest, requestParser);
		} finally {
			// clean
			VitamCollections.Cunit.getCollection().deleteOne(new Document(Unit.ID, uuid.toString()));
		}
	}

	/**
	 * Test method for {@link fr.gouv.vitam.database.collections.DbRequest#execRequest(fr.gouv.vitam.request.parser.RequestParser, fr.gouv.vitam.database.collections.Result)}.
	 * @throws Exception 
	 */
	@Test
	public void testExecRequestMultiple() throws Exception {
		// input data
		GUID uuid = GUIDFactory.newUnitGUID(tenantId);
		GUID uuid2 = GUIDFactory.newUnitGUID(tenantId);
		try {
			DbRequest dbRequest = new DbRequest();
			RequestParser requestParser = null;
			
			// INSERT
			String insertRequestString = createInsertRequestWithUUID(uuid);
			// Now considering insert request and parsing it as in Data Server (POST command)
			requestParser = RequestParserHelper.getParser(insertRequestString, mongoDbVarNameAdapter);
			LOGGER.debug("InsertParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			insertRequestString = createInsertChild2ParentRequest(uuid2, uuid);
			// Now considering insert request and parsing it as in Data Server (POST command)
			requestParser = RequestParserHelper.getParser(insertRequestString, mongoDbVarNameAdapter);
			LOGGER.debug("InsertParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);
			
			// SELECT
			String selectRequestString = createSelectRequestWithUUID(uuid);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				requestParser = 
						RequestParserHelper.getParser(selectRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// SELECT
			selectRequestString = clientSelect2Build(uuid2);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				requestParser = 
						RequestParserHelper.getParser(selectRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// SELECT
			selectRequestString = clientSelectMultipleBuild(uuid, uuid2);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				requestParser = 
						RequestParserHelper.getParser(selectRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// UPDATE
			String updateRequestString = createUpdateRequestWithUUID(uuid);
			// Now considering update request and parsing it as in Data Server (PATCH command)
			try {
				requestParser = 
						RequestParserHelper.getParser(updateRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("UpdateParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// SELECT ALL
			selectRequestString = createSelectAllRequestWithUUID(uuid);
			// Now considering select request and parsing it as in Data Server (GET command)
			try {
				requestParser = 
						RequestParserHelper.getParser(selectRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("SelectParser: {}", requestParser);
			// Now execute the request
			executeRequest(dbRequest, requestParser);

			// DELETE
			String deleteRequestString = clientDelete2Build(uuid2);
			// Now considering delete request and parsing it as in Data Server (DELETE command)
			try {
				requestParser = 
						RequestParserHelper.getParser(deleteRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("DeleteParser: " + requestParser.toString());
			// Now execute the request
			executeRequest(dbRequest, requestParser);
			deleteRequestString = createDeleteRequestWithUUID(uuid);
			// Now considering delete request and parsing it as in Data Server (DELETE command)
			try {
				requestParser = 
						RequestParserHelper.getParser(deleteRequestString, mongoDbVarNameAdapter);
			} catch (InvalidParseOperationException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			LOGGER.debug("DeleteParser: " + requestParser.toString());
			// Now execute the request
			executeRequest(dbRequest, requestParser);
		} finally {
			// clean
			VitamCollections.Cunit.getCollection().deleteOne(new Document(Unit.ID, uuid.toString()));
			VitamCollections.Cunit.getCollection().deleteOne(new Document(Unit.ID, uuid2.toString()));
		}
	}
	
	@Test
	public void testInsertUnitRequest() throws Exception {
	    GUID uuid = GUIDFactory.newUnitGUID(tenantId);
	    GUID uuid2 = GUIDFactory.newUnitGUID(tenantId);
        DbRequest dbRequest = new DbRequest();
        RequestParser requestParser = null;
        
        requestParser = RequestParserHelper.getParser(createInsertRequestWithUUID(uuid), mongoDbVarNameAdapter);
        executeRequest(dbRequest, requestParser);
        assertEquals(1, VitamCollections.Cunit.getCollection().count());

        requestParser = RequestParserHelper.getParser(createInsertChild2ParentRequest(uuid2, uuid), mongoDbVarNameAdapter);
        executeRequest(dbRequest, requestParser);
        assertEquals(2, VitamCollections.Cunit.getCollection().count());
	}

	/**
	 * @param dbRequest
	 * @param requestParser
	 */
	private void executeRequest(DbRequest dbRequest, RequestParser requestParser) {
		try {
			Result result = dbRequest.execRequest(requestParser, null);
			LOGGER.info("XXXXXXXX "+requestParser.getClass().getSimpleName()+" Result XXXXXXXX: " + result);
			assertEquals("Must have 1 result", result.getNbResult(), 1);
			assertEquals("Must have 1 result", result.getCurrentIds().size(), 1);
		} catch (InstantiationException | IllegalAccessException | MetaDataExecutionException
				| InvalidParseOperationException | MetaDataMaxDepthException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * @param uuid
	 * @return
	 */
	private String createDeleteRequestWithUUID(GUID uuid) {
		Delete delete = new Delete();
		try {
			delete.addQueries(and().add(eq(id(), uuid.toString()), eq(TITLE, VALUE_MY_TITLE)));
		} catch (InvalidCreateOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String deleteRequestString = delete.getFinalDelete().toString();
		LOGGER.debug("DeleteString: " + deleteRequestString);
		return deleteRequestString;
	}

	/**
	 * @param uuid
	 * @return
	 */
	private String createSelectAllRequestWithUUID(GUID uuid) {
		String selectRequestString;
		Select select = new Select();
		try {
			select.addUsedProjection(all())
			.addQueries(and().add(eq(id(), uuid.toString()), eq(TITLE, VALUE_MY_TITLE)));
		} catch (InvalidCreateOperationException | InvalidParseOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		selectRequestString = select.getFinalSelect().toString();
		LOGGER.debug("SelectAllString: " + selectRequestString);
		return selectRequestString;
	}

	/**
	 * @param uuid
	 * @return
	 */
	private String createUpdateRequestWithUUID(GUID uuid) {
		Update update = new Update();
		try {
			update.addActions(set("NewVar", false), inc(MY_INT, 2), set(DESCRIPTION, "New description"))
			.addQueries(and().add(eq(id(), uuid.toString()), eq(TITLE, VALUE_MY_TITLE)));
		} catch (InvalidCreateOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String updateRequestString = update.getFinalUpdate().toString();
		LOGGER.debug("UpdateString: " + updateRequestString);
		return updateRequestString;
	}

	/**
	 * @param uuid
	 * @return
	 */
	private String createSelectRequestWithUUID(GUID uuid) {
		Select select = new Select();
		try {
			select.addUsedProjection(id(), TITLE, DESCRIPTION)
			.addQueries(and().add(eq(id(), uuid.toString()), eq(TITLE, VALUE_MY_TITLE)));
		} catch (InvalidCreateOperationException | InvalidParseOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String selectRequestString = select.getFinalSelect().toString();
		LOGGER.debug("SelectString: " + selectRequestString);
		return selectRequestString;
	}

	/**
	 * @param uuid
	 * @return
	 */
	private String createInsertRequestWithUUID(GUID uuid) {
		// INSERT
		List<String> list = Arrays.asList("val1", "val2");
		ObjectNode data = JsonHandler.createObjectNode().put(id(), uuid.toString())
				.put(TITLE, VALUE_MY_TITLE).put(DESCRIPTION, "Ma description")
				.put(CREATED_DATE, ""+LocalDateUtil.now()).put(MY_INT, 20)
				.put(MY_BOOLEAN, false).putNull(EMPTY_VAR).put(MY_FLOAT, 2.0);
		try {
			data.putArray(ARRAY_VAR).addAll((ArrayNode) JsonHandler.toJsonNode(list));
		} catch (InvalidParseOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		try {
			data.putArray(ARRAY2_VAR).addAll((ArrayNode) JsonHandler.toJsonNode(list));
		} catch (InvalidParseOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		// Create Insert command as in Internal Vitam Modules
		Insert insert = new Insert();
		insert.addData(data);
		String insertRequestString = insert.getFinalInsert().toString();
		LOGGER.debug("InsertString: " + insertRequestString);
		return insertRequestString;
	}
	
	   /**
     * @param uuid child
     * @param uuid2 parent
     * @return
     */
    private String createInsertChild2ParentRequest(GUID child, GUID parent) throws Exception {
        ObjectNode data = JsonHandler.createObjectNode().put(id(), child.toString())
                .put(TITLE, VALUE_MY_TITLE+"2").put(DESCRIPTION, "Ma description2")
                .put(CREATED_DATE, ""+LocalDateUtil.now()).put(MY_INT, 10);
        Insert insert = new Insert();
        insert.addData(data).addQueries(exists("Title"));
        String insertRequestString = insert.getFinalInsert().toString();
        LOGGER.debug("InsertString: " + insertRequestString);
        return insertRequestString;
    }

	/**
	 * @param uuid
	 * @return
	 */
	private String clientRichSelectAllBuild(GUID uuid) {
		String selectRequestString;
		Select select = new Select();
		try {
			select.addUsedProjection(all())
			.addQueries(and().add(eq(id(), uuid.toString()), eq(TITLE, VALUE_MY_TITLE),
					exists(CREATED_DATE), missing(UNKNOWN_VAR), isNull(EMPTY_VAR),
					or().add(in(ARRAY_VAR, "val1"), nin(ARRAY_VAR, "val3")),
					gt(MY_INT, 1), lt(MY_INT, 100),
					ne(MY_BOOLEAN, true), range(MY_FLOAT, 1.0, false, 100.0, true),
					term(TITLE, VALUE_MY_TITLE), size(ARRAY2_VAR, 2)));
		} catch (InvalidCreateOperationException | InvalidParseOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		selectRequestString = select.getFinalSelect().toString();
		LOGGER.debug("SelectAllString: " + selectRequestString);
		return selectRequestString;
	}

	/**
	 * @param uuid
	 * @return
	 */
	private String clientRichUpdateBuild(GUID uuid) {
		Update update = new Update();
		try {
			update.addActions(set("NewVar", false), inc(MY_INT, 2), set(DESCRIPTION, "New description"),
					unset(UNKNOWN_VAR), push(ARRAY_VAR, "val2"), min(MY_FLOAT, 1.5),
					add(ARRAY2_VAR, "val2"))
			.addQueries(and().add(eq(id(), uuid.toString()), eq(TITLE, VALUE_MY_TITLE)));
		} catch (InvalidCreateOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String updateRequestString = update.getFinalUpdate().toString();
		LOGGER.debug("UpdateString: " + updateRequestString);
		return updateRequestString;
	}

	/**
	 * @param uuid
	 * @return
	 */
	private String clientSelect2Build(GUID uuid) {
		Select select = new Select();
		try {
			select.addUsedProjection(id(), TITLE, DESCRIPTION)
			.addQueries(eq(id(), uuid.toString()).setDepthLimit(2));
		} catch (InvalidCreateOperationException | InvalidParseOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String selectRequestString = select.getFinalSelect().toString();
		LOGGER.debug("SelectString: " + selectRequestString);
		return selectRequestString;
	}

	/**
	 * @param uuid father
	 * @param uuid2 son
	 * @return
	 */
	private String clientSelectMultipleBuild(GUID uuid, GUID uuid2) {
		Select select = new Select();
		try {
			select.addUsedProjection(id(), TITLE, DESCRIPTION)
			.addQueries(and().add(eq(id(), uuid.toString()), eq(TITLE, VALUE_MY_TITLE)),
					eq(id(), uuid2.toString()));
		} catch (InvalidCreateOperationException | InvalidParseOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String selectRequestString = select.getFinalSelect().toString();
		LOGGER.debug("SelectString: " + selectRequestString);
		return selectRequestString;
	}

	/**
	 * @param uuid
	 * @return
	 */
	private String clientDelete2Build(GUID uuid) {
		Delete delete = new Delete();
		try {
			delete.addQueries(path(uuid.toString()));
		} catch (InvalidCreateOperationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		String deleteRequestString = delete.getFinalDelete().toString();
		LOGGER.debug("DeleteString: " + deleteRequestString);
		return deleteRequestString;
	}
	
	@Test
	public void testSetDebug(){
		DbRequest request = new DbRequest();
		assertEquals(true, request.debug);
		request.setDebug(false);
		assertEquals(false, request.debug);
	}
	
	@Test
	public void testResult() throws Exception{
		DbRequest dbRequest = new DbRequest();
		GUID uuid = GUIDFactory.newUnitGUID(tenantId);
		String insertRequestString = createInsertRequestWithUUID(uuid);
		InsertParser insertParser = new InsertParser(mongoDbVarNameAdapter);
		insertParser.parse(insertRequestString);
		
		LOGGER.debug("InsertParser: {}", insertParser);
		Result result = dbRequest.execRequest(insertParser, null);
		assertEquals("Document{{Result=null}}", result.getFinal().toString());
		
		Bson projection = null;
		result.setFinal(projection);
		assertEquals(1, result.currentIds.size());
		assertEquals(1, (int) result.nbResult);
		
		result.putFrom(result);
		assertEquals(1, result.currentIds.size());
		assertEquals(1, (int) result.nbResult);
		
		Set<String> currentIds = new HashSet<String>();
		currentIds.add("UNITS");
		currentIds.add("OBJECTGROUPS");
		result.setCurrentIds(currentIds);
		assertEquals(2, result.currentIds.size());
	}
	
	@Test
	public void testMongoDbAccess(){		
		MongoDbAccess.reset();
		MongoDatabase db = mongoDbAccess.getMongoDatabase();
		assertEquals(0, db.getCollection("Unit").count());
		assertEquals(0, db.getCollection("Objectgroup").count());
		mongoDbAccess = new MongoDbAccess(mongoClient, "vitam-test", CREATE);
		assertNotNull(mongoDbAccess.toString());
	}
}