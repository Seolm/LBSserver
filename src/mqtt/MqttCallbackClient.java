package mqtt;
 
 
import java.util.Arrays;
 
 
 
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
 
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
 
import org.eclipse.paho.client.mqttv3.IMqttToken;
 
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
 
import org.eclipse.paho.client.mqttv3.MqttCallback;
 
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
 
import org.eclipse.paho.client.mqttv3.MqttException;
 
import org.eclipse.paho.client.mqttv3.MqttMessage;
 
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
 
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
 
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import LocationService.FindLocation;
import dao2.LocationDAO;
import model.Location;
import mqtt.Util;
 
 
 
//MQTT Ŭ���̾�Ʈ
 
public class MqttCallbackClient implements MqttCallback{
 
	private static MqttCallbackClient mqttCallbackClient = null;
    private String brokerUrl;
 
    private String clientId;
 
    private MqttAsyncClient client;
 
    private MqttConnectOptions mqttConnetOptions;
 
    private Object syncObject = new Object(); 
 
    private boolean isConnected = false;
 
    
    public static MqttCallbackClient getInstance() {
    	if(mqttCallbackClient == null) {
        	mqttCallbackClient = new MqttCallbackClient();
        }
        return mqttCallbackClient;
    }
    
 
    //---------------------------------�ʱ�ȭ---------------------------------
 
    //localhost�� ǥ�� MQTT ��Ʈ�� �����Ѵ�.
 
    public MqttCallbackClient() {
 
        this.brokerUrl = "tcp://127.0.0.1:1883";
 
        this.clientId = MqttAsyncClient.generateClientId();
 
        initClient();
        
        mqttConnetOptions.setMaxInflight(20000);
        
    }
 
    
 
    //url�� �Է¹޾� �����Ѵ�.
 
    public MqttCallbackClient(String brokerUrl) {
 
        this.brokerUrl = brokerUrl;
 
        this.clientId = MqttAsyncClient.generateClientId();
 
        initClient();
 
    }
 
    
 
    //url�� Ŭ���̾�ƮID�� �Է¹޾� �����Ѵ�.
 
    public MqttCallbackClient(String brokerUrl, String clientId) {
 
        this.brokerUrl = brokerUrl;
 
        this.clientId = clientId;
 
        initClient();
 
    }
 
    
 
    //Ŭ���̾�Ʈ �ʱ�ȭ
 
    private void initClient() {
 
        //���� Ȥ�� �޸� persistence��ü�� �����Ѵ�. ���⼭�� �޸𸮷� �Ͽ���.
 
        MemoryPersistence persistence = new MemoryPersistence();
 
        mqttConnetOptions = new MqttConnectOptions();
 
        try {
 
            client = new MqttAsyncClient(this.brokerUrl, this.clientId, persistence);
 
            client.setCallback(this);
 
        } catch (MqttException e) {
 
            e.printStackTrace();
 
        }
 
    }
 
    
 
    //MQTT ���� ����
 
    public MqttConnectOptions getMqttConnetOptions() {
 
        return mqttConnetOptions;
 
    }
 
    
 
    public boolean isConnected() {
 
        return isConnected;
 
    }
 
 
 
    //---------------------------------MQTT �޼ҵ�---------------------------------
 
    public void connect() {
 
        Connector connector = new Connector();
 
        connector.connect();
 
    }
 
    
 
    public void publish(String topicName, int qos, String payload) {
 
        Publisher publisher = new Publisher();
 
        publisher.publish(topicName, qos, payload);
 
    }
 
    
 
    public void subscribe(String topicName, int qos) {
 
        Subscriber subscriber = new Subscriber();
 
        subscriber.subscribe(topicName, qos);
 
    }
 
    
 
    public void unsubscribe(String topicName) {
 
        Unsubscriber unsubscriber = new Unsubscriber();
 
        unsubscriber.unsubscribe(topicName);
 
    }
 
    
 
