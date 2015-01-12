package GA;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class CityGa {
	
	/**
	 * 种群规模
	 */
	private int scale;
	
	/**
	 * 城市数量
	 */
	private int cityNum;
	
	/**
	 * 城市列表
	 */
	private ArrayList<City> cityList;
	
	/**
	 * 最大运行代数
	 */
	private int maxGen;
	
	/**
	 * 当前运行代数
	 */
	private int curGen;
	
	/**
	 * 交叉概率
	 */
	private double pc;
	
	/**
	 * 变异概率
	 */
	private double pm;
	
	/**
	 * 种群中个体的累计概率
	 */
	private double[] pi;
	
	/**
	 *  初始种群，父代种群，行数表示种群规模，一行代表一个个体，即染色体，列表示染色体基因片段
	 */
	private int[][] oldPopulation;
	
	/**
	 * 新的种群，子代种群
	 */
	private int[][] newPopulation;
	
	/**
	 * 种群适应度，表示种群中各个个体的适应度
	 */
	private double[] fitness;
	
	/**
	 * 距离矩阵，每行代表一条染色体
	 */
	private double[][] distance;
	
	/**
	 * 最佳出现代数
	 */
	private int bestGen;
	
	/**
	 * 最佳长度
	 */
	private double bestLen;
	
	/**
	 * 最佳路径
	 */
	private int[] bestRoute;
	
	/**
	 * 随机数
	 */
	private Random random;
	
	/**
	 * 酒店帮助类
	 */
	private HotelHelper hotelHelper;
	
	/**
	 * 游玩天数的上限
	 */
	private double upDay = 3;
	/**
	 * 游玩天数的下限
	 */
	private double downDay = 2.0;

	/**
	 * 
	 * @param scale 种群规模
	 * @param maxGen 运行代数
	 * @param pc 交叉概率
	 * @param pm 变异概率
	 */
	public CityGa(int scale, int maxGen, double pc, double pm){
		this.scale = scale;
		this.maxGen = maxGen;
		this.pc = pc;
		this.pm = pm;
		
		this.hotelHelper = new HotelHelper("./gadata/hotel.txt");
	}
	
	/**
	 * 生成一个0-65535之间的随机数
	 * @return
	 */
	private int getRandomNum(){
		return this.random.nextInt(65535);
	}
	
	/**
	 * 初始化算法，从file中加载数据文件
	 * @param filename
	 * @throws IOException 
	 */
	public void init(String filename) throws IOException{
		this.cityList = new ArrayList<City>();
		
		//读取城市信息
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String tmpStr = null;
		while((tmpStr = reader.readLine()) != null ){
			String[] arr = tmpStr.split("\\s");
			City city = new City();
			city.setSid(arr[0]);
			city.setDays(Double.parseDouble(arr[1]));
			city.setPrice(Double.parseDouble(arr[2]));
			city.setLng(Double.parseDouble(arr[3]));
			city.setLat(Double.parseDouble(arr[4]));
			city.setViewCount(Integer.parseInt(arr[5]));
			cityList.add(city);
		}
		reader.close();
		
		this.cityNum = this.cityList.size();
		
		this.bestLen = Integer.MIN_VALUE;
		this.bestGen = 0;
		this.bestRoute = new int[cityNum];
		this.curGen = 0;
		
		this.newPopulation = new int[scale][cityNum];
		this.oldPopulation = new int[scale][cityNum];
		this.fitness = new double[scale];
		this.pi = new double[scale];
		
		this.random = new Random(System.currentTimeMillis());
	}
	
	/**
	 * 初始化种群
	 * 以01001的形式编码染色体
	 */
	private void initGroup(){
		int i, k;
		for (k = 0; k < scale; k++) {
			for (i = 0; i < cityNum; i++) {
				//以01001的形式编码
				oldPopulation[k][i] = getRandomNum() % 2;
			}
		}
	}
	
	/**
	 * 评价函数，用于计算适度
	 * @param chromosome 染色体，包含：城市1,城市2...城市n
	 * @return the total distance of all chromosome's cities;
	 */
	private double evaluate(int[] chromosome){
		double ticketPrice = 0;//门票
		double hotness = 0;//热度
		double days = 0.0;
		//酒店当前染色体对应的酒店信息
		ArrayList<Hotel> hotels = new ArrayList<Hotel>();
		for (int i = 0; i < chromosome.length; i++) {
			if (chromosome[i] == 1) {
				City city = cityList.get(i);
				ticketPrice +=  city.getPrice();
				hotness += (double)city.getViewCount();
				days += city.getDays();
				//获得该景点的酒店信息
				Hotel hotel = hotelHelper.getHotel(city.getLng(), city.getLat());
				if (hotel != null) {
					hotels.add(hotel);
				}
			}
		}
		
		if (days <= downDay || days > upDay) {
			return 0.01;
		}
		
		Collections.sort(hotels);
		double hotelPrice = 0.0;
		/* 判断酒店的个数是否大于需要入住的天数
		 * 如果大于则按照入住的天数计算价格
		 * 如果小于则计算所有酒店的价格，剩余天数就按照最低价格计算
		 */
		int len = Math.min(hotels.size(), (int)upDay);
		if (len != 0) {
			for (int i = 0; i < len; i++) {
				hotelPrice += hotels.get(i).getPrice();
			}
			int span = (int)(upDay - hotels.size());
			for (int i = 0; i < span; i++) {
				hotelPrice += hotels.get(0).getPrice();
			}
		}
		
		double price = hotelPrice + ticketPrice;
		double fitness = (10000.0 / (price + 10.0)) * 0.6 + Math.pow(hotness, 1.0/3.0) * 0.4;
//		System.out.println("fiteness:" + fitness);
		return fitness;
	}
	
	/**
	 * 计算种群中各个个体的累积概率，
	 * 前提是已经计算出各个个体的适应度fitness[max]，
	 * 作为赌轮选择策略一部分，Pi[max]
	 */
	private void countRate(){
		double sumFitness = 0; 
		for (int i = 0; i < scale; i++) {
			sumFitness += fitness[i];
		}
		
		//计算累计概率
		this.pi[0] = fitness[0] / sumFitness;
		for (int i = 1; i < scale; i++) {
			pi[i] = (fitness[i] / sumFitness) + pi[i - 1]; 
		}
	}
	
	/**
	 *  挑选某代种群中适应度最高的个体，直接复制到子代中，
	 *  前提是已经计算出各个个体的适应度Fitness[max]
	 */
	private void selectBestGh(){
		int maxId = 0;
		double maxEvaluation = fitness[0];
		//记录适度最大的cityId和适度
		for (int i = 1; i < scale; i++) {
			if (maxEvaluation < fitness[i]) {
				maxEvaluation = fitness[i];
				maxId = i;
			}
		}
		
		//记录最好的染色体出现代数
		if (bestLen < maxEvaluation) {
			bestLen = maxEvaluation;
			bestGen = curGen;
			for (int i = 0; i < cityNum; i++) {
				bestRoute[i] = oldPopulation[maxId][i];
			}
		}
		
		// 将当代种群中适应度最高的染色体maxId复制到新种群中，排在第一位0
		this.copyGh(0, maxId);
	}
	
	/**
	 * 复制染色体，将oldPopulation复制到newPopulation
	 * @param curP 新染色体在种群中的位置
	 * @param oldP 旧的染色体在种群中的位置
	 */
	private void copyGh(int curP, int oldP){
		for (int i = 0; i < cityNum; i++) {
			newPopulation[curP][i] = oldPopulation[oldP][i];
		}
	}
	
	/**
	 * 赌轮选择策略挑选
	 */
	private void select(){
		int selectId = 0;
		double tmpRan;
//		System.out.print("selectId:");
		for (int i = 1; i < scale; i++) {
			tmpRan = (double)((getRandomNum() % 1000) / 1000.0);
			for (int j = 0; j < scale; j++) {
				selectId = j;
				if (tmpRan <= pi[j]) {
					break;
				}
			}
//			System.out.print(selectId+" ");
			copyGh(i, selectId);
		}
	}
	
	/**
	 * 进化函数，正常交叉变异
	 */
	public void evolution(){
		// 挑选某代种群中适应度最高的个体
		selectBestGh();
		// 赌轮选择策略挑选scale-1个下一代个体
		select();
		
		double ran;
		for (int i = 0; i < scale; i = i+2) {
			ran = random.nextDouble();
			if (ran < this.pc) {
				//如果小于pc，则进行交叉
				crossover(i, i+1);
			}else{
				//否者，进行变异
				ran = random.nextDouble();
				if (ran < this.pm) {
					//变异染色体i
					onVariation(i);
				}
				
				ran = random.nextDouble();
				if (ran < this.pm) {
					//变异染色体i+1
					onVariation(i + 1);
				}
			}
		}
	}
	
	/**
	 * 两点交叉,相同染色体交叉产生不同子代染色体
	 * @param k1 染色体编号 1|234|56
	 * @param k2 染色体编号 7|890|34
	 */
	private void crossover(int k1, int k2){
		//随机发生交叉的位置
		int pos1 = getRandomNum() % cityNum;
		int pos2 = getRandomNum() % cityNum;
		//确保pos1和pos2两个位置不同
		while(pos1 == pos2){
			pos2 = getRandomNum() % cityNum;
		}
		
		//确保pos1小于pos2
		if (pos1 > pos2) {
			int tmpPos = pos1;
			pos1 = pos2;
			pos2 = tmpPos;
		}
		
		//交换两条染色体中间部分
		for (int i = pos1; i < pos2; i++) {
			int t = newPopulation[k1][i];
			newPopulation[k1][i] = newPopulation[k2][i];
			newPopulation[k2][i] = t;
		}
	}
	
	/**
	 * 多次对换变异算子
	 * 如：123456变成153426，基因2和5对换了
	 * @param k 染色体标号
	 */
	private void onVariation(int k){
		//对换变异次数
		int index;
		index = getRandomNum() % cityNum;
		newPopulation[k][index] = getRandomNum() % 2;
	}
	
	/**
	 * 解决问题
	 */
	public void solve(){
		//初始化种群
		initGroup();
		//计算初始适度
		for (int i = 0; i < scale; i++) {
			fitness[i] = this.evaluate(oldPopulation[i]);
		}
		// 计算初始化种群中各个个体的累积概率，pi[max]
		countRate();
		
		System.out.println("初始种群...");
		
		//开始进化
		for (curGen = 0; curGen < maxGen; curGen++) {
			evolution();
			// 将新种群newGroup复制到旧种群oldGroup中，准备下一代进化
			for (int i = 0; i < scale; i++) {
				for (int j = 0; j < cityNum; j++) {
					oldPopulation[i][j] = newPopulation[i][j];
				}
			}
			
			//计算当前代的适度
			for (int i = 0; i < scale; i++) {
				fitness[i] = this.evaluate(oldPopulation[i]);
			}
			
			// 计算当前种群中各个个体的累积概率，pi[max]
			countRate();
		}
		
		selectBestGh();
		
		System.out.println("最后种群");
		for (int i = 0; i < scale; i++) {
			double price = 0.0;
			double hotness = 0.0;
			double days = 0.0;
			for (int j = 0; j < cityNum; j++) {
//				System.out.print(oldPopulation[i][j] + ",");
				if (oldPopulation[i][j] == 1) {
					City city = cityList.get(j);
					price += city.getPrice();
					hotness += city.getViewCount();
					days += city.getDays();
					System.out.print(city.getSid() + ",");
				}
			}
			System.out.print("  天数：" + days + " --价格：" + price + " --热度:" + hotness);
			System.out.print(" 适度：" + fitness[i]);
			System.out.println();
		}
		
		System.out.println("最佳长度出现代数：");
		System.out.println(bestGen);
		System.out.println("最佳长度");
		System.out.println(bestLen);
		System.out.println("最佳路径：");
		for (int i = 0; i < cityNum; i++) {
			System.out.print(bestRoute[i] + ",");
		}
		
		
		
	}
	
	
	public static void main(String[] args) throws IOException{
		long begin = System.currentTimeMillis();
		
		CityGa ga = new CityGa(300, 100, 0.8, 0.9);
		ga.init("./gadata/city.txt");
		ga.solve();
		
		long end = System.currentTimeMillis();
		long time = (end - begin);
		System.out.println();
		System.out.println("耗时："+ time +" ms");
	}
	
	
	
	
	
	
	
	
	

}
