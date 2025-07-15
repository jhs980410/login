document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("send-code-form");

    form.addEventListener("submit", function(e) {
        e.preventDefault(); // 기본 동작 방지

        Swal.fire({
            title: '전송 중...',
            text: '이메일을 보내는 중입니다. 잠시만 기다려주세요.',
            allowOutsideClick: false,
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });

        // UX를 위한 짧은 지연 후 전송
        setTimeout(() => {
            form.submit();
        }, 1000);
    });
});