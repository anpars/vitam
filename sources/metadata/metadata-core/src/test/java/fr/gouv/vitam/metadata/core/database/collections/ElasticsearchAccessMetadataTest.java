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
package fr.gouv.vitam.metadata.core.database.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fr.gouv.vitam.common.database.server.elasticsearch.ElasticsearchNode;
import fr.gouv.vitam.common.exception.VitamApplicationServerException;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.junit.JunitHelper;
import fr.gouv.vitam.common.junit.JunitHelper.ElasticsearchTestConfiguration;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.metadata.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.metadata.api.exception.MetaDataNotFoundException;

public class ElasticsearchAccessMetadataTest {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(DbRequestTest.class);

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    private final static String CLUSTER_NAME = "vitam-cluster";
    private final static String HOST_NAME = "127.0.0.1";
    private static ElasticsearchAccessMetadata esClient;

    private static final int TENANT_ID = 0;
    private static final String S1 = "{ \"title\":\"title1\", \"_max\": \"5\", \"_min\": \"2\"}";
    private static final String S2 = "{\"_id\":\"id2\", \"title\":\"title2\", \"_up\":\"id1\"}";
    private static final String S3 =
        "{$roots:[\"id2\"],$query:[],$filter:{},$action:[{$set:{\"title\":\"Archive2\"}}]}";
    private static final String S4 = "{$query: { $or : [ { $match : { 'id' : 'id2' , '$max_expansions' : 1  } } ] }}";
    private static final String S6 = "{$match:{ \"id \": \"id2 \"}}";
    private static final String S5 =
        "{$bool : {$must : {$match : {\"title\" : {$query : \"Archive3\",$type : \"boolean\"}}},$filter : {$terms : {\"_up\" : [ \"aeaqaaaaaet33ntwablhaaku6z67pzqaaaas\" ]}}} }";

    private static ElasticsearchTestConfiguration config = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // ES
        try {
            config = JunitHelper.startElasticsearchForTest(tempFolder, CLUSTER_NAME);
        } catch (VitamApplicationServerException e1) {
            assumeTrue(false);
        }

        final List<ElasticsearchNode> nodes = new ArrayList<ElasticsearchNode>();
        nodes.add(new ElasticsearchNode(HOST_NAME, config.getTcpPort()));

        esClient = new ElasticsearchAccessMetadata(CLUSTER_NAME, nodes);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        if (config == null) {
            return;
        }
        JunitHelper.stopElasticsearchForTest(config);
        esClient.close();
    }


    @Test
    public void testElasticsearchAccessMetadatas() {
        // add index
        assertEquals(true, esClient.addIndex(MetadataCollections.C_UNIT));
        // add unit
        final String id = GUIDFactory.newUnitGUID(TENANT_ID).toString();
        assertEquals(true, esClient.addEntryIndex(MetadataCollections.C_UNIT, id, S1));
        // delete unit
        try {
            esClient.deleteEntryIndex(MetadataCollections.C_UNIT, Unit.TYPEUNIQUE, id);
        } catch (final MetaDataExecutionException e) {
            fail(e.getMessage());
        } catch (final MetaDataNotFoundException e) {
            fail(e.getMessage());
        }

        try {
            esClient.deleteEntryIndex(MetadataCollections.C_UNIT, Unit.TYPEUNIQUE,
                GUIDFactory.newUnitGUID(TENANT_ID).toString());
            fail("Unit not found");

        } catch (final MetaDataExecutionException e) {} catch (final MetaDataNotFoundException e) {}

        // delete index
        assertEquals(true, esClient.deleteIndex(MetadataCollections.C_UNIT));

    }

    @Test
    public void testElasticsearchUpdateAccessMetadatas() throws Exception {

        // add index
        assertEquals(true, esClient.addIndex(MetadataCollections.C_UNIT));
        // add unit
        String id = GUIDFactory.newUnitGUID(TENANT_ID).toString();
        assertEquals(true, esClient.addEntryIndex(MetadataCollections.C_UNIT, id, S1));

        // update index
        assertEquals(true, esClient.updateEntryIndex(MetadataCollections.C_UNIT, id, S3));

    }

}
