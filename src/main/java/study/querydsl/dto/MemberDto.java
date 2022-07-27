package study.querydsl.dto;

import lombok.Data;
import study.querydsl.domain.Member;

@Data
public class MemberDto {

    private Long id;
    private String username;
    private String teamName;

    public MemberDto(Long id, String username, String teamName) {
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }

    public MemberDto(Member member) {
        id = member.getId();
        username = getUsername();
        teamName = null;
    }
}
