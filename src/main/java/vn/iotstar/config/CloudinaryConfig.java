package vn.iotstar.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @Bean
    Cloudinary cloudinary(
            @Value("${cloudinary.cloud-name:dfdfdf}") String cloudName,
            @Value("${cloudinary.api-key:576632571682623}") String apiKey,
            @Value("${cloudinary.api-secret:ikPEbngxnKwAw-XkvR1WVEaQZcI}") String apiSecret) {
        return new Cloudinary(Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }
}
