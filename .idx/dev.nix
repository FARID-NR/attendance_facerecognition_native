{ pkgs, ... }: {
  channel = "stable-24.05";

  packages = [
    pkgs.android-tools
    pkgs.unzip
    pkgs.wget
    pkgs.sdkmanager 
    pkgs.openjdk17
  ];

  env = {
    ANDROID_HOME = "${builtins.getEnv "HOME"}/android-sdk";
    PATH = "${builtins.getEnv "HOME"}/android-sdk/cmdline-tools/latest/bin:${builtins.getEnv "HOME"}/android-sdk/platform-tools:$PATH";
    JAVA_HOME = "${pkgs.openjdk17}";
  };

  idx = {
    extensions = [
      # contoh: "vscodevim.vim"
    ];
    previews = {
      enable = true;
      previews = {
        # web preview bisa ditambah nanti
      };
    };
    workspace = {
      onCreate = {
        # perintah on create bisa ditambah nanti
      };
      onStart = {
        # perintah on start bisa ditambah nanti
      };
    };
  };
}
