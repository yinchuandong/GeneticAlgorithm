package GA;

import java.util.Comparator;

/**
 * 酒店的实体模型
 * @author yinchuandong
 *
 */
public class Hotel implements Comparable<Hotel>{

	private String sid;
	private double lng;
	private double lat;
	private double price;
	
	
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	
	@Override
	public int compareTo(Hotel o) {
		if (this.getPrice() > o.getPrice()) {
			return 1;
		}
		return 0;
	}
	
	
}
