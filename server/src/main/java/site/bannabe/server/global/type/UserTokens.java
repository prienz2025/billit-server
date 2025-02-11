package site.bannabe.server.global.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserTokens {

  private String refreshToken;
  private String deviceToken;

}