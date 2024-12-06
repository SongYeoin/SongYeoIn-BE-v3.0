package com.syi.project.period;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DayOfWeekMapper {
  private static final Map<String, List<String>> dayMapping = Map.of(
      "월~금", Arrays.asList("월요일", "화요일", "수요일", "목요일", "금요일"),
      "월~일", Arrays.asList("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"),
      "주말", Arrays.asList("토요일", "일요일")
  );

  public static List<String> mapToDays(String dayOfWeek) {
    return dayMapping.getOrDefault(dayOfWeek, List.of(dayOfWeek));
  }

}
