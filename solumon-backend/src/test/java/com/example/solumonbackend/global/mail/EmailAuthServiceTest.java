package com.example.solumonbackend.global.mail;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import com.example.solumonbackend.member.entity.Member;
import com.example.solumonbackend.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Transactional
@ExtendWith(MockitoExtension.class)
class EmailAuthServiceTest {

  @Mock
  private JavaMailSender javaMailSender;

  @Mock
  PasswordEncoder passwordEncoder;

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private EmailAuthService emailAuthService;

  @Test
  @DisplayName("이메일 인증 번호 발송 성공")
  void sendSimpleMessage_success() throws Exception {
    //given
    Session fakeSession = Session.getDefaultInstance(System.getProperties());
    MimeMessage fakeMimeMessage = new MimeMessage(fakeSession);

    when(javaMailSender.createMimeMessage())
        .thenReturn(fakeMimeMessage);

    //when
    emailAuthService.sendSimpleMessage("test@gmail.com");

    ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

    //then
    verify(javaMailSender, times(1)).createMimeMessage();
    verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());

    assertEquals("solumon 회원가입 인증 코드", mimeMessageCaptor.getValue().getSubject());
    assertEquals("test@gmail.com", mimeMessageCaptor.getValue().getAllRecipients()[0].toString());
  }

  @Test
  @DisplayName("임시 비밀번호 발송 성공")
  void sendTempPasswordMessage_success() throws Exception {
    //given
    Session fakeSession = Session.getDefaultInstance(System.getProperties());
    MimeMessage fakeMimeMessage = new MimeMessage(fakeSession);

    when(memberRepository.findByEmail("test@gmail.com"))
        .thenReturn(Optional.of(Member.builder().build()));
    when(javaMailSender.createMimeMessage())
        .thenReturn(fakeMimeMessage);

    //when
    emailAuthService.sendTempPasswordMessage("test@gmail.com");

    ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
    ArgumentCaptor<MimeMessage> mimeMessageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

    //then
    verify(memberRepository, times(1)).findByEmail("test@gmail.com");
    verify(memberRepository, times(1)).save(memberCaptor.capture());
    verify(javaMailSender, times(1)).createMimeMessage();
    verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());

    assertEquals("solumon 임시 비밀번호 안내 메일입니다.", mimeMessageCaptor.getValue().getSubject());
    assertEquals("test@gmail.com", mimeMessageCaptor.getValue().getAllRecipients()[0].toString());
  }

  @Test
  @DisplayName("임시 비밀번호 발송 실패 - 존재하지 않는 회원")
  void sendTempPasswordMessage_fail_notFoundMember() throws Exception {
    //given
    when(memberRepository.findByEmail("test@gmail.com"))
        .thenReturn(Optional.empty());

    //when
    MemberException exception = assertThrows(MemberException.class,
        () -> emailAuthService.sendTempPasswordMessage("test@gmail.com"));

    //then
    verify(memberRepository, times(1)).findByEmail("test@gmail.com");

    assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
  }

}