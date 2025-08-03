package com.solit.sync2sing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class Sync2singApplicationTests {

	@Autowired
	private DataSource dataSource;  // DB 연결 테스트용

	@Test
	void contextLoads() throws SQLException {
		// DB 연결 테스트
		assertNotNull(dataSource.getConnection());
	}

}
