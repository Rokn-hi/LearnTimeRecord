package com.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class JdbcUtils {
	//��Ҫ����c3p0-config.xml�����ļ�
	private static ComboPooledDataSource dataSource = new ComboPooledDataSource();
	//ThreadLocal<T>���װConnection��ÿ���߳����Լ����е�����Connection
	private static ThreadLocal<Connection> tl = new ThreadLocal<>();


	public static Connection getConnection() throws SQLException {
		//��ȡ���߳��µ�����Connection
		Connection conn = tl.get();
		if (conn != null)
			return conn;

		return dataSource.getConnection();
	}

	// ��ȡ���õ����ӳض���
	public static DataSource getDataSource() {
		return dataSource;
	}

	// ��װ��������
	public static void startTransaction() throws SQLException {
		Connection conn = tl.get();
		if (conn != null) {
			throw new RuntimeException("�Ѿ���������");
		}
		conn = getConnection();
		conn.setAutoCommit(false);
		tl.set(conn);
	}

	// ��װ�ύ����
	public static void commitTransaction() throws SQLException {
		Connection conn = tl.get();
		if (conn == null)
			throw new RuntimeException("û��������");
		conn.commit();
		conn.close();
		//�������񣬰��̵߳�����Connectionȥ��
		tl.remove();
	}

	// ��װ�ع�����
	public static void rollbackTransaction() throws SQLException {
		Connection conn = tl.get();
		if (conn == null)
			throw new RuntimeException("û��������");
		conn.rollback();
		conn.close();
		tl.remove();
	}

	// �ر����ӣ��黹�����ӳ�
	public static void releaseConnection(Connection connection) throws SQLException {
		// ������Ҫ�رյ�connection
		// ���con==null��֤��û�п��������Կ��Թر����connection
		Connection conn = tl.get();
		if (conn == null) {
			connection.close();
		}

		// ���con!=null��֤���������񣬴�ʱ�жϴ����connection����������con�Ƿ���ͬһ������
		// �ǵĻ���֤��connection���������Ӳ��ɹرգ����ǵĻ�֤������ͨ���ӣ����Թر�
		if (conn != connection)
			connection.close();

	}
}
