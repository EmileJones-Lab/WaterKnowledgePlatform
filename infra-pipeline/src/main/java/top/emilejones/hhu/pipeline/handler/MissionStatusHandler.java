package top.emilejones.hhu.pipeline.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import top.emilejones.hhu.domain.pipeline.MissionStatus;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 将数据库中任务状态类型转换为enum类
 *
 * @author Yeyezhi
 */
@MappedTypes(MissionStatus.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class MissionStatusHandler extends BaseTypeHandler<MissionStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, MissionStatus parameter, JdbcType jdbcType)
            throws SQLException {
        int code = switch (parameter) {
            case CREATED -> 0;
            case PENDING -> 1;
            case RUNNING -> 2;
            case ERROR -> 3;
            case SUCCESS -> 4;
            default -> throw new SQLException("Unsupported MissionStatus enum constant: " + parameter);
        };
        ps.setInt(i, code);
    }

    @Override
    public MissionStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toEnum(rs.getInt(columnName), rs.wasNull());
    }

    @Override
    public MissionStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toEnum(rs.getInt(columnIndex), rs.wasNull());
    }

    @Override
    public MissionStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toEnum(cs.getInt(columnIndex), cs.wasNull());
    }

    private MissionStatus toEnum(int code, boolean wasNull) throws SQLException {
        if (wasNull) {
            return null;
        }
        return switch (code) {
            case 0 -> MissionStatus.CREATED;
            case 1 -> MissionStatus.PENDING;
            case 2 -> MissionStatus.RUNNING;
            case 3 -> MissionStatus.ERROR;
            case 4 -> MissionStatus.SUCCESS;
            default -> throw new SQLException("Unknown MissionStatus code: " + code);
        };
    }
}
