package org.springblade.modules.admin.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * BIGDECIMAL 去除末尾的0
 */
public class BigDecimalHandler extends BaseTypeHandler<BigDecimal> {


	@Override
	public void setNonNullParameter(PreparedStatement preparedStatement, int i, BigDecimal bigDecimal, JdbcType jdbcType) throws SQLException {
		if(bigDecimal != null){
			String bigDecimalStr = bigDecimal.stripTrailingZeros().toPlainString();
			bigDecimal = new BigDecimal(bigDecimalStr);
		}
		preparedStatement.setBigDecimal(i,bigDecimal);
	}

	@Override
	public BigDecimal getNullableResult(ResultSet resultSet, String s) throws SQLException {
		BigDecimal bigDecimal = resultSet.getBigDecimal(s);
		if(bigDecimal != null){
			String bigDecimalStr = bigDecimal.stripTrailingZeros().toPlainString();
			bigDecimal = new BigDecimal(bigDecimalStr);
		}
		return bigDecimal;
	}

	@Override
	public BigDecimal getNullableResult(ResultSet resultSet, int i) throws SQLException {
		BigDecimal bigDecimal = resultSet.getBigDecimal(i);
		if(bigDecimal != null){
			String bigDecimalStr = bigDecimal.stripTrailingZeros().toPlainString();
			bigDecimal = new BigDecimal(bigDecimalStr);
		}
		return bigDecimal;
	}

	@Override
	public BigDecimal getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
		BigDecimal bigDecimal = callableStatement.getBigDecimal(i);
		if(bigDecimal != null){
			String bigDecimalStr = bigDecimal.stripTrailingZeros().toPlainString();
			bigDecimal = new BigDecimal(bigDecimalStr);
		}
		return bigDecimal;
	}

}
