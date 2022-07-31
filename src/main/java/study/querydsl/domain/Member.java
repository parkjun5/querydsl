package study.querydsl.domain;

import lombok.*;

import javax.persistence.*;

import java.util.Objects;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;
    private int age;

    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_id")
    @ToString.Exclude
    private Team team;

    public Member(String username) {
        this.age = 0;
        this.username = username;
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public Member(String username, MemberRole memberRole) {
        this.username = username;
        this.memberRole = memberRole;
    }

    public static Member createMember(String username, int age, Team team) {
        Member member = new Member();
        member.username = username;
        member.age = age;
        if(team != null) {
            member.changeTeam(team);
        }
        return member;
    }

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return age == member.age && Objects.equals(id, member.id) && Objects.equals(username, member.username) && memberRole == member.memberRole && Objects.equals(team, member.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, age, memberRole, team);
    }
}
