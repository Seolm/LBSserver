package dao2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import model.Location;

public class LocationDAO {
	
	private static LocationDAO locationDAO = null;
	private Connection conn = null;
	/*private PreparedStatement pstmt = null;*/
	private String[] engDistrict = {"Gangnam-gu","Gangdong-gu","Gangbuk-gu","Gangseo-gu","Gwanak-gu",
	         "Gwangjin-gu","Guro-gu","Geumcheon-gu","Nowon-gu","Dobong-gu",
	         "Dongdaemun-gu","Dongjak-gu","Mapo-gu","Seodaemun-gu", "Seocho-gu",
	         "Seongdong-gu","Seongbuk-gu","Songpa-gu", "Yangcheon-gu", "Yeongdeungpo-gu",
	         "Yongsan-gu", "Eunpyeong-gu", "Jongno-gu", "Jung-gu", "Jungnang-gu"};
	   
	
	synchronized public static LocationDAO getInstance() {
		if(locationDAO == null){
			locationDAO = new LocationDAO();
		}
		return locationDAO;
	}
	
	private LocationDAO() {
		initFirstConnection();
	}
	
	private void initFirstConnection() {
		// Config Setting
        try {
            setupFirstDriver();
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        System.out.println("FirstConnection Created");
	}
	
	public void setupFirstDriver() {
		String url = "jdbc:mysql://localhost:3306/cclocation?useSSL=false";
        String jdbc = "com.mysql.jdbc.Driver";
        String user = "root";
        String pass = "930324";
        
        try {
	        Class.forName(jdbc);
	
	        // ���� DB���� Ŀ�ؼ��� �������ִ� ���丮 ����
	        ConnectionFactory connFactory = new DriverManagerConnectionFactory(url, user, pass);
	        
	        // DBCP�� Ŀ�ؼ� Ǯ�� Ŀ�ؼ��� ������ �� ���
	        PoolableConnectionFactory poolableConnFactory = new PoolableConnectionFactory(connFactory, null);
	        // Ŀ�ؼ��� ��ȿ���� Ȯ���ϴ� ����
	        poolableConnFactory.setValidationQuery("select 1");
	        
	        // Ŀ�ؼ� Ǯ�� ���� ����
	        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
	        poolConfig.setTimeBetweenEvictionRunsMillis(1000L * 60L * 1L);		// ��ȿ Ŀ�ؼ� �˻� �ֱ�
	        poolConfig.setTestWhileIdle(true); 									// Ǯ�� �ִ� Ŀ�ؼ��� ��ȿ���� �˻� ����
	        poolConfig.setMaxIdle(5);											// �ּ� ����
	        poolConfig.setMaxTotal(30);											// �ִ� ����
	        
	        // Ŀ�ؼ� Ǯ ����
	        GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnFactory, poolConfig);
	
	        poolableConnFactory.setPool(connectionPool);
	        Class.forName("org.apache.commons.dbcp2.PoolingDriver");
	
	        // Pooling�� ���� JDBC ����̹� ���� �� ���
	        PoolingDriver driver = (PoolingDriver)DriverManager.getDriver("jdbc:apache:commons:dbcp:");
	        driver.registerPool("cp", connectionPool);
        } catch(Exception e) {
        	System.out.println("ERROR============setupFirstDriver");
        }
    }
	
	public void dbConnect() {

        String url = "jdbc:mysql://localhost:3306/cclocation?useSSL=false";
        String jdbc = "com.mysql.jdbc.Driver";
        String user = "root";
        String pass = "930324";
        
        try {
            Class.forName(jdbc);
            
            conn = DriverManager.getConnection(url, user, pass);
        } catch(Exception e) {
            System.out.println("ERROR============dbConnect: " + e.getMessage());
        } finally{
        }
	}
	
	public void close(Connection conn, PreparedStatement pstmt){
        try{
            if ( pstmt != null){ pstmt.close(); }
        }catch(Exception e){}
        
        try{
            if ( conn != null){ conn.close(); }
        }catch(Exception e){}        
    }
    
    public void close(Connection conn, PreparedStatement pstmt, ResultSet rs){
        try{
            if ( rs != null){ rs.close(); }
        }catch(Exception e){}
        
        try{
            if ( pstmt != null){ pstmt.close(); }
        }catch(Exception e){}
        
        try{
            if ( conn != null){ conn.close(); }
        }catch(Exception e){}        
    }
	
