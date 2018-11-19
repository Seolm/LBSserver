package main;

import model.AccessDistrict;
import mqtt.MqttCallbackClient;

public class Main {

	public static void main(String[] args) {
		System.out.println("start main");
		
		MqttCallbackClient.getInstance().connect();
		MqttCallbackClient.getInstance().subscribe("location", 1);
		
		AccessDistrict d = new AccessDistrict();
		d.findSurfaceArea();
		
		/*
		 * MqttCallbackClient mqttCallbackClient = new MqttCallbackClient();
		 * mqttCallbackClient.connect();
		 * mqttCallbackClient.subscribe("/location", 1);
		 */
		/*MemoryPersistence persistence = new MemoryPersistence();
	
		try {
			MqttClient sampleClient = new MqttClient("tcp://127.0.0.1:1883", "client1", persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			MqttMessage message = new MqttMessage("¾È³ç00hello".getBytes());
			sampleClient.connect(connOpts);
			message.setQos(1);
			byte han[] = new byte[10];
			han[0] = Byte.parseByte("¤¾");
			System.out.println(han);
				sampleClient.publish(String.valueOf(han), message);
			
			System.out.println("Message published");
			sampleClient.disconnect();

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		//Publish.getInstance().testpublish("test","¾È³çhh11");
	}

}
