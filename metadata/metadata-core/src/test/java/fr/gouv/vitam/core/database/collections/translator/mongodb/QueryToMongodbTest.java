package fr.gouv.vitam.core.database.collections.translator.mongodb;

import static org.junit.Assert.*;

import java.util.List;

import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.gouv.vitam.builder.request.construct.Select;
import fr.gouv.vitam.builder.request.construct.query.Query;
import fr.gouv.vitam.core.database.collections.MongoDbHelper;
import fr.gouv.vitam.core.database.collections.translator.mongodb.QueryToMongodb;
import fr.gouv.vitam.parser.request.parser.SelectParser;

@SuppressWarnings("javadoc")
public class QueryToMongodbTest {
    private static final String exampleMd = "{ $roots : [ 'id0' ], $query : [ "
            + "{ $path : [ 'id1', 'id2'] },"
            + "{ $and : [ "
            + "{$exists : 'mavar1'}, "
            + "{$missing : 'mavar2'}, "
            + "{$isNull : 'mavar3'}, "
            + "{ $or : [ "
            + "{$in : { 'mavar4' : [1, 2, 'maval1'] }}, "
            + "{ $nin : { 'mavar5' : ['maval2', true] } } ] } ] },"
            + "{ $not : [ "
            + "{ $size : { 'mavar5' : 5 } }, "
            + "{ $gt : { 'mavar6' : 7 } }, "
            + "{ $lte : { 'mavar7' : 8 } } ] , $exactdepth : 4},"
            + "{ $not : [ "
            + "{ $eq : { 'mavar8' : 5 } }, "
            + "{ $ne : { 'mavar9' : 'ab' } }, "
            + "{ $range : { 'mavar10' : { $gte : 12, $lte : 20} } } ], $depth : 1}, "
            + "{ $and : [ { $term : { 'mavar14' : 'motMajuscule', 'mavar15' : 'simplemot' } } ] }, "
            + "{ $regex : { 'mavar14' : '^start?aa.*' }, $depth : -1 } "
            + "], "
            + "$filter : {$offset : 100, $limit : 1000, $hint : ['cache'], "
            + "$orderby : { maclef1 : 1 , maclef2 : -1,  maclef3 : 1 } },"
            + "$projection : {$fields : {#dua : 1, #all : 1}, $usage : 'abcdef1234' } }";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }
    
    private Select createSelect() {
        try {
            final SelectParser request1 = new SelectParser();
            request1.parse(exampleMd);
            assertNotNull(request1);
            return request1.getRequest();
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
            return null;
        }
    }

    @Test
    public void testGetCommands() {
        try {
            Select select = createSelect();
            Bson bsonRoot = QueryToMongodb.getRoots("_up", select.getRoots());
            List<Query> list = select.getQueries();
            for (int i = 0; i < list.size(); i++) {
                System.out.println(i+" = "+list.get(i).toString());
                Bson bsonQuery = QueryToMongodb.getCommand(list.get(i));
                Bson pseudoRequest = QueryToMongodb.getFullCommand(bsonQuery, bsonRoot);
                System.out.println(i+" = "+MongoDbHelper.bsonToString(pseudoRequest, false));
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
