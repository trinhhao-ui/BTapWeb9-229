package vn.iotstar;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

    @Bean
    CommandLineRunner init(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

            // Khởi tạo tài khoản admin
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Administrator")
                        .images("/images/user.png")
                        .role(adminRole)
                        .enabled(true)
                        .build();
                userRepository.save(admin);
            }

            // Khởi tạo tài khoản sinh viên Trịnh Văn Phú Hào
            if (userRepository.findByUsername("user01").isEmpty()) {
                User user = User.builder()
                        .username("user01")
                        .email("user01@gmail.com")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Trịnh Văn Phú Hào")
                        .images("/images/24110013.jpg")
                        .role(userRole)
                        .enabled(true)
                        .build();
                userRepository.save(user);
            }
        };
    }

    @Bean
    org.springframework.context.ApplicationListener<org.springframework.boot.context.event.ApplicationReadyEvent> readyListener(
            @org.springframework.beans.factory.annotation.Value("${server.port:8080}") String port,
            @org.springframework.beans.factory.annotation.Value("${server.servlet.context-path:}") String contextPath
    ) {
        return event -> {
            String baseUrl = "http://localhost:" + port + contextPath;
            System.out.println("\n=================================================================");
            System.out.println("  🚀 ỨNG DỤNG ĐÃ KHỞI CHẠY THÀNH CÔNG!");
            System.out.println("  👉 Click để mở trên trình duyệt: " + baseUrl);
            System.out.println("  👉 Trang Đăng nhập: " + baseUrl + "/login");
            System.out.println("  👉 Trang Quản lý Sản phẩm: " + baseUrl + "/products");
            System.out.println("  👉 Trang Quản lý Users (Admin): " + baseUrl + "/users");
            System.out.println("=================================================================\n");
        };
    }
}
