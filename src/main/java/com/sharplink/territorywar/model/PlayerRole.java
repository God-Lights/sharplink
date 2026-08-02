package com.sharplink.territorywar.model;

public enum PlayerRole {
    /** 팀을 창단한 사람. 팀 내 유일. */
    LEADER,
    /** 창단 시점부터 함께한 정식 팀원. */
    MEMBER,
    /** 6팀이 다 찬 뒤 늦게 합류했거나, 팀이 소멸해 무소속이 된 사람. 실제 플레이 권한은 MEMBER와 동일. */
    SEMI_MEMBER
}
