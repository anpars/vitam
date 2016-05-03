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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.builder.request.construct.Delete;
import fr.gouv.vitam.builder.request.construct.configuration.ParserTokens.MULTIFILTER;
import fr.gouv.vitam.builder.request.construct.configuration.ParserTokens.QUERY;
import fr.gouv.vitam.builder.request.construct.query.BooleanQuery;
import fr.gouv.vitam.builder.request.construct.query.ExistsQuery;
import fr.gouv.vitam.builder.request.construct.query.PathQuery;
import fr.gouv.vitam.builder.request.exception.InvalidCreateOperationException;

@SuppressWarnings("javadoc")
public class DeleteTest {

    @Test
    public void testSetMult() {
        final Delete delete = new Delete();
        assertTrue(delete.getFilter().size() == 0);
        delete.setMult(true);
        assertTrue(delete.getFilter().size() == 1);
        delete.setMult(false);
        assertTrue(delete.getFilter().size() == 1);
        assertTrue(delete.filter.has(MULTIFILTER.mult.exactToken()));
        delete.resetFilter();
        assertTrue(delete.getFilter().size() == 0);
    }

    @Test
    public void testAddRequests() {
        final Delete delete = new Delete();
        assertTrue(delete.queries.isEmpty());
        try {
            delete.addQueries(
                    new BooleanQuery(QUERY.and).add(new ExistsQuery(QUERY.exists, "varA"))
                            .setRelativeDepthLimit(5));
            delete.addQueries(new PathQuery("path1", "path2"),
                    new ExistsQuery(QUERY.exists, "varB").setExactDepthLimit(10));
            delete.addQueries(new PathQuery("path3"));
            assertEquals(4, delete.queries.size());
            delete.resetQueries();
            assertEquals(0, delete.queries.size());
        } catch (final InvalidCreateOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetFinalDelete() {
        final Delete delete = new Delete();
        assertTrue(delete.queries.isEmpty());
        try {
            delete.addQueries(new PathQuery("path3"));
            assertEquals(1, delete.queries.size());
            delete.setMult(true);
            final ObjectNode node = delete.getFinalDelete();
            assertEquals(3, node.size());
        } catch (final InvalidCreateOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
