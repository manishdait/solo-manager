import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class SetupCommand {
  private final static Pattern ACCOUNT_JSON_REGEX = Pattern.compile("\\{\\s*\"accountId\"\\s*:\\s*\"([^\"]*)\"\\s*," + "\\s*\"publicKey\"\\s*:\\s*\"([^\"]*)\"\\s*," + "\\s*\"balance\"\\s*:\\s*(\\d+)\\s*}");
  
  private SoloConfig config;

  public SetupCommand(SoloConfig config) {
    Objects.requireNonNull(config, "config must not be null");
    this.config = config;
  }


  public Account init() {
    createKindClusterIfNotExists();
    configureClusterAndDeployment();
    generateKeysAndDeployNetwork();
    deployConsensusNode();
    deployMirrorNode();
    deployExplorer();
    deployJsonRpc();

    return createAccount();
  }

  void createKindClusterIfNotExists() {
    var clustername = config.clustername();
    var result = Process.executeCommand("/bin/bash", "-c", "kind get clusters | grep -x " + clustername);
    if (clustername != null && clustername.equals(result.output())) {
      IO.println("Creating Cluster (" + config.clustername() + ")...." + Process.SKIPPED  + " (Cluster Already Exist)" );
      return;
    }

    new Process("Creating Cluster (" + config.clustername() + ")....", "kind", "create", "cluster", "-n", clustername).execute();
  }


  void configureClusterAndDeployment() {
    IO.println("Configure cluster and create deployment:");

    var processes = List.of(
      new Process("\t- Connecting to the Kind cluster...", "solo", "cluster-ref", "config", "connect", "--cluster-ref", "kind-" + config.clustername(), "--context",  "kind-" + config.clustername()),
      new Process("\t- Creating a new deployment...", "solo", "deployment", "config", "create", "-n",  config.namespace(), "--deployment", config.deployment()),
      new Process("\t- Add cluster to deployment...", "solo", "deployment", "cluster", "attach", "--deployment", config.deployment(), "--cluster-ref", "kind-" + config.clustername(), "--num-consensus-nodes", "1")
    );
    
    ConsoleUtil.sleep();
    processes.stream().forEach((process) -> process.execute());
  }

  void generateKeysAndDeployNetwork() {
    IO.println("Generate keys and deploy network:");
    
    var processes = List.of(
      new Process("\t- Generating Keys...", "solo", "keys", "consensus", "generate", "--gossip-keys", "--tls-keys", "-i", config.nodeId(), "--deployment", config.deployment()),
      new Process("\t- Settting Up Cluster with Shared Components...", "solo", "cluster-ref", "config", "setup", "--cluster-setup-namespace", config.namespace()),
      new Process("\t- Deploying the Network...", "solo", "consensus", "network", "deploy", "-i", config.nodeId(), "--deployment",  config.deployment(), "--consensus-node-version", config.consensusNodeVersion())
    );

    ConsoleUtil.sleep();
    processes.stream().forEach((process) -> process.execute());
  }


  void deployConsensusNode() {
    var portForwardingCmd = String.join(" ", List.of(
      "kubectl port-forward svc/haproxy-node1-svc -n " + config.namespace() + " " + config.ports().get(SoloConfig.Port.HAPROXY_HOST) + ":50211 > /dev/null 2>&1 &",
      "kubectl port-forward svc/envoy-proxy-node1-svc -n " + config.namespace() + " " + config.ports().get(SoloConfig.Port.ENVOY_HOST) + ":8080 > /dev/null 2>&1 &"
    ));

    IO.println("Deploy Consensus Node (" + config.consensusNodeVersion() + "):");
    var processes = List.of(
      new Process("\t- Setting Up Consensus Node...", "solo", "consensus", "node", "setup", "-i", config.nodeId(), "--deployment", config.deployment(), "--consensus-node-version", config.consensusNodeVersion(), "--quiet-mode"),
      new Process("\t- Starting Consensus Node...", "solo", "consensus", "node", "start", "-i", config.nodeId(), "--deployment", config.deployment()),
      new Process("\t- Forwarding Consensus Node Port...", "/bin/bash", "-c", portForwardingCmd)
    );

    ConsoleUtil.sleep();
    processes.stream().forEach((process) -> process.execute());
  }

  void deployMirrorNode() {
    if (!config.installMirrorNode()) {
      IO.println("Deploy Mirror Node: " + Process.SKIPPED);
      return;
    }

    var portForwardingCmd = String.join(" ", List.of(
      "kubectl port-forward svc/mirror-1-rest -n " + config.namespace() + " " + config.ports().get(SoloConfig.Port.MIRROR_NODE_REST) + ":80 > /dev/null 2>&1 &",
      "kubectl port-forward svc/mirror-1-grpc -n " + config.namespace() + " " + config.ports().get(SoloConfig.Port.MIRROR_NODE_GRPC) + ":5600 > /dev/null 2>&1 &",
      "kubectl port-forward svc/mirror-1-web3 -n " + config.namespace() + " " + config.ports().get(SoloConfig.Port.MIRROR_NODE_WEB3) + ":80 > /dev/null 2>&1 &",
      "kubectl port-forward svc/mirror-1-restjava -n " + config.namespace() + " " + config.ports().get(SoloConfig.Port.MIRROR_NODE_JAVA) + ":80 > /dev/null 2>&1 &"
    ));

    IO.println("Deploy Mirror Node (" + config.mirrorNodeVersion() + "):");
    var processes = List.of(
      new Process("\t- Starting Mirror Node...", "solo", "mirror", "node", "add", "--cluster-ref", "kind-" + config.namespace(), "--deployment", config.deployment(), "--mirror-node-version", config.mirrorNodeVersion(), "--enable-ingress", "--pinger",  "--no-parallel-deploy", "-f",  "config/mirror_node_config.yml"),
      new Process("\t- Forwarding Mirror Node Port...", "/bin/bash", "-c", portForwardingCmd)
    );

    ConsoleUtil.sleep();
    processes.stream().forEach((process) -> process.execute());
  }

  void deployExplorer() {
    if (!config.installExplorer()) {
      IO.println("Deploy Mirror Node Explorer: " + Process.SKIPPED);
      return;
    }


    var portForwardingCmd = "kubectl port-forward svc/hiero-explorer-1-solo -n " + config.namespace() + " " + config.ports().get(SoloConfig.Port.EXPLORER)  + ":80 > /dev/null 2>&1 &";
    
    IO.println("Deploy Mirror Node Explorer:");
    var processes = List.of(
      new Process("\t- Starting Mirror Node Explorer...", "solo", "explorer", "node", "add", "--deployment", config.deployment(), "--cluster-ref", "kind-" + config.namespace()),
      new Process("\t- Forwarding Explorer Port...", "/bin/bash", "-c", portForwardingCmd)
    );

    ConsoleUtil.sleep();
    processes.stream().forEach((process) -> process.execute());
  }

  void deployJsonRpc() {
    if (!config.installExplorer()) {
      IO.println("Deploy JSON-RPC Relay: " + Process.SKIPPED);
      return;
    }

    var portForwardingCmd = "kubectl port-forward svc/relay-1 -n " + config.namespace() + " " + config.ports().get(SoloConfig.Port.EXPLORER)  + ":7546 > /dev/null 2>&1 &";
    

    IO.println("Deploy JSON-RPC Relay:");
    var processes = List.of(
      new Process("\t- Starting Json Rpc Relay...", "solo", "relay", "node", "add", "-i", config.nodeId(), "--deployment", config.deployment(), "--dev", "-f", "config/json_rpc_config.yml"),
      new Process("\t- Forwarding Json Rpc Relay Port...", "/bin/bash", "-c", portForwardingCmd)
    );

    ConsoleUtil.sleep();
    processes.stream().forEach((process) -> process.execute());
  }
  
  Account createAccount() {
    IO.println("Create Ledger Account");

    var accountLogs = new Process("\t- Creating a new ledger account...", "solo", "ledger", "account", "create", "--generate-ecdsa-key", "--deployment", config.deployment()).execute();
    var matcher = ACCOUNT_JSON_REGEX.matcher(accountLogs.output());

    String accountId = null;
    String publicKey = null;

    if (matcher.find()) {
      accountId = matcher.group(1);
      publicKey = matcher.group(2);
    } else {
      throw new RuntimeException("Failed to extract account details");
    }
    
    var privateKey = new Process("\t- Resolving privateKey for account...", "/bin/bash", "-c", "kubectl get secret account-key-" + accountId + " -n " + config.namespace() + " -o jsonpath='{.data.privateKey}' | base64 -d | xargs").execute().output();
    new Process("\t- Funding the account with hbars...", "solo", "ledger", "account", "update", "--account-id", accountId, "--hbar-amount", SoloConfig.Default.HBAR_FUND , "--deployment", config.deployment()).execute();

    return  new Account(accountId, publicKey, privateKey);
  }

  
  record Account(String accountId, String publicKey, String privateKey) implements Serializable {
    public Account {
      Objects.requireNonNull(accountId, "accountId must not be null");
      Objects.requireNonNull(publicKey, "publicKey must not be null");
      Objects.requireNonNull(privateKey, "privateKet must not be null");
    }
  }
}
