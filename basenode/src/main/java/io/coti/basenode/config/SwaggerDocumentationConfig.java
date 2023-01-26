package io.coti.basenode.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerDocumentationConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info().title("COTI APIs")
                        .description("APIs for COTI Fullnode")
                        .version("3.2.0")
                        .license(new License().name("GNU General Public License")
                                .url("http://www.gnu.org/licenses/")));
    }

}
