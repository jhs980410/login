document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("convert-form");
    form.addEventListener("submit", function (e) {
        e.preventDefault(); // 기본 제출 막기

        Swal.fire({
            title: "전환 성공",
            text: "계정을 소셜 로그인으로 전환되었습니다",
            allowOutsideClick: false,
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });

        // 실제 form 전송
        setTimeout(() => {
            window.location.href = form.getAttribute("action") + "?type=" +
                encodeURIComponent(document.querySelector("input[name='type']").value);
        }, 1200);
    });
});