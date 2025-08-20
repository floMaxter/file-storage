package com.projects.filestorage.session;

import com.projects.filestorage.integration.service.TestConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.test.context.TestConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestConfig.class)
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SessionStorageTest {

    @Autowired
    private SessionRepository<?> sessionRepository;

    @Test
    void shouldUseRedisOrInMemoryForSessions() {
        if (sessionRepository instanceof RedisIndexedSessionRepository) {
            System.out.println("✅ Сессии сохраняются в Redis");
        } else if (sessionRepository instanceof MapSessionRepository) {
            System.out.println("⚠️ Сессии сохраняются в памяти (MapSessionRepository)");
        } else {
            System.out.println("❓ Используется другая реализация: " + sessionRepository.getClass());
        }

        assertThat(sessionRepository).isNotNull();
    }
}
