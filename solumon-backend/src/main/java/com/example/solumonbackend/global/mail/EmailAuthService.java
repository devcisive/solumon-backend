package com.example.solumonbackend.global.mail;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MailException;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import java.security.SecureRandom;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAuthService {

  private final JavaMailSender javaMailSender;
  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  private static final char[] rndAllCharacters = new char[]{
      //number
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      //uppercase
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      //lowercase
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      //special symbols
      '@', '$', '!', '%', '?', '&', '#', '^', '+', '='
  };

  public String sendSimpleMessage(String email) throws Exception {
    String code = createRandomCode();

    MimeMessage message = createMessage(email, code);

    try {
      javaMailSender.send(message); // 메일 발송
    } catch (Exception e) {
      log.error("An error occurred during email sending: {}", e.getMessage(), e);
      throw new MailException(ErrorCode.FAIL_TO_SEND_MAIL);
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

  @Transactional
  public void sendTempPasswordMessage(String email) throws Exception {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));

    // 탈퇴한 회원일 경우 불가
    if (member.getUnregisteredAt() != null) {
      throw new MemberException(ErrorCode.UNREGISTERED_MEMBER);
    }

    String tempPassword = createRandomPassword();

    member.setPassword(passwordEncoder.encode(tempPassword));
    memberRepository.save(member);

    MimeMessage message = createTempPasswordMessage(email, tempPassword);

    try {
      javaMailSender.send(message); // 메일 발송
    } catch (Exception e) {
      log.error("An error occurred during email sending: {}", e.getMessage(), e);
      throw new MailException(ErrorCode.FAIL_TO_SEND_MAIL);
    }
  }

  private String createRandomPassword() {
    SecureRandom random = new SecureRandom();
    StringBuilder randomPassword = new StringBuilder();

    int rndAllCharactersLength = rndAllCharacters.length;
    for (int i = 0; i < 10; i++) {
      randomPassword.append(rndAllCharacters[random.nextInt(rndAllCharactersLength)]);
    }

    return randomPassword.toString();
  }

  private MimeMessage createTempPasswordMessage(String email, String password) throws MessagingException {
    MimeMessage message = javaMailSender.createMimeMessage();

    message.addRecipients(RecipientType.TO, email);
    message.setSubject("solumon 임시 비밀번호 안내 메일입니다.");
    String msg = "\n"
        + "<!DOCTYPE html>\n"
        + "<html lang=\"en\">\n"
        + "  <head>\n"
        + "    <meta charset=\"UTF-8\" />\n"
        + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
        + "    <title>Solumon</title>\n"
        + "  </head>\n"
        + "  <body>\n"
        + "    <h1>Solumon에서 발급한 임시 비밀번호입니다.</h1>\n"
        + "    <h3>로그인 후 회원정보 수정에서 비밀번호를 변경해주세요.</h3>\n"
        + "    <div style=\"align-self: center;  border: 5px solid black; width: 50%; height: 10%;\">\n"
        + "      <h1 style=\"text-align: center; font-size: 50px;\">" + password + "</h1>\n"
        + "  </body>\n"
        + "</html>";

    message.setText(msg, "utf-8", "html");
    return message;
  }

}
