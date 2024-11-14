package com.syi.project.auth.service;

import com.syi.project.auth.dto.DuplicateCheckDTO;
import com.syi.project.auth.dto.MemberDTO;
import com.syi.project.auth.dto.MemberLoginRequestDTO;
import com.syi.project.auth.dto.MemberLoginResponseDTO;
import com.syi.project.auth.dto.MemberSignUpRequestDTO;
import com.syi.project.auth.dto.MemberSignUpResponseDTO;
import com.syi.project.common.enums.CheckStatus;
import com.syi.project.common.enums.Role;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {

  DuplicateCheckDTO checkMemberIdDuplicate(String memberId);

  DuplicateCheckDTO checkEmailDuplicate(String email);

  MemberSignUpResponseDTO register(MemberSignUpRequestDTO requestDTO);

  MemberLoginResponseDTO login(MemberLoginRequestDTO requestDTO, Role requriedRole);

  Page<MemberDTO> getFilteredMembers(CheckStatus checkStatus, Role role, Pageable pageable);

  MemberDTO getMemberDetail(String memberId);

  String refreshToken(String refreshToken);
}
