package com.syi.project.support.service;

import com.syi.project.common.utils.S3Uploader;
import com.syi.project.discord.config.DiscordBotConfig;
import com.syi.project.discord.service.DiscordBotService;
import com.syi.project.file.entity.File;
import com.syi.project.support.entity.Support;
import com.syi.project.support.entity.SupportFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportDiscordService {

  private final DiscordBotConfig discordBotConfig;
  private final S3Uploader s3Uploader;
  private final DiscordBotService discordBotService;

  /**
   * 문의글을 디스코드로 전송
   * @param support 디스코드로 전송할 문의 정보
   */
  public void sendSupportToDiscord(Support support, String additionalComment) {
    try {
      String channelId = discordBotConfig.getDevChannelId();
      TextChannel channel = discordBotService.getJda().getTextChannelById(channelId);

      if (channel == null) {
        log.error("디스코드 채널을 찾을 수 없습니다. 채널 ID: {}", channelId);
        return;
      }

      // 문의 정보를 포함한 임베드 메시지 생성
      MessageEmbed embed = createSupportEmbed(support);

      // 먼저 임베드 메시지 전송
      channel.sendMessageEmbeds(embed).queue();

      // 추가 메시지가 있는 경우 전송
      if (additionalComment != null && !additionalComment.trim().isEmpty()) {
        EmbedBuilder commentEmbed = new EmbedBuilder();
        commentEmbed.setTitle("💬 관리자 추가 메시지");
        commentEmbed.setDescription(additionalComment);
        commentEmbed.setColor(new Color(119, 59, 147)); // 보라색 계열

        channel.sendMessageEmbeds(commentEmbed.build()).queue();
      }

      // 파일 처리: 이미지는 직접 보여주고, 다른 파일은 링크로 전송
      if (support.getFiles() != null && !support.getFiles().isEmpty()) {
        sendFilesToDiscord(channel, support.getFiles());
      }

      log.debug("문의글이 디스코드로 성공적으로 전송되었습니다. 문의 ID: {}", support.getId());
    } catch (Exception e) {
      log.error("디스코드로 문의글 전송 중 오류 발생 - supportId: {}", support.getId(), e);
    }
  }

  /**
   * 문의 정보를 담은 임베드 메시지 생성
   */
  private MessageEmbed createSupportEmbed(Support support) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    // 임베드 제목을 "📣 새로운 문의"로 변경
    embedBuilder.setTitle("📣 새로운 문의");

    // 문의자와 등록일을 하단에 인라인으로 배치
    embedBuilder.addField("문의자", support.getMember().getName(), true);
    embedBuilder.addField("등록일", support.getRegDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), true);

    // 문의 ID
    embedBuilder.addField("문의 ID", support.getId().toString(), false);

    // 제목 필드 추가
    embedBuilder.addField("제목", support.getTitle(), false);

    // 문의 내용을 별도 필드로 추가
    embedBuilder.addField("내용", support.getContent(), false);

    // 임베드 색상 설정
    embedBuilder.setColor(new Color(204, 153, 0));

    return embedBuilder.build();
  }

  /**
   * 첨부 파일을 디스코드로 전송
   * - 이미지 파일은 직접 보여줌
   * - 문서 등 다른 파일은 링크로 전송
   */
  private void sendFilesToDiscord(TextChannel channel, List<SupportFile> files) {
    List<String> fileMessages = new ArrayList<>();
    StringBuilder imageLinks = new StringBuilder();
    StringBuilder documentLinks = new StringBuilder();

    for (SupportFile supportFile : files) {
      File file = supportFile.getFile();
      String fileUrl = s3Uploader.getUrl(file.getPath());
      String fileName = file.getOriginalName();
      String mimeType = file.getMimeType();

      // 이미지 파일인 경우
      if (mimeType.startsWith("image/")) {
        // 디스코드에서 자동으로 임베딩 되도록 이미지 URL만 별도 메시지로 전송
        imageLinks.append(fileUrl).append("\n");
      } else {
        // 이미지가 아닌 파일은 링크 형태로 제공
        documentLinks.append("[📎 ").append(fileName).append("](").append(fileUrl).append(")\n");
      }
    }

    // 이미지 링크 전송
    if (imageLinks.length() > 0) {
      channel.sendMessage("**📷 이미지 파일:**").queue();
      // 각 이미지 URL을 개별 메시지로 전송하여 디스코드에서 자동 임베딩 되도록 함
      String[] images = imageLinks.toString().split("\n");
      for (String imageUrl : images) {
        if (!imageUrl.isEmpty()) {
          channel.sendMessage(imageUrl).queue();
        }
      }
    }

    // 문서 링크 전송
    if (documentLinks.length() > 0) {
      EmbedBuilder docEmbed = new EmbedBuilder();
      docEmbed.setTitle("📑 첨부 파일");
      docEmbed.setDescription(documentLinks.toString());
      docEmbed.setColor(new Color(255, 153, 0));
      channel.sendMessageEmbeds(docEmbed.build()).queue();
    }
  }
}