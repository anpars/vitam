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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.builder.request.construct.Select;
import fr.gouv.vitam.builder.request.construct.configuration.ParserTokens.FILTERARGS;
import fr.gouv.vitam.builder.request.construct.configuration.ParserTokens.PROJECTION;
import fr.gouv.vitam.builder.request.construct.configuration.ParserTokens.QUERY;
import fr.gouv.vitam.builder.request.construct.configuration.ParserTokens.SELECTFILTER;
import fr.gouv.vitam.builder.request.construct.query.BooleanQuery;
import fr.gouv.vitam.builder.request.construct.query.ExistsQuery;
import fr.gouv.vitam.builder.request.construct.query.PathQuery;
import fr.gouv.vitam.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;

@SuppressWarnings("javadoc")
public class SelectTest {

    @Test
    public void testAddLimitFilter() {
        final Select select = new Select();
        assertNull(select.filter);
        select.setLimitFilter(0, 0);
        assertFalse(select.filter.has(SELECTFILTER.limit.exactToken()));
        assertFalse(select.filter.has(SELECTFILTER.offset.exactToken()));
        select.setLimitFilter(1, 0);
        assertFalse(select.filter.has(SELECTFILTER.limit.exactToken()));
        assertTrue(select.filter.has(SELECTFILTER.offset.exactToken()));
        select.setLimitFilter(0, 1);
        assertTrue(select.filter.has(SELECTFILTER.limit.exactToken()));
        assertFalse(select.filter.has(SELECTFILTER.offset.exactToken()));
        select.setLimitFilter(1, 1);
        assertTrue(select.filter.has(SELECTFILTER.limit.exactToken()));
        assertTrue(select.filter.has(SELECTFILTER.offset.exactToken()));
        select.resetLimitFilter();
        assertFalse(select.filter.has(SELECTFILTER.limit.exactToken()));
        assertFalse(select.filter.has(SELECTFILTER.offset.exactToken()));
    }

    @Test
    public void testAddHintFilter() {
        final Select select = new Select();
        assertNull(select.filter);
        try {
            select.addHintFilter(FILTERARGS.cache.exactToken());
        } catch (InvalidParseOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(select.filter.has(SELECTFILTER.hint.exactToken()));
        assertEquals(1, select.filter.get(SELECTFILTER.hint.exactToken()).size());
        try {
            select.addHintFilter(FILTERARGS.nocache.exactToken());
        } catch (InvalidParseOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(select.filter.has(SELECTFILTER.hint.exactToken()));
        assertEquals(2, select.filter.get(SELECTFILTER.hint.exactToken()).size());
        select.resetHintFilter();
        assertFalse(select.filter.has(SELECTFILTER.hint.exactToken()));
    }

    @Test
    public void testAddOrderByAscFilter() {
        final Select select = new Select();
        assertNull(select.filter);
        try {
            select.addOrderByAscFilter("var1", "var2");
            assertEquals(2, select.filter.get(SELECTFILTER.orderby.exactToken()).size());
            select.addOrderByAscFilter("var3").addOrderByAscFilter("var4");
            assertEquals(4, select.filter.get(SELECTFILTER.orderby.exactToken()).size());
            select.addOrderByDescFilter("var1", "var2");
            assertEquals(4, select.filter.get(SELECTFILTER.orderby.exactToken()).size());
            select.addOrderByDescFilter("var3").addOrderByDescFilter("var4");
        } catch (InvalidParseOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(4, select.filter.get(SELECTFILTER.orderby.exactToken()).size());
        select.resetOrderByFilter();
        assertFalse(select.filter.has(SELECTFILTER.orderby.exactToken()));
    }

    @Test
    public void testAddUsedProjection() {
        final Select select = new Select();
        assertNull(select.projection);
        try {
            select.addUsedProjection("var1", "var2");
            assertEquals(2, select.projection.get(PROJECTION.fields.exactToken()).size());
            select.addUsedProjection("var3").addUsedProjection("var4");
            assertEquals(4, select.projection.get(PROJECTION.fields.exactToken()).size());
            select.addUnusedProjection("var1", "var2");
            // used/unused identical so don't change the number
            assertEquals(4, select.projection.get(PROJECTION.fields.exactToken()).size());
            select.addUnusedProjection("var3").addUnusedProjection("var4");
        } catch (InvalidParseOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(4, select.projection.get(PROJECTION.fields.exactToken()).size());
        select.resetUsedProjection();
        assertFalse(select.projection.has(PROJECTION.fields.exactToken()));
    }

    @Test
    public void testAddUsageProjection() {
        final Select select = new Select();
        assertNull(select.projection);
        try {
            select.setUsageProjection("usage");
        } catch (InvalidParseOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(select.projection.has(PROJECTION.usage.exactToken()));
        select.resetUsageProjection();
        assertFalse(select.projection.has(PROJECTION.usage.exactToken()));
    }

    @Test
    public void testAddRequests() {
        final Select select = new Select();
        assertTrue(select.queries.isEmpty());
        try {
            select.addQueries(
                    new BooleanQuery(QUERY.and).add(new ExistsQuery(QUERY.exists, "varA"))
                            .setRelativeDepthLimit(5));
            select.addQueries(new PathQuery("path1", "path2"),
                    new ExistsQuery(QUERY.exists, "varB").setExactDepthLimit(10));
            select.addQueries(new PathQuery("path3"));
            assertEquals(4, select.getQueries().size());
            select.setLimitFilter(10, 10);
            try {
                select.addHintFilter(FILTERARGS.cache.exactToken());
                select.addOrderByAscFilter("var1").addOrderByDescFilter("var2");
                select.addUsedProjection("var3").addUnusedProjection("var4");
                select.setUsageProjection("usageId");
                ObjectNode node = select.getFinalSelect();
                assertEquals(4, node.size());
                assertEquals(0, select.getRoots().size());
                select.addRoots("root1", "root2");
                assertEquals(2, select.getRoots().size());
                node = select.getFinalSelect();
                assertEquals(4, node.size());
                ;
                select.resetQueries();
                assertEquals(0, select.getQueries().size());
            } catch (InvalidParseOperationException e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        } catch (final InvalidCreateOperationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
