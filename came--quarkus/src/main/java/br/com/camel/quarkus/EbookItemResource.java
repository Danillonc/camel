package br.com.camel.quarkus;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ebook/item")
public class EbookItemResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response postEbook(String data) {
        System.out.println("Entrada: "+ data);
        return Response.status(200).build();
    }

    @GET
    public Response getEbook(@QueryParam("pedidoId") String pedidoId, @QueryParam("ebookId") String ebookId, @QueryParam("clienteId") String clienteId) {
        System.out.printf("Ebook ID: %s, Cliente ID: %15s, Pedido ID: %s %n", ebookId, clienteId, pedidoId);
        return Response.status(200).build();
    }
}
