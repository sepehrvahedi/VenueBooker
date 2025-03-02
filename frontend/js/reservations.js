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

function renderReservations(bundles) {
    reservationsContainer.innerHTML = "";

    bundles.forEach((bundle) => {
        const card = document.createElement("div");
        card.className = "reservation-card";

        // Bundle details
        const bundleDetails = document.createElement("div");
        bundleDetails.className = "bundle-details";

        const nameSection = document.createElement("div");
        nameSection.className = "info-section name-section";
        const nameIcon = document.createElement("div");
        nameIcon.className = "info-icon";
        const nameText = document.createElement("div");
        nameText.className = "info-text";
        nameText.textContent = `Bundle: ${bundle.name}`;
        nameSection.appendChild(nameIcon);
        nameSection.appendChild(nameText);
        bundleDetails.appendChild(nameSection);

        const priceSection = document.createElement("div");
        priceSection.className = "info-section price-section";
        const priceIcon = document.createElement("div");
        priceIcon.className = "info-icon";
        const priceText = document.createElement("div");
        priceText.className = "info-text";
        priceText.textContent = `Price: $${bundle.price}`;
        priceSection.appendChild(priceIcon);
        priceSection.appendChild(priceText);
        bundleDetails.appendChild(priceSection);

        const productsSection = document.createElement("div");
        productsSection.className = "info-section tags-section";
        const productsIcon = document.createElement("div");
        productsIcon.className = "info-icon";
        const productsContent = document.createElement("div");
        productsContent.className = "tags-content";
        (bundle.products || []).forEach((product) => {
            const productSpan = document.createElement("span");
            productSpan.className = "tag-item";
            productSpan.textContent = product;
            productsContent.appendChild(productSpan);
        });
        productsSection.appendChild(productsIcon);
        productsSection.appendChild(productsContent);
        bundleDetails.appendChild(productsSection);

        const statusSection = document.createElement("div");
        statusSection.className = "info-section status-section";
        const statusIcon = document.createElement("div");
        statusIcon.className = "info-icon";
        const statusText = document.createElement("div");
        statusText.className = "info-text";
        statusText.textContent = `Status: ${bundle.active ? "Available" : "Unavailable"}`;
        statusSection.appendChild(statusIcon);
        statusSection.appendChild(statusText);
        bundleDetails.appendChild(statusSection);

        card.appendChild(bundleDetails);

        // Reservations details
        if (bundle.reservations && bundle.reservations.length > 0) {
            const reservationsList = document.createElement("div");
            reservationsList.className = "reservations-list";

            bundle.reservations.forEach((reservation) => {
                const reservationItem = document.createElement("div");
                reservationItem.className = "reservation-item";

                const idSection = document.createElement("div");
                idSection.className = "info-section id-section";
                const idIcon = document.createElement("div");
                idIcon.className = "info-icon";
                const idText = document.createElement("div");
                idText.className = "info-text";
                idText.textContent = `ID: ${reservation.userId || 'N/A'}`;
                idSection.appendChild(idIcon);
                idSection.appendChild(idText);
                reservationItem.appendChild(idSection);

                const dateSection = document.createElement("div");
                dateSection.className = "info-section date-section";
                const dateIcon = document.createElement("div");
                dateIcon.className = "info-icon";
                const dateText = document.createElement("div");
                dateText.className = "info-text";
                const [year, month, day] = reservation.reservationDate;
                dateText.textContent = `Date: ${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
                dateSection.appendChild(dateIcon);
                dateSection.appendChild(dateText);
                reservationItem.appendChild(dateSection);

                const createdSection = document.createElement("div");
                createdSection.className = "info-section created-section";
                const createdIcon = document.createElement("div");
                createdIcon.className = "info-icon";
                const createdText = document.createElement("div");
                createdText.className = "info-text";
                const createdDate = new Date(reservation.createdAt * 1000); // Convert to milliseconds
                createdText.textContent = `Created: ${createdDate.toLocaleString()}`;
                createdSection.appendChild(createdIcon);
                createdSection.appendChild(createdText);
                reservationItem.appendChild(createdSection);

                reservationsList.appendChild(reservationItem);
            });

            card.appendChild(reservationsList);
        } else {
            const noReservations = document.createElement("div");
            noReservations.className = "info-section no-reservations";
            const noReservationsText = document.createElement("div");
            noReservationsText.className = "info-text";
            noReservationsText.textContent = "No reservations for this bundle.";
            noReservations.appendChild(noReservationsText);
            card.appendChild(noReservations);
        }

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
        const bundles = data['bundles'] || []; // Use 'bundles' as per your API response
        renderReservations(bundles);

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