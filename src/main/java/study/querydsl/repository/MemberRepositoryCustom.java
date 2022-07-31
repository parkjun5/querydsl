package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> findAllQuerydsl();
    List<MemberTeamDto> findByUsernameQuery(String username);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCond memberSearchCond, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCond memberSearchCond, Pageable pageable);
}
