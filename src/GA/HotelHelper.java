package GA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class HotelHelper {

	private HashMap<String, Hotel> hotelMap;
	
	/**
	 * 酒店数据的文件名
	 * @param fileName
	 */
	public HotelHelper(String fileName){
		hotelMap = new HashMap<String, Hotel>();
		try {
			loadData(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	private void loadData(String fileName) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
		String buff = "";
		while((buff = reader.readLine()) != null){
			String[] arr = buff.split("\\s");
			Hotel hotel = new Hotel();
			hotel.setSid(arr[0]);
			hotel.setLng(Double.parseDouble(arr[1]));
			hotel.setLat(Double.parseDouble(arr[2]));
			hotel.setPrice(Double.parseDouble(arr[3]));
			hotelMap.put(arr[1] + "-" + arr[2], hotel);
		}
		reader.close();
	}
	
	/**
	 * 获得酒店实体
	 * @param lng 经度
	 * @param lat 纬度
	 * @return
	 */
	public Hotel getHotel(double lng, double lat){
		String key = lng + "-" + lat;
		return hotelMap.get(key);
	}
	
	public static void main(String[] args){
//		HotelHelper helper = new HotelHelper("./gadata/hotel.txt");
//		Hotel hotel = helper.getHotel(113.28587, 23.194652);
		
		System.out.println(Math.pow(60000.0, 1.0 / 3.0));
		
	}
}
