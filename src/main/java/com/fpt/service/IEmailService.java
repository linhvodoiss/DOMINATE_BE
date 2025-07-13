package com.fpt.service;

public interface IEmailService {

	void sendRegistrationUserConfirm(String email);

	void sendResetPassword(String email);
	void sendEmailForConfirmOrder(String email, Long packageId, Integer orderId);
}
