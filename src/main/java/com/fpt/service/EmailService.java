package com.fpt.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.fpt.entity.User;
import com.fpt.repository.RegistrationUserTokenRepository;
import com.fpt.repository.ResetPasswordTokenRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class EmailService implements IEmailService {
	@Value("${frontend.url}")
	private String frontendUrl;

	@Autowired
	private IUserService userService;

	@Autowired
	private RegistrationUserTokenRepository registrationUserTokenRepository;

	@Autowired
	private ResetPasswordTokenRepository resetPasswordTokenRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Override
	public void sendRegistrationUserConfirm(String email) {

		User user = userService.findUserByEmail(email);
		String token = registrationUserTokenRepository.findByUserId(user.getId());

		String confirmationUrl = frontendUrl +"/active?token=" + token;

		String subject = "Xác Nhận Đăng Ký Account";
		String content = "Bạn đã đăng ký thành công. Click vào link dưới đây để kích hoạt tài khoản \n"
				+ confirmationUrl;

		sendEmail(email, subject, content);
	}

	@Override
	public void sendResetPassword(String email) {

		User user = userService.findUserByEmail(email);
		String token = resetPasswordTokenRepository.findByUserId(user.getId());

		String confirmationUrl = frontendUrl +"/new-password?token=" + token;

		String subject = "Thiết lập lại mật khẩu";
		String content = "Click vào link dưới đây để thiết lập lại mật khẩu (nếu không phải bạn xin vui lòng bỏ qua).\n"
				+ confirmationUrl;

		sendEmail(email, subject, content);
	}
	@Override
	public void sendEmailForConfirmOrder(String email, Long packageId, Integer orderId) {
		User user = userService.findUserByEmail(email);

		if (user == null) {
			throw new IllegalArgumentException("Không tìm thấy người dùng với email: " + email);
		}

		String confirmationUrl = frontendUrl +"/orders/" + packageId + "?orderId=" + orderId;

		String subject = "Confirm Your Payment";
		String content = "<p>Hello " + user.getFirstName() + ",</p>"
				+ "<p>Thank you for your order. Please <a href=\"" + confirmationUrl + "\">click here to confirm or view your payment</a>.</p>"
				+ "<p>If you did not make this order, please ignore this email.</p>"
				+ "<p>Best regards,<br/>DOMINATE Team</p>";

		sendEmail(email, subject, content);
	}



	private void sendEmail(final String recipientEmail, final String subject, final String content) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(recipientEmail);
			helper.setSubject(subject);
			helper.setText(content, true); // true => nội dung là HTML

			mailSender.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException("Failed to send email", e);
		}
	}

}
