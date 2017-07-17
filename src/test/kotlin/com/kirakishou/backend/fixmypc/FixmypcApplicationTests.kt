package com.kirakishou.backend.fixmypc

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

@TestPropertySource(locations = arrayOf("classpath:repositories-test.properties"))
@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = arrayOf(FixmypcApplication::class))
@WebAppConfiguration
class FixmypcApplicationTests {

	@Test
	fun contextLoads() {
	}

}
