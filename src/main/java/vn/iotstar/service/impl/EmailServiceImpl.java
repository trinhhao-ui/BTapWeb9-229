package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:trinhphuhao2108@gmail.com}")
    private String fromEmail;

    @Override
    public void sendOtp(String email, String otp, String subject) {
        System.out.println("=================================================");
        System.out.println(">>> GỬI OTP CHO EMAIL: " + email);
        System.out.println(">>> MÃ OTP LÀ: " + otp);
        System.out.println("=================================================");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject(subject);
            message.setText("""
                    Xin chào,
                    Mã OTP của bạn là: %s
                    OTP có hiệu lực trong 5 phút và chỉ sử dụng một lần.
                    Không chia sẻ mã này cho người khác.
                    """.formatted(otp));
            mailSender.send(message);
            System.out.println(">>> ĐÃ GỬI EMAIL THÀNH CÔNG ĐẾN: " + email);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email qua SMTP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
