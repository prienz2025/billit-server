package site.bannabe.server.global.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@Service
@RequiredArgsConstructor
public class MailService {

  private static final String FIND_PASSWORD_AUTH_CODE_MAIL_TITLE = "[Bannabe] 비밀번호 찾기 인증코드";
  private static final String CONTENT_HTML_TEMPLATE = "email-auth-code";
  private static final String AUTH_CODE = "authCode";

  private final JavaMailSender mailSender;

  private final TemplateEngine templateEngine;

  @Async("asyncMailExecutor")
  public void sendAuthCodeMail(String email, String authCode) {
    MimeMessage mimeMessage = mailSender.createMimeMessage();

    try {
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
      mimeMessageHelper.setTo(email);
      mimeMessageHelper.setSubject(FIND_PASSWORD_AUTH_CODE_MAIL_TITLE);
      String htmlContent = generateAuthCodeMailContent(authCode);
      mimeMessageHelper.setText(htmlContent, true);
      mailSender.send(mimeMessage);
    } catch (MessagingException | MailException e) {
      throw new BannabeServiceException(ErrorCode.MAIL_SEND_FAILED);
    }

  }

  private String generateAuthCodeMailContent(String authCode) {
    Context context = new Context();
    context.setVariable(AUTH_CODE, authCode);
    return templateEngine.process(CONTENT_HTML_TEMPLATE, context);
  }

}