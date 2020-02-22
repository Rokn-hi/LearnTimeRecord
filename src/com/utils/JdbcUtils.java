package com.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class JdbcUtils {
	//需要配种c3p0-config.xml配置文件
	private static ComboPooledDataSource dataSource = new ComboPooledDataSource();
	//ThreadLocal<T>类封装Connection，每个线程有自己独有的事务Connection
	private static ThreadLocal<Connection> tl = new ThreadLocal<>();


	public static Connection getConnection() throws SQLException {
		//获取该线程下的事务Connection
		Connection conn = tl.get();
		if (conn != null)
			return conn;

		return dataSource.getConnection();
	}

	// 获取配置的连接池对象
	public static DataSource getDataSource() {
		return dataSource;
	}

	// 封装开启事务
	public static void startTransaction() throws SQLException {
		Connection conn = tl.get();
		if (conn != null) {
			throw new RuntimeException("已经开启事务");
		}
		conn = getConnection();
		conn.setAutoCommit(false);
		tl.set(conn);
	}

	// 封装提交事务
	public static void commitTransaction() throws SQLException {
		Connection conn = tl.get();
		if (conn == null)
			throw new RuntimeException("没开启事务");
		conn.commit();
		conn.close();
		//结束事务，把线程的事务Connection去除
		tl.remove();
	}

	// 封装回滚事务
	public static void rollbackTransaction() throws SQLException {
		Connection conn = tl.get();
		if (conn == null)
			throw new RuntimeException("没开启事务");
		conn.rollback();
		conn.close();
		tl.remove();
	}

	// 关闭连接，归还给连接池
	public static void releaseConnection(Connection connection) throws SQLException {
		// 传入需要关闭的connection
		// 如果con==null，证明没有开事务，所以可以关闭这个connection
		Connection conn = tl.get();
		if (conn == null) {
			connection.close();
		}

		// 如果con!=null，证明开了事务，此时判断传入的connection与事务连接con是否是同一个连接
		// 是的话，证明connection是事务连接不可关闭，不是的话证明是普通连接，可以关闭
		if (conn != connection)
			connection.close();

	}
}
