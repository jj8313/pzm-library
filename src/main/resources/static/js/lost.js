// 분실 버튼 누르면 사유 입력 창 (loanList, bookLoans에서 같이 씀)
document.addEventListener('DOMContentLoaded', function () {
  const forms = document.querySelectorAll('.lost-form');

  forms.forEach(function (form) {
    form.addEventListener('submit', function (e) {
      const reason = prompt('분실 사유를 적어 주세요.\n(예: 회원이 잃어버림, 서가에 없음)');

      // 취소를 눌렀으면 → 보내지 않기
      if (reason === null) {
        e.preventDefault();
        return;
      }

      // 아무것도 안 썼으면 → 보내지 않기
      if (reason.trim() === '') {
        alert('사유를 입력해 주세요.');
        e.preventDefault();
        return;
      }

      // 쓴 사유를 hidden 칸에 넣고 → 그대로 전송
      form.reason.value = reason.trim();
    });
  });
});