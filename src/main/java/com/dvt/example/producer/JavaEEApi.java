package com.dvt.example.producer;

import io.swagger.annotations.*;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/javaee")
@ApplicationScoped
public class JavaEEApi {

	final static Logger LOG = LoggerFactory.getLogger(JavaEEApi.class);

	private static final String template = "Hello, %s";

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Pushes name on topic", tags = "Topic")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Pushed to Topic", response = String.class),
			@ApiResponse(code = 400, message = "Input validation failed", response = Error.class),
			@ApiResponse(code = 404, message = "Not found", response = Error.class),
			@ApiResponse(code = 500, message = "Internal server error", response = Error.class) })
	public Name nameQueue(@Context Request request,
			@ApiParam(value = "Get value name [TOPIC]", type = "String") @QueryParam("name") String name)
			throws Exception {

		Connection connection = null;
		InitialContext initialContext = null;
		try {
			initialContext = new InitialContext();

			Topic topic = (Topic) initialContext.lookup("topic/exampleTopic");
			ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
			connection = cf.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(topic);
			MessageConsumer messageConsumer1 = session.createConsumer(topic);
			MessageConsumer messageConsumer2 = session.createConsumer(topic);
			TextMessage message = session.createTextMessage("This is a text message");
			producer.send(message);
			connection.start();
			TextMessage messageReceived = (TextMessage) messageConsumer1.receive();
			messageReceived = (TextMessage) messageConsumer2.receive();

			return new Name(String.format(template, messageReceived.getText()));
		} finally {
			if (connection != null) {
				connection.close();
			}

			if (initialContext != null) {
				initialContext.close();
			}
		}
	}
}
