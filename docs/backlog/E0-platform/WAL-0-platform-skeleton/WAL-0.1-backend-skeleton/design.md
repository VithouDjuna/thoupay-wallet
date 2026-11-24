# Design: WAL-0.1: Backend skeleton + module boundaries

Author: Developer
Status: **TODO**. Write this before you start the sub-ticket. Half a page is enough.

## Approach
1. read requirement from the tickets
2. setup project structure 
3. added required dependency such as spring boot starter, postgresql drive, flyway, testcontainer, and spring-modulith
4. create module for each services such as admin, identity and more and applied test for modulith
5. setup db for postgresql and configuration in application.yml
6. implement simple db migration with flyway 
7. implement test container for posgresql with customer verification

## Decisions and alternatives
I followed all in tickets but gradle i choose groovy because im already familar with it 
and spring boot version upgrade to 4.x.x because there so many CVE critical in spring ecosystem

## Edge cases
- Blocker: edge cases. Answer all 6. For example, two pods run Flyway at the same time: is that safe, and why? (Answert: safe because we have flyway history that tracks the change so if change the old version will failed)
- Blocker: schema layout and F6. Choose one schema or one per module, and say where flyway_schema_history lives. Every later migration depends on this, so it's costly to change (choose on schema and flyway_schema_history lives in application.yml with datasource configuration postgresql(db) -> public(schema) -> flyway_schema_history).
- Major: Groovy. "I'm familiar with it" is a personal preference. Either switch to Kotlin DSL, or argue for Groovy in terms of what's better for the team.

answer:
we're a Java team. Groovy reads close to Java, whereas Kotlin DSL introduces Kotlin-specific syntax people would need to learn just to touch the build.


- Major: T2 and the test plan. Compare @ServiceConnection with @DynamicPropertySource, and add one test or check for each acceptance criterion (AC).

- Major: the CVE reason for Spring Boot. It's vague, and the ticket already requires Boot 4, so give specifics or drop it.
Answer: Spring Boot 4 is required by the project specification. This implementation follows the required framework version.

## Test plan
I already test change existing script in flyway migration and it failed as expected 
for test container due to i verified port are generated randomly so there is no effect with production

## Risks / questions for the tech lead
no comment