Tegin uue TestAuthController.java faili, kus on api/test/login endpoint testide jaoks.\
Selleks, et saaks sealt sisse logida peab playwright test login saatma sinna endpointi requesti\
koos emaili ja PlaywrightSecret väärtusega.

PlaywrightSecret on backendi .env failis kirjas ja backend kontrollib, et kui backendis või frontendis\
pole seda väärtust, siis login failib, ja kui on siis saab sisse logida kui saadetud väärtus on sama\
mis backendi .env väärtus.

Oli vaja ka natuke muuta SecurityConfig.java faili, et võimaldada sisse logimine ilma Google.\

