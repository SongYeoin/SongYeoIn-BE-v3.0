package com.syi.project.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented     // JavaDocs에 포함되도록 하는 것
@Constraint(validatedBy = StartBeforeEndValidator.class)   // 제약 조건을 나타내는 애너테이션, 검증을 수행할 클래스
@Target({ ElementType.TYPE })   // 어노테이션을 적용할 위치 지정, TYPE은 클래스, 인터페이스. 열거형 등 타입 수준에서만 사용 가능함
@Retention(RetentionPolicy.RUNTIME)   // 얼마나 오래 유지 되는지 설정, 런타임시에도 검증 수행할 수 있도록 함
public @interface StartBeforeEnd {
  String message() default "시작 날짜/시간이 종료 날짜/시간보다 빨라야 합니다.";    // 어노테이션 속성으로, 검증 실패시 표시할 에러 메시지
  Class<?>[] groups() default {};   // 유효성 검사 그룹 지정하는 속성, 기본값은 빈 배열, 특정 그룹에서만 사용하도록 적용도 가능
  Class<? extends Payload>[] payload() default {};  // 검증에 대한 추가적인 메타데이터 정보 전달할 때 사용

  // 필드 이름을 속성으로 받아 검증에 사용할 수 있도록 설정
  String startDateField() default "";
  String endDateField() default "";
  String startTimeField() default "";
  String endTimeField() default "";
}
