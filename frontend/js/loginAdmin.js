// Admin Login Form Handler
document.getElementById("login-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    // Clear previous error messages
    clearErrors();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const data = {
        username: username,
        password: password,
        role: "ADMIN"  // Changed to ADMIN role
    };

    try {
        const response = await fetch("http://localhost:8080/api/v1/user/admin/login", {
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
                // Store the admin token
                localStorage.setItem('adminToken', result.jwtToken);

                // Clear the form and errors
                document.getElementById("login-form").reset();
                clearErrors();

                // Show success message
                showSuccess("Admin login successful!");
                
                // Show the shadow modal after successful login
                setTimeout(() => {
                    showShadowModal();
                }, 1000);
            } else {
                // Handle validation errors returned from the server
                handleValidationErrors(result);
            }
        } else {
            // Handle unexpected server errors
            handleUnexpectedError(result);
        }
    } catch (error) {
        console.error('Login error:', error);
        alert("An unexpected error occurred: " + error.message);
        document.getElementById('login-form').reset();
        clearErrors();
    }
});

// Shadow User Form Handler
document.getElementById("shadow-form").addEventListener("submit", async function (event) {
    event.preventDefault();
    
    // Get the username to shadow
    const shadowUsername = document.getElementById("shadow-username").value;
    
    // Get the admin token from localStorage
    const adminToken = localStorage.getItem('adminToken');
    
    if (!adminToken) {
        alert("Admin authentication token is missing. Please login again.");
        hideShadowModal();
        return;
    }
    
    try {
        const response = await fetch("http://localhost:8080/api/v1/user/admin/shadow", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json",
                "Authorization": `Bearer ${adminToken}`
            },
            body: JSON.stringify({ username: shadowUsername })
        });
        
        const result = await response.json();
        
        if (response.ok && result.valid) {
            // Store the new shadow token (replacing the admin token)
            localStorage.setItem('userToken', result.token);
            
            // Show success message
            showSuccess(`Shadowing ${shadowUsername} as ${result.role}. Redirecting...`);
            
            // Redirect based on user role
            setTimeout(() => {
                if (result.role === "CUSTOMER") {
                    window.location.href = '../customer_page/customerDashboard.html';
                } else if (result.role === "SUPPLIER") {
                    window.location.href = '../supplier_page/supplier_page.html';
                } else {
                    alert("Unknown role detected. Please contact system administrator.");
                }
            }, 2000);
        } else {
            // Handle error
            document.getElementById("shadow-username-error").textContent = 
                result.error || "User not found or cannot be shadowed";
        }
    } catch (error) {
        console.error('Shadow error:', error);
        alert("An unexpected error occurred while attempting to shadow: " + error.message);
    }
});

// Function to show the shadow modal
function showShadowModal() {
    document.getElementById("shadow-overlay").style.display = "block";
    document.getElementById("shadow-modal").style.display = "block";
}

// Function to hide the shadow modal
function hideShadowModal() {
    document.getElementById("shadow-overlay").style.display = "none";
    document.getElementById("shadow-modal").style.display = "none";
}

// Close modal when clicking outside
document.getElementById("shadow-overlay").addEventListener("click", function() {
    hideShadowModal();
});

// Function to handle unexpected errors
function handleUnexpectedError(result) {
    const errorMessages = [];
    if (typeof result === 'object') {
        if (result.error) {
            errorMessages.push(result.error);
        } else {
            Object.entries(result).forEach(([field, messages]) => {
                if (Array.isArray(messages)) {
                    errorMessages.push(messages[0]);
                }
            });
        }
    } else {
        errorMessages.push("An unexpected error occurred during login.");
    }
    
    if (errorMessages.length > 0) {
        document.getElementById("login-error").textContent = errorMessages.join('. ');
    } else {
        document.getElementById("login-error").textContent = "Login failed. Please check your credentials.";
    }
}

function handleValidationErrors(result) {
    const errorMessages = [];

    if (typeof result === 'object') {
        // Check for an error field
        if (result.error) {
            document.getElementById("login-error").textContent = result.error;
            return;
        }

        Object.entries(result).forEach(([field, messages]) => {
            if (Array.isArray(messages)) {
                showError(`${field}-error`, messages[0]);
                document.getElementById(field).classList.add('error');
            }
        });
    } else {
        showError("login-error", "An error occurred during login.");
    }
}

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

