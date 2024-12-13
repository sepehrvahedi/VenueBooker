document.getElementById("register-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;
    const email = document.getElementById('email').value;

    if(password != confirmPassword) {
        console.log(password, confirmPassword)
        alert('Passwords do not match. Please try again.'); // Notify the user
        return; // Stop the registration process
    }

    const data = {
        username: username,
        email: email,
        password: password,
    };

    try {
        const response = await fetch("https://example.com/api/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data)
        });

        // Check if the response is ok (status in the range 200-299)
        if (response.ok) {
            const result = await response.json();
            localStorage.setItem('registerCustomerToken', result.token); // Save JWT token in local storage
            alert('Registration successful!'); // Notify the user
            window.location.href = "../login/loginCustomer.html"; // Redirect to login page
        } else {
            const error = await response.json();
            alert(`Registration failed: ${error.message}`); // Notify the user of the error
        }
    } catch (error) {
        alert(error.message);
    }
});
