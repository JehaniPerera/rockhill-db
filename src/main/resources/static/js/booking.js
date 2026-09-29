async function checkAvailability() {

    const date =
        document.getElementById("eventDate").value;

    const result =
        document.getElementById("result");

    if (!date) {
        result.innerHTML =
            "Please select an event date.";
        return;
    }

    try {

        const response = await fetch(
            `/api/bookings/availability?date=${date}`
        );

        if (!response.ok) {
            throw new Error("Request failed");
        }

        const data = await response.json();

        if (data.available) {

            result.innerHTML =
                "✅ Venue is Available";

        } else {

            result.innerHTML =
                "❌ Venue is Not Available";
        }

    } catch (error) {

        console.error(error);

        result.innerHTML =
            "❌ Could not connect to the server.";
    }
}

//New Booking Fr0m
const bookingForm = document.getElementById("bookingForm");

if (bookingForm) {

    bookingForm.addEventListener("submit", function(event) {

        event.preventDefault();

        const message = document.getElementById("bookingMessage");

        message.innerHTML =
            "✅ Booking details entered successfully."; //Booking submission will be connected to the backend.

    });

}
