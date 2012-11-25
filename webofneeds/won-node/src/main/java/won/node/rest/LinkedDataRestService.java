package won.node.rest;

import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import won.protocol.exception.NoSuchConnectionException;
import won.protocol.exception.NoSuchNeedException;
import won.protocol.model.Connection;
import won.protocol.model.Need;
import won.protocol.model.WON;
import won.protocol.service.NeedInformationService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;

/**
 * Date: 2/15/11
 * Time: 12:27 AM
 *
 * @author Florian Kleedorfer
 */

@Path("/") //the servlet-mapping defines where this root path is published externally
public class LinkedDataRestService {
    final Logger logger = LoggerFactory.getLogger(getClass());

    private String needURIPrefix;
    private String connectionURIPrefix;
    private String dataURIPrefix;


    @Autowired
    private NeedInformationService needInformationService;

    @GET
    @Path("/resource/need/{identifier}")
    public Response showNeedResource(
            @Context UriInfo uriInfo,
            @PathParam("identifier") String identifier){
        return Response.seeOther(URI.create(this.dataURIPrefix + "/need/"+identifier)).build();
    }

    @GET
    @Path("/resource/need/{identifier}/connections")
    public Response showNeedResourceConnections(
            @Context UriInfo uriInfo,
            @PathParam("identifier") String identifier){
        return Response.seeOther(URI.create(this.dataURIPrefix + "/need/"+identifier+"/connections")).build();
    }


    @GET
    @Path("/resource/connection/{identifier}")
    public Response showConnectionResource(
            @Context UriInfo uriInfo,
            @PathParam("identifier") String identifier){
        return Response.seeOther(URI.create(this.dataURIPrefix + "/connection/" +identifier)).build();
    }


    @GET
    @Path("/data/need")
    @Produces("application/rdf+xml,application/x-turtle,text/turtle,text/rdf+n3,application/json")
    public Response listNeedURIs(
            @Context UriInfo uriInfo,
            @DefaultValue("-1") @QueryParam("page") int page) {
        logger.debug("listNeedURIs() called");
        Collection<URI> uris = null;
        if (page >= 0) {
            uris = needInformationService.listNeedURIs(page);
        } else {
            uris = needInformationService.listNeedURIs();
        }
        Model model = ModelFactory.createDefaultModel();
        Bag needs = model.createBag();
        for (URI needURI : uris) {
            needs.add(needURI);
        }
        return Response.ok(model).build();
    }

    @GET
    @Path("/data/connection")
    @Produces("application/rdf+xml,application/x-turtle,text/turtle,text/rdf+n3,application/json")
    public Response listConnectionURIs(
            @Context UriInfo uriInfo,
            @DefaultValue("-1") @QueryParam("page") int page) {
        logger.debug("listNeedURIs() called");
        Collection<URI> uris = null;
        if (page >= 0) {
            uris = needInformationService.listConnectionURIs(page);
        } else {
            uris = needInformationService.listConnectionURIs();
        }
        Model model = ModelFactory.createDefaultModel();
        Bag needs = model.createBag();
        for (URI needURI : uris) {
            needs.add(needURI);
        }
        return Response.ok(model).build();
    }

