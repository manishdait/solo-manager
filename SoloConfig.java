import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public record SoloConfig (
  String clustername,
  String namespace,
  String deployment,
  String consensusNodeVersion,
  String mirrorNodeVersion,
  String nodeId,
  boolean installBlockNode,
  boolean installMirrorNode,
  boolean installExplorer,
  boolean installRelay,
  Map<Port, Integer> ports,
  boolean verbose
) implements Serializable {
  public SoloConfig {
    Objects.requireNonNull(clustername, "clustername must not be null");
    Objects.requireNonNull(namespace, "clustername must not be null");
    Objects.requireNonNull(deployment, "deployment must not be null");
    Objects.requireNonNull(consensusNodeVersion, "consensusNodeVersion must not be null");
    Objects.requireNonNull(mirrorNodeVersion, "mirrorNodeVersion must not be null");
    Objects.requireNonNull(ports, "ports must not be null");
  }


  public static SoloConfig resolveConfiguration(String... args) {
    if (args.length < 2) {
      return defaultConfig();
    }

    var installBlockNode = false;
    var installMirrorNode = false;
    var installExplorer = false;
    var installRelay = false;

    var consensusNodeVersion = Default.CONSENSUS_NODE_VERSION;
    var mirrorNodeVersion = Default.MIRROR_NODE_VERSION;

    var verbose = false;

    for (int i = 1; i < args.length; i++) {
      var flag = args[i];
    
      switch (flag) {
        case "--mirror-node" ->  {
          i++;
          installMirrorNode = Boolean.valueOf(getArgsAt(args, i));
        }
        case "--block-node" -> {
          i++;
          installBlockNode = Boolean.valueOf(getArgsAt(args, i));
        }
        case "--explorer" -> {
          i++;
          installExplorer = Boolean.valueOf(getArgsAt(args, i));
        }
        case "--relay" -> {
          i++;
          installRelay = Boolean.valueOf(getArgsAt(args, i));
        }  
        case "--mirror-node-tag" -> {
          i++;
          mirrorNodeVersion = getArgsAt(args, i);
        }
        case "--consensus-node-tag" -> {
          i++;
          consensusNodeVersion = getArgsAt(args, i);
        }
        case "-v" -> verbose = true;
        default -> throw new IllegalArgumentException("Invalid flag: " + flag);
      }
    }

    return new SoloConfig(
      Default.CLUSTERNAME,
      Default.NAMESPACE,
      Default.DEPLOYMENT, 
      consensusNodeVersion, 
      mirrorNodeVersion, 
      Default.NODE_ID,
      installBlockNode,
      installMirrorNode, 
      installExplorer, 
      installRelay,
      defaultPorts(),
      verbose
    );
  }

  static SoloConfig defaultConfig() {
    return new SoloConfig(
      Default.CLUSTERNAME,
      Default.NAMESPACE,
      Default.DEPLOYMENT, 
      Default.CONSENSUS_NODE_VERSION, 
      Default.MIRROR_NODE_VERSION, 
      Default.NODE_ID,
      Default.INSTALL_BLOCK_NODE, 
      Default.INSTALL_MIRROR_NODE, 
      Default.INSTALL_EXPLORER, 
      Default.INSTALL_RELAY,
      defaultPorts(),
      false
    );
  }


  private static Map<Port, Integer> defaultPorts() {
    return Map.of(
      Port.HAPROXY_HOST, Default.HAPROXY_HOST_PORT,
      Port.ENVOY_HOST, Default.ENVOY_HOST_PORT,
      Port.BLOCK_NODE, Default.BLOCK_NODE_PORT,
      Port.MIRROR_NODE_REST, Default.MIRROR_REST_PORT,
      Port.MIRROR_NODE_JAVA, Default.MIRROR_JAVA_PORT,
      Port.MIRROR_NODE_GRPC, Default.MIRROR_GRPC_PORT,
      Port.MIRROR_NODE_WEB3, Default.MIRROR_WEB3_PORT,
      Port.EXPLORER, Default.EXPLORER_PORT,
      Port.RELAY_PORT, Default.JSON_RPC_RELAY_PORT
    );
  }

  private static String getArgsAt(String[] args, int index) {
    if (index >= args.length) {
      throw new IllegalArgumentException("Invalid arguments");
    }

    return args[index];
  }

  enum Port {
    HAPROXY_HOST(Default.HAPROXY_HOST_PORT),
    ENVOY_HOST(Default.ENVOY_HOST_PORT),
    BLOCK_NODE(Default.BLOCK_NODE_PORT),
    MIRROR_NODE_REST(Default.MIRROR_REST_PORT),
    MIRROR_NODE_JAVA(Default.MIRROR_JAVA_PORT),
    MIRROR_NODE_GRPC(Default.MIRROR_GRPC_PORT),
    MIRROR_NODE_WEB3(Default.MIRROR_WEB3_PORT),
    EXPLORER(Default.EXPLORER_PORT),
    RELAY_PORT(Default.JSON_RPC_RELAY_PORT);

    int value;

    Port(int value) {
      this.value = value;
    }
  }

  abstract class Default {
    static final String CLUSTERNAME = "solo";
    static final String NAMESPACE = "solo";
    static final String DEPLOYMENT = "solo-deployment";
    static final String CONSENSUS_NODE_VERSION="v0.77.0";
    static final String MIRROR_NODE_VERSION="v0.163.0";
    static final String NODE_ID = "node1";
    static final int HAPROXY_HOST_PORT = 35211;
    static final int ENVOY_HOST_PORT = 9998;
    static final int BLOCK_NODE_PORT = 40840;
    static final int MIRROR_REST_PORT = 38081;
    static final int MIRROR_JAVA_PORT = 8084;
    static final int MIRROR_GRPC_PORT = 5600;
    static final int MIRROR_WEB3_PORT = 8545;
    static final int EXPLORER_PORT = 8081;
    static final int JSON_RPC_RELAY_PORT = 37546;
    static final boolean INSTALL_BLOCK_NODE = false;
    static final boolean INSTALL_MIRROR_NODE = false;
    static final boolean INSTALL_EXPLORER = false;
    static final boolean INSTALL_RELAY = false;
    static final String HBAR_FUND = "10000000";
  }
}