	public void insertLocation(Location location) {
		PreparedStatement pstmt = null;
		try {
			//dbConnect();
			String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
			conn = DriverManager.getConnection(jdbcDriver);
			String sql = "insert into locationdata(client_id, latitude, longitude, time, district, datetime) values(?,?,?,?,?,STR_TO_DATE(?,\'%Y%m%d%k%i%s\'))";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, location.getClient_id());
			pstmt.setDouble(2, location.getLatitude());
			pstmt.setDouble(3, location.getLongitude());
			pstmt.setLong(4, location.getTime());
			pstmt.setString(5, location.getDistrict());
			pstmt.setString(6, Long.toString(location.getTime()));
			
			pstmt.executeUpdate();
		} catch(Exception e) {
			System.out.println("ERROR============insertLocation: " + e.getMessage());
		} finally {
			close(conn, pstmt);
		}
	}
	
	public void insertCurrentLocation(Location location) {
		PreparedStatement pstmt = null;
		try {
			//dbConnect();
			String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
			conn = DriverManager.getConnection(jdbcDriver);
			
			String sql = "insert into currentlocation values (?, ?, ?, ?, ?, STR_TO_DATE(?,\'%Y%m%d%k%i%s\')) on duplicate key "
					+ "update client_id=?, latitude=?, longitude=?, time=?, district=?, datetime=STR_TO_DATE(?,\'%Y%m%d%k%i%s\');";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, location.getClient_id());
			pstmt.setDouble(2, location.getLatitude());
			pstmt.setDouble(3, location.getLongitude());
			pstmt.setLong(4, location.getTime());
			pstmt.setString(5, location.getDistrict());
			pstmt.setString(6, Long.toString(location.getTime()));
			
			pstmt.setString(7, location.getClient_id());
			pstmt.setDouble(8, location.getLatitude());
			pstmt.setDouble(9, location.getLongitude());
			pstmt.setLong(10, location.getTime());
			pstmt.setString(11, location.getDistrict());
			pstmt.setString(12, Long.toString(location.getTime()));
			
			pstmt.executeUpdate();
		} catch(Exception e) {
			System.out.println("ERROR============insertCurrentLocation: " + e.getMessage());
		} finally {
			close(conn, pstmt);
		}
	}
	
	public List<Location> getAllLeastLocation(){
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Location> listLocation = new ArrayList<Location>();
		try {
			//dbConnect();
			String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
			conn = DriverManager.getConnection(jdbcDriver);
			/*String sql = "select * from locationdata where (client_id, time) in "
					+ "(select client_id, max(time) from locationdata group by client_id);";*/
			String sql = "select * from currentlocation";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				Location location = new Location();
				location.setClient_id(rs.getString("client_id"));
				location.setLatitude(rs.getDouble("latitude"));
				location.setLongitude(rs.getDouble("longitude"));
				location.setTime(rs.getLong("time"));
				listLocation.add(location);
			}
		} catch(Exception e) {
			System.out.println("ERROR============getAllLeastLocation: " + e.getMessage());
		} finally {
			close(conn, pstmt, rs);
		}
		return listLocation;
	}
	
	public List<String> getClientsAboutLocationRec(Double[] locations) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<String> clients = new ArrayList<String>();
		try {
			//dbConnect();
			String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
			conn = DriverManager.getConnection(jdbcDriver);
			/*String sql = "select client_id from locationdata where latitude<=? and "
					+ "latitude>=? and longitude<=? and longitude>=? and (client_id,time) in "
					+ "(select client_id,max(time) from locationdata group by client_id);";*/
			//37.55022105257947,127.13762815237351,37.5102109744057,127.08662815237346
			//�ϵ� ����, �ϵ� �浵, ���� ����, ���� �浵
			// 0 2 1 3
			String sql = "select client_id from currentlocation where latitude<=? and "
					+ "latitude>=? and longitude<=? and longitude>=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, locations[0]);
			pstmt.setDouble(2, locations[2]);
			pstmt.setDouble(3, locations[1]);
			pstmt.setDouble(4, locations[3]);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				clients.add(rs.getString("client_id"));
			}
		} catch(Exception e) {
			System.out.println("ERROR============getClientsAboutLocationRec: " + e.getMessage());
		} finally {
			close(conn, pstmt, rs);
		}
		return clients;
	}

	public List<String> getClientsAboutLocationCir(Double[] center, int radius) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<String> clients = new ArrayList<String>();
		try {
			//dbConnect();
			String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
			conn = DriverManager.getConnection(jdbcDriver);
			/*String sql = "SELECT client_id from locationdata "
					+ "where ((6371000*acos(cos(radians(?))"
					+ "*cos(radians(latitude))* cos(radians(longitude)-radians(?))"
					+ "+ sin(radians(?))*sin(radians(latitude)))) <= ?) "
					+ "and (client_id, time) in (select client_id, max(time) "
					+ "from locationdata group by client_id);";*/
			String sql = "SELECT client_id, (6371000*acos(cos(radians(?))*cos(radians(latitude))"
					+ "*cos(radians(longitude)-radians(?)) +sin(radians(?))*sin(radians(latitude)))) "
					+ "AS distance FROM currentlocation HAVING distance <= ? ORDER BY distance;";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, center[0]);
			pstmt.setDouble(2, center[1]);
			pstmt.setDouble(3, center[0]);
			pstmt.setInt(4, radius);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				clients.add(rs.getString("client_id"));
			}
		} catch(Exception e) {
			System.out.println("ERROR============getClientsAboutLocationCir: " + e.getMessage());
		} finally {
			close(conn, pstmt, rs);
		}
		return clients;
	}
	
	public List<String> getKneighbors(Double[] center, int radius, int people, String client) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<String> clients = new ArrayList<String>();
		try {
			//dbConnect();
			String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
			conn = DriverManager.getConnection(jdbcDriver);
			/*String sql = "SELECT client_id, (6371000*acos(cos(radians(?))"
					+ "*cos(radians(latitude))*cos(radians(longitude)-radians(?))+"
					+ "sin(radians(?))*sin(radians(latitude)))) as distance "
					+ "from locationdata where client_id != ? and (client_id, time) in "
					+ "(select client_id, max(time) from locationdata group by client_id) "
					+ "having distance <= ? order by distance limit ?;";*/
			String sql = "SELECT client_id, (6371000*acos(cos(radians(?))*cos(radians(latitude))"
					+ "*cos(radians(longitude)-radians(?))+sin(radians(?))*sin(radians(latitude)))) "
					+ "as distance from currentlocation where client_id != ? having distance <= ?"
					+ " order by distance limit ?;";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setDouble(1, center[0]);
			pstmt.setDouble(2, center[1]);
			pstmt.setDouble(3, center[0]);
			pstmt.setString(4, client);
			pstmt.setInt(5, radius);
			pstmt.setInt(6, people);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				clients.add(rs.getString("client_id"));
			}
		} catch(Exception e) {
			System.out.println("ERROR============getKneighbors: " + e.getMessage());
		} finally {
			close(conn, pstmt, rs);
		}
		return clients;
	}
	
	public List<Location> getTrackingData(long[] dates, String client) {
		PreparedStatement pstmt = null;
		List<Location> trackingLogs = new ArrayList<Location>();
		ResultSet rs = null;
		try {
			//dbConnect();
			String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
			conn = DriverManager.getConnection(jdbcDriver);
			String sql = "select latitude, longitude, time from locationdata "
					+ "where client_id=? and time>=? and time<=? order by time;";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, client);
			pstmt.setLong(2, dates[0]*100);
			pstmt.setLong(3, dates[1]*100);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				Location location = new Location();
				location.setLatitude(rs.getDouble("latitude"));
				location.setLongitude(rs.getDouble("longitude"));
				location.setTime(rs.getLong("time"));
				trackingLogs.add(location);
			}
			System.out.println(trackingLogs.size()+"���� Tracking Data �˻�");
		} catch(Exception e) {
			System.out.println("ERROR============getTrackingData: " + e.getMessage());
		} finally {
			close(conn, pstmt, rs);
		}
		return trackingLogs;
	}
	
	public String checkLatestLocation(String client_id) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String latestDistrict = "";
		try {
			//dbConnect();
			String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
			conn = DriverManager.getConnection(jdbcDriver);
			String sql = "select district from currentlocation where client_id=?";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, client_id);
			
			rs = pstmt.executeQuery();
			
			if(rs.next())
				latestDistrict = rs.getString("district");
		} catch(Exception e) {
			System.out.println("ERROR============checkLatestLocation: " + e.getMessage());
		} finally {
			close(conn, pstmt, rs);
		}
		return latestDistrict;
	}
	
	public int getUsersCountByDistrict(String district) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
	      int count = 0;
	      
	      //select * from locationdata where time > DATE_SUB("20180517102530", INTERVAL 30 SECOND);
	      
	      try {
	    	//dbConnect();
				String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
				conn = DriverManager.getConnection(jdbcDriver);
	         String sql = "select client_id from currentlocation where district like ? ";
	         pstmt = conn.prepareStatement(sql);
	         pstmt.setString(1, district + '%');
	         rs = pstmt.executeQuery();
	         
	         while(rs.next()) {
	            count++;
	         }
	         
	      } catch(Exception e) {
	         System.out.println("ERROR============getUsersCountByDistrict: " + e.getMessage());
	      } finally {
	         close(conn, pstmt, rs);
	      }

	      return count;
	   }
	   
	   public int[] getUsersCountByDistrictDuringTheDay(String date) {
		   PreparedStatement pstmt = null;
		   ResultSet rs = null;
	      int[] countByDistrict = new int[engDistrict.length];

	      try {
	    	//dbConnect();
				String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
				conn = DriverManager.getConnection(jdbcDriver);

	         for(int i=0;i<engDistrict.length;i++) {
	            String sql = "select count(distinct client_id) from locationdata where district like ? "
	                  + "and datetime >= STR_TO_DATE(?,\"%Y%m%d%k%i%s\") and datetime <= STR_TO_DATE(?,\"%Y%m%d%k%i%s\")";
	            pstmt = conn.prepareStatement(sql);
	            pstmt.setString(1, engDistrict[i] + '%');
	            pstmt.setString(2, date + "000000");
	            pstmt.setString(3, date + "235959");
	            rs = pstmt.executeQuery();

	            while(rs.next()) {
	               countByDistrict[i] = rs.getInt(1);
	            }
	         }

	      } catch(Exception e) {
	         System.out.println("ERROR============getUsersCountByDistrictDuringTheDay: " + e.getMessage());
	      } finally {
	         close(conn, pstmt, rs);
	      }

	      ////////////////////////
	      //for(int i=0;i<countByDistrict.length;i++)
	         //System.out.println(i+ ">> " + countByDistrict[i] + " ");

	      return countByDistrict;
	   }
	   
	   public int[] getUsersCountByDuration(String district, String fromDate, String toDate) {
		   PreparedStatement pstmt = null;
		   PreparedStatement pstmt2 = null;
		   PreparedStatement pstmt3 = null;

		   ResultSet rs = null;
		   ResultSet rs2 = null;
		   ResultSet rs3 = null;
	      int[] countByTheDay = null;
	      
	      try {
	    	//dbConnect();
				String jdbcDriver = "jdbc:apache:commons:dbcp:cp";
				conn = DriverManager.getConnection(jdbcDriver);
	         String numOfData = "select DATEDIFF(STR_TO_DATE(?,\"%Y%m%d%\"), STR_TO_DATE(?,\"%Y%m%d%\"))";
	         pstmt = conn.prepareStatement(numOfData);
	         pstmt.setString(1, toDate);
	         pstmt.setString(2, fromDate);
	         rs = pstmt.executeQuery();
	               
	         while(rs.next()) {
	            countByTheDay = new int [(rs.getInt(1)+1)*2];
	         }
	      } catch(Exception e) {
		        System.out.println("ERROR============getUsersCountByDuration11111: " + e.getMessage());
		  } finally {
		         close(conn, pstmt, rs);
		  }
	      try {
	         String standardDate = toDate + "235959";
	         String sql = "select count(distinct client_id) from locationdata where district like ? " + 
	               "and datetime >= DATE_SUB(?, INTERVAL 12 HOUR) and datetime <= STR_TO_DATE(?,\"%Y%m%d%k%i%s\")";
	         for(int i=0;i<countByTheDay.length;i++) {
	            pstmt2 = conn.prepareStatement(sql);
	            pstmt2.setString(1, district + '%');
	            pstmt2.setString(2, standardDate);
	            pstmt2.setString(3, standardDate);
	            rs2 = pstmt2.executeQuery();
	                        
	            while(rs2.next()) {
	               countByTheDay[i] = rs2.getInt(1);
	            }
	            rs2.close();
	            
	            sql = "select DATE_SUB(?, INTERVAL 12 HOUR);";
		         pstmt3 = conn.prepareStatement(sql);
		         pstmt3.setString(1, standardDate);
		         rs3 = pstmt3.executeQuery();
		         
		         while(rs3.next()) {
		        	 standardDate = rs3.getString(1);
		         }
	            
	            //System.out.println(standardDate + ">" + countByTheDay[i]);
	            sql = "select count(distinct client_id) from locationdata where district like ? " + 
	                  "and datetime >= DATE_SUB(?, INTERVAL 12 HOUR) and datetime <= STR_TO_DATE(?,\"%Y-%m-%d %k:%i:%s\")";
	         }
	         
	         
	      } catch(Exception e) {
	         System.out.println("ERROR============getUsersCountByDuration: " + e.getMessage());
	      } finally {
	         close(conn, pstmt2, rs2);
	         close(conn, pstmt3, rs3);
	      }
	      
	      return countByTheDay;
	   }
}