    @GET
    @Path("/data/need/{identifier}")
    @Produces("application/rdf+xml,application/x-turtle,text/turtle,text/rdf+n3,application/json")
    public Response readNeed(
            @Context UriInfo uriInfo,
            @PathParam("identifier") String identifier) {
        logger.debug("readNeed() called");
        URI needUri = URI.create(this.needURIPrefix+"/"+identifier);

        try {
            Need need = needInformationService.readNeed(needUri);
            Model model = ModelFactory.createDefaultModel();
            model.createResource()
                .addProperty(WON.STATE,need.getState().name())
                .addProperty(WON.RESOURCE_URI, model.createResource(needUri.toString()))
                .addProperty(WON.HAS_CONNECTIONS, model.createResource(needUri+"/connections"))
            ;
            return Response.ok(model).build();
        } catch (NoSuchNeedException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/data/connection/{identifier}")
    @Produces("application/rdf+xml,application/x-turtle,text/turtle,text/rdf+n3,application/json")
    public Response readConnection(
            @Context UriInfo uriInfo,
            @PathParam("identifier") String identifier) {
        logger.debug("readConnection() called");
        URI connectionUri = URI.create(this.connectionURIPrefix+"/"+identifier);

        try {
            Connection connection = needInformationService.readConnection(connectionUri);
            Model model = ModelFactory.createDefaultModel();
            model.createResource()
                    .addProperty(WON.STATE, connection.getState().name())
                    .addProperty(WON.REMOTE_CONNECTION, connection.getRemoteConnectionURI().toString())
                    .addProperty(WON.REMOTE_NEED, connection.getRemoteNeedURI().toString())
                    .addProperty(WON.RESOURCE_URI, model.createResource(connectionUri.toString()))
                    .addProperty(WON.BELONGS_TO_NEED, model.createResource(connection.getNeedURI().toString()))
            ;
            return Response.ok(model).build();
        } catch (NoSuchConnectionException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    @GET
    @Path("/data/need/{identifier}/connections")
    @Produces("application/rdf+xml,application/x-turtle,text/turtle,text/rdf+n3,application/json")
    public Response readConnectionsOfNeed(
            @Context UriInfo uriInfo,
            @PathParam("identifier") String identifier,
            @DefaultValue("-1") @QueryParam("page") int page) {
        logger.debug("readConnectionsOfNeed() called");
        URI needURI = URI.create(this.needURIPrefix+"/"+identifier);

        try {
            Collection<URI> uris = null;
            if (page >= 0) {
                uris = needInformationService.listConnectionURIs(needURI, page);
            } else {
                uris = needInformationService.listConnectionURIs(needURI);
            }
            Model model = ModelFactory.createDefaultModel();
            Bag connections = model.createBag();
            for (URI connURI : uris) {
                connections.add(needURI);
            }
            return Response.ok(model).build();
        } catch (NoSuchNeedException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    /*
    @PUT
    @Path("/{component:[^/]+}/create")
    @Produces("application/rdf+xml,application/x-turtle,text/turtle,text/rdf+n3,application/json")
    public Response create(@Context UriInfo uriInfo, @Context HttpServletRequest servletRequest) {
      if (logger.isDebugEnabled()) {
        logger.debug("create() called, uri=" + uriInfo.getAbsolutePath());
      }
      ResourceContainer<?> container = containerManager.getContainerForResource(uriInfo.getAbsolutePath());
      if (container == null) {
        return Responses.notFound().build();
      }
      Resource res = container.create();
      //TODO: better move this message-creation into generic message handling of resourceContainers
      Message msg = new ResourceContainer.CreateResponseMessage(container.getUri(), URI.create(servletRequest.getParameter(Message.KEY_SENDER)), res.getUri());
      return Response.ok(MessageTransferObject.fromMessage(msg)).build();
    }

    @GET
    @Path("{component: .+}")
    @Produces("application/rdf+xml,application/x-turtle,text/turtle,text/rdf+n3,application/json")
    public Response show(@Context UriInfo uriInfo) {
      if (logger.isDebugEnabled()) {
        logger.debug("show() called, uri=" + uriInfo.getAbsolutePath());
      }
      ResourceContainer<?> container = containerManager.getContainerForResource(uriInfo.getAbsolutePath());
      if (container == null) {
        return Responses.notFound().build();
      }
      Resource res = container.get(uriInfo.getAbsolutePath());
      if (res != null) {
        return Response.ok(res.toString()).build();
      }
      return Responses.notFound().build();
    }


    @PUT
    @Path("/{component:.+}/{resourceId:.+}/{method:.+}")
    @Produces("application/rdf+xml,application/x-turtle,text/turtle,text/rdf+n3,application/json,application/xml")
    public Response callMethod(
        @PathParam("component") String component,
        @PathParam("resourceId") String resourceId,
        @PathParam("method") String method,
        @Context UriInfo uriInfo,
        @Context final HttpServletRequest servletRequest) {

      if (logger.isDebugEnabled()) {
        logger.debug("callMethod() called, uri=" + uriInfo.getAbsolutePath());
      }

      //extract the recipient URI
      //we assume that the resource (recipient) URI is the uri used for the servletRequest, minus the last segment

      URI recipientURI = extractRecipientUriFromRequest(uriInfo);

      //find the container for the recipient

      ResourceContainer<?> container = containerManager.getContainerForResource(recipientURI);
      if (container == null) {
        return Responses.notFound().build();
      }

      //create a Message object and let the container handle it

      final Message message = createMessageFromRequest(method, servletRequest, recipientURI);
      if (logger.isDebugEnabled()) {
        logger.debug("processing this incoming message: " + message);
      }
      Message response = null;
      try {
        response = container.handle(message);
      } catch (Exception e) {
        //TODO use specific exceptions for expected error cases
        Response resp = Response.serverError().build();
        return resp;
      }
      if (logger.isDebugEnabled()) {
        logger.debug("outgoing response (if any): " + response);
      }
      if (response != null) {
        if (!response.getRecipient().equals(message.getSender())) {
          throw new IllegalStateException("response can only be returned to the original sender");
        }
        if (!response.getSender().equals(message.getRecipient())) {
          throw new IllegalStateException("response can only be returned from the original recipient");
        }
        //response is serialized via jaxb.
        return Response.ok(MessageTransferObject.fromMessage(response)).build();
      }
      return Response.ok().build();
    }

    private Message createMessageFromRequest(String method, HttpServletRequest servletRequest, URI recipientURI) {
      Map<String, String> params = new HashMap<String, String>();
      //TODO: servletRequest's param map has String[] values, we use String. Will we ever have issues
      //with multiple values for the same param name...? Maybe we should change the Message interface/base impl
      //to reflect that
      Map<String, String[]> servletParamMap = servletRequest.getParameterMap();
      for (String key : servletParamMap.keySet()) {
        params.put(key, servletParamMap.get(key)[0]);
      }
      params.put(Message.KEY_RECIPIENT, recipientURI.toString());
      params.put(Message.KEY_METHOD, method);
      return new MessageImpl(params);
    }


    private URI extractRecipientUriFromRequest(UriInfo uriInfo) {
      UriBuilder builder = uriInfo.getBaseUriBuilder();
      List<PathSegment> segments = uriInfo.getPathSegments();
      for (int i = 0; i < segments.size() - 1; i++) {
        builder.segment(segments.get(i).getPath());
      }
      return builder.build();
    }
    */

    public void setNeedInformationService(final NeedInformationService needInformationService) {
        this.needInformationService = needInformationService;
    }

    public void setNeedURIPrefix(String needURIPrefix) {
        this.needURIPrefix = needURIPrefix;
    }

    public void setConnectionURIPrefix(String connectionURIPrefix) {
        this.connectionURIPrefix = connectionURIPrefix;
    }

    public void setDataURIPrefix(String dataURIPrefix) {
        this.dataURIPrefix = dataURIPrefix;
    }
}
