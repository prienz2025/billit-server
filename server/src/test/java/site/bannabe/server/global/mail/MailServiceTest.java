package site.bannabe.server.global.mail;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

  @InjectMocks
  private MailService mailService;

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private TemplateEngine templateEngine;

  @BeforeEach
  void setup() {
    MimeMessage mimeMessage = mock(MimeMessage.class);
    given(mailSender.createMimeMessage()).willReturn(mimeMessage);
  }

  @Test
  @DisplayName("이메일 전송 정상 작동")
  void sendAuthCodeMail() {
    //given
    String email = "test@test.com";
    String authCode = "1234";
    String expctedHtmlContent = "<html><body><p>인증코드: 123456</p></body></html>";

    given(templateEngine.process(eq("email-auth-code"), any(Context.class))).willReturn(expctedHtmlContent);

    //when
    mailService.sendAuthCodeMail(email, authCode);

    //then
    verify(mailSender).createMimeMessage();
    verify(templateEngine).process(eq("email-auth-code"), any(Context.class));
    verify(mailSender).send(any(MimeMessage.class));
  }

  @Test
  @DisplayName("이메일 전송 실패 시 예외 발생")
  void sendMailException() {
    //given
    String email = "test@test.com";
    String authCode = "123456";

    given(templateEngine.process(anyString(), any(Context.class))).willReturn("htmlContent");
    willThrow(new MailSendException(ErrorCode.MAIL_SEND_FAILED.getMessage())).given(mailSender).send(any(MimeMessage.class));

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> mailService.sendAuthCodeMail(email, authCode))
        .withMessage(ErrorCode.MAIL_SEND_FAILED.getMessage());

    verify(mailSender).createMimeMessage();
    verify(templateEngine).process(anyString(), any(Context.class));
    verify(mailSender).send(any(MimeMessage.class));
  }


}