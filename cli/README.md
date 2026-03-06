# reshapr-cli

This is the command line interface for the Reshapr project.

## Installing the CLI

```shell
npm install -g reshapr-cli
```

## Running the CLI in dev mode

After cloning the repository, you can run the CLI in development mode using the following commands.

1. First, ensure you have the required dependencies installed. You can do this by running:

```shell
npm install
```

2. Next, you have to start the CLI in development mode using:

```shell    
npm run dev
```

This will keep the CLI running and watch for changes in the source code, allowing you to develop
and test your changes in real-time.

3. Finally, you have to link the `reshapr` binary to your JavaScript entrypoint for the CLI:

```shell
npm link
```

### Executing some commands

```shell
# Login to the reShapr local control-plane server
reshapr login --server http://localhost:5555

# Logout once your job is done.
reshapr logout 
```
