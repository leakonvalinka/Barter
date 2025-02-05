# ASE PR - Barter

This project was created for the course "PR Advanced Software Engineering" at the University of Technology in Vienna. It was a group project with a team consisting of 6 students. My role was Quality Assurance, so my focus in the project was testing (while also developing, like everyone else).

## What is Barter?

Barter is a neighborhood skill-sharing platform, where users can post what they offer to the community, explore what other users in their area have to offer or get recommendations based on their demands.

## Techstack

- Java with [Quarkus](https://quarkus.io/), QuarkusTest/JUnit5 for testing
- [RabbitMQ](https://www.rabbitmq.com/) for messaging
- Postgres with Postgis 
- [Angular](https://angular.dev/) 
- [Playwright](https://playwright.dev/) for automated system tests

## Starting the application

In order to use Barter, both the backend and frontend must be up and running.

To start the backend, navigate into the /backend directory and run `./mvnw compile quarkus:dev`. Or, if you have the [Quarkus CLI](https://quarkus.io/guides/cli-tooling) installed, `quarkus dev` does the same. This might take a while for the first start-up. This starts the Quarkus backend in dev mode, making the application available through localhost at port 8080. A dev UI is hosted at [/q/dev-ui](http://localhost:8080/q/dev-ui/extensions).

To start the frontend, navigate into the /frontend directory and run `ng serve` or `npm run start`. It is then available through localhost at port 4200. Use **user@example.com** and **Password123!** to log in as an ADMIN or use any other user (with USER role) that can be found in the _import\_dev\_data.sql_ file (located in _\backend\src\main\resources\test-data_) with the respective email and **Password123!**.

_Note: We were using Google's Map API for maps and address handling. However, the corresponding API key was deactivated after the project had ended in order to avoid potential costs. Therefore, location related things are not displayed properly in the frontend and might cause errors._

## Running the tests

To run the backend tests use `./mvnw clean verify`.

To run the E2E tests, [Playwright](https://playwright.dev/) is needed. Install it with `npm init playwright@latest`, details can be found [here](https://playwright.dev/docs/intro). The tests can then be run with `npx playwright test --ui`, which also displays a helpful UI.

_Note: As mentioned before, Google's Map API is not available anymore for this project, causing some test to fail._