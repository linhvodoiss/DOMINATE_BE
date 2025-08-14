package com.fpt.service;


import com.fpt.websocket.PaymentSocketService;
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
import java.io.UnsupportedEncodingException;

@Component
public class EmailService implements IEmailService {
	@Value("${frontend.url}")
	private String frontendUrl;
	@Value("${spring.mail.admin}")
	private String emailAdmin;

	@Autowired
	private IUserService userService;

	@Autowired
	private RegistrationUserTokenRepository registrationUserTokenRepository;

	@Autowired
	private ResetPasswordTokenRepository resetPasswordTokenRepository;

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private PaymentSocketService paymentSocketService;
	@Override
	public void sendRegistrationUserConfirm(String email) {

		User user = userService.findUserByEmail(email);
		String token = registrationUserTokenRepository.findByUserId(user.getId());

		String confirmationUrl = frontendUrl +"/active?token=" + token;

		String subject = "Confirm Your Account Registration";
		String content = "<html><body>" +
				"<p>Hi <strong>" + user.getFirstName() + "</strong>,</p>" +
				"<p>Thank you for registering your account with DOMinate.</p>" +
				"<p>Please <a href=\"" + confirmationUrl + "\"><strong>click here to activate your account</strong></a>.</p>" +
				"<p>If you did not register, please ignore this email.</p>" +
				"<br/><p>Best regards,<br/>DOMinate Team</p>" +
				"</body></html>";

		sendEmail(email, subject, content);
	}

	@Override
	public void sendResetPassword(String email) {

		User user = userService.findUserByEmail(email);
		String token = resetPasswordTokenRepository.findByUserId(user.getId());

		String confirmationUrl = frontendUrl +"/new-password?token=" + token;

		String subject = "Reset Your Password";
		String content = "<html><body>" +
				"<p>Hi <strong>" + user.getFirstName() + "</strong>,</p>" +
				"<p>We received a request to reset your password.</p>" +
				"<p>Please <a href=\"" + confirmationUrl + "\"><strong>click here to reset your password</strong></a>.</p>" +
				"<p>If you did not make this request, you can safely ignore this email.</p>" +
				"<br/><p>Best regards,<br/>DOMinate Team</p>" +
				"</body></html>";

		sendEmail(email, subject, content);
	}

	@Override
	public void sendEmailForCustomer(String email, Long packageId, Integer orderId) {
		User user = userService.findUserByEmail(email);

		if (user == null) {
			throw new IllegalArgumentException("Not found user with email: " + email);
		}

		String confirmationUrl = frontendUrl +"/orders/" + packageId + "?orderId=" + orderId;

		String subject = "Confirm Your Payment";
		String content = "<p>Hello " + user.getFirstName() + ",</p>"
				+ "<p>Thank you for your order. Please <a href=\"" + confirmationUrl + "\">click here to view your payment</a>.</p>"
				+ "<p>If you did not make this order, please ignore this email.</p>"
				+ "<p>Best regards,<br/>DOMINATE Team</p>";

		sendEmail(email, subject, content);
	}

	@Override
	public void sendEmailForNotificationAdmin(String email, Long packageId, Integer orderId) {

		String confirmationUrl = frontendUrl +"/admin/preview/"  + orderId;

		String subject = "Confirm Your Order "+orderId;
		String content = "<p>Hello " + ",</p>"
				+ "<p>DOMINATE have an new order. Please <a href=\"" + confirmationUrl + "\">click here to confirm </a>.</p>";


		sendEmail(email, subject, content);
	}

	@Override
	public void sendEmailReport(Long packageId, Integer orderId, String content) {

		String confirmationUrl = frontendUrl +"/admin/preview/"  + orderId;

		String subject = "Report order "+orderId;
		String contentMess = "<p>Reason: " + content +",</p>"
				+ "<p>Check <a href=\"" + confirmationUrl + "\">click here to view order </a>.</p>";


		sendEmail(emailAdmin, subject, contentMess);
		paymentSocketService.notifyOrderReport(orderId, content);
	}




	private void sendEmail(final String recipientEmail, final String subject, final String content) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(recipientEmail);
			helper.setSubject(subject);
			helper.setText(content, true);
			helper.setFrom("noreply@dominate.com", "DOMinate Team");
			mailSender.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException("Failed to send email", e);
		} catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
