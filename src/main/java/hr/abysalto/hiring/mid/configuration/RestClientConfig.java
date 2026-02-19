package hr.abysalto.hiring.mid.configuration;

import hr.abysalto.hiring.mid.client.ProductClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Value("${dummyjson.base-url}")
    private String dummyJsonBaseUrl;

    @Bean
    public RestClient dummyJsonRestClient() {
        return RestClient.builder()
                .baseUrl(dummyJsonBaseUrl)
                .build();
    }

    @Bean
    public ProductClient productClient(RestClient dummyJsonRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(dummyJsonRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(ProductClient.class);
    }
}
