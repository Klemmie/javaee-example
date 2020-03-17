package com.dvt.example.producer;

import java.util.UUID;

import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.tests.util.ActiveMQTestBase;
import org.junit.Before;
import org.junit.Test;

/**
 * A simple test-case used for documentation purposes.
 */
public class SimpleTest extends ActiveMQTestBase {

	protected ActiveMQServer server;

	protected ClientSession session;

	protected ClientSessionFactory sf;

	protected ServerLocator locator;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		server = createServer(false, createDefaultInVMConfig());
		server.start();
		locator = createInVMNonHALocator();
		sf = createSessionFactory(locator);
		session = addClientSession(sf.createSession(false, true, true));
	}

	@Test
	public void simpleTest() throws Exception {
		final String data = "Simple Text " + UUID.randomUUID().toString();
		final String queueName = "simpleQueue";
		final String addressName = "simpleAddress";
		session.createQueue(addressName, RoutingType.ANYCAST, queueName);
		ClientProducer producer = session.createProducer(addressName);
		ClientMessage message = session.createMessage(false);
		message.getBodyBuffer().writeString(data);
		producer.send(message);
		producer.close();
		ClientConsumer consumer = session.createConsumer(queueName);
		session.start();
		message = consumer.receive(1000);
		assertNotNull(message);
		message.acknowledge();
		assertEquals(data, message.getBodyBuffer().readString());
	}
}
