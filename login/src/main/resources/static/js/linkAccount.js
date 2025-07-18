document.addEventListener("DOMContentLoaded", () => {
    // 코드 전송 form 처리
    const sendCodeForm = document.getElementById("send-code-form");

    sendCodeForm.addEventListener("submit", function (e) {
        e.preventDefault();

        Swal.fire({
            title: '전송 중...',
            text: '이메일을 보내는 중입니다. 잠시만 기다려주세요.',
            allowOutsideClick: false,
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });

        setTimeout(() => {
            sendCodeForm.submit(); // 실제 전송
        }, 1000);
    });

    // 코드 검증 AJAX 처리
    const verifyForm = document.getElementById("verify-code-form");

    verifyForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const email = document.getElementById("email").value;
        const authCode = document.getElementById("authCode").value;

        try {
            const res = await fetch("/link/verifyCode", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email, authCode })
            });

            const result = await res.json();

            if (result.status === "success") {
                if (result.mode === "login") {
                    Swal.fire("인증 완료", "이제 로그인해주세요.", "success").then(() => {
                        window.location.href = "/member/loginPage";
                    });
                } else if (result.mode === "link") {
                    Swal.fire("인증 완료", "연동을 계속 진행합니다.", "success").then(() => {
                        window.location.href = "/link/confirm";
                    });
                }
            } else {
                Swal.fire("인증 실패", result.message || "인증 코드가 올바르지 않습니다.", "error");
            }
        } catch (error) {
            Swal.fire("오류", "서버와의 통신에 실패했습니다.", "error");
        }
    });
});
