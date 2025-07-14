$(document).ready(function () {
    $('.recovery-form').on('submit', function (e) {
        e.preventDefault();

        const email = $('#user_recover').val().trim();

        if (!email) {
            Swal.fire('오류', '이메일을 입력해주세요.', 'warning');
            return;
        }

        $.ajax({
            url: '/api/auth/sendCode',
            type: 'POST',
            data: { email },
            success: function () {
                Swal.fire('성공', '비밀번호 재설정 이메일이 전송되었습니다.', 'success');
            },
            error: function (xhr) {
                if (xhr.status === 404) {
                    Swal.fire('오류', '등록되지 않은 이메일입니다.', 'error');
                } else {
                    Swal.fire('오류', '알 수 없는 오류가 발생했습니다.', 'error');
                }
            }
        });
    });
});
