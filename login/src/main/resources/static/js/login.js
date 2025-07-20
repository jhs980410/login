function getDeviceId() {
    try {
        if (!window.localStorage) return null;

        let deviceId = localStorage.getItem("device_id");
        if (!deviceId) {
            // UUID 지원 여부 확인
            if (crypto && crypto.randomUUID) {
                deviceId = crypto.randomUUID();
            } else {
                // fallback: 랜덤 문자열
                deviceId = 'dev-' + Math.random().toString(36).substring(2, 15);
            }
            localStorage.setItem("device_id", deviceId);
        }
        return deviceId;
    } catch (e) {
        console.warn("기기 ID 생성 실패:", e);
        return null;
    }
}

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

// 🔹 로그인 요청 처리 (쿠키 기반 인증용)
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("login-form");
    if (!form) return;

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const email = document.getElementById("user_login").value.trim();
        const password = document.getElementById("user_password").value.trim();
        const autoLogin = document.getElementById("remember_me").checked;
        const deviceId = getDeviceId();
        const deviceidTest = "force-new-device-001";
        fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include", //  쿠키 전송 필수
            body: JSON.stringify({ email, password, autoLogin, deviceId })
        })
            .then(async res => {
                const data = await res.json().catch(() => ({}));
                if (res.status === 200) {
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
                }  else if (res.status === 403) {
                    showError(data.message || "새 기기에서의 로그인입니다. 본인 인증이 필요합니다.");

                    Swal.fire({
                        icon: 'info',
                        title: '새 기기 로그인 감지',
                        text: '본인 인증을 위해 페이지로 이동합니다.',
                        timer: 2500,
                        showConfirmButton: false,
                        willClose: () => {
                            const email = encodeURIComponent(data.email);  //  data.email 사용
                            location.href = `/link/account?email=${email}&type=LOCAL&mode=login`;
                        }
                    });
                }else {
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
