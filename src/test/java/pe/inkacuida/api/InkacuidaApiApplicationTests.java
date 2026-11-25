package pe.inkacuida.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Arranca la aplicación completa con H2 en memoria (no necesita Neon). */
@SpringBootTest
@ActiveProfiles("local")
class InkacuidaApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
