
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
function login() {
    const email = document.getElementById("user_login").value.trim();
    const password = document.getElementById("user_password").value.trim();

    if (!email || !password) {
        alert("이메일과 비밀번호를 입력해주세요.");
        return;
    }

    fetch("/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, password })
    })
        .then(async res => {
            if (!res.ok) {
                const error = await res.json().catch(() => ({}));
                alert(error.message || "로그인에 실패했습니다.");
                return;
            }

            return res.json();
        })
        .then(data => {
            if (data && data.token) {
                localStorage.setItem("jwt", data.token);
                alert("로그인 성공!");
                window.location.href = "/home";  // 로그인 성공 후 이동할 경로
            }
        })
        .catch(err => {
            console.error("Login Error:", err);
            alert("서버 오류가 발생했습니다.");
        });
}
