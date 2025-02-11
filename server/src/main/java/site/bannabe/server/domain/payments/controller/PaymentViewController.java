package site.bannabe.server.domain.payments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import site.bannabe.server.domain.payments.controller.response.PaymentInitializeResponse;
import site.bannabe.server.domain.payments.entity.PaymentType;
import site.bannabe.server.domain.payments.service.PaymentViewService;

@Controller
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentViewController {

  private final PaymentViewService paymentViewService;

  @PreAuthorize("hasRole('USER')")
  @GetMapping("/checkout")
  public String paymentRequest(
      @RequestParam("rentalItemToken") String rentalItemToken,
      @RequestParam("rentalTime") Integer rentalTime,
      @RequestParam("paymentType") PaymentType paymentType,
      Model model
  ) {
    PaymentInitializeResponse response = paymentViewService.getPaymentRequest(rentalItemToken, rentalTime, paymentType);
    model.addAttribute("paymentRequest", response);
    return "payments/checkout";
  }

  @GetMapping("/success")
  public String paymentSuccess() {
    return "payments/success";
  }

  @GetMapping("/failure")
  public String paymentFailure() {
    return "payments/failure";
  }


}