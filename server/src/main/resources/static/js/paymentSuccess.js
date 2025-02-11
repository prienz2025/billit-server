window.addEventListener('load', fetchPaymentConfirm)

async function fetchPaymentConfirm() {
  const urlParams = new URLSearchParams(window.location.search);
  const payload = {
    paymentKey: urlParams.get('paymentKey'),
    amount: urlParams.get('amount'),
    orderId: urlParams.get('orderId')
  };

  const accessToken = sessionStorage.getItem('accessToken');

  await fetch('http://localhost:8080/v1/payments/confirm', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`,
    },
    body: JSON.stringify(payload),
  })
  .then(res => res.json())
  .then(payload => {
    const token = payload.data.rentalHistoryToken;
    window.location.href = `bannabe://payment-success?rentalHistoryToken=${token}`;
  })
  .catch(err => console.error(err));

}