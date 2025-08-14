package com.fpt.service;

public interface IEmailService {

	void sendRegistrationUserConfirm(String email);

	void sendResetPassword(String email);
	void sendEmailForCustomer(String email, Long packageId, Integer orderId);
	void sendEmailForNotificationAdmin(String email, Long packageId, Integer orderId);
	void sendEmailReport(Long packageId, Integer orderId, String content);
}
