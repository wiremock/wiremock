# Agent Rules

## Running the build
Always use `./gradlew` to run the build. This ensures that the correct version of Gradle is used and all plugins are applied.

## Messaging/WebSocket Code

The following packages contain messaging/websocket functionality:
- `wiremock-core/src/main/java/com/github/tomakehurst/wiremock/websocket/`
- `wiremock-core/src/main/java/com/github/tomakehurst/wiremock/websocket/message/`
- Message-related classes in `wiremock-core/src/main/java/com/github/tomakehurst/wiremock/verification/` (classes with "Message" in the name)

### Rules for these packages:

1. **No Javadoc** - Do not add javadoc comments to classes, interfaces, methods, or fields.

2. **No Comments** - Do not add inline comments except in rare cases to explain non-obvious decisions that cannot be made clear through better naming or code structure.

3. **Always import** - Do not use fully-qualified class names. Always use `import` statements.

