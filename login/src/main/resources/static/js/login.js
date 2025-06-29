
// 로그인/회원가입 탭 전환 기능S
$(function() {
    tab = $('.tabs h3 a');  // 탭 버튼 (로그인, 회원가입)
    tab.on('click', function(event) {
        event.preventDefault();  // 기본 링크 이동 막기
        tab.removeClass('active');  // 모든 탭에서 active 제거
        $(this).addClass('active'); // 클릭한 탭에 active 부여
        tab_content = $(this).attr('href'); // 이동할 콘텐츠 ID 얻기
        $('div[id$="tab-content"]').removeClass('active'); // 모든 콘텐츠 비활성화
        $(tab_content).addClass('active'); // 선택한 콘텐츠 활성화
    });
});


// 슬라이드쇼 기능
$(function() {
    $('#slideshow > div:gt(0)').hide();  // 첫 번째 div 제외 숨기기
    setInterval(function() {
        $('#slideshow > div:first')
            .fadeOut(1000)
            .next()
            .fadeIn(1000)
            .end()
            .appendTo('#slideshow');  // 다음 div를 앞으로 보내기 (무한 슬라이드)
    }, 3850);
});


// 3. 커스텀 클래스 전환 함수 정의
(function($) {
    'use strict';
    $.fn.swapClass = function(remove, add) {
        this.removeClass(remove).addClass(add);
        return this;
    };
}(jQuery));

// 4. 약관/비밀번호 찾기/폼 열고 닫는 토글 기능
$(function() {
    $('.agree, .forgot, #toggle-terms, .log-in, .sign-up').on('click', function(event) {
        event.preventDefault();
        var user = $('.user'), terms = $('.terms'), form = $('.form-wrap'), recovery = $('.recovery'), close = $('#toggle-terms');

        // 약관 또는 로그인일 때 처리
        if ($(this).hasClass('agree') || $(this).hasClass('log-in') || ($(this).is('#toggle-terms') && terms.hasClass('open'))) {
            if (terms.hasClass('open')) {
                form.swapClass('open', 'closed');
                terms.swapClass('open', 'closed');
                close.swapClass('open', 'closed');
            } else {
                if ($(this).hasClass('log-in')) return;
                form.swapClass('closed', 'open');
                terms.swapClass('closed', 'open').scrollTop(0);
                close.swapClass('closed', 'open');
                user.addClass('overflow-hidden');
            }
        }

        // 비밀번호 찾기 또는 회원가입일 때 처리
        else if ($(this).hasClass('forgot') || $(this).hasClass('sign-up') || $(this).is('#toggle-terms')) {
            if (recovery.hasClass('open')) {
                form.swapClass('open', 'closed');
                recovery.swapClass('open', 'closed');
                close.swapClass('open', 'closed');
            } else {
                if ($(this).hasClass('sign-up')) return;
                form.swapClass('closed', 'open');
                recovery.swapClass('closed', 'open');
                close.swapClass('closed', 'open');
                user.addClass('overflow-hidden');
            }
        }
    });
});


// 5. 비밀번호 찾기 메시지 표시 기능
$(function() {
    $('.recovery .button').on('click', function(event) {
        event.preventDefault();
        $('.recovery .mssg').addClass('animate'); // 메시지 나타남
        setTimeout(function() {
            // 폼 닫고 메시지 리셋
            $('.form-wrap').swapClass('open', 'closed');
            $('.recovery').swapClass('open', 'closed');
            $('#toggle-terms').swapClass('open', 'closed');
            $('.tabs-content .fa').swapClass('active', 'inactive');
            $('.recovery .mssg').removeClass('animate');
        }, 2500); // 2.5초 후 닫힘
    });
});

//  데모용: 폼 제출 막기 (기능 미구현 상태)
// $(function() {
//     $('.button').on('click', function(event) {
//         $(this).stop();         // 애니메이션 중지 (불필요한 동작 방지)
//         event.preventDefault(); // 폼 제출 막기
//         return false;
//     });
// });

//이메일 유효성검사 / /
function validateEmail($input) {
    const email = $input.val().trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email) {
        $input[0].setCustomValidity("이메일 주소를 입력해주세요.");
        $input[0].reportValidity();
        return false;
    }

    if (!emailRegex.test(email)) {
        $input[0].setCustomValidity("올바른 이메일 형식이 아닙니다. 예: example@domain.com");
        $input[0].reportValidity();
        console.log($input[0].reportValidity());
        console.log("올바른 이메일 형식이 아닙니다. 예: example@domain.com");
        return false;
    }

    $input[0].setCustomValidity("");
    return true;
}


$('.signup-form').on('submit', function (e) {
    const $emailInput = $('#user_email');

    if (!validateEmail($emailInput)) {
        e.preventDefault(); // 유효하지 않으면 전송 막기
        return;
    }

    // 이어서 닉네임, 비밀번호 등 다른 필드도 추가 검사 가능
});

function checkPasswordMatch() {
    const pw = $("#user_pass").val();
    const confirmPw = $("#user_pass_confirm").val();

    const lengthValid = pw.length >= 8;  //길이
    const letterValid = /[a-zA-Z]/.test(pw); // 영문
    const numberValid = /[0-9]/.test(pw);  // 숫자  text = true반환
    const specialValid = /[!@#$%^&*(),.?":{}|<>]/.test(pw); //특수문자

    //
    if (lengthValid && letterValid && numberValid && specialValid) {
        $("#pw_feedback").text("").hide(); // 조건 만족 시 메시지 숨김
    } else {
        $("#pw_feedback")
            .css("color", "red")
            .text("8~20자의 영문, 숫자, 특수문자를 모두 포함한 비밀번호를 입력해주세요.")
            .show();
    }


    // 비밀번호 일치 여부 확인
    if (pw && confirmPw) {
        if (pw === confirmPw) {
            $("#pw_match_feedback").css("color", "green").text("비밀번호가 일치합니다.");
        } else {
            $("#pw_match_feedback").css("color", "red").text("비밀번호가 일치하지 않습니다.");
        }
    } else {
        $("#pw_match_feedback").text(""); // 아무 것도 입력 안 됐을 때는 초기화
    }
}
$(document).ready(function () {
    $("#user_pass, #user_pass_confirm").on("input", checkPasswordMatch);
});


