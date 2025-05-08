package com.syi.project.discord.listener;

import com.syi.project.discord.config.DiscordBotConfig;
import com.syi.project.support.dto.DeveloperResponseDTO;
import com.syi.project.support.enums.SupportStatus;
import com.syi.project.support.service.DeveloperResponseService;
import com.syi.project.support.service.SupportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageListener extends ListenerAdapter {

  private final DeveloperResponseService developerResponseService;
  private final DiscordBotConfig config;

  // !답변 123 내용 형식의 명령어 패턴
  private static final Pattern RESPONSE_PATTERN = Pattern.compile("^!답변\\s+(\\d+)\\s+(.+)$", Pattern.DOTALL);

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    // 봇 자신의 메시지는 무시
    if (event.getAuthor().isBot()) return;

    // 설정된 채널 ID가 있다면 해당 채널에서만 동작
    if (config.getDevChannelId() != null &&
        !config.getDevChannelId().isEmpty() &&
        !event.getChannel().getId().equals(config.getDevChannelId())) {
      return;
    }

    String content = event.getMessage().getContentRaw();

    // !답변 명령어 처리
    if (content.startsWith("!답변")) {
      Matcher matcher = RESPONSE_PATTERN.matcher(content);

      if (!matcher.find()) {
        event.getChannel().sendMessage("올바른 형식: `!답변 [문의ID] [답변내용]`").queue();
        return;
      }

      try {
        Long supportId = Long.parseLong(matcher.group(1));
        String responseContent = matcher.group(2);
        String developerId = event.getAuthor().getId();
        String developerName = event.getAuthor().getName();

        log.info("개발팀 응답 등록 요청 - 문의ID: {}, 개발자: {}", supportId, developerName);

        // 👉 모든 처리 위임
        developerResponseService.handleDiscordResponse(supportId, responseContent, developerId, developerName);

        event.getChannel().sendMessage("✅ 문의 #" + supportId + "에 대한 답변이 등록되었습니다.").queue();

      } catch (NumberFormatException e) {
        event.getChannel().sendMessage("❌ 문의ID는 숫자여야 합니다.").queue();
      } catch (Exception e) {
        event.getChannel().sendMessage("❌ 오류 발생: " + e.getMessage()).queue();
      }
    }
  }
}