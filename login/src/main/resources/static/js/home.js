document.addEventListener("DOMContentLoaded", () => {
    const logoutBtn = document.getElementById("logout-btn");
    logoutBtn.addEventListener("click", () => {
        // ✅ JWT 삭제
        localStorage.removeItem("jwt");

        // ✅ 로그인 페이지로 이동
        window.location.href = "/member/loginPage";
    });
});