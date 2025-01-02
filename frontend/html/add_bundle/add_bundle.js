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
    const name = document.getElementById('productName').value.trim();
    const price = parseFloat(document.getElementById('productPrice').value);
    const selectedTags = [];

    // A placeholder array of product tags
    const checkboxes = document.querySelectorAll('#productTags input[type="checkbox"]:checked');
    checkboxes.forEach((checkbox) => {
      selectedTags.push(checkbox.value);
    });

    if (name === '' || name === null || name === undefined) {
        alert('empty name inputs not accepted');
        return;
    }
    if (price === 0) {
        alert('empty price input not accepted')
        return;
    }
    if (selectedTags.length === 0) {
        alert('you must choose at least one tag.')
        return;
    }
    // This token is from your sample; typically you’d get it from localStorage or another secure place
    const token = localStorage.getItem('userToken');
    console.log(token);
    // Prepare request body in JSON
    // Match the sample: name, price, tags
    const requestBody = {
        name: name,
        price: price || 1000,
        products: selectedTags
    };

    // Make the POST request
    const response = await fetch("http://localhost:8080/api/v1/bundles", {
        method: "POST",
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

    setTimeout(() => {
        window.location.href = '../supplier_page/supplier_page.html';
    }, 1000); // 1 second "upload" delay

    } catch (err) {
    console.error(err.error);
    alert("An error occurred while sending data: " + err.error);
    }
});