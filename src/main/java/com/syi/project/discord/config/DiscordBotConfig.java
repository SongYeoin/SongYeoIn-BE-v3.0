package com.syi.project.discord.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import lombok.Getter;

@Configuration
@PropertySource("classpath:application.yml")
@Getter
public class DiscordBotConfig {

  @Value("${discord.bot.token}")
  private String token;

  @Value("${discord.bot.dev-channel-id}")
  private String devChannelId;
}