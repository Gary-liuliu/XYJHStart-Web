package org.xyjh.xyjhstartweb;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;

import static org.assertj.core.api.Assertions.assertThat;

class DuduPlanMapperScanTests {
    @Test
    void applicationScansLegacyAndDuduPlanMappers() {
        MapperScan mapperScan = XyjhStartWebApplication.class.getAnnotation(MapperScan.class);

        assertThat(mapperScan.value()).containsExactlyInAnyOrder(
                "org.xyjh.xyjhstartweb.mapper",
                "org.xyjh.xyjhstartweb.duduplan.mapper"
        );
    }
}
