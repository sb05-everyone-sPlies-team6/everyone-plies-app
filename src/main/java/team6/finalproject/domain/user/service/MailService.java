package team6.finalproject.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String from;

  // @Async("notiExecutor")
  public void sendMail(String to, String tempPassword, long ttlSeconds) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(from);
      message.setTo(to);
      message.setSubject("[모두의 플리] 임시 비밀번호 안내");
      message.setText(
          "안녕하세요.\n\n" +
              "임시 비밀번호가 발급되었습니다.\n\n" +
              "임시 비밀번호: " + tempPassword + "\n" +
              "유효 시간: " + (ttlSeconds / 60) + "분\n\n" +
              "로그인 후 반드시 비밀번호를 재설정해 주세요.\n"
      );

      mailSender.send(message);
    } catch (Exception e) {
      log.error("[메일 전송 실패] to: " + to);
    }
  }





}
