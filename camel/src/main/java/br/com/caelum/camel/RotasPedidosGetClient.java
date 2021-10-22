package br.com.caelum.camel;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.dataformat.xmljson.XmlJsonDataFormat;
import org.apache.camel.impl.DefaultCamelContext;

public class RotasPedidosGetClient {

    public static void main(String[] args) throws Exception {

        final XmlJsonDataFormat xmlJsonDataFormat = new XmlJsonDataFormat();
        xmlJsonDataFormat.setRootName("pedido");

        CamelContext context = new DefaultCamelContext();
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://localhost:61616/"));
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                errorHandler(deadLetterChannel("activemq:queue:pedidos.DLQ")
                        .logExhaustedMessageHistory(true)
                        .maximumRedeliveries(3)
                        .redeliveryDelay(3000).onRedelivery(exchange -> {
                            int counter = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER);
                            int max = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_MAX_COUNTER);
                            System.out.println("Redelivery "+ counter +"/"+ max);
                        }));

                from("activemq:queue:pedidos")
                        .routeId("rota-pedidos")
                        .to("validator:pedido.xsd")
                        .multicast()
                        .to("direct:soap")
                        .to("direct:http");

                from("direct:http")
                        .routeId("rota-http")
                        .setProperty("pedidoId", xpath("/pedido/id/text()"))
                        .setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()"))
                        .split()
                        .xpath("/pedido/itens/item")
                        .filter()
                        .xpath("/item/formato[text()='EBOOK']")
                        .setProperty("ebookId", xpath("/item/livro/codigo/text()"))
                        .marshal(xmlJsonDataFormat)
                        .log("${id} - ${body}")
                        .setHeader(Exchange.HTTP_METHOD, HttpMethods.GET)
                        .setHeader(Exchange.HTTP_QUERY, simple("ebookId=${property.ebookId}&pedidoId=${property.pedidoId}&clienteId=${property.clienteId}"))
                        .to("http4://localhost:8080/ebook/item");

                        from("direct:soap")
                                .routeId("rota-soap")
                                .to("xslt:pedido-para-soap.xslt")
                                .log("SERVICE SOAP: ${body}")
                                .to("mock:soap");
            }
        });

        context.start();
        Thread.sleep(20000);
        context.stop();

    }
}
