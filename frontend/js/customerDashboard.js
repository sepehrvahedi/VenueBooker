const bundlesContainer = document.getElementById("bundlesContainer");
const modal = document.getElementById("modal");
const modalImage = document.getElementById("modal-image");
const modalTitle = document.getElementById("modal-title");
const modalPrice = document.getElementById("modal-price");
const modalTags = document.getElementById("modal-tags");
const modalDescription = document.getElementById("modal-description");
const modalStatus = document.getElementById("modal-status");
const reserveButton = document.getElementById("reserveButton");
const dateSelection = document.getElementById("date-selection");
const reserveDate = document.getElementById("reserveDate");
const okButton = document.getElementById("okButton");
const closeDateButton = document.getElementById("closeDateButton");
const closeModalBtn = document.getElementsByClassName("close")[0];
let currentBundle = null;
const base_url = 'http://localhost:8080/api/v1/customer/bundles'


window.addEventListener("DOMContentLoaded", fetchBundles);

// Close modal at any stage
modal.addEventListener("click", function (event) {
    if (event.target === modal) {
        modal.style.display = "none";
        resetReservationState();
    }
});

closeModalBtn.onclick = function () {
    modal.style.display = "none";
    resetReservationState();
};

closeDateButton.onclick = function () {
    dateSelection.style.display = "none";
    reserveButton.style.display = "block";
};

function renderBundles(bundles) {
    bundlesContainer.innerHTML = "";

    bundles.forEach((bundle) => {
        const card = document.createElement("div");
        card.className = "bundle-card";
        card.addEventListener("click", () => openModal(bundle));

        const img = document.createElement("img");
        img.src = bundle['imageUrl'] || "../../images/others/no_image.png";
        card.appendChild(img);

        const detailsDiv = document.createElement("div");
        detailsDiv.className = "bundle-details";
        card.appendChild(detailsDiv);

        const nameSection = document.createElement("div");
        nameSection.className = "info-section name-section";
        const nameIcon = document.createElement("div");
        nameIcon.className = "info-icon";
        const nameText = document.createElement("div");
        nameText.className = "info-text";
        nameText.textContent = bundle.name;
        nameSection.appendChild(nameIcon);
        nameSection.appendChild(nameText);
        detailsDiv.appendChild(nameSection);

        const priceSection = document.createElement("div");
        priceSection.className = "info-section price-section";
        const priceIcon = document.createElement("div");
        priceIcon.className = "info-icon";
        const priceText = document.createElement("div");
        priceText.className = "info-text";
        priceText.textContent = `$${bundle.price}`;
        priceSection.appendChild(priceIcon);
        priceSection.appendChild(priceText);
        detailsDiv.appendChild(priceSection);

        const tagsSection = document.createElement("div");
        tagsSection.className = "info-section tags-section";
        const tagsIcon = document.createElement("div");
        tagsIcon.className = "info-icon";
        const tagsContent = document.createElement("div");
        tagsContent.className = "tags-content";
        (bundle.products || []).forEach((tag) => {
            const tagSpan = document.createElement("span");
            tagSpan.className = "tag-item";
            tagSpan.textContent = tag.toString();
            tagsContent.appendChild(tagSpan);
        });
        tagsSection.appendChild(tagsIcon);
        tagsSection.appendChild(tagsContent);
        detailsDiv.appendChild(tagsSection);

        // Add description section
        const descriptionSection = document.createElement("div");
        descriptionSection.className = "info-section description-section";
        const descriptionIcon = document.createElement("div");
        descriptionIcon.className = "info-icon";
        descriptionIcon.style.backgroundImage = "url('../../images/icons/description.png')";
        const descriptionText = document.createElement("div");
        descriptionText.className = "info-text";
        descriptionText.textContent = bundle.description || "No description";
        descriptionSection.appendChild(descriptionIcon);
        descriptionSection.appendChild(descriptionText);
        detailsDiv.appendChild(descriptionSection);

        bundlesContainer.appendChild(card);
    });
}

function openModal(bundle) {
    currentBundle = bundle;
    modalImage.src = bundle['imageUrl'] || "../../images/others/no_image.png";
    modalTitle.textContent = bundle.name;
    modalPrice.textContent = `$${bundle.price}`;
    modalTags.textContent = (bundle.products || []).join(", ");
    modalDescription.textContent = bundle.description || "No description";
    modalStatus.textContent = bundle.active ? "Available" : "Unavailable";
    reserveButton.style.display = "block";
    reserveButton.disabled = !bundle.active; // Disable if unavailable
    dateSelection.style.display = "none"; // Hide date selection initially
    okButton.disabled = true; // Disable OK button initially
    modal.style.display = "block";
}

function resetReservationState() {
    currentBundle = null;
    dateSelection.style.display = "none";
    reserveButton.style.display = "block";
    reserveButton.disabled = true;
    reserveDate.value = "";
    okButton.disabled = true;
}

