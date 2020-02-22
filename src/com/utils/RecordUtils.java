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
	 * ������
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
			System.out.println("��Hello,My dear,which day do you want to in?��");
			System.out.println("1-����    2-����    3-���Լ�ѡ��һ��");
			String date = welcome();
			while(true) {
				System.out.println("1-��ѯ   2-���   3-�޸�   4-ɾ��  5-�˳�");
				int flag2 = 0;
				try {
					flag2 = dataBase(date);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(flag2==5)
					break;
			}
			
			System.out.println("������������������\n1-����  2-����");
			int choice = strToInt("[12]");
			if(choice==2)
				flag=false;
		}
		JdbcUtils.commitTransaction();
	}
	
	private static void commitOrRollback() {
		System.out.println("�Ƿ�ԸղŲ���ʵ�У�\n1-ȷ��  2-ȡ��");
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
	
	
	
	
	
	
	//�������֣���֤��һ����Χ
	/**
	 * @param regex
	 * @return 
	 */
	private static int strToInt(String regex) {
		Scanner scan = new Scanner(System.in);
		String str = scan.next();
		while(!str.matches(regex)) {
			System.out.println("����д��ȷѡ��");
			str = scan.next();
		}
		return Integer.parseInt(str);
	}

	
	
	
	
	
	//��ʼ����
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

	
	
	
	
	
	
	// ���Ӽ�¼
	/**
	 * @param param
	 */
	public static void add(java.sql.Date param) {
		ArrayList<String> list = new ArrayList<>();
		long minutes = 0;
		Scanner scan = new Scanner(System.in);
		System.out.println("��������ѧϰ��¼��");
		String str = scan.next();
		while (!"end".equals(str)) {
			if(str.matches("[0-9]{2}:[0-9]{2}-[0-9]{2}:[0-9]{2}")) {
				minutes += changeToMin(str);
				list.add(str);
				str = scan.next();
			}else{
				System.out.println("�����ʽ��Ч(HH:mm-HH:mm)");
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
			// ���ڼ�¼��update
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
			// �����ڼ�¼��insert
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

	// ����HH:mmʱ�������ȡ���minutes
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

	
	
	
	
	
	
	// �޸ļ�¼
	/**
	 * @param param
	 */
	public static void modify(java.sql.Date param) {
		Map<String, Object> result = getData(param);//��ȡ������data
		if(result==null) {
			System.out.println("û�����ڼ�¼");
			return;
		}
		String str = (String) result.get("record");//��ȡrecord
		String[] strs = str.split("\n");		   //�Ի��з��ָ�ÿ��ʱ���
		ArrayList<String> list = new ArrayList<>();//��ÿ��ʱ��ε���һ��list��
		for (int i = 0; i < strs.length; i++) {
			list.add(strs[i].trim());
		}
		for (int i = 0; i < list.size(); i++) {	    //չʾÿ��ʱ��Σ�ѯ���޸���һ��
			System.out.println(list.get(i)+"*****"+i);
		}
		System.out.println("which item do u want to modify?(number&newItem)end����");
		Scanner scan = new Scanner(System.in);
		str = scan.next();
		
		ArrayList<String> newItem = new ArrayList<>();//��һ��list����Ҫ�޸ĵ�item�����������
		while(!"end".equals(str)) {
			newItem.add(str);
			str = scan.next();
		}
		
		int minutes =  (int) result.get("minutes");
		for (String string : newItem) {
			String[] item = string.split("&");//ÿ����index���µ�ʱ���
			int index = Integer.parseInt(item[0].trim());//indexת��int��
			String oldItem = list.get(index);//��ȡԭ����ʱ���
			long oldMinutes = changeToMin(oldItem);
			long newMinutes = changeToMin(item[1].trim());
			minutes = (int) (minutes + newMinutes - oldMinutes);//�����޸ĺ��minutes
			list.set(index, item[1]);//���µ�ʱ��θ��ǵ��ɵ�
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
	
	
	
	
	
	
	

	// ��ȡĳ��ļ�¼
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
	
	

	// ��ѯ��¼
	/**
	 * @param param
	 */
	public static void query(java.sql.Date param) {
		Map<String, Object> result = getData(param);
		if(result==null) {
			System.out.println("û������");
			return;
		}
		Set<String> key = result.keySet();
		for (String string : key) {
			System.out.println(string + "  " + result.get(string));
		}
	}

	
	
	
	
	
	// ɾ����¼
	/**
	 * @param param
	 */
	public static void delete(java.sql.Date param) {
		Map<String, Object> result = getData(param);//��ȡ������data
		if(result==null) {
			System.out.println("û������");
			return;
		}
		String str = (String) result.get("record");//��ȡrecord
		String[] strs = str.split("\n");		   //�Ի��з��ָ�ÿ��ʱ���
		ArrayList<String> list = new ArrayList<>();//��ÿ��ʱ��ε���һ��list��
		for (int i = 0; i < strs.length; i++) {
			list.add(strs[i].trim());
		}
		for (int i = 0; i < list.size(); i++) {	    //չʾÿ��ʱ��Σ�ѯ���޸���һ��
			System.out.println(list.get(i)+"*****"+i);
		}
		System.out.println("which want delete(num)end����");
		Scanner scan = new Scanner(System.in);
		str = scan.next();
		
		ArrayList<String> index = new ArrayList<>();//����Ҫ�޸ĵ�index�浽һ��list��
		while(!"end".equals(str)) {
			index.add(str);
		}
		int minutes = (int) result.get("minutes");//��ȡԭ����minutes
		for (String string : index) {//ɾ��
			String time = list.get(Integer.parseInt(string));
			long old = changeToMin(time);
			minutes = (int) (minutes - old);
			list.remove(Integer.parseInt(string));
		}
		String record = list.get(0);//ɾ������дrecord
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

	
	
	
	
	
	
	// ѡ����һ��
	/**
	 * @return
	 */
	private static String welcome() {
		int num = strToInt("[123]");//������123������һ��
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
		if (num==1) {
			String date = sdf.format(new Date());
			System.out.println("��Welcome��" + date);
			return date;
		}
		
		if (num==2) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, -24);
			String yesterdayDate = sdf.format(calendar.getTime());
			System.out.println("��Welcome��" + yesterdayDate);
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
				System.out.println("��Welcome��" + str);
				return str;
		}
		return null;
		
	}
}
