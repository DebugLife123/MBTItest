package com.debuglife.mbti;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=false",
        "spring.data.redis.host=localhost",
        "management.health.redis.enabled=false"
})
class MbtiAiApplicationTests {
    @Test void contextLoads() { }
}
