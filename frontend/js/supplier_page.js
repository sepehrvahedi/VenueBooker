const token = localStorage.getItem("userToken");
const bundlesContainer = document.getElementById("bundlesContainer");

// 1) Fetch bundles on page load
window.addEventListener("DOMContentLoaded", fetchBundles);
let priceSlider;
let maxPossiblePrice = 0;
let initialized = false;

// Initialize price range slider
function initializePriceSlider(bundles) {
    maxPossiblePrice = Math.max(...bundles.map(bundle => parseFloat(bundle.price)));
    const sliderElement = document.getElementById('slider');
    
    // If slider already exists, destroy it first
    if (priceSlider) {
        priceSlider.destroy();
    }
    
    // Create new slider
    priceSlider = noUiSlider.create(sliderElement, {
        start: [0, maxPossiblePrice],
        connect: true,
        range: {
            'min': 0,
            'max': maxPossiblePrice
        }
    });
    
    // Update labels on slide
    const minLabel = document.getElementById('minPriceLabel');
    const maxLabel = document.getElementById('maxPriceLabel');
    
    priceSlider.on('update', function(values) {
        minLabel.textContent = '$' + Math.round(values[0]);
        maxLabel.textContent = '$' + Math.round(values[1]);
    });
}

// async function fetchBundles() {
// try {
//     const response = await fetch("http://localhost:8080/api/v1/bundles", {
//     headers: {
//         Authorization: "Bearer " + token,
//     },
//     });

//     if (!response.ok) {
//     alert("Failed to fetch bundles");
//     return;
//     }

//     const data = await response.json();
//     const bundles = data.bundles;
//     renderBundles(bundles);
// } catch (error) {
//     console.error(error);
//     alert("Error fetching bundles");
// }
// }
function renderBundles(bundles) {
    // Clear container
    bundlesContainer.innerHTML = "";

    // Create a card for each bundle
    bundles.forEach((bundle) => {
        const card = document.createElement("div");
        card.className = "bundle-card";

        // Bundle image
        const img = document.createElement("img");
        img.src = bundle.imageUrl || "../../images/others/no_image.png";
        card.appendChild(img);
        
        // Container for details
        const detailsDiv = document.createElement("div");
        detailsDiv.className = "bundle-details";
        card.appendChild(detailsDiv);

        // Name section
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

        // Price section
        const priceSection = document.createElement("div");
        priceSection.className = "info-section price-section";
        const priceIcon = document.createElement("div");
        priceIcon.className = "info-icon";
        const priceText = document.createElement("div");
        priceText.className = "info-text";
        priceText.textContent = bundle.price;
        priceSection.appendChild(priceIcon);
        priceSection.appendChild(priceText);
        detailsDiv.appendChild(priceSection);

        // Quantity section
        // const quantitySection = document.createElement("div");
        // quantitySection.className = "info-section quantity-section";
        // const quantityIcon = document.createElement("div");
        // quantityIcon.className = "info-icon";
        // const quantityText = document.createElement("div");
        // quantityText.className = "info-text";
        // quantityText.textContent = bundle.quantity || "N/A";
        // quantitySection.appendChild(quantityIcon);
        // quantitySection.appendChild(quantityText);
        // detailsDiv.appendChild(quantitySection);

        // Tags section
        const tagsSection = document.createElement("div");
        tagsSection.className = "info-section tags-section";
        const tagsIcon = document.createElement("div");
        tagsIcon.className = "info-icon";
        const tagsContent = document.createElement("div");
        tagsContent.className = "tags-content";
        (bundle.products || []).forEach((tag) => {
            const tagSpan = document.createElement("span");
            tagSpan.className = "tag-item";
            tagSpan.textContent = tag;
            tagsContent.appendChild(tagSpan);
        });
        tagsSection.appendChild(tagsIcon);
        tagsSection.appendChild(tagsContent);
        detailsDiv.appendChild(tagsSection);

        // Actions section
        const actionsDiv = document.createElement("div");
        actionsDiv.className = "actions";

        // Delete button
        // const deleteBtn = document.createElement("button");
        // deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
        // deleteBtn.onclick = () => handleDelete(bundle.id);
        // actionsDiv.appendChild(deleteBtn);

        // Edit button
        const editBtn = document.createElement("button");
        editBtn.innerHTML = '<i class="fas fa-pen"></i>';
        editBtn.onclick = () => {
            window.location.href = `../edit_bundle/edit_bundle.html?id=${bundle.id}`;
        };
        actionsDiv.appendChild(editBtn);

        // Availability toggle
        const toggleLabel = document.createElement("label");
        toggleLabel.className = "switch";

        const toggleInput = document.createElement("input");
        toggleInput.type = "checkbox";
        toggleInput.checked = bundle.active;
        toggleInput.onchange = () => handleToggle(bundle.id, toggleInput.checked);

        const sliderSpan = document.createElement("span");
        sliderSpan.className = "slider round";

        toggleLabel.appendChild(toggleInput);
        toggleLabel.appendChild(sliderSpan);
        actionsDiv.appendChild(toggleLabel);

        card.appendChild(actionsDiv);
        bundlesContainer.appendChild(card);
    });
}

// DELETE handler
// async function handleDelete(bundleId) {
// const confirmed = confirm("Are you sure you want to delete this item?");
// if (!confirmed) return;

