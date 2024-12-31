document.addEventListener("DOMContentLoaded", async () => {
    const token = localStorage.getItem("userToken");
    if (!token) {
      alert("Unauthorized access. Please log in first.");
      window.location.href = "../login/loginSupplier.html"; // Redirect to login page if no token
      return;
    }
    //get username
    const tokenPayload = JSON.parse(atob(token.split('.')[1]));
    document.getElementById('username').innerHTML = tokenPayload['user']['username'];

    // 1. Get the `id` from the URL query string
    const urlParams = new URLSearchParams(window.location.search);
    const bundleId = urlParams.get("id");
    if (!bundleId) {
      alert("No bundle ID found in the URL.");
      return;
    }

    try {
      // 2. Fetch all bundles
      const response = await fetch("http://localhost:8080/api/v1/bundles", {
        headers: {
          Authorization: "Bearer " + token,
        },
      });

      if (!response.ok) {
        alert("Failed to fetch bundles from the server.");
        return;
      }

      const data = await response.json();
      const bundles = data['bundles'];

      // 3. Find the bundle with the matching ID
      const bundle = bundles.find((b) => b.id === bundleId);
      if (!bundle) {
        alert("No bundle found with the specified ID.");
        return;
      }

      // 4. Populate the fields with the bundle's data
      document.getElementById("productName").value = bundle.name;
      document.getElementById("productPrice").value = bundle.price;
      document.getElementById("productDescription").value = bundle.products.join(", ");

      // Optionally, you could add logic to pre-populate the image if your bundle data includes an image URL.
      // For example:
      if (bundle['imageUrl']) {
        const previewImage = document.getElementById("previewImage");
        previewImage.src = bundle['imageUrl'];
        previewImage.style.display = "block";
        document.querySelector(".image-upload i").style.display = "none";
        document.querySelector(".image-upload p").style.display = "none";
      }
    } catch (error) {
      console.error("Error fetching bundles:", error.error);
      alert("An error occurred while fetching bundle data.");
    }
  });

// ----- IMAGE UPLOAD + SPINNER -----
    
const imageUploadDiv = document.getElementById('imageUpload');
const fileInput = document.getElementById('fileInput');
const spinnerOverlay = document.getElementById('spinnerOverlay');
const previewImage = document.getElementById('previewImage');

// When user clicks the imageUploadDiv, trigger file input click
imageUploadDiv.addEventListener('click', () => {
  fileInput.click();
});

fileInput.addEventListener('change', (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // Show spinner (simulate upload)
  spinnerOverlay.style.display = 'flex';

  // In a real-world scenario, you'd send the file via AJAX/fetch here and
  // track progress events. For demonstration, let's just read it locally.
  const reader = new FileReader();
  reader.onload = function (e) {
    // Hide spinner
    spinnerOverlay.style.display = 'none';

    // Show the uploaded image
    previewImage.src = e.target.result;
    previewImage.style.display = 'block';

    // Optionally hide the placeholder icon/text
    // (One approach is to simply set them to display:none)
    imageUploadDiv.querySelector('i').style.display = 'none';
    imageUploadDiv.querySelector('p').style.display = 'none';
  };

  // Simulate upload time by using a setTimeout
  setTimeout(() => {
    reader.readAsDataURL(file);
  }, 1000); // 1 second "upload" delay
});

// ----- SUBMIT / POST DATA -----
const doneBtn = document.getElementById('doneBtn');

doneBtn.addEventListener('click', async () => {
  try {
    // Gather form data
    // const name = document.getElementById('productName').value.trim();
    const urlParams = new URLSearchParams(window.location.search);
    const bundleId = urlParams.get("id");
    const price = parseFloat(document.getElementById('productPrice').value);
    const description = document.getElementById('productDescription').value.trim();

    // A placeholder array of products: your API expects ["DJ"], but let's
    // send [description] or something else you desire
    const productsArray = [description || "DJ"];

    // This token is from your sample; typically you’d get it from localStorage or another secure place
    const token = localStorage.getItem('userToken');
    console.log(token);
    // Prepare request body in JSON
    // Match the sample: name, price, products array
    const requestBody = {
      ids: [bundleId],
      price: price || 1000,
      products: productsArray
    };

    // Make the PUT request
    const response = await fetch("http://localhost:8080/api/v1/bundles", {
      method: "PUT",
      headers: {
        Authorization: "Bearer " + token,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
      const errorText = await response.text();
      alert("Request failed: " + errorText);
      return;
    }

    const responseData = await response.json();
    alert("Data posted successfully!");
    window.location.href = '../supplier_page/supplier_page.html';
  } catch (err) {
    console.error(err);
    alert("An error occurred while sending data: " + error.error);
  }
});