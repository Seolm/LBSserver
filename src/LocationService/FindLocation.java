package LocationService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dao2.LocationDAO;
import model.AccessDistrict;
import model.District;
import model.Location;
import publishData.Publish;

public class FindLocation {
	List<District> surfaceAreaOfDistrict = new AccessDistrict().getSurfaceArea();
	List<District> surfaceAreaOfSpecificDistrict = new AccessDistrict().getSpecificSurfaceArea();
	String foundDistrict = "loc,";
	Location clientLocation = null;

	public String getLocation() {
		String newGu = foundDistrict.split(",")[1];
		String newDong = foundDistrict.split(",")[2];
		String newDistrict = newGu + " " + newDong;

		return newDistrict; // format : gu dong
	}

	public boolean contain(District district, Location location) {
		Double latitude = location.getLatitude();
		Double longitude = location.getLongitude();
		if (district.getMinLat() <= latitude && latitude <= district.getMaxLat() && district.getMinLong() <= longitude
				&& longitude <= district.getMaxLong())
			return true;
		else
			return false;
	}

	public void callContain(List<District> d, List<Integer> index) {
		for (int i = 0; i < d.size(); i++) {
			if (contain(d.get(i), clientLocation)) {
				index.add(i);
			}
		}
	}

	public void closestRectangle(Location location) {
		List<Integer> index = new ArrayList<Integer>();
		List<Integer> specificIndex = new ArrayList<Integer>();
		clientLocation = location;

		System.out.println(location.getLatitude() + " : " + location.getLongitude());

		callContain(surfaceAreaOfDistrict, index);
		callContain(surfaceAreaOfSpecificDistrict, specificIndex);

		System.out.println("index : " + index);
		System.out.println("sindex : " + specificIndex);

		findDistrict(index, specificIndex);
	}

	public boolean intersects(double long1, double lat1, double long2, double lat2) {
		Double clientLongitude = clientLocation.getLongitude();
		Double clientLatitude = clientLocation.getLatitude();

		if (long1 > long2) {
			return intersects(long2, lat2, long1, lat1);
		}
		if (clientLongitude == long1 || clientLongitude == long2) {
			clientLongitude += 0.0001;
		}

		if (clientLongitude > long2 || clientLongitude < long1 || clientLatitude >= Double.max(lat1, lat2)) {
			return false;
		}

		if (clientLatitude < Double.min(lat1, lat2)) {
			return true;
		}
		double red = (clientLongitude - long1) / (double) (clientLatitude - lat1);
		double blue = (long2 - long1) / (double) (lat2 - lat1);

		return red >= blue;
	}

	public void findDistrict(List<Integer> index, List<Integer> specificIndex) {
		searchLocation("Seoul_District_Table.csv", index);
		searchLocation("Seoul_Specific_District_Table.csv", specificIndex);
		
		String newGu = foundDistrict.split(",")[1];
		String newDong = foundDistrict.split(",")[2];
		String newDistrict = newGu+" "+newDong;
		String oldDistrict = LocationDAO.getInstance().checkLatestLocation(clientLocation.getClient_id());
		
		if(!newDistrict.equals(oldDistrict)) {
			publishData.Publish.getInstance().publishFoundLocation(clientLocation.getClient_id(), foundDistrict);
		}
		
	}

	public void searchLocation(String csvFile, List<Integer> index) {
		BufferedReader br = null;
		String line = "";
		int count = 0;

		try {
			br = new BufferedReader(new FileReader(csvFile));

			long start = System.currentTimeMillis();
			while ((line = br.readLine()) != null) {
				if (count != 0 && index.contains(count - 1)) {
					String[] str = line.split("<coordinates>");
					String[] onlyGeo = str[1].split("</coordinates>");
					String[] longLat = onlyGeo[0].split(",");
					String[] name = line.split("\",")[1].split(",");
					boolean inside = false;
					double long1 = 0.0;
					double long2 = 0.0;
					double lat1 = 0.0;
					double lat2 = 0.0;

					// Double clientLatitude = clientLocation.getLatitude();
					// Double clientLongitude = clientLocation.getLongitude();
					long1 = Double.parseDouble(longLat[0]);
					lat1 = Double.parseDouble(longLat[1]);
					for (int i = 2; i < longLat.length - 1; i++) {
						if (i % 2 == 0) {
							String[] longitude = longLat[i].split(" ");
							long2 = Double.parseDouble(longitude[1]);
						} else {
							lat2 = Double.parseDouble(longLat[i]);

							if (intersects(long1, lat1, long2, lat2)) {
								// System.out.println(inside + ">> " + long1 +
								// lat1 + long2 + lat2);
								inside = !inside;
							}

							/*
							 * => 0.052 if((long1 > clientLongitude) != (long2 >
							 * clientLongitude)) { double atX = (lat2 - lat1) *
							 * (clientLongitude - long1)/(long2 - long1) + lat1;
							 * if(clientLatitude < atX) {
							 * System.out.println(inside + ">> " + long1 + lat1
							 * + long2 + lat2); inside = !inside; }
							 * 
							 * }
							 */

							long1 = long2;
							lat1 = lat2;
						}
					}

					if (inside)
						foundDistrict += name[3] + ",";
				}
				count++;
			}

			long end = System.currentTimeMillis();
			System.out.println("\n>> �떎�뻾 �떆媛� : " + (end - start) / 1000.0);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
