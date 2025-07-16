document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.getElementById("logout-btn");

    logoutBtn.addEventListener("click", async () => {
        const isSocial = localStorage.getItem("isSocial") === "true";

        try {
            // 쿠키 기반 로그아웃 요청 (refreshToken은 쿠키에 있으므로 body 불필요)
            await fetch("/api/auth/logout", {
                method: "POST"
            });
        } catch (error) {
            console.error("로그아웃 중 오류 발생:", error);
        }

        // 로컬스토리지 토큰 정리 (혹시 과거에 저장된 게 있다면)
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("isSocial");

        // 로그인 페이지로 이동
        window.location.href = "/member/loginPage";
    });
});
