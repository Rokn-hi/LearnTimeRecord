package com.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

//自定义QueryRunner类，封装得到连接getConnection和释放releaseConnection
//使用此类减少得到连接与释放连接的步骤
//需要与JdbcUtils工具类一并使用
//只有无参构造，不需传入连接池，使用方法不需给Conncetion

public class TxQueryRunner extends QueryRunner{

	@Override
	public int[] batch(String sql, Object[][] params) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		int[] result = super.batch(conn, sql, params);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		T result = super.query(conn, sql, rsh, params);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		T result = super.query(conn, sql, rsh);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public int update(String sql) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		int result = super.update(conn, sql);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public int update(String sql, Object param) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		int result = super.update(conn, sql, param);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public int update(String sql, Object... params) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		int result = super.update(conn, sql, params);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public <T> T insert(String sql, ResultSetHandler<T> rsh) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		T result = super.insert(conn, sql, rsh);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		T result = super.insert(conn, sql, rsh, params);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public <T> T insertBatch(String sql, ResultSetHandler<T> rsh, Object[][] params) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		T result = super.insertBatch(conn, sql, rsh, params);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public int execute(String sql, Object... params) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		int result = super.execute(conn, sql, params);
		JdbcUtils.releaseConnection(conn);
		return result;
	}

	@Override
	public <T> List<T> execute(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		Connection conn = JdbcUtils.getConnection();
		List<T> result = super.execute(conn, sql, rsh, params);
		JdbcUtils.releaseConnection(conn);
		return result;
	}
	
}
