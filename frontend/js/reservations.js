const token = localStorage.getItem("userToken");
const reservationsContainer = document.getElementById("reservationsContainer");
const doneButton = document.getElementById("doneButton");
const resetSearch = document.getElementById("resetSearch");
let sorting = "";
const base_url = 'http://localhost:8080/api/v1/customer/bundles';

window.addEventListener("DOMContentLoaded", fetchReservations);

// Close modal logic (none needed here, but kept for consistency)
function closeModal() {
    // No modal in this page, placeholder
}

function renderReservations(reservations) {
    reservationsContainer.innerHTML = "";

    reservations.forEach((reservation) => {
        const card = document.createElement("div");
        card.className = "reservation-card";

        const detailsDiv = document.createElement("div");
        detailsDiv.className = "reservation-details";

        const idSection = document.createElement("div");
        idSection.className = "info-section id-section";
        const idIcon = document.createElement("div");
        idIcon.className = "info-icon";
        const idText = document.createElement("div");
        idText.className = "info-text";
        idText.textContent = `ID: ${reservation.reservationId}`;
        idSection.appendChild(idIcon);
        idSection.appendChild(idText);
        detailsDiv.appendChild(idSection);

        const nameSection = document.createElement("div");
        nameSection.className = "info-section name-section";
        const nameIcon = document.createElement("div");
        nameIcon.className = "info-icon";
        const nameText = document.createElement("div");
        nameText.className = "info-text";
        nameText.textContent = `Bundle: ${reservation.bundleName}`;
        nameSection.appendChild(nameIcon);
        nameSection.appendChild(nameText);
        detailsDiv.appendChild(nameSection);

        const dateSection = document.createElement("div");
        dateSection.className = "info-section date-section";
        const dateIcon = document.createElement("div");
        dateIcon.className = "info-icon";
        const dateText = document.createElement("div");
        dateText.className = "info-text";
        dateText.textContent = `Date: ${reservation.reservationDate}`;
        dateSection.appendChild(dateIcon);
        dateSection.appendChild(dateText);
        detailsDiv.appendChild(dateSection);

        const statusSection = document.createElement("div");
        statusSection.className = "info-section status-section";
        const statusIcon = document.createElement("div");
        statusIcon.className = "info-icon";
        const statusText = document.createElement("div");
        statusText.className = "info-text";
        statusText.textContent = `Status: ${reservation.status}`;
        statusSection.appendChild(statusIcon);
        statusSection.appendChild(statusText);
        detailsDiv.appendChild(statusSection);

        card.appendChild(detailsDiv);
        reservationsContainer.appendChild(card);
    });
}

async function fetchReservations() {
    console.log("Fetching reservations...");
    try {
        const token = localStorage.getItem("userToken");
        if (!token) {
            alert("Unauthorized access. Please log in first.");
            window.location.href = "../login/loginCustomer.html";
            return;
        }
        const tokenPayload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('username').innerHTML = tokenPayload['user']['username'];

        doneButton.textContent = "Loading...";
        doneButton.classList.add("loading");

        const searchReservation = document.getElementById("searchReservation").value;
        const searchDate = document.getElementById("searchDate").value;
        const searchStatus = document.getElementById("searchStatus").value;

        let query = `/reservations?`;
        if (searchReservation) query += `reservationId=${encodeURIComponent(searchReservation)}&`;
        if (searchDate) query += `reservationDate=${encodeURIComponent(searchDate)}&`;
        if (searchStatus) query += `status=${encodeURIComponent(searchStatus)}&`;
        if (sorting) query += `sorting=${sorting}`;

        const response = await fetch(base_url + `${query}`, {
            headers: {
                Authorization: "Bearer " + token,
            },
        });

        if (!response.ok) {
            alert("Failed to fetch reservations");
            doneButton.textContent = "Done";
            doneButton.classList.remove("loading");
            return;
        }

        const data = await response.json();
        const reservations = data['reservations'] || []; // Adjust based on your API response
        renderReservations(reservations);

        doneButton.textContent = "Done";
        doneButton.classList.remove("loading");
    } catch (error) {
        console.error(error);
        alert("Error fetching reservations");
        doneButton.textContent = "Done";
        doneButton.classList.remove("loading");
    }
}

resetSearch.addEventListener("click", () => {
    document.getElementById("searchReservation").value = "";
    document.getElementById("searchDate").value = "";
    document.getElementById("searchStatus").value = "";
    sorting = "";
    document.querySelectorAll(".sort-button").forEach((btn) => btn.classList.remove("active"));
    fetchReservations();
});

doneButton.addEventListener("click", fetchReservations);

document.querySelectorAll(".sort-button").forEach((button) => {
    button.addEventListener("click", () => {
        document.querySelectorAll(".sort-button").forEach((btn) => btn.classList.remove("active"));
        button.classList.add("active");
        sorting = button.getAttribute("data-sort");
        fetchReservations();
    });
});