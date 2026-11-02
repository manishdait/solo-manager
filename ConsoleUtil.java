import java.time.Duration;


public abstract class ConsoleUtil {
  private final static String ANSI_GREEN = "\u001B[32m";
  private final static String ANSI_YELLOW = "\u001B[33m";
  private final static String ANSI_RED = "\u001B[31m";
  private final static String ANSI_GRAY = "\u001B[90m";
  private final static String ANSI_RESET = "\u001B[0m";

  

  public static void sleep() {
    try {
      Thread.sleep(Duration.ofSeconds(2));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static String error(String message) {
    return ANSI_RED + message + ANSI_RESET;
  }

  public static String warning(String message) {
    return ANSI_YELLOW + message + ANSI_RESET;
  }

  public static String success(String message) {
    return ANSI_GREEN + message + ANSI_RESET;
  }

  public static String log(String message) {
    return ANSI_GRAY + message + ANSI_RESET;
  }
}
