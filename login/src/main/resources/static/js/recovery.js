$(function () {
    // 1단계: 이메일 입력
    $('.recovery-form').on('submit', function (event) {
        event.preventDefault();
        const email = $('#user_recover').val().trim();

        if (!email) {
            Swal.fire('오류', '이메일을 입력해주세요.', 'error');
            return;
        }

        Swal.fire({
            title: '전송 중...',
            text: '이메일을 보내는 중입니다. 잠시만 기다려주세요.',
            allowOutsideClick: false,
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });

        $.ajax({
            url: '/api/auth/sendCode',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ email }),
            success: function () {
                Swal.fire('성공', '인증번호가 이메일로 전송되었습니다.', 'success');
                switchToStep(2);
            },
            error: function (xhr) {
                if (xhr.status === 404) {
                    Swal.fire('오류', '해당 이메일로 등록된 계정을 찾을 수 없습니다.', 'error');
                } else if (xhr.status === 403) {
                    Swal.fire('오류', '소셜 로그인 계정은 비밀번호를 재설정할 수 없습니다.', 'error');
                } else {
                    Swal.fire('오류', '이메일 전송 중 오류가 발생했습니다.', 'error');
                }
             }
        });
    });

    // 2단계: 인증번호 확인
    $('#verify-code-form').on('submit', function (event) {
        event.preventDefault();
        const code = $('#verify_code_input').val().trim();

        if (!code) {
            Swal.fire('오류', '인증번호를 입력해주세요.', 'error');
            return;
        }

        $.ajax({
            url: '/api/auth/verifyCode',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ code }),
            success: function () {
                Swal.fire('인증 성공', '새 비밀번호를 설정해주세요.', 'success');
                switchToStep(3);
            },
            error: function () {
                Swal.fire('오류', '인증번호가 올바르지 않습니다.', 'error');
            }
        });
    });

    // 3단계: 비밀번호 재설정
    $('#reset-password-form').on('submit', function (event) {
        event.preventDefault();

        const newPassword = $('#new_password').val().trim();
        const confirmPassword = $('#confirm_password').val().trim();

        if (!newPassword || !confirmPassword) {
            Swal.fire('오류', '모든 필드를 입력해주세요.', 'error');
            return;
        }

        if (newPassword !== confirmPassword) {
            Swal.fire('오류', '비밀번호가 일치하지 않습니다.', 'error');
            return;
        }

        $.ajax({
            url: '/api/auth/resetPassword',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ password: newPassword }),
            success: function () {
                Swal.fire('완료', '비밀번호가 성공적으로 변경되었습니다.', 'success')
                    .then(() => location.href = '/member/loginPage');
            },
            error: function () {
                Swal.fire('오류', '비밀번호 변경에 실패했습니다.', 'error');
            }
        });
    });


});


// 🔄 단계 전환
function switchToStep(step) {
    $('.recovery-form, .verify-code-form, .reset-password-form')
        .removeClass('open').addClass('closed');

    if (step === 1) $('.recovery-form').removeClass('closed').addClass('open');
    else if (step === 2) $('.verify-code-form').removeClass('closed').addClass('open');
    else if (step === 3) $('.reset-password-form').removeClass('closed').addClass('open');
}
