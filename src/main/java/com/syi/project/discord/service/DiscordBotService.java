package com.syi.project.discord.service;

import com.syi.project.discord.config.DiscordBotConfig;
import com.syi.project.discord.listener.MessageListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordBotService {

  private final DiscordBotConfig config;
  private final MessageListener messageListener;
  private JDA jda;

  @PostConstruct
  public void init() {
    try {
      jda = JDABuilder.createDefault(config.getToken())
          .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
          .setActivity(Activity.watching("문의에 답변하는 중"))
          .addEventListeners(messageListener)
          .build();

      jda.awaitReady();
      log.info("디스코드 봇이 성공적으로 시작되었습니다. 봇 이름: {}", jda.getSelfUser().getName());
    } catch (Exception e) {
      log.error("디스코드 봇 초기화 중 오류 발생", e);
    }
  }

  @PreDestroy
  public void shutdown() {
    if (jda != null) {
      jda.shutdown();
      log.info("디스코드 봇이 종료되었습니다.");
    }
  }

  // JDA 객체를 반환하는 getter 메소드 추가
  public JDA getJda() {
    return jda;
  }
}