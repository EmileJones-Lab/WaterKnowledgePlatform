package top.emilejones.hhu.knowledge.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import top.emilejones.hhu.domain.knowledge.KnowledgeDocumentType;

/**
 * 将knowledge_document数据库中type类型转换为enum类
 * @author EmileNathon
 */
@MappedTypes(KnowledgeDocumentType.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class KnowledgeDocumentTypeHandler extends BaseTypeHandler<KnowledgeDocumentType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, KnowledgeDocumentType parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setInt(i, parameter.ordinal());
    }

    @Override
    public KnowledgeDocumentType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toEnum(rs.getInt(columnName), rs.wasNull());
    }

    @Override
    public KnowledgeDocumentType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toEnum(rs.getInt(columnIndex), rs.wasNull());
    }

    @Override
    public KnowledgeDocumentType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toEnum(cs.getInt(columnIndex), cs.wasNull());
    }

    private KnowledgeDocumentType toEnum(int code, boolean wasNull) throws SQLException {
        if (wasNull) {
            return null;
        }
        KnowledgeDocumentType[] values = KnowledgeDocumentType.values();
        if (code < 0 || code >= values.length) {
            throw new SQLException("Unknown KnowledgeDocumentType code: " + code);
        }
        return values[code];
    }
}
