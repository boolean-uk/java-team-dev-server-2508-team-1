# Team Simulation - Server (Java Version)

## Setting Up and Getting Started

1. You'll need a Postgres Database set up either on Neon, in Docker or some other location. 
2. Make a note of the usual credentials for it.
3. Find the `application.yml.example` file in the `resources` folder and rename it to `application.yml`.
4. Fill out the appropriate section with your database credentials.
5. You will also need to add an appropriate JWT Secret, similar to the way it is done in the Security lesson.
6. If you are unsure what this should be then check in with the tutor who will be able to give you some pointers. NOTE: If the secret is not long enough you will get errors when trying to Sign Up and Log In to the site.
7. Run the `main` method that is inside the `Main` class to run the application.
8. If you initially start with an empty database then the application will create some tables and relationships between them. It will also initially populate them with some dummy data that may or may not be used by the application.
9. Once you have things fully up and running you may wish to remove the code that creates the dummy data, it is located in the `run` method in the `Main` class. I would suggest leaving the part that creates the Roles if they are missing but commenting out the rest will prevent the rest of the dummy data from being populated.

## Working with the Project

The code in the application is designed to recreate the functionality that is found in the Node version of the Server which was supplied as an initial proof of concept by the client.

The Client code should allow you to login and display the initial dashboard in almost identical ways, whether the Java or the Node versions of the Server are in use. The functionality is severely limited beyond that, however.

Once you are fully up to speed and working on the project it is perfectly acceptable to change the structure of this code in any way that you see fit (as long as you adhere to whatever process your team has in place). There are sections in the code which need to be refactored (take a look at the `payload/response` package for one example), there are classes which are only there to replicate the functionality of the original Node version (the `Author` class is one example) and there are hard coded responses which need removing. 

## Enjoy!

Once you have your teams set up, enjoy working on the code.

We look forward to seeing what you manage to produce from it!

----Test access Magnus-----
