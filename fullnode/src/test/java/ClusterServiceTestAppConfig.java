import io.coti.common.services.ClusterService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackageClasses = ClusterService.class,
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE, value = {ClusterService.class} )
        }
)
@EnableScheduling
@PropertySource("classpath:application.properties")
public class ClusterServiceTestAppConfig {
}