    public void disconnect() {
 
        Disconnector disconnector = new Disconnector();
 
        disconnector.disconnect();
 
    }
 
    //---------------------------------MQTT �޼ҵ� ���� Ŭ����---------------------------------
 
    private class Connector {
 
        public void connect() {
 
            
 
            //���� ���� ���� ���� ������
 
            IMqttActionListener connectListener = new IMqttActionListener() {
 
                
 
                @Override
 
                public void onSuccess(IMqttToken token) {
 
                    System.out.println(Util.getTime() + "connect success."); 
 
                    synchronized (syncObject) {
 
                        isConnected = true;
 
                        syncObject.notifyAll();
 
                    }
 
                }
 
                
 
                @Override
 
                public void onFailure(IMqttToken token, Throwable cause) {
 
                    System.out.println(Util.getTime() + "connect failed. : " + cause.toString());
 
                    isConnected = false;
 
                }
 
            };
 
            try {
 
                //�ɼ�, ���ؽ�Ʈ, ������
 
                client.connect(mqttConnetOptions, "Connect context", connectListener);
 
            } catch (MqttSecurityException e) {
 
                e.printStackTrace();
 
            } catch (MqttException e) {
 
                e.printStackTrace();
 
            }
 
        }
 
    }
 
    
 
    private class Publisher {
 
        public void publish(String topicName, int qos, String payload) {
 
            synchronized (syncObject) {
 
                if(!isConnected) {
 
                    try {
 
                        syncObject.wait();
 
                    } catch (InterruptedException e) {
 
                        e.printStackTrace();
 
                    }
 
                }
 
            }
 
            MqttMessage message = new MqttMessage(payload.getBytes());
 
            message.setQos(qos);
 
            //Publish ���� ���� ���� ������
 
            IMqttActionListener publishListener = new IMqttActionListener() {
 
                
 
                @Override
 
                public void onSuccess(IMqttToken token) {
 
                    System.out.println(Util.getTime() + "publish success. : " + Arrays.toString(token.getTopics())); 
 
                }
 
                
 
                @Override
 
                public void onFailure(IMqttToken token, Throwable cause) {
 
                    System.out.println(Util.getTime() + "publish failed. : " + Arrays.toString(token.getTopics()) 
 
                    + " cause : " + cause.toString());
 
                }
 
            };
 
            try {
 
                //����, �޽���, ���ؽ�Ʈ, ������
 
                client.publish(topicName, message, "publish context", publishListener);
 
            } catch (MqttPersistenceException e) {
 
                e.printStackTrace();
 
            } catch (MqttException e) {
 
                e.printStackTrace();
 
            }
 
        }
 
    }
 
    
 
    private class Subscriber {
 
        public void subscribe(String topicName, int qos) {
 
            synchronized (syncObject) {
 
                if(!isConnected) {
 
                    try {
 
                        syncObject.wait();
 
                    } catch (InterruptedException e) {
 
                        e.printStackTrace();
 
                    }
 
                }
 
            }
 
            //subscribe ���� ���� ���� ������
 
            IMqttActionListener subscribeListener = new IMqttActionListener() {
 
                
 
                @Override
 
                public void onSuccess(IMqttToken token) {
 
                    System.out.println(Util.getTime() + "subscribe success. : " + Arrays.toString(token.getTopics())); 
 
                }
 
                
 
                @Override
 
                public void onFailure(IMqttToken token, Throwable cause) {
 
                    System.out.println(Util.getTime() + "subscribe failed. : " + Arrays.toString(token.getTopics()) 
 
                    + " cause : " + cause.toString());
 
                }
 
            };
 
            
 
            try {
 
                //����, qos, ���ؽ�Ʈ, ������
 
                client.subscribe(topicName, qos, "subscribe context", subscribeListener);
 
            } catch (MqttException e) {
 
                e.printStackTrace();
 
            }
 
            
 
        }
 
    }
 
    
 
