package com.syi.project.auth.service;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.common.enums.Role;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {

  DuplicateCheckDTO checkMemberIdDuplicate(String memberId);

  DuplicateCheckDTO checkEmailDuplicate(String email);

  MemberSignUpResponseDTO register(MemberSignUpRequestDTO requestDTO);

  MemberLoginResponseDTO login(MemberLoginRequestDTO requestDTO, Role requriedRole);

  String refreshToken(String refreshToken);
}
