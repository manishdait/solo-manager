import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Process {
  public static final String SUCCESS = ConsoleUtil.success("Success");
  public static final String FAILED = ConsoleUtil.error("Failed");
  public static final String SKIPPED = ConsoleUtil.warning("Skipped");

  private static final char[] spinnerSet = {'|', '/', '-', '\\'};

  private String name;
  private String[] args;

  public Process(String name, String... args) {
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(args, "args must not be null");

    this.name = name;
    this.args = args;
  }


  public ExecutionResult execute() {
    IO.print(name);

    var execution = CompletableFuture.supplyAsync(() -> executeCommand(args) );
    var count = 0;

    while (!execution.isDone()) {
      try {
        IO.print(spinnerSet[count]);
        count = (count + 1) % spinnerSet.length;
        Thread.sleep(100);
        IO.print('\b');
      } catch (Exception e) {
        IO.println(e.getMessage());
      }
    }

    var result = execution.join();

    if (result.exitCode() != 0) {
      IO.println(FAILED);
      throw new RuntimeException("Fail to execute the process with args: " + Arrays.toString(args) + "\n" +  ConsoleUtil.log(result.output));
    } else {
      IO.println(SUCCESS);
      return result;
    }
  }

  public static ExecutionResult executeCommand(final String... args) {
    Objects.requireNonNull(args, "args must not be null");
    
    var processBuilder = new ProcessBuilder(args);

    try {
      var process = processBuilder.start();

      var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      var logs = new StringBuilder();

      String line;
      while ((line = reader.readLine()) != null) {
        logs.append(line);
        logs.append("\n");
      }

      var exitCode = process.waitFor();
      return new ExecutionResult(logs.toString(), exitCode);
    } catch (Exception e) {
      return new ExecutionResult(e.getMessage(), 1);
    }
  }

  record ExecutionResult(String output, int exitCode) {}
}
