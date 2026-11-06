import java.util.Objects;

public class PurgeCommand {
  public SoloConfig config;

  public PurgeCommand(SoloConfig config) {
    Objects.requireNonNull(config, "config must not be null");
    this.config = config;
  }

  public void init() {
    destroyJsonRpcRelay();
    destroyExplorer();
    destroyMirrorNode();
    destroyConsensusNode();
    destroyCluster();
    deleteSoloConfig();
    dockerSystemCleanup();
  }

  void destroyJsonRpcRelay() {
    if (!config.installRelay()) {
      IO.println("Destroying JSON-RPC Relay..." + Process.SKIPPED);
      return;
    }
    new Process("Destroying JSON-RPC Relay...", "solo", "relay", "node", "destroy", "-i", config.nodeId(), "--deployment", config.deployment(), "--cluster-ref", "kind-" + config.clustername()).execute();
  }

  void destroyExplorer() {
    if (!config.installExplorer()) {
      IO.println("Destroying Mirror Node Explorer..." + Process.SKIPPED);
      return;
    }
    new Process("Destroying Mirror Node Explorer...", "solo", "explorer", "node", "destroy", "--deployment", config.deployment(), "--force").execute();
  }

  void destroyMirrorNode() {
    if (!config.installMirrorNode()) {
      IO.println("Destroying Mirror Node..." + Process.SKIPPED);
      return;
    }
    new Process("Destroying Mirror Node...", "solo", "mirror", "node", "destroy", "--deployment", config.deployment(), "--force").execute();
  }

  void destroyConsensusNode() {
    new Process("Destroying Consensus Node...", "solo", "consensus", "network", "destroy", "--deployment", config.deployment(), "--force").execute();
  }

  void destroyCluster() {
    new Process("Destroy Cluster...", "kind", "delete", "cluster", "-n", config.clustername()).execute();
  }

  void deleteSoloConfig() {
    new Process("Remove (~/.solo) directory....", "rm", "-rf", "~/.solo", "logs/solo_state.txt").execute();
  }

  void dockerSystemCleanup() {
    new Process("Docker Cleanup...", "docker", "volume", "prune", "-f").execute();
  }
}
