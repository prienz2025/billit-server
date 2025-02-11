package site.bannabe.server.global.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthCode {

  private String authCode;

  private Boolean verified;

  public void markAsVerified() {
    this.verified = true;
  }

  public boolean isVerified() {
    return this.verified;
  }

}