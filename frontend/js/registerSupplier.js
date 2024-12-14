document.getElementById("register-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    // Clear previous error messages
    clearErrors();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;

    if (password !== confirmPassword) {
        showError("confirm-password-error", "Passwords do not match");
        return;
    }

    const data = {
        username: username,
        password: password,
        role: "SUPPLIER"
    };

    try {
        const response = await fetch("http://localhost:8080/api/v1/user", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (response.ok) {
            if (result.jwtToken) {
                localStorage.setItem('userToken', result.jwtToken);

                // Clear the form
                document.getElementById("register-form").reset();
                clearErrors();

                // Show success message and redirect
                showSuccess("Registration successful! Redirecting to login page...");
                setTimeout(() => {
                    window.location.href = '../login/loginSupplier.html';
                }, 2000);
            }
        } else {
            // Handle validation errors
            if (typeof result === 'object') {
                Object.entries(result).forEach(([field, messages]) => {
                    if (Array.isArray(messages)) {
                        showError(`${field}-error`, messages[0]);
                        document.getElementById(field).classList.add('error');
                    }
                });
            } else {
                showError("username-error", "An error occurred during registration");
            }
        }
    } catch (error) {
        console.error('Registration error:', error);
        showError("username-error", "An error occurred. Please check if the server is running.");
    }
});

function clearErrors() {
    const errorElements = document.getElementsByClassName("error-message");
    Array.from(errorElements).forEach(element => {
        element.textContent = "";
    });

    const inputElements = document.getElementsByTagName("input");
    Array.from(inputElements).forEach(element => {
        element.classList.remove('error');
    });
}

function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
    }
}

function showSuccess(message) {
    // Create a success message element
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.style.cssText = `
        position: fixed;
        top: 20px;
        left: 50%;
        transform: translateX(-50%);
        background-color: #4CAF50;
        color: white;
        padding: 15px 30px;
        border-radius: 5px;
        box-shadow: 0 2px 5px rgba(0,0,0,0.2);
    `;
    successDiv.textContent = message;

    document.body.appendChild(successDiv);

    // Remove the success message after the redirect
    setTimeout(() => {
        successDiv.remove();
    }, 2000);
}
