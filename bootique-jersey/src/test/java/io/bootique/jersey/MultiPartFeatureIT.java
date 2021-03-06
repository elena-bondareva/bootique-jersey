package io.bootique.jersey;

import com.google.inject.Module;
import io.bootique.test.junit.BQTestFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertEquals;

// see https://github.com/bootique/bootique-jersey/issues/11
public class MultiPartFeatureIT {

    @ClassRule
    public static BQTestFactory TEST_FACTORY = new BQTestFactory().autoLoadModules();

    private Client multiPartClient;

    @BeforeClass
    public static void startJetty() {
        TEST_FACTORY.app("-s").modules(createTestModule()).run();
    }

    protected static Module createTestModule() {
        return b -> JerseyModule.extend(b).addFeature(MultiPartFeature.class).addResource(Resource.class);
    }

    @Before
    public void before() {
        ClientConfig config = new ClientConfig();
        config.register(MultiPartFeature.class);
        this.multiPartClient = ClientBuilder.newClient(config);
    }

    @Test
    public void testResponse() {

        FormDataBodyPart part = new FormDataBodyPart("upload", "I am a part", MediaType.TEXT_PLAIN_TYPE);
        FormDataMultiPart multipart = new FormDataMultiPart();
        multipart.bodyPart(part);

        Response r = multiPartClient.target("http://127.0.0.1:8080/").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(multipart, multipart.getMediaType()));

        assertEquals(Status.OK.getStatusCode(), r.getStatus());
        assertEquals("{\"message\":\"I am a part\"}", r.readEntity(String.class));

        r.close();
    }

    @Path("/")
    public static class Resource {

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public Response uploadMultiPart(@FormDataParam("upload") String upload) {
            return Response.ok().entity("{\"message\":\"" + upload + "\"}").build();
        }
    }
}
