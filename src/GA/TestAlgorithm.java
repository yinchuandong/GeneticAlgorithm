package GA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


public class TestAlgorithm {

	/**
	 * 城市的列表
	 */
	private ArrayList<City> cityList;
	
	/**
	 * 最终的路线列表
	 */
	private HashMap<String, Double> routesMap;
	/**
	 * 天数上限
	 */
	private double upLimit = 3.0;
	/**
	 * 天数下限
	 */
	private double downLimit = 2.0;
	
	
	public TestAlgorithm(){
		cityList = new ArrayList<City>();
		routesMap = new HashMap<>();;
	}
	
	/**
	 * 从文件中加载数据，转为City对象
	 * @param fileName
	 * @throws IOException 
	 */
	public void loadData(String fileName) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
		String str = null;
		while((str = reader.readLine()) != null ){
			String[] arr = str.split("\\s");
			City city = new City();
			city.setSid(arr[0]);
			city.setDays(Double.parseDouble(arr[1]));
			city.setPrice(Double.parseDouble(arr[2]));
			city.setLng(Double.parseDouble(arr[3]));
			city.setLat(Double.parseDouble(arr[4]));
			cityList.add(city);
		}
		reader.close();
	}
	
	/**
	 * 计算出符合天数的第一代
	 */
	public void calculate(){
		int len = cityList.size();
		//控制跨度
		for (int span = 1; span < len; span++) {
			for(int i=0; i < len; i++){
				double tmpDays = 0;
				double tmpPrice = 0;
				String tmpRoute = "";
				for (int j = i; j < len; j += span) {
					City city = cityList.get(j);
					tmpDays += city.getDays();
					if (tmpDays > upLimit) {
						tmpDays -= city.getDays();
						continue;
					}
					tmpRoute += j + ",";
					tmpPrice += city.getPrice();
					if (tmpDays > downLimit && tmpDays <= upLimit) {
						tmpRoute = tmpRoute.substring(0, tmpRoute.length() - 1);
						if (!routesMap.containsKey(tmpRoute)) {
							routesMap.put(tmpRoute, tmpDays);
							System.out.print(tmpRoute);
							System.out.print("---" + tmpDays);
							System.out.println("---" + tmpPrice);
						}
						tmpRoute = "";
						tmpDays = 0;
						tmpPrice = 0;
					}
				}
			}
		}
	}
	
	/**
	 * 保存运算的结果到文件中
	 * 编码为 0,1,0,1,1这种类型，1代表改下标对应的城市被选中,0代表不被选中
	 * @param fileName
	 * @throws FileNotFoundException 
	 */
	public void saveResult(String fileName) throws FileNotFoundException{
		PrintWriter writer = new PrintWriter(new File(fileName));
		Iterator<String> iter = routesMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			String[] arr = key.split(",");
			String codeStr = "";
			for (int i = 0; i < cityList.size(); i++) {
				boolean isFound = false;
				for (int j = 0; j < arr.length; j++) {
					if (i == Integer.parseInt(arr[j])) {
						isFound = true;
						break;
					}else{
						isFound = false;
					}
				}
				if (isFound) {
					codeStr += "1,";
				}else{
					codeStr += "0,";
				}
			}
			codeStr = codeStr.substring(0, codeStr.length() - 1) + "\r\n";
			System.out.print(codeStr);
			writer.write(codeStr);
			writer.flush();
		}
		
		writer.close();
	}
	
	
	public static void main(String[] args) throws IOException{
		
		TestAlgorithm model = new TestAlgorithm();
		model.loadData("./gadata/city.txt");
		model.calculate();
		model.saveResult("./gadata/city_first_gen.txt");
		System.out.println();
	}
}
