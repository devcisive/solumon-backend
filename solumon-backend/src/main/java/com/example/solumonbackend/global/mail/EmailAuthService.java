package com.example.solumonbackend.global.mail;

import java.util.Random;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAuthService {

  private final JavaMailSender javaMailSender;

  public String sendSimpleMessage(String email) throws Exception {
    String code = createRandomCode();

    MimeMessage message = createMessage(email, code);

    try {
      javaMailSender.send(message); // 메일 발송
    } catch (MailException es) {
      es.printStackTrace();
      throw new IllegalArgumentException();
    }

    return code;
  }

  private MimeMessage createMessage(String email, String code) throws MessagingException {
    MimeMessage message = javaMailSender.createMimeMessage();

    message.addRecipients(RecipientType.TO, email);
    message.setSubject("solumon 회원가입 인증 코드");
    String msg = "\n"
        + "<!DOCTYPE html>\n"
        + "<html lang=\"en\">\n"
        + "  <head>\n"
        + "    <meta charset=\"UTF-8\" />\n"
        + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
        + "    <title>Solumon</title>\n"
        + "  </head>\n"
        + "  <body>\n"
        + "    <h1>Solumon 회원가입 인증 코드입니다.</h1>\n"
        + "    <h3>아래 6자리 숫자를 입력칸에 입력해주세요</h3>\n"
        + "    <div style=\"align-self: center;  border: 5px solid black; width: 50%; height: 10%;\">\n"
        + "      <h1 style=\"text-align: center; font-size: 50px;\">" + code + "</h1>\n"
        + "  </body>\n"
        + "</html>";

    message.setText(msg, "utf-8", "html");
    return message;
  }

  private String createRandomCode() {
    StringBuilder key = new StringBuilder();
    Random randomCode = new Random();

    for (int i = 0; i < 6; i++) { // 인증코드 6자리
      key.append((randomCode.nextInt(10)));
    }

    return key.toString();
  }


}
