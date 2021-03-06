/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.functionaltest.cucumber.step;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientNotFoundException;
import fr.gouv.vitam.access.external.common.exception.AccessExternalClientServerException;
import fr.gouv.vitam.common.FileUtil;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ContextModel;

/**
 * Context Step
 */
public class ContextStep {

    private World world;
    private String fileName;
    private String query;

    private static final String OPERATION_ID = "Operation-Id";
    private String contextIdentifier;
    private String contextName;

    /**
     * generic model result
     */
    private JsonNode model;
    public ContextStep(World world) {
        this.world = world;
    }

    /**
     * define a context file
     *
     * @param fileName name of a context file
     */
    @Given("^un fichier contexte nommé (.*)$")
    public void a_context_file_named(String fileName) {
        this.fileName = fileName;
    }

    /**
     * define a context name
     *
     * @param contextName name of a context
     */
    @Given("^un contexte nommé (.*)$")
    public void a_context_named(String contextName) {
        this.contextName = contextName;
    }

    /**
     * define a context query fileName
     *
     * @param queryContextfileName name of a context
     */
    @Given("^un fichier requete nommé (.*)$")
    public void a_file_query_named(String queryContextfileName) {
        this.fileName = queryContextfileName;
    }


    @Then("^j'importe ce contexte en succès")
    public void success_upload_context()
        throws IOException,
        AccessExternalClientServerException,
        InvalidParseOperationException {

        Path context = Paths.get(world.getBaseDirectory(), fileName);
        VitamContext vitamContext = new VitamContext(world.getTenantId());
        vitamContext.setApplicationSessionId(world.getApplicationSessionId());

        final RequestResponse response = world.getAdminClient()
            .createContexts(vitamContext, Files.newInputStream(context, StandardOpenOption.READ));

        assertThat(response.isOk()).isTrue();
    }

    /**
     * @throws AccessExternalClientServerException
     * @throws InvalidParseOperationException
     * @throws IOException
     */
    @Then("^j'importe ce contexte en échec")
    public void fail_upload_context()
        throws AccessExternalClientServerException, InvalidParseOperationException, IOException {
        Path context = Paths.get(world.getBaseDirectory(), fileName);
        final RequestResponse response =
            world.getAdminClient().createContexts(
                new VitamContext(world.getTenantId()).setApplicationSessionId(world.getApplicationSessionId()),
                Files.newInputStream(context, StandardOpenOption.READ));
        assertThat(Response.Status.BAD_REQUEST.getStatusCode() == response.getStatus());
    }

    @When("^je modifie le contexte nommé (.*)$")
    public void update_context_by_query(String name)
        throws InvalidParseOperationException, VitamClientException, IOException, AccessExternalClientException,
        InvalidCreateOperationException {
        this.contextName = name;
        Path queryFile = Paths.get(world.getBaseDirectory(), fileName);
        this.query = FileUtil.readFile(queryFile.toFile());
        if (world.getOperationId() != null) {
            this.query = this.query.replace(OPERATION_ID, world.getOperationId());
        }

        JsonNode queryDsl = JsonHandler.getFromString(query);

        find_a_context_id(name);

        VitamContext context = new VitamContext(world.getTenantId());
        context.setApplicationSessionId(world.getApplicationSessionId());

        RequestResponse<ContextModel> requestResponse =
            world.getAdminClient().updateContext(context, contextIdentifier, queryDsl);

        assertThat(requestResponse.isOk()).isTrue();
    }



    @When("^je recherche un contexte nommé (.*)$")
    public void find_a_context_id(String name)
        throws VitamClientException, InvalidCreateOperationException, InvalidParseOperationException {
        Select select = new Select();
        select.setQuery(QueryHelper.eq(ContextModel.TAG_NAME, name));
        JsonNode queryDsl = select.getFinalSelect();
        RequestResponse<ContextModel> requestResponse =
            world.getAdminClient().findContexts(
                new VitamContext(world.getTenantId()).setAccessContract(null)
                    .setApplicationSessionId(world.getApplicationSessionId()),
                queryDsl);
        if (requestResponse.isOk()) {
            ContextModel model = ((RequestResponseOK<ContextModel>) requestResponse).getFirstResult();
            this.model = JsonHandler.toJsonNode(model);
            contextIdentifier = ((RequestResponseOK<ContextModel>) requestResponse).getFirstResult().getIdentifier();
            return;
        }
        throw new VitamClientException("No context was found");

    }

    @Then("^les métadonnées du context sont$")
    public void metadata_are(DataTable dataTable) throws Throwable {
        List<List<String>> raws = dataTable.raw();
        for (List<String> raw : raws) {
            String index = raw.get(0);
            String value = raw.get(1);
            if (this.model.get(index).isArray()) {
                assertThat(value).isEqualTo(this.model.get(index).toString());
            } else {
                assertThat(value).contains(this.model.get(index).asText());

            }
        }
    }
}
