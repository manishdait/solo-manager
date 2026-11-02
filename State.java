import java.io.Serializable;
import java.util.Objects;

public record State(SoloConfig config, SetupCommand.Account account) implements Serializable {
  public State {
    Objects.requireNonNull(config, "config must not be null");
    Objects.requireNonNull(account, "account must not be null");
  }
}
