             #########################
             |--------K9 QUERY-------|
             |  CLI dog.ceo API tool | 
             #########################
             Written by : Aven Stewart

-------------------------------------------------------
                      SUMMARY
-------------------------------------------------------
This tool is meant as a basic competency demonstration
for the Java programming language. Functionally, it 
serves as a CLI frontend for the dog.ceo API that 
allows users to request a random image of a dog breed 
or sub-breed.

This tool also serves mock data for three specific API 
calls in the event the API goes down. It has a built-in
method to simulate an API-down event by returning fake 
500 errors to API requests when the "simulate_downtime" 
property is set to TRUE in config.properties.

The three calls that this program will respond to with 
mock data during an API-down event are requests for a 
random image of a bulldog, french bulldog sub-breed, or 
boston bulldog sub-breed.

The program will also update the mock data for these 
three calls any time one of these calls are requested 
when the API is in a functional state.

As an additional feature, the "USER_AGENT" property can 
be used to change from the default user agent should 
the API blacklist it. Any standard user agent string is 
acceptable, but is not gauranteed to be accepted by the 
API.

-------------------------------------------------------
		ADDITIONAL INFORMATION
-------------------------------------------------------

The Jackson JSON processing library is used to parse 
all API responses. jackson-core, jackson-annotations, 
and jackson-databind are all included in the lib 
directory. 

-------------------------------------------------------

                Thank you for reading!