// try {
//     const response = await fetch(`http://localhost:8080/api/v1/bundles/delete/${bundleId}`, {
//     method: "DELETE",
//     headers: {
//         Authorization: "Bearer " + token,
//     },
//     });

//     if (!response.ok) {
//     alert("Delete failed");
//     return;
//     }

//     alert("Bundle deleted successfully");
//     // Re-fetch or remove the card from DOM
//     fetchBundles();
// } catch (error) {
//     console.error(error);
//     alert("Error deleting bundle");
// }
// }

// Toggle Availability
async function handleToggle(bundleId, newActiveStatus) {
// e.g. PUT /api/v1/bundles/status with JSON { id: "...", active: false or true }
const payload = {
    id: bundleId,
    active: newActiveStatus,
};

try {
    const response = await fetch("http://localhost:8080/api/v1/bundles/status", {
    method: "PUT",
    headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + token,
    },
    body: JSON.stringify(payload),
    });

    if (!response.ok) {
    alert("Failed to update status");
    return;
    }

    console.log("Status updated successfully");
} catch (error) {
    console.error(error);
    alert("Error updating status");
}
}

const doneButton = document.getElementById("doneButton");
let sorting = ""; // To track the current sorting option

// Fetch bundles on page load
window.addEventListener("DOMContentLoaded", fetchBundles);

// Event listener for search and sort actions
document.getElementById("resetSearch").addEventListener("click", resetSearch);
doneButton.addEventListener("click", fetchBundles);

// Add click events to sort buttons
document.querySelectorAll(".sort-button").forEach((button) => {
    button.addEventListener("click", () => {
        document.querySelectorAll(".sort-button").forEach((btn) => btn.classList.remove("active"));
        button.classList.add("active");
        sorting = button.getAttribute("data-sort");
    });
});

async function fetchBundles() {
    console.log("Fetching bundles...");
    try {
        // Show loading state on DONE button
        doneButton.textContent = "Loading...";
        doneButton.classList.add("loading");

        // Build query params for search and sorting
        const name = document.getElementById("searchName").value;
        const active = document.getElementById("searchStatus").value;

        const [minPrice, maxPrice] = priceSlider ? priceSlider.get() : [0, maxPossiblePrice];
        

        const tags = document.getElementById("searchTags").value;

        let query = `?`;
        if (name) query += `name=${name}&`;
        if (active) query += `active=${active}&`;

        if (minPrice > 0) query += `minPrice=${Math.round(minPrice)}&`;
        if (maxPrice < maxPossiblePrice) query += `maxPrice=${Math.round(maxPrice)}&`;

        if (tags) query += `products=${tags}&`;
        if (sorting) query += `sorting=${sorting}`;

        const response = await fetch(`http://localhost:8080/api/v1/bundles${query}`, {
            headers: {
                Authorization: "Bearer " + token,
            },
        });

        if (!response.ok) {
            alert("Failed to fetch bundles");
            doneButton.textContent = "DONE";
            doneButton.classList.remove("loading");
            return;
        }

        const data = await response.json();
        const bundles = data.bundles;
        if (!initialized){
            initialized = true;
            initializePriceSlider(bundles);
        }
        renderBundles(bundles);

        // Reset DONE button state
        doneButton.textContent = "DONE";
        doneButton.classList.remove("loading");
    } catch (error) {
        console.error(error);
        alert("Error fetching bundles");
        doneButton.textContent = "DONE";
        doneButton.classList.remove("loading");
    }
}

// function renderBundles(bundles) {
//     // Clear container
//     bundlesContainer.innerHTML = "";

//     // Render each bundle
//     bundles.forEach((bundle) => {
//         const card = document.createElement("div");
//         card.className = "bundle-card";

//         // Image
//         const img = document.createElement("img");
//         img.src = bundle.imageUrl || "https://via.placeholder.com/180x100?text=No+Image";
//         card.appendChild(img);

//         // Details
//         const detailsDiv = document.createElement("div");
//         detailsDiv.className = "bundle-details";
//         card.appendChild(detailsDiv);

//         const nameDiv = document.createElement("div");
//         nameDiv.className = "bundle-name";
//         nameDiv.textContent = bundle.name;
//         detailsDiv.appendChild(nameDiv);

//         const priceDiv = document.createElement("div");
//         priceDiv.className = "bundle-price";
//         priceDiv.textContent = `Price: ${bundle.price}`;
//         detailsDiv.appendChild(priceDiv);

//         const tagsDiv = document.createElement("div");
//         tagsDiv.className = "tags-container";
//         (bundle.products || []).forEach((tag) => {
//             const tagSpan = document.createElement("span");
//             tagSpan.className = "tag-item";
//             tagSpan.textContent = tag;
//             tagsDiv.appendChild(tagSpan);
//         });
//         detailsDiv.appendChild(tagsDiv);

//         bundlesContainer.appendChild(card);
//     });
// }

function resetSearch() {
    document.getElementById("searchName").value = "";
    document.getElementById("searchStatus").value = "";
    
    if (priceSlider) {
        priceSlider.set([0, maxPossiblePrice]);
    }

    document.getElementById("searchTags").value = "";
    sorting = "";
    document.querySelectorAll(".sort-button").forEach((btn) => btn.classList.remove("active"));
    fetchBundles();
}
