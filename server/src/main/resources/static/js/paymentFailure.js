window.addEventListener('load', renderingPaymentFailure);

function renderingPaymentFailure() {
  const urlParams = new URLSearchParams(window.location.search);
  let errorCode = urlParams.get('code');
  let message = urlParams.get('message');
  if (!errorCode) {
    errorCode = 'PAY_PROCESS_ABORTED';
  }
  if (!message) {
    message = '결제 에러가 발생했습니다! 토스로 문의하세요.';
  }
  document.getElementById('code').innerText = errorCode;
  document.getElementById('message').innerText = message;
}

