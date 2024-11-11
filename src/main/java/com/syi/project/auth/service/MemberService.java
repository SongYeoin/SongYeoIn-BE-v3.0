package com.syi.project.auth.service;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {

  DuplicateCheckDTO checkMemberIdDuplicate(String memberId);
  DuplicateCheckDTO checkEmailDuplicate(String email);
  MemberSignUpResponseDTO register(MemberSignUpRequestDTO requestDTO);
}
