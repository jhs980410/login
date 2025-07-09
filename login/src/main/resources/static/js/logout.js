document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.getElementById("logout-btn");
    logoutBtn.addEventListener("click", async () => {
        const refreshToken = localStorage.getItem("refreshToken"); // 자동 로그인용 토큰

        try {
            if (refreshToken) {
                await fetch("/api/auth/logout", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ refreshToken })
                });
            }
        } catch (error) {
            console.error("로그아웃 중 오류 발생:", error);
        }

        // 클라이언트 저장 토큰 제거
        localStorage.removeItem("jwt");
        localStorage.removeItem("refreshToken");

        // 로그인 페이지로 이동
        window.location.href = "/member/loginPage";
    });
});
