# solo-manager

A lightweight Java CLI tool for deploying and managing a local **Hiero Solo Network** using the Solo CLI.

## Commands Overview

| Command | Description |
| :--- | :--- |
|	`setup`   | Sets up and deploys a local Hiero Solo network and its selected components. |
|	`purge` |	Destroys the Solo deployment, cleans up resources, and stops background port-forwarding processes. |


## Usage

### 1. Setup

Deploy a local Hiero Solo network with the default configuration:

```bash
java SoloManager.java setup
```

### 2. Configure Deployment
You can customize the deployment by enabling or disabling ecosystem components and specifying component versions.

**Core/Light Deployment (Consensus only):**
```bash
java SoloManager.java setup
```

**Full Stack Deployment:**
```bash
java SoloManager.java setup --mirror-node true --relay true --explorer true
```

**Custom Deployment:**

```bash
java SoloManager.java setup \
  --mirror-node false \
  --relay true \
  --explorer false \
  --consensus-version v0.72.0 \
  --mirror-node-version v0.153.0
```

### 3. Purge

Remove the existing Solo deployment, clean up associated resources, and stop background port-forwarding processes.

```bash
java SoloManager.java purge
```

## Configuration Flags

| Flag | Argument |	Default |	Description |
| :--- | :--- | :--- | :--- |
| `--mirror-node` |	`true/false` |	`false` |	Deploys the Mirror Node. |
| `--relay` |	`true/false` |	`false` |	Deploys the JSON-RPC Relay. |
| `--explorer` |	`true`/`false` | `false` | Deploys the Block Explorer. |
| `--consensus-node-tag` |	`string` | `v0.77.0` | Sets the Consensus Node release tag. |
| `--mirror-node-tag` | `string` | `v0.163.0` | Sets the Mirror Node release tag. |



## Port Forwarding

The tool automatically configures background port-forwarding for the deployed Hiero services.

| Service | Port |
|---|---:|
| Consensus Node gRPC | `35211` |
| Mirror Node REST API | `38081` |
| Mirror Node Java REST API | `8084` |
| Mirror Node Explorer | `8081` |
| JSON-RPC Relay | `37546` |

## Requirements
Ensure the following dependencies are installed and available in your system:
- Java 25 or later
- Docker
- kind
- kubectl
- solo