package com.project.saasbilling.service;

import com.project.saasbilling.model.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@saasbilling.com}")
    private String fromEmail;

    @Value("${spring.application.name:SaaS Billing Platform}")
    private String appName;

    /**
     * Send subscription created email.
     */
    @Async
    public void sendSubscriptionCreatedEmail(User user, Subscription subscription) {
        String subject = "Welcome! Your subscription is active";
        String content = buildSubscriptionCreatedEmail(user, subscription);
        sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Send subscription cancelled email.
     */
    @Async
    public void sendSubscriptionCancelledEmail(User user, Subscription subscription) {
        String subject = "Your subscription has been cancelled";
        String content = buildSubscriptionCancelledEmail(user, subscription);
        sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Send invoice email.
     */
    @Async
    public void sendInvoiceEmail(User user, Invoice invoice) {
        String subject = "Invoice " + invoice.getInvoiceNumber() + " - Payment Due";
        String content = buildInvoiceEmail(user, invoice);
        sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Send payment confirmation email.
     */
    @Async
    public void sendPaymentConfirmationEmail(User user, PaymentLog payment, Invoice invoice) {
        String subject = "Payment Confirmed - " + invoice.getInvoiceNumber();
        String content = buildPaymentConfirmationEmail(user, payment, invoice);
        sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Send generic email.
     */
    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Email service error: {}", e.getMessage());
        }
    }

    private String buildSubscriptionCreatedEmail(User user, Subscription subscription) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4F46E5; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to %s!</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>Your subscription to the <strong>%s</strong> plan is now active!</p>
                            <p><strong>Subscription Details:</strong></p>
                            <ul>
                                <li>Plan: %s</li>
                                <li>Start Date: %s</li>
                                <li>Next Billing Date: %s</li>
                            </ul>
                            <p>Thank you for choosing us!</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                appName,
                user.getFullName(),
                subscription.getPlanName(),
                subscription.getPlanName(),
                subscription.getStartDate(),
                subscription.getNextBillingDate(),
                appName);
    }

    private String buildSubscriptionCancelledEmail(User user, Subscription subscription) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #DC2626; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Subscription Cancelled</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>Your subscription to the <strong>%s</strong> plan has been cancelled.</p>
                            <p>Your access will remain active until: <strong>%s</strong></p>
                            <p>We're sorry to see you go! If you change your mind, you can resubscribe anytime.</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                subscription.getPlanName(),
                subscription.getEndDate(),
                appName);
    }

    private String buildInvoiceEmail(User user, Invoice invoice) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #4F46E5; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .invoice-box { background: white; padding: 20px; border: 1px solid #ddd; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Invoice %s</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>A new invoice has been generated for your subscription.</p>
                            <div class="invoice-box">
                                <p><strong>Invoice Number:</strong> %s</p>
                                <p><strong>Amount:</strong> %s %s</p>
                                <p><strong>Due Date:</strong> %s</p>
                            </div>
                            <p>Please make payment before the due date to avoid service interruption.</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                invoice.getInvoiceNumber(),
                user.getFullName(),
                invoice.getInvoiceNumber(),
                invoice.getCurrency(),
                invoice.getTotalAmount(),
                invoice.getDueDate(),
                appName);
    }

    private String buildPaymentConfirmationEmail(User user, PaymentLog payment, Invoice invoice) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #059669; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background: #f9f9f9; }
                        .payment-box { background: white; padding: 20px; border: 1px solid #ddd; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Payment Confirmed!</h1>
                        </div>
                        <div class="content">
                            <p>Hi %s,</p>
                            <p>Your payment has been successfully processed.</p>
                            <div class="payment-box">
                                <p><strong>Invoice:</strong> %s</p>
                                <p><strong>Amount Paid:</strong> %s %s</p>
                                <p><strong>Transaction ID:</strong> %s</p>
                                <p><strong>Date:</strong> %s</p>
                            </div>
                            <p>Thank you for your payment!</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                user.getFullName(),
                invoice.getInvoiceNumber(),
                payment.getCurrency(),
                payment.getAmount(),
                payment.getTransactionId(),
                payment.getProcessedAt(),
                appName);
    }
}
