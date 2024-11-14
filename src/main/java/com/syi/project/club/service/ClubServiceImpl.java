package com.syi.project.club.service;

import com.syi.project.club.entity.Club;
import com.syi.project.club.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ClubServiceImpl implements ClubService {

    @Autowired
    private ClubRepository clubRepository;

    @Override
    public List<ClubVO> getListPaging(Criteria cri, Integer classNo) {
        // 페이징 처리 및 classNo 기준으로 데이터 조회 (기본 예시)
        return clubRepository.findAll()
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public int getTotal(Criteria cri, Integer classNo) {
        // 총 개수를 반환 (기본 예시)
        return (int) clubRepository.count();
    }

    @Override
    public ClubVO getPage(int clubNo) {
        Club club = clubRepository.findById((long) clubNo).orElse(null);
        return convertToVO(club);
    }

    @Override
    @Transactional
    public boolean modifyAdmin(ClubVO clubVO) {
        Club club = clubRepository.findById(clubVO.getId()).orElse(null);
        if (club == null) {
            return false;
        }
        // 클럽 수정 로직 (필요한 필드만 수정)
        club.setName(clubVO.getName());
        club.setWriter(clubVO.getWriter());
        club.setCheckStatus(clubVO.getCheckStatus());
        club.setCheckCmt(clubVO.getCheckCmt());
        club.setStudyDate(clubVO.getStudyDate());
        club.setParticipants(clubVO.getParticipants());
        club.setContent(clubVO.getContent());
        // 파일 관련 수정 (필요시 추가)
        return true;
    }

    @Override
    @Transactional
    public boolean delete(int clubNo) {
        Club club = clubRepository.findById((long) clubNo).orElse(null);
        if (club != null) {
            clubRepository.delete(club);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean register(ClubVO clubVO) {
        Club club = new Club();
        club.setName(clubVO.getName());
        club.setWriter(clubVO.getWriter());
        club.setCheckStatus(clubVO.getCheckStatus());
        club.setCheckCmt(clubVO.getCheckCmt());
        club.setStudyDate(clubVO.getStudyDate());
        club.setParticipants(clubVO.getParticipants());
        club.setContent(clubVO.getContent());
        // 파일 관련 설정
        clubRepository.save(club);
        return true;
    }

    private ClubVO convertToVO(Club club) {
        if (club == null) {
            return null;
        }
        ClubVO clubVO = new ClubVO();
        clubVO.setId(club.getId());
        clubVO.setName(club.getName());
        clubVO.setWriter(club.getWriter());
        clubVO.setChecker(club.getChecker());
        clubVO.setParticipants(club.getParticipants());
        clubVO.setContent(club.getContent());
        clubVO.setRegDate(club.getRegDate());
        clubVO.setStudyDate(club.getStudyDate());
        clubVO.setCheckStatus(club.getCheckStatus());
        clubVO.setCheckCmt(club.getCheckCmt());
        clubVO.setFileOriginalName(club.getFileOriginalName());
        clubVO.setFileSavedName(club.getFileSavedName());
        clubVO.setFileType(club.getFileType());
        clubVO.setFileSize(club.getFileSize());
        clubVO.setFilepath(club.getFilepath());
        clubVO.setFileRegDate(club.getFileRegDate());
        return clubVO;
    }
}
