$(function () {
    let emailCheckPassed = false;
    let nameCheckPassed = false;

    //  이메일 중복 확인 버튼 클릭
    $('#check_email_btn').on('click', function () {
        const email = $('#user_email').val().trim();

        if (!email) {
            $('#email_feedback').text("이메일을 입력해주세요.").css("color", "red");
            emailCheckPassed = false;
            return;
        }

        $.ajax({
            url: '/api/members/check-email',
            method: 'GET',
            data: { email },
            success: function (res) {
                $('#email_feedback')
                    .text(res.message)
                    .css("color", res.status === 'ok' ? "green" : "red");
                emailCheckPassed = res.status === 'ok';
            },
            error: function () {
                $('#email_feedback').text("오류가 발생했습니다. 다시 시도해주세요.").css("color", "red");
                emailCheckPassed = false;
            }
        });
    });

    //  닉네임 중복 확인 버튼 클릭
    $('#check_name_btn').on('click', function () {
        const userName = $('#user_name').val().trim();

        if (!userName) {
            $('#name_feedback').text("닉네임을 입력해주세요.").css("color", "red");
            nameCheckPassed = false;
            return;
        }

        $.ajax({
            url: '/api/members/check-nickname',
            method: 'GET',
            data: { nickname: userName },
            success: function (res) {
                $('#name_feedback')
                    .text(res.message)
                    .css("color", res.status === 'ok' ? "green" : "red");
                nameCheckPassed = res.status === 'ok';
            },
            error: function () {
                $('#name_feedback').text("오류가 발생했습니다. 다시 시도해주세요.").css("color", "red");
                nameCheckPassed = false;
            }
        });
    });

    //  비밀번호 실시간 유효성/일치 검사
    $('#user_pass, #user_pass_confirm').on('input', function () {
        const pw = $('#user_pass').val();
        const confirmPw = $('#user_pass_confirm').val();

        // 유효성 검사
        if (isValidPassword(pw)) {
            $('#pw_feedback').text('').hide();
        } else {
            $('#pw_feedback')
                .css('color', 'red')
                .text('8~20자의 영문, 숫자, 특수문자를 모두 포함한 비밀번호를 입력해주세요.')
                .show();
        }

        // 일치 여부 검사
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

    //  폼 제출 시 최종 유효성 검사
    $('.signup-form').on('submit', function (e) {
        const email = $('#user_email').val().trim();
        const pw = $('#user_pass').val();
        const confirmPw = $('#user_pass_confirm').val();
        const isChecked = $('#agree_terms').is(':checked');

        if (!emailCheckPassed) {
            e.preventDefault();
            alert("이메일 중복 확인을 완료해주세요.");
            return;
        }

        if (!nameCheckPassed) {
            e.preventDefault();
            alert("닉네임 중복 확인을 완료해주세요.");
            return;
        }

        if (!validateEmail($('#user_email'))) {
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
            alert("비밀번호 확인을 완료해주세요.");
            return;
        }
        if (!isChecked) {
            e.preventDefault();
            alert("이용약관에 동의해주세요");
            return;
        }

        // 기타 약관 체크 등 추가 가능
    });

    //  이메일 유효성 검사 함수
    function validateEmail($input) {
        const email = $input.val().trim();
        const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!pattern.test(email)) {
            $('#email_feedback').text("유효한 이메일 형식을 입력해주세요.").css("color", "red");
            return false;
        }
        return true;
    }

    //  비밀번호 유효성 검사 함수
    function isValidPassword(pw) {
        const pattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/;
        return pattern.test(pw);
    }
});
