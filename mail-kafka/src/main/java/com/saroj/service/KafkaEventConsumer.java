/**
 * 
 */
package com.saroj.service;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import org.slf4j.LoggerFactory;

import com.saroj.controller.BaseController;
import com.saroj.dao.DataStore;
import com.saroj.dao.MongoDataStore;

/**
 * @author sarojrout
 *
 */
public class KafkaEventConsumer extends Thread implements EventConsumer {

	final static String clientId = "SarojKafkaClient";
	final static String TOPIC = "test-email";
	private static final String MONGO_HOST = "localhost";
	private static final int MONGO_PORT = 27017;
	private ConsumerConnector consumerConnector;
//	private final static org.slf4j.Logger logger = LoggerFactory
//			.getLogger(BaseController.class);

	public static void main(String[] argv) throws UnknownHostException {
		KafkaEventConsumer kafkaConsumer = new KafkaEventConsumer();
		kafkaConsumer.start();
	}

	void test(){

	}
	public KafkaEventConsumer() {
		Properties properties = new Properties();
		properties.put("zookeeper.connect", "localhost:2181");
		properties.put("group.id", "test-group");
		ConsumerConfig consumerConfig = new ConsumerConfig(properties);
		consumerConnector = Consumer
				.createJavaConsumerConnector(consumerConfig);
	}

	/**
	 * This thread will pull the events from the topic and insert into mongo DB
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		DataStore store = null;
		try {
			store = MongoDataStore.getInstance(MONGO_HOST, MONGO_PORT);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(TOPIC, new Integer(1));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector
				.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(TOPIC);
		System.out.println(consumerMap);
		KafkaStream<byte[], byte[]> stream = consumerMap.get(TOPIC).get(1);

		System.out.println(stream);
		ConsumerIterator<byte[], byte[]> it = stream.iterator();
		while (it.hasNext()) {
			try {
				String data = new String(it.next().message());
				System.out.println(data);
				store.storeRawEvent(data);
			} catch (Exception e) {
//				logger.error(
//						"Throwing Exception while inserting data to Mongo DB",
//						e);
			}
		}
	}

}
