package com.syi.project.common.annotation;

import com.syi.project.course.entity.Course;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;

public class StartBeforeEndValidator implements ConstraintValidator<StartBeforeEnd, Object> {

  private String startDateField;
  private String endDateField;
  private String startTimeField;
  private String endTimeField;

  @Override
  public void initialize(StartBeforeEnd constraintAnnotation) {
    this.startDateField = constraintAnnotation.startDateField();
    this.endDateField = constraintAnnotation.endDateField();
    this.startTimeField = constraintAnnotation.startTimeField();
    this.endTimeField = constraintAnnotation.endTimeField();
  }

  @Override
  public boolean isValid(Object object, ConstraintValidatorContext context) {
    try {
      // 날짜 필드 가져오기
      LocalDate startDate = getFieldAsDate(object, startDateField);
      LocalDate endDate = getFieldAsDate(object, endDateField);

      // 시간 필드 가져오기
      LocalTime startTime = getFieldAsTime(object, startTimeField);
      LocalTime endTime = getFieldAsTime(object, endTimeField);

      // 날짜 검증
      if (startDate != null && endDate != null) {
        if (endDate.isBefore(startDate)) return false;
      }

      // 시간 검증
      if (startTime != null && endTime != null) {
        if (endTime.isBefore(startTime)) return false;
      }

      return true; // 유효함

    } catch (Exception e) {
      e.printStackTrace();
      return false; // 오류 발생 시 유효하지 않은 것으로 간주
    }
  }

  private LocalDate getFieldAsDate(Object object, String fieldName) throws IllegalAccessException, NoSuchFieldException {
    if (fieldName.isEmpty()) return null;
    Field field = object.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return (LocalDate) field.get(object);
  }

  private LocalTime getFieldAsTime(Object object, String fieldName) throws IllegalAccessException, NoSuchFieldException {
    if (fieldName.isEmpty()) return null;
    Field field = object.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return (LocalTime) field.get(object);
  }
}