reserveButton.addEventListener("click", () => {
    if (currentBundle && currentBundle.active) {
        reserveButton.style.display = "none";
        dateSelection.style.display = "block";
        okButton.disabled = true; // Enable only after date is selected
        reserveDate.focus(); // Focus the date input to trigger calendar
    }
});

reserveDate.addEventListener("change", (event) => {
    const input = event.target;
    const value = input.value;
    if (value && isValidDateFormat(value)) {
        okButton.disabled = false; // Enable OK button when a valid date is selected
    } else {
        okButton.disabled = true; // Disable if invalid
    }
});

reserveDate.addEventListener("input", (event) => {
    const value = event.target.value;
    if (value && isValidDateFormat(value)) {
        okButton.disabled = false; // Enable OK button for manual input
    } else {
        okButton.disabled = true; // Disable if invalid
    }
});

// Validate date format (YYYY-MM-DD)
function isValidDateFormat(dateString) {
    const regex = /^\d{4}-\d{2}-\d{2}$/;
    if (!regex.test(dateString)) return false;
    const date = new Date(dateString);
    const [year, month, day] = dateString.split('-');
    return date.getFullYear() == year && date.getMonth() + 1 == month && date.getDate() == day;
}

async function reserveBundle(bundleId, date) {
    try {
        const token = localStorage.getItem("userToken");
        if (!token) {
            alert("Please log in to reserve a bundle.");
            return;
        }

        const payload = {
            bundleId: bundleId,
            reservationDate: date
        };

        const response = await fetch(base_url + "/reserve", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: "Bearer " + token,
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorData = await response.json();
            alert(`Reservation failed: ${errorData.message || "Unknown error"}`);
            return;
        }

        const result = await response.json();
        alert("Bundle reserved successfully! Reservation ID: " + result.reservationId);
        modal.style.display = "none";
        fetchBundles(); // Refresh bundles to reflect reservation status if needed
    } catch (error) {
        console.error("Error reserving bundle:", error);
        alert("An error occurred while reserving the bundle.");
    }
}

okButton.addEventListener("click", () => {
    if (currentBundle && reserveDate.value && isValidDateFormat(reserveDate.value)) {
        reserveBundle(currentBundle.id || currentBundle.name, reserveDate.value);
    } else {
        alert("Please select a valid date in YYYY-MM-DD format.");
    }
});

const doneButton = document.getElementById("doneButton");
let sorting = "";

document.getElementById("resetSearch").addEventListener("click", resetSearch);
doneButton.addEventListener("click", fetchBundles);

document.querySelectorAll(".sort-button").forEach((button) => {
    button.addEventListener("click", () => {
        document.querySelectorAll(".sort-button").forEach((btn) => btn.classList.remove("active"));
        button.classList.add("active");
        sorting = button.getAttribute("data-sort");
        fetchBundles();
    });
});

async function fetchBundles() {
    console.log("Fetching bundles...");
    try {
        // Check if user is logged in
        const token = localStorage.getItem("userToken");
        if (!token) {
            alert("Unauthorized access. Please log in first.");
            window.location.href = "../login/loginCustomer.html"; // Redirect to login page if no token
            return;
        }
        // Get username
        const tokenPayload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('username').innerHTML = tokenPayload['user']['username'];

        // Show loading state on DONE button
        doneButton.textContent = "Loading...";
        doneButton.classList.add("loading");

        // Build query params for search and sorting
        const name = document.getElementById("searchName").value;
        const active = document.getElementById("searchStatus").value;
        const minPrice = document.getElementById("minPrice").value;
        const maxPrice = document.getElementById("maxPrice").value;
        const tags = document.getElementById("searchTags").value;

        let query = `?`;
        if (name) query += `name=${encodeURIComponent(name)}&`;
        if (active) query += `active=${active}&`;
        if (minPrice) query += `minPrice=${minPrice}&`;
        if (maxPrice) query += `maxPrice=${maxPrice}&`;
        if (tags) query += `products=${encodeURIComponent(tags)}&`;
        if (sorting) query += `sorting=${sorting}`;

        // Fetch real data from the server
        const response = await fetch(base_url + `${query}`, {
            headers: {
                Authorization: "Bearer " + token,
            },
        });

        if (!response.ok) {
            alert("Failed to fetch bundles");
            doneButton.textContent = "Done";
            doneButton.classList.remove("loading");
            return;
        }

        const data = await response.json();
        const bundles = data['bundles']; // Adjust based on your API response structure
        renderBundles(bundles);

        // Reset DONE button state
        doneButton.textContent = "Done";
        doneButton.classList.remove("loading");
    } catch (error) {
        console.error(error);
        alert("Error fetching bundles");
        doneButton.textContent = "Done";
        doneButton.classList.remove("loading");
    }
}

function resetSearch() {
    document.getElementById("searchName").value = "";
    document.getElementById("searchStatus").value = "";
    document.getElementById("minPrice").value = "";
    document.getElementById("maxPrice").value = "";
    document.getElementById("searchTags").value = "";
    sorting = "";
    document.querySelectorAll(".sort-button").forEach(
        (btn) => btn.classList.remove("active")
    );
    fetchBundles();
}