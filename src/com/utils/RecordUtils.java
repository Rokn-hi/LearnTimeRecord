package com.utils;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.dbutils.handlers.MapHandler;

public class RecordUtils {

	/**
	 * 主流程
	 * @throws Exception 
	 */
	public static void service() throws Exception {
		try {
			JdbcUtils.startTransaction();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Scanner scan = new Scanner(System.in);
		boolean flag = true;
		while(flag) {
			System.out.println("【Hello,My dear,which day do you want to in?】");
			System.out.println("1-今天    2-昨天    3-我自己选择一天");
			String date = welcome();
			while(true) {
				System.out.println("1-查询   2-添加   3-修改   4-删除  5-退出");
				int flag2 = 0;
				try {
					flag2 = dataBase(date);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(flag2==5)
					break;
			}
			
			System.out.println("继续操作其它日期吗？\n1-继续  2-结束");
			int choice = strToInt("[12]");
			if(choice==2)
				flag=false;
		}
		JdbcUtils.commitTransaction();
	}
	
	private static void commitOrRollback() {
		System.out.println("是否对刚才操作实行？\n1-确认  2-取消");
		int last = strToInt("[12]");
		if(last==2) {
			try {
				JdbcUtils.rollbackTransaction();
				JdbcUtils.startTransaction();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	
	
	//输入数字，保证在一定范围
	/**
	 * @param regex
	 * @return 
	 */
	private static int strToInt(String regex) {
		Scanner scan = new Scanner(System.in);
		String str = scan.next();
		while(!str.matches(regex)) {
			System.out.println("请填写正确选项");
			str = scan.next();
		}
		return Integer.parseInt(str);
	}

	
	
	
	
	
	//开始操作
	/**
	 * @param date
	 * @throws Exception
	 */
	private static int dataBase(String date) throws Exception {
		java.sql.Date param = java.sql.Date.valueOf(date);
		int num = strToInt("[12345]");
		switch (num) {
		case 1:
			query(param);
			return 1;
		case 2:
			add(param);
			return 2;
		case 3:
			modify(param);
			return 3;
		case 4:
			delete(param);
			return 4;
		default:
			return 5;
		}
		

	}

	
	
	
	
	
	
	// 增加记录
	/**
	 * @param param
	 */
	public static void add(java.sql.Date param) {
		ArrayList<String> list = new ArrayList<>();
		long minutes = 0;
		Scanner scan = new Scanner(System.in);
		System.out.println("【请输入学习记录】");
		String str = scan.next();
		while (!"end".equals(str)) {
			if(str.matches("[0-9]{2}:[0-9]{2}-[0-9]{2}:[0-9]{2}")) {
				minutes += changeToMin(str);
				list.add(str);
				str = scan.next();
			}else{
				System.out.println("这个格式无效(HH:mm-HH:mm)");
				str = scan.next();
			}
			
		}
		String record;
		if(list.size()!=0) {
			record = list.get(0);
			for(int i=1;i<list.size();i++) {
				record = record + "\n" +list.get(i);
			}
		
			Double hours = (double) (minutes / 60);
			Map<String, Object> result = getData(param);
			TxQueryRunner qr = new TxQueryRunner();
			// 存在记录就update
			if (result != null) {
				minutes += (int) result.get("minutes");
				hours = (double) (minutes / 60);
				record = result.get("record") + "\n" + record;
				String sql = "update learn_time_record set minutes=?,hours=?,record=? " + "  where date=?";
				Object[] params = { minutes, hours, record, param };
				try {
					qr.update(sql, params);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			// 不存在记录就insert
			else {
				String sql = "insert into learn_time_record(date,minutes,hours,record) " + "  values(?,?,?,?)";
				Object[] params = { param, minutes, hours, record };
				try {
					qr.update(sql, params);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		commitOrRollback();
	}

	// 把两HH:mm时间相减获取相隔minutes
	/**
	 * @param str
	 * @return
	 */
	private static long changeToMin(String str) {
		String[] string = str.split("-");
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = sdf.parse(string[0]);
			d2 = sdf.parse(string[1]);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (d2.getTime() - d1.getTime()) / 1000 / 60;
	}

	
	
	
	
	
	
	// 修改记录
	/**
	 * @param param
	 */
	public static void modify(java.sql.Date param) {
		Map<String, Object> result = getData(param);//获取该日期data
		if(result==null) {
			System.out.println("没个日期记录");
			return;
		}
		String str = (String) result.get("record");//获取record
		String[] strs = str.split("\n");		   //以换行符分割每个时间段
		ArrayList<String> list = new ArrayList<>();//把每个时间段导入一个list中
		for (int i = 0; i < strs.length; i++) {
			list.add(strs[i].trim());
		}
		for (int i = 0; i < list.size(); i++) {	    //展示每个时间段，询问修改那一个
			System.out.println(list.get(i)+"*****"+i);
		}
		System.out.println("which item do u want to modify?(number&newItem)end结束");
		Scanner scan = new Scanner(System.in);
		str = scan.next();
		
		ArrayList<String> newItem = new ArrayList<>();//以一个list存需要修改的item及代替的内容
		while(!"end".equals(str)) {
			newItem.add(str);
			str = scan.next();
		}
		
		int minutes =  (int) result.get("minutes");
		for (String string : newItem) {
			String[] item = string.split("&");//每个拆开index和新的时间段
			int index = Integer.parseInt(item[0].trim());//index转会int型
			String oldItem = list.get(index);//获取原来的时间段
			long oldMinutes = changeToMin(oldItem);
			long newMinutes = changeToMin(item[1].trim());
			minutes = (int) (minutes + newMinutes - oldMinutes);//计算修改后的minutes
			list.set(index, item[1]);//把新的时间段覆盖掉旧的
		}
		Double hours = (double) (minutes/60);
		String record = list.get(0);
		for (int i = 1;i<list.size();i++) {
			record = record + "\n" + list.get(i);
		}
		
		TxQueryRunner qr = new TxQueryRunner();
		String sql = "update learn_time_record set minutes=?,hours=?,record=? " + "  where date=?";
		Object[] params = { minutes, hours, record, param };
		try {
			qr.update(sql, params);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		commitOrRollback();
	}
	
	
	
	
	
	
	

	// 获取某天的记录
	/**
	 * @param param
	 * @return
	 */
	private static Map<String, Object> getData(java.sql.Date param) {
		TxQueryRunner qr = new TxQueryRunner();
		String sql = "select * from learn_time_record where date=?";
		Map<String, Object> result = null;
		try {
			result = qr.query(sql, new MapHandler(), param);
		} catch (Exception e) {
			return null;
		}
		return result;
	}
	
	

	// 查询记录
	/**
	 * @param param
	 */
	public static void query(java.sql.Date param) {
		Map<String, Object> result = getData(param);
		if(result==null) {
			System.out.println("没这日期");
			return;
		}
		Set<String> key = result.keySet();
		for (String string : key) {
			System.out.println(string + "  " + result.get(string));
		}
	}

	
	
	
	
	
	// 删除记录
	/**
	 * @param param
	 */
	public static void delete(java.sql.Date param) {
		Map<String, Object> result = getData(param);//获取该日期data
		if(result==null) {
			System.out.println("没这日期");
			return;
		}
		String str = (String) result.get("record");//获取record
		String[] strs = str.split("\n");		   //以换行符分割每个时间段
		ArrayList<String> list = new ArrayList<>();//把每个时间段导入一个list中
		for (int i = 0; i < strs.length; i++) {
			list.add(strs[i].trim());
		}
		for (int i = 0; i < list.size(); i++) {	    //展示每个时间段，询问修改那一个
			System.out.println(list.get(i)+"*****"+i);
		}
		System.out.println("which want delete(num)end结束");
		Scanner scan = new Scanner(System.in);
		str = scan.next();
		
		ArrayList<String> index = new ArrayList<>();//把需要修改的index存到一个list中
		while(!"end".equals(str)) {
			index.add(str);
		}
		int minutes = (int) result.get("minutes");//获取原来的minutes
		for (String string : index) {//删除
			String time = list.get(Integer.parseInt(string));
			long old = changeToMin(time);
			minutes = (int) (minutes - old);
			list.remove(Integer.parseInt(string));
		}
		String record = list.get(0);//删除后重写record
		for(int i = 1;i<list.size();i++) {
			record = record +"\n" +list.get(i);
		}
		Double hours = (double) (minutes/60);
		TxQueryRunner qr = new TxQueryRunner();
		String sql = "update learn_time_record set minutes=?,hours=?,record=? " + "  where date=?";
		Object[] params = { minutes, hours, record, param };
		try {
			qr.update(sql, params);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		commitOrRollback();
	}

	
	
	
	
	
	
	// 选择那一天
	/**
	 * @return
	 */
	private static String welcome() {
		int num = strToInt("[123]");//必须在123中输入一个
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
		if (num==1) {
			String date = sdf.format(new Date());
			System.out.println("【Welcome】" + date);
			return date;
		}
		
		if (num==2) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, -24);
			String yesterdayDate = sdf.format(calendar.getTime());
			System.out.println("【Welcome】" + yesterdayDate);
			return yesterdayDate;
		}
		
		if (num==3) {
			System.out.println("Please enter what day you want");
			Scanner scan = new Scanner(System.in);
			String str = scan.next();
			while (true) {
				Date date = null;

				try {
					date = sdf.parse(str);
				} catch (ParseException e) {
					System.out.println("please enter right date:yyyy-MM-dd");
					str = scan.next();
					continue;
				}

				if (!str.equals(sdf.format(date))) {
					System.out.println("please enter right date:yyyy-MM-dd");
					str = scan.next();
				} else {
					break;
				}
			}
				System.out.println("【Welcome】" + str);
				return str;
		}
		return null;
		
	}
}
