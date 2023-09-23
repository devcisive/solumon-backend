package com.example.solumonbackend.global.util;

import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.global.exception.MemberException;
import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.bcrypt.BCrypt;

@UtilityClass
public class PasswordUtil {

  public static void checkPassword(String password) {
    if (password.length() < 8 || password.length() > 20) {
      throw new MemberException(ErrorCode.PASSWORD_MUST_BE_BETWEEN_8_TO_20_CHARCTERS);
    }

    boolean hasSpecialCharacter = false;
    for (char c: password.toCharArray()) {
      if (c >= 33 && c <= 47 || c >= 58 && c <= 64 || c >= 91 && c <= 96 || c >= 123 && c <= 126) {
        hasSpecialCharacter = true;
        break;
      }
    }
    if (!hasSpecialCharacter) {
      throw new MemberException(ErrorCode.PASSWORD_MUST_HAVE_SPECIAL_CHARACTER);
    }
  }

  public static String encryptPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  public static boolean equalsPassword(String password, String encryptedPassword) {
    return BCrypt.checkpw(password, encryptedPassword);
  }
}