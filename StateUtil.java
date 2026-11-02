import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public abstract class StateUtil {
  private final static String FILE = "logs/solo_state.txt";

  public static State readState() {
    try (var in = new ObjectInputStream(new FileInputStream(FILE))) {
      var log = (State) in.readObject();
      return log;
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Failed to read solo state", e);
    }
  }

  public static void writeState(State state) {
    Objects.requireNonNull(state, "log must not be null");

    try (var out = new ObjectOutputStream(new FileOutputStream(FILE))) {
      out.writeObject(state);
    } catch (IOException e) {
      throw new RuntimeException("Failed to write solo state", e);
    }
  }
}
