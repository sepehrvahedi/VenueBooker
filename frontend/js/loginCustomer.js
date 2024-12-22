document.getElementById("login-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    // Clear previous error messages
    clearErrors();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const data = {
        username: username,
        password: password,
        role: "CUSTOMER"
    };

    try {
        const response = await fetch("http://localhost:8080/api/v1/user/login", {
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

                // Clear the form and errors
                document.getElementById("login-form").reset();
                clearErrors();

                // Show success message and redirect
                showSuccess("Login successful! Redirecting to dashboard...");
                setTimeout(() => {
                    window.location.href = '../dashboard/customerDashboard.html';
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
                showError("username-error", result.message || "Invalid credentials");
            }
        }
    } catch (error) {
        console.error('Login error:', error);
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
        const inputField = document.getElementById(elementId.replace('-error', ''));
        if (inputField) {
            inputField.classList.add('error');
        }
    }
}

function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.textContent = message;

    document.body.appendChild(successDiv);

    setTimeout(() => {
        successDiv.remove();
    }, 2000);
}