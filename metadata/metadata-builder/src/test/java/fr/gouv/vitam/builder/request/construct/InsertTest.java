/*******************************************************************************
 * This file is part of Vitam Project.
 * 
 * Copyright Vitam (2012, 2015)
 *
 * This software is governed by the CeCILL 2.1 license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 *******************************************************************************/
package fr.gouv.vitam.builder.request.construct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.builder.request.construct.Insert;
import fr.gouv.vitam.builder.request.construct.configuration.ParserTokens.MULTIFILTER;
import fr.gouv.vitam.builder.request.construct.configuration.ParserTokens.QUERY;
import fr.gouv.vitam.builder.request.construct.query.BooleanQuery;
import fr.gouv.vitam.builder.request.construct.query.ExistsQuery;
import fr.gouv.vitam.builder.request.construct.query.PathQuery;
import fr.gouv.vitam.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.json.JsonHandler;

@SuppressWarnings("javadoc")
public class InsertTest {

    @Test
    public void testSetMult() {
        final Insert insert = new Insert();
        assertTrue(insert.getFilter().size() == 0);
        insert.setMult(true);
        assertTrue(insert.getFilter().size() == 1);
        insert.setMult(false);
        assertTrue(insert.getFilter().size() == 1);
        assertTrue(insert.filter.has(MULTIFILTER.mult.exactToken()));
        insert.resetFilter();
        assertTrue(insert.getFilter().size() == 0);
    }

    @Test
    public void testAddData() {
        final Insert insert = new Insert();
        assertNull(insert.data);
        insert.addData(JsonHandler.createObjectNode().put("var1", 1));
        insert.addData(JsonHandler.createObjectNode().put("var2", "val"));
        assertEquals(2, insert.data.size());
        insert.resetData();
        assertEquals(0, insert.data.size());
    }

    @Test
    public void testAddRequests() {
        final Insert insert = new Insert();
        assertTrue(insert.queries.isEmpty());
        try {
            insert.addQueries(
                    new BooleanQuery(QUERY.and).add(new ExistsQuery(QUERY.exists, "varA"))
                            .setRelativeDepthLimit(5));
            insert.addQueries(new PathQuery("path1", "path2"),
                    new ExistsQuery(QUERY.exists, "varB").setExactDepthLimit(10));
            insert.addQueries(new PathQuery("path3"));
            assertEquals(4, insert.queries.size());
            insert.resetQueries();
            assertEquals(0, insert.queries.size());
        } catch (final InvalidCreateOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetFinalInsert() {
        final Insert insert = new Insert();
        assertTrue(insert.queries.isEmpty());
        try {
            insert.addQueries(new PathQuery("path3"));
            assertEquals(1, insert.queries.size());
            insert.setMult(true);
            insert.addData(JsonHandler.createObjectNode().put("var1", 1));
            final ObjectNode node = insert.getFinalInsert();
            assertEquals(4, node.size());
        } catch (final InvalidCreateOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
