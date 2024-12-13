document.getElementById("login-form").addEventListener("submit", async function (event) {
    event.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    try {
        const response = await fetch("https://example.com/api/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ username, password }),
        });

        if (!response.ok) {
            throw new Error("Login failed. Please try again.");
        }

        const data = await response.json();
        alert("Login successful! Redirecting...");
        localStorage.setItem("customerTokenLogin", data.token);
        window.location.href = "/dashboard"; // Replace with your dashboard URL
    } catch (error) {
        alert(error.message);
    }
});
