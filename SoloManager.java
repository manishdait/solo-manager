void main(String... args) {
  if (args.length == 0) {
    return;
  }

  var opt = args[0];
  switch (opt) {
    case "setup" -> setupSolo(args);
    case "purge" -> purgeSolo();
    default -> throw new IllegalArgumentException("Invalid operation: " + opt);
  }
}

static void setupSolo(String... args) {
  var config = SoloConfig.resolveConfiguration(args);
  var account = new SetupCommand(config).init();

  StateUtil.writeState(new State(config, account));

  IO.println("\n🎉 Solo Network Configured Successfully 🎉\n");

  IO.println("Account Details:");
  IO.println("AccountID: " + account.accountId());
  IO.println("PublicKey: " + account.publicKey());
  IO.println("PrivateKey: " + account.privateKey());
}

static void purgeSolo() {
  var state = StateUtil.readState();
  new PurgeCommand(state.config()).init();
  
  IO.println("\nDestroyed Solo Network Successfully\n");
}
