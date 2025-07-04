$(function () {
    // 이메일 유효성 및 중복 검사
    $('#user_email').on('blur', function () {
        const $emailInput = $(this);
        if (!validateEmail($emailInput)) return;

        $.ajax({
            url: '/api/member/check-email',
            type: 'GET',
            data: { email: $emailInput.val().trim() },
            success: function (res) {
                if (res.exists) {
                    $('#email-check-feedback').text('이미 사용 중인 이메일입니다.').css('color', 'red');
                } else {
                    $('#email-check-feedback').text('사용 가능한 이메일입니다.').css('color', 'green');
                }
            },
            error: function () {
                $('#email-check-feedback').text('이메일 확인 중 오류 발생').css('color', 'red');
            }
        });
    });

    // 폼 제출 시 유효성 검사
    $('.signup-form').on('submit', function (e) {
        const $emailInput = $('#user_email');
        const pw = $('#user_pass').val();
        const confirmPw = $('#user_pass_confirm').val();

        if (!validateEmail($emailInput)) {
            e.preventDefault();
            return;
        }

        if (!isValidPassword(pw)) {
            e.preventDefault();
            $("#pw_feedback").text("8~20자의 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
                .css("color", "red").show();
            return;
        }

        if (pw !== confirmPw) {
            e.preventDefault();
            $("#pw_match_feedback").text("비밀번호가 일치하지 않습니다.").css("color", "red");
            return;
        }
    });

    // 비밀번호 실시간 확인
    $('#user_pass, #user_pass_confirm').on('input', function () {
        const pw = $('#user_pass').val();
        const confirmPw = $('#user_pass_confirm').val();

        if (isValidPassword(pw)) {
            $('#pw_feedback').text('').hide();
        } else {
            $('#pw_feedback')
                .css('color', 'red')
                .text('8~20자의 영문, 숫자, 특수문자를 모두 포함한 비밀번호를 입력해주세요.')
                .show();
        }

        if (pw && confirmPw) {
            if (pw === confirmPw) {
                $('#pw_match_feedback').css('color', 'green').text('비밀번호가 일치합니다.');
            } else {
                $('#pw_match_feedback').css('color', 'red').text('비밀번호가 일치하지 않습니다.');
            }
        } else {
            $('#pw_match_feedback').text('');
        }
    });
});
//이메일중복확인
// 이메일 중복 확인 AJAX 요청
$('#check_email_btn').on('click', function () {
    const email = $('#user_email').val().trim();

    if (!email) {
        $('#email_feedback').text("이메일을 입력해주세요.").css("color", "red");
        return;
    }

    $.ajax({
        url: '/api/members/check-email',   //  RESTful GET API
        method: 'GET',
        data: { email: email },
        success: function (res) {
            if (res.exists) {
                $('#email_feedback').text("이미 사용 중인 이메일입니다.").css("color", "red");
            } else {
                $('#email_feedback').text("사용 가능한 이메일입니다.").css("color", "green");
            }
        },
        error: function () {
            $('#email_feedback').text("오류가 발생했습니다. 다시 시도해주세요.").css("color", "red");
        }
    });
});


$('#check_name_btn').on('click', function () {
    const userName = $('#user_name').val().trim();

    if (!userName) {
        $('#name_feedback').text("닉네임을 입력해주세요.").css("color", "red");
        return;
    }

    $.ajax({
        url: '/api/members/check-nickname',   //  RESTful GET API
        method: 'GET',
        data: { nickname: userName },
        success: function (res) {
            if (res.exists) {
                $('#name_feedback').text("이미 사용 중인 닉네임입니다.").css("color", "red");
            } else {
                $('#name_feedback').text("사용 가능한 닉네임입니다.").css("color", "green");
            }
        },
        error: function () {
            $('#name_feedback').text("오류가 발생했습니다. 다시 시도해주세요.").css("color", "red");
        }
    });
});

