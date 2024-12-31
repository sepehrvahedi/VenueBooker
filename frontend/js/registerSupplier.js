document.getElementById("register-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    // Clear previous error messages
    clearErrors();

    const name = document.getElementById("name").value;
    const surname = document.getElementById("surname").value;
    const username = document.getElementById("username").value;
    const phone = document.getElementById("phone").value;
    const nationalNumber = document.getElementById("national-number").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;

    // Client-side validation
    if (!/^(09\d{9})$/.test(phone)) {
        showError("phone-error", "Phone number must start with 09 and be 11 digits long.");
        event.target.value = ''; // Remove non-digit characters
        return;
    }
    // Validate national number
    if (!/^\d{10}$/.test(nationalNumber)) {
        showError("national-number-error", "National number must be 10 digits long and contain only numbers.");
        return;
    }

    // Validate password confirmation
    if (password !== confirmPassword) {
        showError("confirm-password-error", "Passwords do not match");
        return;
    }

    // Prepare data for registration
    const data = {
        name: name,
        surname: surname,
        username: username,
        phone: phone,
        nationalNumber: nationalNumber,
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
            } else {
                // Handle validation errors returned from the server
                handleValidationErrors(result);
            }
        } else {
            // Handle unexpected server errors
            handleUnexpectedError(result);
        }
    } catch (error) {
        console.error('Registration error:', error);
        // Redirect to error page for unexpected exceptions
        alert("An unexpected error occurred: " + error.message);
        document.getElementById("register-form").reset();
        clearErrors();
    }
});

// Function to handle validation errors
function handleValidationErrors(result) {
    if (typeof result === 'object') {

        Object.entries(result).forEach(([field, messages]) => {
            if (Array.isArray(messages)) {
                showError(`${field}-error`, messages[0]);
                document.getElementById(field).classList.add('error');
            }
        });
    } else {
        showError("register-error", "An error occurred during registration");
    }
}

// Function to handle unexpected errors
function handleUnexpectedError(result) {
    const errorMessages = [];
    if (typeof result === 'object') {
        Object.entries(result).forEach(([field, messages]) => {
            if (Array.isArray(messages)) {
                errorMessages.push(messages[0]);
            }
        });
    } else {
        errorMessages.push("An unexpected error occurred during registration.");
    }
    
    alert(errorMessages.join('\n'))
}


// Event listeners for phone and national number inputs
document.getElementById("phone").addEventListener("input", function (event) {
    const value = event.target.value;
    // Validate phone number
    if (!/^\d*$/.test(value)) {
        showError("phone-error", "Only digits are allowed.");
        event.target.value = value.replace(/\D/g, ''); // Remove non-digit characters
    } else if (!value.startsWith("09")) {
        showError("phone-error", "Phone number must start with 09.");
    } else if(value.length < 11){
        showError('phone-error', 'Phone number must be 11 digits long.')
    } else if (value.length > 11) {
        event.target.value = value.slice(0, 11); // Limit to 11 digits
    } else {
        clearErrors();
    }
});

document.getElementById("national-number").addEventListener("input", function (event) {
    const value = event.target.value;
    if (!/^\d*$/.test(value)) {
        showError("national-number-error", "Only digits are allowed.");
        event.target.value = value.replace(/\D/g, ''); // Remove non-digit characters
    } else if (value.length < 10) {
        showError("national-number-error", "National number must be 10 digits long.");
    } else if (value.length > 10) {
        event.target.value = value.slice(0, 10); // Limit to 10 digits
    } else {
        clearErrors();
    }
});

document.getElementById("confirm-password").addEventListener("input", function () {
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;
    if (confirmPassword !== password) {
        showError("confirm-password-error", "Passwords do not match");
    } else {
        clearErrors();
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
        // top: 20px;
        // right: 20px;
        background-color: #4CAF50;
        color: white;
        // padding: 15px;
        // z-index: 1000;
    `;
    successDiv.textContent = message;
    document.body.appendChild(successDiv);
    setTimeout(() => {
        document.body.removeChild(successDiv);
    }, 3000);
}