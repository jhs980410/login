// 🔹 로그인/회원가입 탭 전환
$(function () {
    const tab = $('.tabs h3 a');
    tab.on('click', function (event) {
        event.preventDefault();
        tab.removeClass('active');
        $(this).addClass('active');

        const target = $(this).attr('href');
        $('div[id$="tab-content"]').removeClass('active');
        $(target).addClass('active');
    });
});

// 🔹 슬라이드쇼
$(function () {
    $('#slideshow > div:gt(0)').hide();
    setInterval(function () {
        $('#slideshow > div:first')
            .fadeOut(1000)
            .next()
            .fadeIn(1000)
            .end()
            .appendTo('#slideshow');
    }, 3850);
});

// 🔹 클래스 전환 유틸 함수
(function ($) {
    $.fn.swapClass = function (remove, add) {
        return this.removeClass(remove).addClass(add);
    };
})(jQuery);

// 🔹 약관/비밀번호 찾기 토글 열고 닫기
$(function () {
    $('.agree, .forgot, #toggle-terms, .log-in, .sign-up').on('click', function (event) {
        event.preventDefault();
        const form = $('.form-wrap');
        const terms = $('.terms');
        const recovery = $('.recovery');
        const cross = $('#toggle-terms');

        const isTermsOpen = terms.hasClass('open');
        const isRecoveryOpen = recovery.hasClass('open');

        const isTermsTrigger = $(this).hasClass('agree');
        const isRecoveryTrigger = $(this).hasClass('forgot');
        const isCloseTrigger = $(this).is('#toggle-terms') && (isTermsOpen || isRecoveryOpen);

        // 열기: 약관 or 비번찾기
        if ((isTermsTrigger && !isTermsOpen) || (isRecoveryTrigger && !isRecoveryOpen)) {
            form.swapClass('closed', 'open');
            if (isTermsTrigger) {
                terms.swapClass('closed', 'open').scrollTop(0);
            } else {
                recovery.swapClass('closed', 'open');
            }
            cross.swapClass('closed', 'open');
        }

        // 닫기: X 버튼 클릭 or 이미 열려있는 상태
        else if (isCloseTrigger) {
            form.swapClass('open', 'closed');
            terms.removeClass('open').addClass('closed');
            recovery.removeClass('open').addClass('closed');
            cross.swapClass('open', 'closed');
        }
    });
});


// 🔹 로그인 요청 처리
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("login-form");
    if (!form) return;

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const email = document.getElementById("user_login").value.trim();
        const password = document.getElementById("user_password").value.trim();
        const autoLogin = document.getElementById("remember_me").checked;

        fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password, autoLogin })
        })
            .then(async res => {
                if (res.status === 200) {
                    const data = await res.json();
                    if (data.accessToken && data.refreshToken) {
                        localStorage.setItem("accessToken", data.accessToken);
                        localStorage.setItem("refreshToken", data.refreshToken);
                        localStorage.setItem("isSocial", "false");

                        Swal.fire({
                            title: "로그인 성공 🎉",
                            text: "잠시 후 홈으로 이동합니다.",
                            icon: "success",
                            timer: 2000,
                            showConfirmButton: false,
                            didOpen: () => Swal.showLoading()
                        }).then(() => {
                            window.location.href = "/home";
                        });
                    } else {
                        showError("로그인에 실패했습니다.");
                    }
                } else {
                    const data = await res.json().catch(() => ({}));
                    showError(data.message || "로그인에 실패했습니다.");
                }
            })
            .catch(err => {
                console.error("Login Error:", err);
                showError("서버 오류가 발생했습니다.");
            });

        function showError(msg) {
            const errorMsg = document.getElementById("error-message");
            if (errorMsg) {
                errorMsg.textContent = msg;
                errorMsg.style.display = "block";
            }
        }
    });
});


// 🔹 구글 로그인 버튼
document.addEventListener("DOMContentLoaded", () => {
    const googleBtn = document.querySelector(".custom-google-btn");
    if (googleBtn) {
        googleBtn.addEventListener("click", () => {
            showOAuthLoading();
            setTimeout(() => {
                window.location.href = "/oauth2/authorization/google";
            }, 800);
        });
    }
});

function showOAuthLoading() {
    Swal.fire({
        title: "구글 로그인 중...",
        text: "잠시만 기다려주세요.",
        icon: "info",
        showConfirmButton: false,
        didOpen: () => Swal.showLoading()
    });
}
