def windows?
    (/cygwin|mswin|mingw|bccwin|wince|emx/ =~ RUBY_PLATFORM) != nil
end

Vagrant.configure("2") do |config|
  config.vm.box = "tcthien/java-dev-server"
  config.vm.box_version = "0.0.7"
  config.vm.box_check_update = false

  # config.vm.synced_folder "#{Dir.home}/.m2/repository", "/share/mavenRepo"
  # config.vm.synced_folder "", "/share/source"

  # MySQL Port
  # config.vm.network "forwarded_port", guest: 3306, host: 3306
  # Cassandra Port
  # config.vm.network "forwarded_port", guest: 9042, host: 9042
  # config.vm.network "forwarded_port", guest: 7000, host: 7000
  # config.vm.network "forwarded_port", guest: 7001, host: 7001
  # config.vm.network "forwarded_port", guest: 9160, host: 9160

  config.vm.provider "virtualbox" do |vb|
     vb.memory = "2048"
     vb.name = "codelab-server"
  end

  # update node+npm
  config.vm.provision "shell", privileged: false, inline: <<-SHELL
    curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.6/install.sh | bash
    export NVM_DIR="\$HOME/.nvm"
    [ -s "\$NVM_DIR/nvm.sh" ] && \. "\$NVM_DIR/nvm.sh"
    [ -s "\$NVM_DIR/bash_completion" ] && \. "\$NVM_DIR/bash_completion"
    nvm install --lts
  SHELL

  # node/npm has symlink errors on windows hosts, this config disables them
  if windows?
    config.vm.provision "shell", privileged: false, inline: <<-SHELL
      export NVM_DIR="\$HOME/.nvm"
      [ -s "\$NVM_DIR/nvm.sh" ] && \. "\$NVM_DIR/nvm.sh"
      npm config set bin-links false
    SHELL
  end
end