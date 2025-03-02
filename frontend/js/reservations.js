const reservationsContainer = document.getElementById("reservationsContainer");
const reservationsButton = document.getElementById("reservationsButton");
const doneButton = document.getElementById("doneButton"); // Placeholder, will be removed if not used
const base_url = 'http://localhost:8080/api/v1/customer/bundles';

window.addEventListener("DOMContentLoaded", fetchReservations);

// Trigger fetch when Reservations button is clicked
reservationsButton.addEventListener("click", (event) => {
    event.preventDefault(); // Prevent default anchor behavior
    fetchReservations();
});


function renderReservations(bundles) {
    reservationsContainer.innerHTML = "";

    bundles.forEach((bundle) => {
        const card = document.createElement("div");
        card.className = "reservation-card";

        // Use bundle imageUrl or fallback to no_image.png
        const img = document.createElement("img");
        img.src = bundle.imageUrl || "../../images/others/no_image.png";
        img.alt = "Bundle Image";
        card.appendChild(img);

        // Bundle details
        const bundleDetails = document.createElement("div");
        bundleDetails.className = "bundle-details";

        const nameSection = document.createElement("div");
        nameSection.className = "info-section name-section";
        const nameIcon = document.createElement("div");
        nameIcon.className = "info-icon";
        const nameText = document.createElement("div");
        nameText.className = "info-text";
        nameText.textContent = bundle.name;
        nameSection.appendChild(nameIcon);
        nameSection.appendChild(nameText);
        bundleDetails.appendChild(nameSection);

        const priceSection = document.createElement("div");
        priceSection.className = "info-section price-section";
        const priceIcon = document.createElement("div");
        priceIcon.className = "info-icon";
        const priceText = document.createElement("div");
        priceText.className = "info-text";
        priceText.textContent = `$${bundle.price}`;
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

        card.appendChild(bundleDetails);

        // Reservations details
        if (bundle.reservations && bundle.reservations.length > 0) {
            const reservationsList = document.createElement("div");
            reservationsList.className = "reservations-list";

            bundle.reservations.forEach((reservation) => {
                const reservationItem = document.createElement("div");
                reservationItem.className = "reservation-item";

                const dateSection = document.createElement("div");
                dateSection.className = "info-section date-section";
                const dateIcon = document.createElement("div");
                dateIcon.className = "info-icon";
                const dateText = document.createElement("div");
                dateText.className = "info-text";
                const [year, month, day] = reservation.reservationDate;
                dateText.textContent = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
                dateSection.appendChild(dateIcon);
                dateSection.appendChild(dateText);
                reservationItem.appendChild(dateSection);

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

        // Optional: Add a loading indicator if doneButton is intended to be used
        if (doneButton) {
            doneButton.textContent = "Loading...";
            doneButton.classList.add("loading");
        }

        const response = await fetch(base_url + `/reservations`, {
            headers: {
                Authorization: "Bearer " + token,
            },
        });

        if (!response.ok) {
            const errorData = await response.json(); // Capture error details
            console.error("Fetch error:", errorData);
            alert(`Failed to fetch reservations: ${errorData.message || response.statusText}`);
            if (doneButton) {
                doneButton.textContent = "Done";
                doneButton.classList.remove("loading");
            }
            return;
        }

        const data = await response.json();
        const bundles = data['bundles'] || []; // Use 'bundles' as per your API response
        renderReservations(bundles);

        if (doneButton) {
            doneButton.textContent = "Done";
            doneButton.classList.remove("loading");
        }
    } catch (error) {
        console.error("Error fetching reservations:", error);
        alert("Error fetching reservations: Check console for details");
        if (doneButton) {
            doneButton.textContent = "Done";
            doneButton.classList.remove("loading");
        }
    }
}

window.addEventListener("DOMContentLoaded", fetchReservations);

// Trigger fetch when Reservations button is clicked
reservationsButton.addEventListener("click", (event) => {
    event.preventDefault(); // Prevent default anchor behavior
    fetchReservations();
});