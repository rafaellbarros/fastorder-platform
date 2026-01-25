package br.com.rafaellbarros.fastorder;

import br.com.rafaellbarros.fastorder.api.gateway.client.UserFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest()
@ActiveProfiles("test")
class GatewayApplicationTests {

	@MockBean
	private UserFeignClient userFeignClient;

	@Test
	void contextLoads() {
	}
}



