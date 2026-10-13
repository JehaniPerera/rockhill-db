// ==========================================
// Rockhill Reception Hall
// Booking & Event Scheduling
// ==========================================


// ==========================================
// 1. CHECK VENUE AVAILABILITY
// ==========================================

async function checkAvailability() {

    const dateInput = document.getElementById("eventDate");
    const result = document.getElementById("result");

    if (!dateInput || !result) {
        return;
    }

    const date = dateInput.value;

    if (!date) {
        result.textContent = "Please select an event date.";
        return;
    }

    result.textContent = "Checking availability...";

    try {

        const response = await fetch(
            `/api/bookings/availability?date=${encodeURIComponent(date)}`
        );

        if (!response.ok) {
            throw new Error("Unable to check availability");
        }

        const data = await response.json();

        if (data.available) {

            result.textContent =
                "Venue is available for the selected date.";

        } else {

            result.textContent =
                "Venue is not available for the selected date.";
        }

    } catch (error) {

        console.error(error);

        result.textContent =
            "Unable to connect to the server.";
    }
}


// ==========================================
// 2. SHOW PACKAGE OR MENU
// ==========================================

const eventTypeInput = document.getElementById("eventType");

if (eventTypeInput) {

    eventTypeInput.addEventListener("change", function () {

        const packageSection =
            document.getElementById("packageSection");

        const menuSection =
            document.getElementById("menuSection");

        const packageInput =
            document.getElementById("packageId");

        const menuInput =
            document.getElementById("menuId");


        if (this.value === "Wedding") {

            // Show Package
            packageSection.style.display = "block";

            // Hide Menu
            menuSection.style.display = "none";

            packageInput.required = true;
            menuInput.required = false;

            menuInput.value = "";

        } else if (this.value === "Other") {

            // Hide Package
            packageSection.style.display = "none";

            // Show Menu
            menuSection.style.display = "block";

            packageInput.required = false;
            menuInput.required = true;

            packageInput.value = "";

        } else {

            // Hide both
            packageSection.style.display = "none";
            menuSection.style.display = "none";

            packageInput.required = false;
            menuInput.required = false;
        }

    });
}


// ==========================================
// 3. SUBMIT BOOKING
// ==========================================

const bookingForm = document.getElementById("bookingForm");

if (bookingForm) {

    bookingForm.addEventListener("submit", async function (event) {

        event.preventDefault();


        // --------------------------------------
        // Get form values
        // --------------------------------------

        const customerName =
            document.getElementById("customerName").value.trim();

        const phone =
            document.getElementById("phone").value.trim();

        const email =
            document.getElementById("email").value.trim();

        const eventType =
            document.getElementById("eventType").value;

        const eventDate =
            document.getElementById("eventDate").value;

        const guestCount =
            Number(document.getElementById("guestCount").value);

        const packageId =
            document.getElementById("packageId").value;

        const menuId =
            document.getElementById("menuId").value;

        const message =
            document.getElementById("bookingMessage");

        const submitButton =
            bookingForm.querySelector("button[type='submit']");


        // --------------------------------------
        // Basic validation
        // --------------------------------------

        if (!customerName) {
            message.textContent = "Please enter your name.";
            return;
        }

        if (!phone) {
            message.textContent = "Please enter your phone number.";
            return;
        }

        if (!email) {
            message.textContent = "Please enter your email address.";
            return;
        }

        if (!eventType) {
            message.textContent = "Please select an event type.";
            return;
        }

        if (!eventDate) {
            message.textContent = "Please select an event date.";
            return;
        }

        if (!guestCount || guestCount < 1) {
            message.textContent = "Guest count must be at least 1.";
            return;
        }


        // --------------------------------------
        // Build booking request
        // --------------------------------------

        const bookingData = {

            customerName: customerName,

            phone: phone,

            email: email,

            eventDate: eventDate,

            eventType: eventType,

            guestCount: guestCount
        };


        // --------------------------------------
        // Wedding → Package
        // --------------------------------------

        if (eventType === "Wedding") {

            if (!packageId) {
                message.textContent =
                    "Please enter a package ID.";
                return;
            }

            bookingData.packageId = Number(packageId);
        }


        // --------------------------------------
        // Other → Menu
        // --------------------------------------

        if (eventType === "Other") {

            if (!menuId) {
                message.textContent =
                    "Please enter a menu ID.";
                return;
            }

            bookingData.menuId = Number(menuId);
        }


        // --------------------------------------
        // Send to Spring Boot
        // --------------------------------------

        try {

            message.textContent =
                "Processing your booking...";

            submitButton.disabled = true;


            const response = await fetch(
                "/api/bookings",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(bookingData)
                }
            );


            // --------------------------------------
            // Read server response
            // --------------------------------------

            const data = await response.json();


            // --------------------------------------
            // Handle error
            // --------------------------------------

            if (!response.ok) {

                console.error(
                    "Booking error:",
                    data
                );

                message.textContent =
                    "We couldn't submit your booking. Please try again.";

                submitButton.disabled = false;

                return;
            }


            // --------------------------------------
            // Success
            // --------------------------------------

            message.innerHTML =
                "Booking request submitted successfully.<br>" +
                "Booking ID: " + data.bookingId + "<br>" +
                "Status: " + data.status;


            // Reset form
            bookingForm.reset();


            // Hide package/menu sections
            document.getElementById("packageSection")
                .style.display = "none";

            document.getElementById("menuSection")
                .style.display = "none";

            document.getElementById("packageId")
                .required = false;

            document.getElementById("menuId")
                .required = false;


        } catch (error) {

            console.error(
                "Connection error:",
                error
            );

            message.textContent =
                "Unable to connect to the server.";

        } finally {

            submitButton.disabled = false;
        }

    });
}