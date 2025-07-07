// 이메일 유효성 검사
function validateEmail($input) {
    const email = $input.val().trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email) {
        $input[0].setCustomValidity("이메일 주소를 입력해주세요.");
        $input[0].reportValidity();
        return false;
    }

    if (!emailRegex.test(email)) {
        $input[0].setCustomValidity("올바른 이메일 형식이 아닙니다. 예: example@domain.com");
        $input[0].reportValidity();
        return false;
    }

    $input[0].setCustomValidity("");
    return true;
}

// 비밀번호 유효성 검사
// function isValidPassword(pw) {
//     const lengthValid = pw.length >= 8;
//     const letterValid = /[a-zA-Z]/.test(pw);
//     const numberValid = /[0-9]/.test(pw);
//     const specialValid = /[!@#$%^&*(),.?":{}|<>]/.test(pw);
//     return lengthValid && letterValid && numberValid && specialValid;
// }
