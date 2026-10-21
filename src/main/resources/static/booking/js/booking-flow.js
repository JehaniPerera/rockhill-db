// ============================================================
// booking-flow.js
// Function 1: Customer details and availability
//
// Wedding booking:
//   Function 1 -> Function 2 (Wedding package selection)
//
// Event booking:
//   Function 1 -> Function 2 (Event menu selection)
//
// Java files do not need to be changed for this JavaScript update.
// ============================================================

function initBookingPage(type) {

    const isWedding = type === 'WEDDING';

    const $ = id => document.getElementById(id);

    const checkBtn = $('checkBtn');
    const continueBtn = $('continueBtn');
    const eventDateInput = $('eventDate');

    // Prevent initialization errors if an expected HTML element is missing.
    if (!checkBtn || !continueBtn || !eventDateInput) {
        console.error(
            'Booking page initialization failed. Missing HTML element:',
            {
                checkBtn: !!checkBtn,
                continueBtn: !!continueBtn,
                eventDate: !!eventDateInput
            }
        );

        return;
    }

    // Track the date that was checked for availability.
    let availableDate = null;

    // Prevent selecting a past date.
    if (typeof tomorrow === 'function') {
        eventDateInput.min = tomorrow();
    }


    // ========================================================
    // EVENT TYPE
    // ========================================================

    if (!isWedding) {

        const eventTypeInput = $('eventType');
        const descWrap = $('descWrap');

        if (eventTypeInput && descWrap) {

            function updateEventType() {

                descWrap.classList.toggle(
                    'hidden',
                    eventTypeInput.value !== 'OTHER'
                );

            }

            eventTypeInput.addEventListener(
                'change',
                updateEventType
            );

            updateEventType();

        }

    }


    // ========================================================
    // RESET AVAILABILITY WHEN THE DATE CHANGES
    // ========================================================

    eventDateInput.addEventListener('change', () => {

        availableDate = null;

        if ($('msg')) {
            clearMsg('msg');
        }

        if ($('suggestions')) {
            $('suggestions').innerHTML = '';
        }

    });


    // ========================================================
    // SHOW ALTERNATIVE DATES
    // ========================================================

    function showSuggestions(list) {

        const box = $('suggestions');

        if (!box) {
            return;
        }

        box.innerHTML = '';

        if (!Array.isArray(list) || list.length === 0) {
            return;
        }

        box.appendChild(
            document.createTextNode('Nearest open dates: ')
        );

        list.forEach(date => {

            const button = document.createElement('button');

            button.type = 'button';

            button.className = 'btn small secondary';

            button.textContent = date;

            button.addEventListener('click', () => {

                eventDateInput.value = date;

                availableDate = null;

                box.innerHTML = '';

                if ($('msg')) {
                    clearMsg('msg');
                }

            });

            box.appendChild(button);

        });

    }


    // ========================================================
    // CHECK DATE AVAILABILITY
    // ========================================================

    async function checkAvailability() {

        const date = eventDateInput.value;

        if (!date) {

            showMsg('msg', 'Please pick an event date.');

            return false;

        }

        try {

            const result = await api(
                '/api/bookings/availability?date=' +
                encodeURIComponent(date)
            );

            if (result && result.available) {

                availableDate = date;

                if ($('suggestions')) {
                    $('suggestions').innerHTML = '';
                }

                showMsg(
                    'msg',
                    date + ' is available.',
                    'success'
                );

                return true;

            }

            availableDate = null;

            showMsg(
                'msg',
                'Sorry, ' + date + ' is not available.'
            );

            showSuggestions(
                result ? result.suggestions : []
            );

            return false;

        } catch (error) {

            availableDate = null;

            console.error(
                'Availability check failed:',
                error
            );

            showMsg(
                'msg',
                error.message || 'Unable to check availability.'
            );

            return false;

        }

    }


    // Connect the Check availability button.
    checkBtn.addEventListener('click', async () => {

        if ($('msg')) {
            clearMsg('msg');
        }

        if ($('suggestions')) {
            $('suggestions').innerHTML = '';
        }

        checkBtn.disabled = true;

        try {

            await checkAvailability();

        } finally {

            checkBtn.disabled = false;

        }

    });


    // ========================================================
    // CONTINUE TO FUNCTION 2
    // ========================================================

    continueBtn.addEventListener('click', async () => {

        if ($('msg')) {
            clearMsg('msg');
        }

        if ($('suggestions')) {
            $('suggestions').innerHTML = '';
        }

        // Determine the event type.
        let eventType;

        if (isWedding) {

            eventType = 'WEDDING';

        } else {

            const eventTypeInput = $('eventType');

            if (!eventTypeInput) {

                showMsg(
                    'msg',
                    'The event type field could not be found.'
                );

                return;

            }

            eventType = eventTypeInput.value;

            if (!eventType) {

                showMsg(
                    'msg',
                    'Please choose the type of event.'
                );

                return;

            }

        }


        // Collect the customer information.
        const body = {

            customerName: $('customerName')
                ? $('customerName').value.trim()
                : '',

            customerPhone: $('customerPhone')
                ? $('customerPhone').value.trim()
                : '',

            customerEmail: $('customerEmail')
                ? $('customerEmail').value.trim()
                : '',

            eventDate: eventDateInput.value,

            eventType: eventType,

            guestCount: $('guestCount')
                ? parseInt($('guestCount').value, 10)
                : NaN

        };


        // Event description is needed for event bookings.
        if (!isWedding) {

            const eventTypeInput = $('eventType');

            const descriptionInput = $('eventDescription');

            if (eventType === 'OTHER') {

                body.eventDescription = descriptionInput
                    ? descriptionInput.value.trim()
                    : '';

            } else if (eventTypeInput) {

                body.eventDescription =
                    eventTypeInput.options[
                        eventTypeInput.selectedIndex
                        ].text;

            }

        }


        // Validate the booking details.
        const problem = validateBooking(body, isWedding);

        if (problem) {

            showMsg('msg', problem);

            return;

        }


        // Ensure the customer has checked this date.
        if (availableDate !== body.eventDate) {

            showMsg(
                'msg',
                'Please click Check availability and confirm your date before continuing.'
            );

            return;

        }


        // Prevent duplicate submissions.
        continueBtn.disabled = true;

        checkBtn.disabled = true;


        try {

            // Create an unsubmitted draft so Function 2 has an ID. Drafts do not block availability.
            const booking = await api(

                isWedding
                    ? '/api/bookings/wedding'
                    : '/api/bookings/event',

                {
                    method: 'POST',
                    body: JSON.stringify(body)
                }

            );


            // Verify that the booking ID was returned.
            if (!booking || booking.id == null) {

                throw new Error(
                    'The booking response does not contain a booking ID. Check the API response.'
                );

            }


            // Choose the correct Function 2 destination.
            const selectionType = isWedding
                ? 'package'
                : 'menu';


            // Navigate to Function 2.
            const destination =
                '/booking/' +
                encodeURIComponent(booking.id) +
                '/select/' +
                selectionType;

            console.log(
                'Booking created successfully. Redirecting to:',
                destination
            );

            window.location.href = destination;


        } catch (error) {

            console.error(
                'Continue to Function 2 failed:',
                error
            );

            showMsg(
                'msg',
                'Unable to continue: ' +
                (error.message || 'An unexpected error occurred.')
            );

            continueBtn.disabled = false;

            checkBtn.disabled = false;

        }

    });


}