    private class Unsubscriber {
 
        public void unsubscribe(String topicName) {
 
            synchronized (syncObject) {
 
                if(!isConnected) {
 
                    try {
 
                        syncObject.wait();
 
                    } catch (InterruptedException e) {
 
                        e.printStackTrace();
 
                    }
 
                }
 
            }
 
            //unsubscribe ���� ���� ���� ������
 
            IMqttActionListener unsubscribeListener = new IMqttActionListener() {
 
                
 
                @Override
 
                public void onSuccess(IMqttToken token) {
 
                    System.out.println(Util.getTime() + "unsubscribe success. : " + Arrays.toString(token.getTopics())); 
 
                }
 
                
 
                @Override
 
                public void onFailure(IMqttToken token, Throwable cause) {
 
                    System.out.println(Util.getTime() + "unsubscribe failed. : " + Arrays.toString(token.getTopics()) 
 
                    + " cause : " + cause.toString());
 
                }
 
            };
 
            try {
 
                //����, ���ؽ�Ʈ, ������
 
                client.unsubscribe(topicName, "unsubscribe context", unsubscribeListener);
 
            } catch (MqttException e) {
 
                e.printStackTrace();
 
            }
 
        }
 
    }
 
    
 
    private class Disconnector {
 
        public void disconnect() {
 
            synchronized (syncObject) {
 
                if(!isConnected) {
 
                    try {
 
                        syncObject.wait();
 
                    } catch (InterruptedException e) {
 
                        e.printStackTrace();
 
                    }
 
                }
 
            }
 
            //disconnect ���� ���� ���� ������
 
            IMqttActionListener disconnectListener = new IMqttActionListener() {
 
                
 
                @Override
 
                public void onSuccess(IMqttToken token) {
 
                    System.out.println(Util.getTime() + "disconnect success. : " + Arrays.toString(token.getTopics())); 
 
                    System.exit(0);
 
                }
 
                
 
                @Override
 
                public void onFailure(IMqttToken token, Throwable cause) {
 
                    System.out.println(Util.getTime() + "disconnect failed. : " + Arrays.toString(token.getTopics()) 
 
                    + " cause : " + cause.toString());
 
                }
 
            };
 
            
 
            try {
 
                //���ؽ�Ʈ, ������
 
                client.disconnect("disconnect context", disconnectListener);
 
            } catch (MqttException e) {
 
                e.printStackTrace();
 
            }
 
        }
 
    }
 
    
 
    
 
    //---------------------------------MQTT �ݹ�---------------------------------
 
    //���� connection ���� �߻�
 
    @Override
 
    public void connectionLost(Throwable cause) {
 
        System.out.println(Util.getTime() + "Connection lost : " + cause);
 
    }
 
    //�޽��� ���� �Ϸ�
 
    @Override
 
    public void deliveryComplete(IMqttDeliveryToken token) {
 
        System.out.println(Util.getTime() + "Delivery complete" + Arrays.toString(token.getTopics()));
 
    }
 
    //�޽��� ����
 
    @Override
 
    public void messageArrived(String topic, MqttMessage message) throws Exception {
 
        System.out.println(Util.getTime() 
 
                + " Topic : " + topic 
 
                + "(" + message.getQos() + ") ,\n"
 
                + "[Message] : " + new String(message.getPayload()));
        
        String mes = new String(message.getPayload());
        String[] m = mes.split(",");
        Location location = new Location();
        
        location.setClient_id(m[0]);
        location.setLatitude(Double.parseDouble(m[1]));
        location.setLongitude(Double.parseDouble(m[2]));
        location.setTime(Long.parseLong(m[3]));
        
        FindLocation fl = new FindLocation();
		fl.closestRectangle(location);
		
		location.setDistrict(fl.getLocation());
        
        LocationDAO.getInstance().insertLocation(location);
		LocationDAO.getInstance().insertCurrentLocation(location);
        
    }
 
}