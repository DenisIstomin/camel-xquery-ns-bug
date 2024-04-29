package bugs;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.builder.LambdaRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.xquery.XQueryBuilder;
import org.apache.camel.model.language.XQueryExpression;
import org.apache.camel.support.ResourceHelper;
import org.apache.camel.support.builder.Namespaces;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.camel.component.xquery.XQueryBuilder.xquery;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Tests extends CamelTestSupport {

    Namespaces ns = new Namespaces("soap", "http://schemas.xmlsoap.org/soap/envelope/");
    String strExpression = "exists(/soap:Envelope/soap:Header)";

    @Test
    void doesNotWork() throws Exception {
        RouteBuilder.addRoutes(context, in -> {
            XQueryExpression xQueryExpression = in.expression().xquery()
                    .expression(strExpression).resultType(String.class).namespaces(ns).end();

            in.from("direct:start").setProperty("isHeaderExists", xQueryExpression);
        });
        run();
    }

    @Test
    void workaroundWorks() throws Exception {
        RouteBuilder.addRoutes(context, in -> {
            XQueryBuilder xQueryIsHeaderExists = xquery(strExpression).resultType(String.class);
            xQueryIsHeaderExists.setNamespaces(ns.getNamespaces());

            in.from("direct:start").setProperty("isHeaderExists", xQueryIsHeaderExists);
        });
        run();
    }

    private void run() throws IOException {
        InputStream inputStream = ResourceHelper.resolveResourceAsInputStream(context, "classpath:soap-message.xml");
        ProducerTemplate producerTemplate = context.createProducerTemplate();

        Exchange exchange = ExchangeBuilder.anExchange(context).withBody(inputStream).build();
        Exchange received = producerTemplate.send("direct:start", exchange);
        assertTrue(received.getProperty("isHeaderExists", boolean.class));
    }
};


