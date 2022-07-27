package study.querydsl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;
import study.querydsl.repository.MemberRepository;


import javax.persistence.EntityManager;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired MemberRepository memberRepository;

    @Autowired EntityManager em;

    @BeforeEach
    void setEntity() {
        Team team = new Team("TeamA");
        Member member = Member.createMember("member1", 15, team);

        memberRepository.save(member);
    }

    @Test
    @DisplayName("맴버 가입 테스트")
    void join() throws Exception {
        //given
        Team team = new Team("TeamB");
        Member member = Member.createMember("member2", 25, team);

        memberRepository.save(member);
        em.flush();
        em.clear();

        //when
        Member findMember = memberRepository.findById(member.getId())
                .orElse(Member.createMember("Empty", 999, null));

        //then
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    }


    @Test
    void findAll() {
        for (int index = 0; index < 5; index++) {
            Team team = new Team("Team_" + index);
            Member member = Member.createMember("member_" + index, 25 + index, team);
            memberRepository.save(member);
        }

        em.flush();
        em.clear();

        //when
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> memberPage = memberRepository.findAll(pageRequest);

        //then
        assertThat(memberPage.getTotalElements()).isEqualTo(10);
        assertThat(memberPage.getTotalPages()).isEqualTo(4);
        assertThat(memberPage.isFirst()).isTrue();
    }
}