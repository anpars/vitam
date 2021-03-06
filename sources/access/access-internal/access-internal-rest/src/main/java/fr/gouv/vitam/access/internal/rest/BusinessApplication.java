package fr.gouv.vitam.access.internal.rest;

import fr.gouv.vitam.access.internal.api.AccessInternalModule;
import fr.gouv.vitam.access.internal.common.model.AccessInternalConfiguration;
import fr.gouv.vitam.access.internal.serve.filter.AccessContratIdContainerFilter;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.server.application.resources.VitamServiceRegistry;
import fr.gouv.vitam.common.serverv2.application.CommonBusinessApplication;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static fr.gouv.vitam.common.serverv2.application.ApplicationParameter.CONFIGURATION_FILE_APPLICATION;

public class BusinessApplication extends Application {

    private final CommonBusinessApplication commonBusinessApplication;

    private Set<Object> singletons;
    
    static AccessInternalModule mock = null;
    static VitamServiceRegistry serviceRegistry = null;

    public BusinessApplication(@Context ServletConfig servletConfig) {
        String configurationFile = servletConfig.getInitParameter(CONFIGURATION_FILE_APPLICATION);

        try (final InputStream yamlIS = PropertiesUtils.getConfigAsStream(configurationFile)) {
            final AccessInternalConfiguration
                accessInternalConfiguration = PropertiesUtils.readYaml(yamlIS, AccessInternalConfiguration.class);
            commonBusinessApplication = new CommonBusinessApplication();
            
            singletons = new HashSet<>();
            singletons.addAll(commonBusinessApplication.getResources());
            
            if (mock != null ){
                singletons.add(new AccessInternalResourceImpl(mock));
            } else {
                singletons.add(new AccessInternalResourceImpl(accessInternalConfiguration));
                singletons.add(new LogbookInternalResourceImpl());
            }
            singletons.add(new AccessContratIdContainerFilter());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Set<Class<?>> getClasses() {
        return commonBusinessApplication.getClasses();
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
    
}
