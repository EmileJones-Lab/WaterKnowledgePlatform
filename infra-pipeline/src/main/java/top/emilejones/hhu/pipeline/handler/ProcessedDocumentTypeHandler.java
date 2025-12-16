package top.emilejones.hhu.pipeline.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import top.emilejones.hhu.domain.pipeline.ProcessedDocumentType;

/**
 * 将数据库中Ocr后的文档类型转换为enum类
 * @author Yeyezhi
 */
@MappedTypes(ProcessedDocumentType.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class ProcessedDocumentTypeHandler extends BaseTypeHandler<ProcessedDocumentType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProcessedDocumentType parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setInt(i, parameter.ordinal());
    }

    @Override
    public ProcessedDocumentType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toEnum(rs.getInt(columnName), rs.wasNull());
    }

    @Override
    public ProcessedDocumentType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toEnum(rs.getInt(columnIndex), rs.wasNull());
    }

    @Override
    public ProcessedDocumentType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toEnum(cs.getInt(columnIndex), cs.wasNull());
    }

    private ProcessedDocumentType toEnum(int code, boolean wasNull) throws SQLException {
        if (wasNull) {
            return null;
        }
        ProcessedDocumentType[] values = ProcessedDocumentType.values();
        if (code < 0 || code >= values.length) {
            throw new SQLException("Unknown ProcessedDocumentType code: " + code);
        }
        return values[code];
    }
}
