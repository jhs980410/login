document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.getElementById("logout-btn");

    logoutBtn.addEventListener("click", async () => {
        const refreshToken = localStorage.getItem("refreshToken");
        const isSocial = localStorage.getItem("isSocial") === "true";

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

        // 로컬 토큰 삭제
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("isSocial");

        // 홈 또는 로그인 페이지로 이동
        window.location.href = "/member/loginPage";
    });
});
