const token = localStorage.getItem("userToken");
          const bundlesContainer = document.getElementById("bundlesContainer");
      
          // 1) Fetch bundles on page load
          window.addEventListener("DOMContentLoaded", fetchBundles);
      
          async function fetchBundles() {
            try {
              const response = await fetch("http://localhost:8080/api/v1/bundles", {
                headers: {
                  Authorization: "Bearer " + token,
                },
              });
      
              if (!response.ok) {
                alert("Failed to fetch bundles");
                return;
              }
      
              const data = await response.json();
              const bundles = data.bundles;
              renderBundles(bundles);
            } catch (error) {
              console.error(error);
              alert("Error fetching bundles");
            }
          }
      
          function renderBundles(bundles) {
            // Clear container
            bundlesContainer.innerHTML = "";
      
            // Create a card for each bundle
            bundles.forEach((bundle) => {
              const card = document.createElement("div");
              card.className = "bundle-card";
      
              // Example of an image. If your "bundle" object has an image URL, use it. 
              // For demonstration, we’ll just use a placeholder image if none is found.
              const img = document.createElement("img");
              img.src = bundle.imageUrl || "https://via.placeholder.com/180x100?text=No+Image";
              card.appendChild(img);
      
              // Container for details
              const detailsDiv = document.createElement("div");
              detailsDiv.className = "bundle-details";
              card.appendChild(detailsDiv);
      
              // Name
              const nameDiv = document.createElement("div");
              nameDiv.className = "bundle-name";
              nameDiv.textContent = bundle.name;
              detailsDiv.appendChild(nameDiv);
      
              // Price
              const priceDiv = document.createElement("div");
              priceDiv.className = "bundle-price";
              priceDiv.textContent = bundle.price;
              detailsDiv.appendChild(priceDiv);
      
              // Quantity (if any)
              const quantityDiv = document.createElement("div");
              quantityDiv.className = "bundle-quantity";
              quantityDiv.textContent = bundle.quantity || "N/A";
              detailsDiv.appendChild(quantityDiv);
      
              // Tags (if your bundle has a "products" array or something similar)
              const tagsDiv = document.createElement("div");
              tagsDiv.className = "tags-container";
              (bundle.products || []).forEach((tag) => {
                const tagSpan = document.createElement("span");
                tagSpan.className = "tag-item";
                tagSpan.textContent = tag;
                tagsDiv.appendChild(tagSpan);
              });
              detailsDiv.appendChild(tagsDiv);
      
              // Actions (3 buttons)
              const actionsDiv = document.createElement("div");
              actionsDiv.className = "actions";
      
              // 2.1) Delete button
              const deleteBtn = document.createElement("button");
              deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
              deleteBtn.onclick = () => handleDelete(bundle.id);
              actionsDiv.appendChild(deleteBtn);
      
              // 2.2) Edit button -> go to add-bundle.html?id=<bundle.id>
              const editBtn = document.createElement("button");
              editBtn.innerHTML = '<i class="fas fa-pen"></i>';
              editBtn.onclick = () => {
                window.location.href = `add-bundle.html?id=${bundle.id}`;
              };
              actionsDiv.appendChild(editBtn);
      
              // 2.3) Availability toggle
              const toggleLabel = document.createElement("label");
              toggleLabel.className = "switch";
      
              const toggleInput = document.createElement("input");
              toggleInput.type = "checkbox";
              toggleInput.checked = bundle.active; // or false if not active
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
          async function handleDelete(bundleId) {
            const confirmed = confirm("Are you sure you want to delete this item?");
            if (!confirmed) return;
      
            try {
              const response = await fetch(`http://localhost:8080/api/v1/bundles/delete/${bundleId}`, {
                method: "DELETE",
                headers: {
                  Authorization: "Bearer " + token,
                },
              });
      
              if (!response.ok) {
                alert("Delete failed");
                return;
              }
      
              alert("Bundle deleted successfully");
              // Re-fetch or remove the card from DOM
              fetchBundles();
            } catch (error) {
              console.error(error);
              alert("Error deleting bundle");
            }
          }
      
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