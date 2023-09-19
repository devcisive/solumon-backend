package com.example.solumonbackend.member.type;

public enum MemberRole {
  GENERAL("ROLE_GENERAL"),
  BANNED("ROLE_BANNED"),
  PERMANENT_BAN("ROLE_PERMANENT_BAN");

  final String role;

  MemberRole(String role) {
    this.role = role;
  }

  public String value() {
    return role;
  }
}
