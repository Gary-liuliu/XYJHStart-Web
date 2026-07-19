package org.xyjh.xyjhstartweb.duduplan.mapper;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DuduChatMessageMapperSqlTests {
    private final Configuration configuration = createConfiguration();

    @Test
    void markReadUsesExecutableComparisonOperator() {
        BoundSql boundSql = getBoundSql("markRead", Map.of(
                "receiverRole", "observer",
                "upToId", 18L,
                "readAt", 1_784_293_000_000L
        ));

        assertThat(boundSql.getSql()).contains("id <= ?").doesNotContain("&lt;");
    }

    @Test
    void recallUsesExecutableComparisonOperator() {
        BoundSql boundSql = getBoundSql("recall", Map.of(
                "messageId", "0ac725d9-1396-4418-a2d1-c1c577b1b17a",
                "senderRole", "owner",
                "recalledAt", 1_784_293_000_000L,
                "earliestServerCreatedAt", 1_784_292_880_000L
        ));

        assertThat(boundSql.getSql()).contains("server_created_at >= ?").doesNotContain("&gt;");
    }

    private BoundSql getBoundSql(String methodName, Map<String, Object> parameters) {
        return configuration
                .getMappedStatement(DuduChatMessageMapper.class.getName() + "." + methodName)
                .getBoundSql(parameters);
    }

    private static Configuration createConfiguration() {
        Configuration configuration = new Configuration();
        configuration.addMapper(DuduChatMessageMapper.class);
        return configuration;
    }
}
